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

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
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
import org.vividus.azure.devops.facade.model.Steps;
import org.vividus.bdd.model.jbehave.Step;
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

    public void createTestCase(String title, List<Step> steps) throws IOException
    {
        LOGGER.atInfo().log("Creating Test Case");
        String response = client.createTestCase(createPayload(title, steps));
        LOGGER.atInfo()
              .addArgument(() -> JsonPathUtils.getData(response, "$.id"))
              .log("Test Case with ID {} has been created");
    }

    public void updateTestCase(String testCaseId, String title, List<Step> steps) throws IOException
    {
        LOGGER.atInfo().addArgument(testCaseId).log("Updating Test Case with ID {}");
        client.updateTestCase(testCaseId, createPayload(title, steps));
        LOGGER.atInfo().addArgument(testCaseId).log("Test Case with ID {} has been updated");
    }

    private List<AddOperation> createPayload(String title, List<Step> steps) throws JacksonException
    {
        return List.of(
            new AddOperation("/fields/System.AreaPath", format("%s\\%s", options.getProject(), options.getArea())),
            new AddOperation("/fields/System.Title", title),
            new AddOperation("/fields/Microsoft.VSTS.TCM.Steps", convertSteps(steps))
        );
    }

    private String convertSteps(List<Step> scenarioSteps) throws JsonProcessingException
    {
        Steps stepss = IntStream.range(0, scenarioSteps.size())
                                .mapToObj(index -> new org.vividus.azure.devops.facade.model.Step(index + 2,
                                        scenarioSteps.get(index).getValue()))
                                .collect(Collectors.collectingAndThen(Collectors.toList(), Steps::new));

        return xmlMapper.writeValueAsString(stepss);
    }
}
