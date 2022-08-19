/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.azure.devops.facade;

import static java.lang.String.format;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.Validate.isTrue;
import static org.vividus.output.ManualStepConverter.convert;
import static org.vividus.output.ManualStepConverter.cutManualIdentifier;
import static org.vividus.output.ManualStepConverter.startsWithManualKeyword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.vividus.azure.devops.client.AzureDevOpsClient;
import org.vividus.azure.devops.client.model.AddOperation;
import org.vividus.azure.devops.client.model.ShallowReference;
import org.vividus.azure.devops.client.model.TestPoint;
import org.vividus.azure.devops.client.model.TestResult;
import org.vividus.azure.devops.client.model.TestRun;
import org.vividus.azure.devops.client.model.WorkItem;
import org.vividus.azure.devops.configuration.AzureDevOpsExporterOptions;
import org.vividus.azure.devops.facade.model.ScenarioPart;
import org.vividus.azure.devops.facade.model.Steps;
import org.vividus.model.jbehave.Scenario;
import org.vividus.model.jbehave.Step;
import org.vividus.output.ManualTestStep;
import org.vividus.output.SyntaxException;
import org.vividus.util.DateUtils;

@Component
public class AzureDevOpsFacade
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureDevOpsFacade.class);

    private final AzureDevOpsClient client;
    private final AzureDevOpsExporterOptions options;
    private final DateUtils dateUtils;

    private final ObjectMapper xmlMapper;

    public AzureDevOpsFacade(AzureDevOpsClient client, AzureDevOpsExporterOptions options, DateUtils dateUtils)
    {
        this.client = client;
        this.options = options;
        this.dateUtils = dateUtils;
        this.xmlMapper = new XmlMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    public WorkItem createTestCase(String suiteTitle, Scenario scenario) throws IOException, SyntaxException
    {
        LOGGER.atInfo().log("Creating Test Case");
        WorkItem testCase = client.createTestCase(createTestCasePayload(suiteTitle, scenario));
        LOGGER.atInfo().addArgument(testCase::getId).log("Test Case with ID {} has been created");
        return testCase;
    }

    public void updateTestCase(Integer testCaseId, String suiteTitle, Scenario scenario)
            throws IOException, SyntaxException
    {
        LOGGER.atInfo().addArgument(testCaseId).log("Updating Test Case with ID {}");
        client.updateTestCase(testCaseId, createTestCasePayload(suiteTitle, scenario));
        LOGGER.atInfo().addArgument(testCaseId).log("Test Case with ID {} has been updated");
    }

    public void createTestRun(Map<Integer, Scenario> scenarios) throws IOException
    {
        String runName = ensureProperty("azure-devops-exporter.test-run.name", options.getTestRun().getName());
        Integer planId = ensureProperty("azure-devops-exporter.test-run.test-plan-id",
                options.getTestRun().getTestPlanId());

        Map<Integer, List<TestPoint>> testCaseIdToTestPoint = client.queryTestPoints(scenarios.keySet()).stream()
                .collect(groupingBy(tp -> Integer.valueOf(tp.getTestCase().getId()), toList()));

        scenarios.keySet().forEach(key ->
        {
            List<TestPoint> testPoints = testCaseIdToTestPoint.get(key);
            isTrue(testPoints != null, "Unable to find test point for test case with id %s in %s test plan",
                    key, planId);
            if (testPoints.size() != 1)
            {
                String suites = testPoints.stream()
                                          .map(TestPoint::getSuite)
                                          .map(ShallowReference::getId)
                                          .collect(Collectors.joining(","));
                String errorFormat = "The test case with id %s is attached to more than one test suite (%s) "
                        + "in %s test plan";
                throw new IllegalArgumentException(String.format(errorFormat, key, suites, planId));
            }
        });

        LOGGER.atInfo().log("Creating Test Run");

        int testRunId = client.createTestRun(createTestRunPayload(planId, runName)).getId();

        LOGGER.atInfo().addArgument(testRunId).log("Test Run with ID {} has been created");

        List<TestResult> testResults = new ArrayList<>(scenarios.size());
        for (Entry<Integer, Scenario> scenarioEntry : scenarios.entrySet())
        {
            Integer testCaseId = scenarioEntry.getKey();
            Integer testPointId = testCaseIdToTestPoint.get(testCaseId).get(0).getId();
            TestResult testResult = createTestResultPayload(testPointId, scenarioEntry);
            testResults.add(testResult);
        }

        client.addTestResults(testRunId, testResults);
    }

    private TestResult createTestResultPayload(Integer testPointId, Entry<Integer, Scenario> scenarioEntry)
            throws IOException
    {
        TestResult testResult = new TestResult();
        testResult.setState("Completed");

        Scenario scenario = scenarioEntry.getValue();

        testResult.setTestCaseTitle(scenario.getTitle());

        testResult.setStartedDate(dateUtils.asOffsetDateTime(scenario.getStart()));
        testResult.setCompletedDate(dateUtils.asOffsetDateTime(scenario.getEnd()));

        Integer testCaseId = scenarioEntry.getKey();
        WorkItem testCase = client.getWorkItem(testCaseId);
        testResult.setRevision(testCase.getRev());

        String outcome = scenario.createStreamOfAllSteps().anyMatch(step -> "failed".equals(step.getOutcome()))
                ? "Failed"
                : "Passed";
        testResult.setOutcome(outcome);

        testResult.setTestPoint(new ShallowReference(testPointId.toString()));

        return testResult;
    }

    private TestRun createTestRunPayload(Integer planId, String testName)
    {
        TestRun testRun = new TestRun();
        testRun.setAutomated(true);
        testRun.setName(testName);
        testRun.setPlan(new ShallowReference(planId.toString()));
        return testRun;
    }

    private List<AddOperation> createTestCasePayload(String suiteTitle, Scenario scenario)
            throws JacksonException, SyntaxException
    {
        String testTitle = scenario.getTitle();
        List<Step> steps = scenario.collectSteps();
        List<AddOperation> operations = createCommonOperations(testTitle);

        if (scenario.isManual())
        {
            operations.add(createOperationForManualTest(suiteTitle, testTitle, steps));
            return operations;
        }

        List<Step> manualPart = steps.stream()
                                     .takeWhile(Step::isManual)
                                     .collect(toList());

        List<Step> automatedSteps = steps;

        if (!manualPart.isEmpty() && startsWithManualKeyword(cutManualIdentifier(manualPart.get(0).getValue())))
        {
            automatedSteps = steps.subList(manualPart.size(), steps.size());
            operations.add(createOperationForManualTest(suiteTitle, testTitle, manualPart));
        }

        if (options.getSectionMapping().getSteps() == ScenarioPart.AUTOMATED)
        {
            String stepsData = convertToStepsData(automatedSteps, Step::getValue, s -> null);
            operations.add(createStepsOperation(stepsData));
        }
        else
        {
            AddOperation description = automatedSteps.stream()
                                                     .map(Step::getValue)
                                                     .collect(collectingAndThen(collectingAndThen(toList(),
                                                             this::convertToDescriptionData),
                                                                 this::createDescriptionOperation));
            operations.add(description);
        }

        operations.addAll(createAutomatedOperations(suiteTitle, testTitle));

        return operations;
    }

    private AddOperation createOperationForManualTest(String suiteTitle, String testTitle, List<Step> steps)
            throws JsonProcessingException, SyntaxException
    {
        if (options.getSectionMapping().getSteps() == ScenarioPart.MANUAL)
        {
            List<ManualTestStep> manualSteps = convert(suiteTitle, testTitle, steps);
            String stepsData = convertToStepsData(manualSteps, ManualTestStep::getAction,
                    ManualTestStep::getExpectedResult);
            return createStepsOperation(stepsData);
        }
        String description = convertToDescriptionData(cutManualIdentifier(steps));
        return createDescriptionOperation(description);
    }

    private List<AddOperation> createAutomatedOperations(String suite, String scenario)
    {
        String automatedTestName = FilenameUtils.getBaseName(suite) + '.' + scenario;
        AddOperation testName = new AddOperation("/fields/Microsoft.VSTS.TCM.AutomatedTestName",
                automatedTestName);
        AddOperation testType = new AddOperation("/fields/Microsoft.VSTS.TCM.AutomatedTestType",
                "VIVIDUS");
        return List.of(testName, testType);
    }

    private <T> String convertToStepsData(List<T> steps, Function<T, String> actionGetter,
            Function<T, String> resultGetter) throws JsonProcessingException
    {
        Steps azureSteps = IntStream.range(0, steps.size())
                                    .mapToObj(index ->
                                    {
                                        T step = steps.get(index);
                                        String action = actionGetter.apply(step);
                                        String result = resultGetter.apply(step);
                                        return new org.vividus.azure.devops.facade.model.Step(index + 2, action,
                                                result);
                                    })
                                    .collect(collectingAndThen(toList(), Steps::new));

        return xmlMapper.writeValueAsString(azureSteps);
    }

    private String convertToDescriptionData(List<String> steps)
    {
        return steps.stream().map(s -> format("<div>%s</div>", s)).collect(joining());
    }

    private AddOperation createStepsOperation(String value)
    {
        return new AddOperation("/fields/Microsoft.VSTS.TCM.Steps", value);
    }

    private AddOperation createDescriptionOperation(String value)
    {
        return new AddOperation("/fields/System.Description", value);
    }

    private List<AddOperation> createCommonOperations(String testTitle)
    {
        List<AddOperation> operations = new ArrayList<>();
        String areaPath = options.getProject();
        if (options.getArea() != null)
        {
            areaPath += "\\" + options.getArea();
        }
        operations.add(new AddOperation("/fields/System.AreaPath", areaPath));
        operations.add(new AddOperation("/fields/System.Title", testTitle));
        return operations;
    }

    private <T> T ensureProperty(String key, T property)
    {
        Validate.isTrue(property != null, "The '%s' property is mandatory to create a test run", key);
        return property;
    }
}
