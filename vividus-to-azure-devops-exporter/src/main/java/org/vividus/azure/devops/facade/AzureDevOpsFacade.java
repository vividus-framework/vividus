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

package org.vividus.azure.devops.facade;

import static java.lang.String.format;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.vividus.bdd.output.ManualStepConverter.convert;
import static org.vividus.bdd.output.ManualStepConverter.cutManualIdentifier;
import static org.vividus.bdd.output.ManualStepConverter.startsWithManualKeyword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.vividus.azure.devops.client.AzureDevOpsClient;
import org.vividus.azure.devops.client.model.AddOperation;
import org.vividus.azure.devops.configuration.AzureDevOpsExporterOptions;
import org.vividus.azure.devops.facade.model.ScenarioPart;
import org.vividus.azure.devops.facade.model.Steps;
import org.vividus.bdd.model.jbehave.Scenario;
import org.vividus.bdd.model.jbehave.Step;
import org.vividus.bdd.output.ManualTestStep;
import org.vividus.bdd.output.SyntaxException;
import org.vividus.util.json.JsonPathUtils;

@Component
public class AzureDevOpsFacade
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureDevOpsFacade.class);

    private final AzureDevOpsClient client;
    private final AzureDevOpsExporterOptions options;

    private final ObjectMapper xmlMapper;

    public AzureDevOpsFacade(AzureDevOpsClient client, AzureDevOpsExporterOptions options)
    {
        this.client = client;
        this.options = options;
        this.xmlMapper = new XmlMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    public void createTestCase(String suiteTitle, Scenario scenario) throws IOException, SyntaxException
    {
        LOGGER.atInfo().log("Creating Test Case");
        String response = client.createTestCase(createPayload(suiteTitle, scenario));
        LOGGER.atInfo()
              .addArgument(() -> JsonPathUtils.getData(response, "$.id"))
              .log("Test Case with ID {} has been created");
    }

    public void updateTestCase(String testCaseId, String suiteTitle, Scenario scenario)
            throws IOException, SyntaxException
    {
        LOGGER.atInfo().addArgument(testCaseId).log("Updating Test Case with ID {}");
        client.updateTestCase(testCaseId, createPayload(suiteTitle, scenario));
        LOGGER.atInfo().addArgument(testCaseId).log("Test Case with ID {} has been updated");
    }

    private List<AddOperation> createPayload(String suiteTitle, Scenario scenario)
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
        operations.add(new AddOperation("/fields/System.AreaPath", format("%s\\%s", options.getProject(),
                options.getArea())));
        operations.add(new AddOperation("/fields/System.Title", testTitle));
        return operations;
    }
}
