/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.xray.exporter;

import static java.lang.System.lineSeparator;
import static java.util.Map.entry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.function.FailableBiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.model.jbehave.NotUniqueMetaValueException;
import org.vividus.model.jbehave.Scenario;
import org.vividus.model.jbehave.Story;
import org.vividus.output.ManualStepConverter;
import org.vividus.output.OutputReader;
import org.vividus.output.SyntaxException;
import org.vividus.xray.configuration.XrayExporterOptions;
import org.vividus.xray.converter.CucumberScenarioConverter;
import org.vividus.xray.converter.CucumberScenarioConverter.CucumberScenario;
import org.vividus.xray.facade.AbstractTestCaseParameters;
import org.vividus.xray.facade.CucumberTestCaseParameters;
import org.vividus.xray.facade.ManualTestCaseParameters;
import org.vividus.xray.facade.XrayFacade;
import org.vividus.xray.facade.XrayFacade.NonEditableIssueStatusException;
import org.vividus.xray.factory.TestCaseFactory;
import org.vividus.xray.factory.TestExecutionFactory;
import org.vividus.xray.model.AbstractTestCase;
import org.vividus.xray.model.TestCaseType;
import org.vividus.xray.model.TestExecution;

@Component
public class XrayExporter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(XrayExporter.class);

    @Autowired private XrayExporterOptions xrayExporterOptions;
    @Autowired private XrayFacade xrayFacade;
    @Autowired private TestCaseFactory testCaseFactory;
    @Autowired private TestExecutionFactory testExecutionFactory;

    private final List<String> errors = new ArrayList<>();

    private final Map<TestCaseType, Function<AbstractTestCaseParameters, AbstractTestCase>> testCaseFactories = Map.of(
        TestCaseType.MANUAL, p -> testCaseFactory.createManualTestCase((ManualTestCaseParameters) p),
        TestCaseType.CUCUMBER, p -> testCaseFactory.createCucumberTestCase((CucumberTestCaseParameters) p)
    );

    private final Map<TestCaseType, CreateParametersFunction> parameterFactories = Map.of(
        TestCaseType.MANUAL, this::createManualTestCaseParameters,
        TestCaseType.CUCUMBER, (title, scenario) -> createCucumberTestCaseParameters(scenario)
    );

    public void exportResults() throws IOException
    {
        List<Entry<String, Scenario>> testCases = new ArrayList<>();
        for (Story story : OutputReader.readStoriesFromJsons(xrayExporterOptions.getJsonResultsDirectory()))
        {
            LOGGER.atInfo().addArgument(story::getPath).log("Exporting scenarios from {} story");

            for (Scenario scenario : story.getFoldedScenarios())
            {
                exportScenario(story.getPath(), scenario).ifPresent(testCases::add);
            }
        }

        addTestCasesToTestSet(testCases);
        addTestCasesToTestExecution(testCases);

        publishErrors();
    }

    private void addTestCasesToTestSet(List<Entry<String, Scenario>> testCases)
    {
        String testSetKey = xrayExporterOptions.getTestSetKey();
        if (testSetKey != null)
        {
            List<String> testCaseIds = testCases.stream()
                                                .map(Entry::getKey)
                                                .collect(Collectors.toList());

            updateSafely(() -> xrayFacade.updateTestSet(testSetKey, testCaseIds), "test set", testSetKey);
        }
    }

    private void addTestCasesToTestExecution(List<Entry<String, Scenario>> testCases)
    {
        String testExecutionKey = xrayExporterOptions.getTestExecutionKey();
        if (testExecutionKey != null)
        {
            TestExecution testExecution = testExecutionFactory.create(testCases);
            updateSafely(() -> xrayFacade.updateTestExecution(testExecution), "test execution", testExecutionKey);
        }
    }

    private void updateSafely(FailableRunnable runnable, String type, String key)
    {
        try
        {
            runnable.run();
        }
        catch (IOException | JiraConfigurationException thrown)
        {
            String errorMessage = String.format("Failed updating %s with the key %s: %s", type, key,
                    thrown.getMessage());
            errors.add(errorMessage);
        }
    }

    private Optional<Entry<String, Scenario>> exportScenario(String storyTitle, Scenario scenario)
    {
        String scenarioTitle = scenario.getTitle();

        if (scenario.hasMetaWithName("xray.skip-export"))
        {
            LOGGER.atInfo().addArgument(scenarioTitle).log("Skip export of {} scenario");
            return Optional.empty();
        }
        LOGGER.atInfo().addArgument(scenarioTitle).log("Exporting {} scenario");

        try
        {
            TestCaseType testCaseType = scenario.isManual() ? TestCaseType.MANUAL : TestCaseType.CUCUMBER;

            String testCaseId = scenario.getUniqueMetaValue("testCaseId").orElse(null);

            AbstractTestCaseParameters parameters = parameterFactories.get(testCaseType).apply(scenarioTitle, scenario);
            AbstractTestCase testCase = testCaseFactories.get(testCaseType).apply(parameters);
            if (testCaseId != null)
            {
                xrayFacade.updateTestCase(testCaseId, testCase);
            }
            else
            {
                testCaseId = xrayFacade.createTestCase(testCase);
            }
            createTestsLink(testCaseId, scenario);
            return Optional.of(entry(testCaseId, scenario));
        }
        catch (IOException | SyntaxException | NonEditableIssueStatusException | NotUniqueMetaValueException
                | JiraConfigurationException e)
        {
            String errorMessage = "Story: " + storyTitle + lineSeparator() + "Scenario: " + scenarioTitle
                    + lineSeparator() + "Error: " + e.getMessage();
            errors.add(errorMessage);
            LOGGER.atError().setCause(e).log("Got an error while exporting");
        }
        return Optional.empty();
    }

    private ManualTestCaseParameters createManualTestCaseParameters(String storyTitle, Scenario scenario)
            throws SyntaxException
    {
        ManualTestCaseParameters parameters = new ManualTestCaseParameters();
        fillTestCaseParameters(parameters, TestCaseType.MANUAL, scenario);
        parameters.setSteps(ManualStepConverter.convert(storyTitle, scenario.getTitle(), scenario.collectSteps()));
        return parameters;
    }

    private CucumberTestCaseParameters createCucumberTestCaseParameters(Scenario scenario)
    {
        CucumberTestCaseParameters parameters = new CucumberTestCaseParameters();
        fillTestCaseParameters(parameters, TestCaseType.CUCUMBER, scenario);
        CucumberScenario cucumberScenario = CucumberScenarioConverter.convert(scenario);
        parameters.setScenarioType(cucumberScenario.getType());
        parameters.setScenario(cucumberScenario.getScenario());
        return parameters;
    }

    private <T extends AbstractTestCaseParameters> void fillTestCaseParameters(T parameters, TestCaseType type,
            Scenario scenario)
    {
        parameters.setType(type);
        parameters.setLabels(scenario.getMetaValues("xray.labels"));
        parameters.setComponents(scenario.getMetaValues("xray.components"));
        parameters.setSummary(scenario.getTitle());
    }

    private void publishErrors()
    {
        if (!errors.isEmpty())
        {
            LOGGER.atError().addArgument(System::lineSeparator).addArgument(() ->
            {
                StringBuilder errorBuilder = new StringBuilder();
                IntStream.range(0, errors.size()).forEach(index ->
                {
                    String errorMessage = errors.get(index);
                    errorBuilder.append("Error #").append(index + 1).append(lineSeparator())
                                .append(errorMessage).append(lineSeparator());
                });
                return errorBuilder.toString();
            }).log("Export failed:{}{}");
            return;
        }
        LOGGER.atInfo().log("Export successful");
    }

    private void createTestsLink(String testCaseId, Scenario scenario)
            throws IOException, NotUniqueMetaValueException, JiraConfigurationException
    {
        Optional<String> requirementId = scenario.getUniqueMetaValue("requirementId");
        if (requirementId.isPresent())
        {
            xrayFacade.createTestsLink(testCaseId, requirementId.get());
        }
    }

    @FunctionalInterface
    private interface CreateParametersFunction
            extends FailableBiFunction<String, Scenario, AbstractTestCaseParameters, SyntaxException>
    {
    }

    @FunctionalInterface
    private interface FailableRunnable
    {
        void run() throws IOException, JiraConfigurationException;
    }
}
