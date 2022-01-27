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

package org.vividus.zephyr.exporter;

import static java.lang.System.lineSeparator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.exporter.facade.ExporterFacade;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.JiraEntity;
import org.vividus.model.jbehave.IContainingMeta;
import org.vividus.model.jbehave.NotUniqueMetaValueException;
import org.vividus.model.jbehave.Scenario;
import org.vividus.model.jbehave.Story;
import org.vividus.output.OutputReader;
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterProperties;
import org.vividus.zephyr.databind.TestCaseDeserializer;
import org.vividus.zephyr.facade.IZephyrFacade;
import org.vividus.zephyr.facade.TestCaseParameters;
import org.vividus.zephyr.facade.ZephyrFacade;
import org.vividus.zephyr.model.ExecutionStatus;
import org.vividus.zephyr.model.TestCaseExecution;
import org.vividus.zephyr.model.TestCaseLevel;
import org.vividus.zephyr.model.ZephyrExecution;
import org.vividus.zephyr.model.ZephyrTestCase;
import org.vividus.zephyr.parser.TestCaseParser;

public class ZephyrExporter
{
    public static final String ZEPHYR_COMPONENTS = "zephyr.components";
    public static final String ZEPHYR_LABELS = "zephyr.labels";
    private static final Logger LOGGER = LoggerFactory.getLogger(ZephyrExporter.class);
    private static final String TEST_CASE_ID = "testCaseId";
    private static final String STORY = "Story: ";
    private static final String ERROR = "Error: ";
    private static final String REQUIREMENT_ID = "requirementId";

    private final List<String> errors = new ArrayList<>();

    private final JiraFacade jiraFacade;
    private final IZephyrFacade zephyrFacade;
    private final TestCaseParser testCaseParser;
    private final ZephyrExporterProperties zephyrExporterProperties;
    private final ObjectMapper objectMapper;

    public ZephyrExporter(JiraFacade jiraFacade, ZephyrFacade zephyrFacade,
            TestCaseParser testCaseParser, ZephyrExporterProperties zephyrExporterProperties)
    {
        this.jiraFacade = jiraFacade;
        this.zephyrFacade = zephyrFacade;
        this.testCaseParser = testCaseParser;
        this.zephyrExporterProperties = zephyrExporterProperties;
        this.objectMapper = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .build()
                .registerModule(new SimpleModule()
                        .addDeserializer(TestCaseExecution.class, new TestCaseDeserializer()));
    }

    public void exportResults() throws IOException, JiraConfigurationException
    {
        if (zephyrExporterProperties.getExportResults())
        {
            for (Story story : OutputReader.readStoriesFromJsons(zephyrExporterProperties.getSourceDirectory()))
            {
                TestCaseLevel testCaseLevel = zephyrExporterProperties.getLevel();
                if (testCaseLevel.equals(TestCaseLevel.STORY))
                {
                    LOGGER.atInfo().addArgument(story::getPath).log("Exporting {} story");
                    exportStory(story);
                }
                if (zephyrExporterProperties.getLevel().equals(TestCaseLevel.SCENARIO))
                {
                    LOGGER.atInfo().addArgument(story::getPath).log("Exporting scenarios from {} story");
                    for (Scenario scenario : story.getFoldedScenarios())
                    {
                        exportScenario(story.getPath(), scenario);
                    }
                }
            }
        }
        exportTestExecutions();
    }

    private void exportTestExecutions() throws IOException, JiraConfigurationException
    {
        ZephyrConfiguration configuration = zephyrFacade.prepareConfiguration();
        List<TestCaseExecution> testCasesForImportingExecution = testCaseParser.createTestCases(objectMapper);
        for (TestCaseExecution testCaseExecution : testCasesForImportingExecution)
        {
            exportTestExecution(testCaseExecution, configuration);
        }
    }

    private void exportTestExecution(TestCaseExecution testCaseExecution, ZephyrConfiguration configuration)
            throws IOException, JiraConfigurationException
    {
        JiraEntity issue = jiraFacade.getIssue(testCaseExecution.getKey());
        ZephyrExecution execution = new ZephyrExecution(configuration, issue.getId(), testCaseExecution.getStatus());
        OptionalInt executionId;

        if (zephyrExporterProperties.getUpdateExecutionStatusesOnly())
        {
            executionId = zephyrFacade.findExecutionId(issue.getId());
        }
        else
        {
            String createExecution = objectMapper.writeValueAsString(execution);
            executionId = OptionalInt.of(zephyrFacade.createExecution(createExecution));
        }
        if (executionId.isPresent())
        {
            String executionBody = objectMapper.writeValueAsString(new ExecutionStatus(
                String.valueOf(configuration.getTestStatusPerZephyrIdMapping().get(execution.getTestCaseStatus()))));
            zephyrFacade.updateExecutionStatus(executionId.getAsInt(), executionBody);
        }
        else
        {
            LOGGER.atInfo().addArgument(testCaseExecution::getKey).log("Test case result for {} was not exported, "
                    + "because execution does not exist");
        }
    }

    private void exportStory(Story story)
    {
        try
        {
            String testCaseId = story.getUniqueMetaValue(TEST_CASE_ID).orElse(null);
            ZephyrTestCase zephyrTest = new ZephyrTestCase();
            TestCaseParameters parameters = createTestCaseStoryParameters(story);
            fillTestCase(parameters, zephyrTest);

            if (testCaseId != null && zephyrExporterProperties.isUpdateCasesOnExport())
            {
                zephyrFacade.updateTestCase(testCaseId, zephyrTest);
            }
            else
            {
                testCaseId = zephyrFacade.createTestCase(zephyrTest);
                zephyrFacade.createTestSteps(story, getIssueId(testCaseId));
            }
            Optional<String> requirementId = story.getUniqueMetaValue(REQUIREMENT_ID);

            ExporterFacade.createTestsLink(testCaseId, requirementId, jiraFacade);
        }
        catch (IOException | NotUniqueMetaValueException | JiraConfigurationException e)
        {
            String errorMessage = STORY + story.getPath() + lineSeparator() + ERROR + e.getMessage();
            errors.add(errorMessage);
            LOGGER.atError().setCause(e).log("Got an error while exporting story");
        }
    }

    private void exportScenario(String storyTitle, Scenario scenario)
    {
        String scenarioTitle = scenario.getTitle();
        LOGGER.atInfo().addArgument(scenarioTitle).log("Exporting {} scenario");

        try
        {
            String testCaseId = scenario.getUniqueMetaValue(TEST_CASE_ID).orElse(null);

            ZephyrTestCase zephyrTest = new ZephyrTestCase();
            TestCaseParameters parameters = createTestCaseScenarioParameters(scenario);
            fillTestCase(parameters, zephyrTest);

            if (testCaseId != null && zephyrExporterProperties.isUpdateCasesOnExport())
            {
                zephyrFacade.updateTestCase(testCaseId, zephyrTest);
            }
            else
            {
                testCaseId = zephyrFacade.createTestCase(zephyrTest);
                zephyrFacade.createTestSteps(scenario, getIssueId(testCaseId));
            }
            Optional<String> requirementId = scenario.getUniqueMetaValue(REQUIREMENT_ID);
            ExporterFacade.createTestsLink(testCaseId, requirementId, jiraFacade);
        }
        catch (IOException | NotUniqueMetaValueException | JiraConfigurationException e)
        {
            String errorMessage = STORY + storyTitle + lineSeparator() + "Scenario: " + scenarioTitle
                    + lineSeparator() + ERROR + e.getMessage();
            errors.add(errorMessage);
            LOGGER.atError().setCause(e).log("Got an error while exporting scenario");
        }
    }

    private TestCaseParameters createTestCaseScenarioParameters(Scenario scenario)
    {
        TestCaseParameters parameters = createTestCaseParameters(scenario);
        parameters.setSummary(scenario.getTitle());
        return parameters;
    }

    @SuppressWarnings("MagicNumber")
    private TestCaseParameters createTestCaseStoryParameters(Story story)
    {
        String summary = story.getPath();
        File summaryFile = new File(summary);
        TestCaseParameters parameters = createTestCaseParameters(story);
        String storyName = summaryFile.getName();
        storyName = storyName.substring(0, storyName.length() - 6);
        parameters.setSummary(storyName);
        return parameters;
    }

    private TestCaseParameters createTestCaseParameters(IContainingMeta entity)
    {
        TestCaseParameters parameters = new TestCaseParameters();
        parameters.setLabels(entity.getMetaValues(ZEPHYR_LABELS));
        parameters.setComponents(entity.getMetaValues(ZEPHYR_COMPONENTS));
        return parameters;
    }

    private void fillTestCase(TestCaseParameters parameters, ZephyrTestCase zephyrTest)
    {
        zephyrTest.setLabels(parameters.getLabels());
        zephyrTest.setComponents(parameters.getComponents());
        zephyrTest.setSummary(parameters.getSummary());
    }

    private String getIssueId(String testCaseId) throws IOException, JiraConfigurationException
    {
        JiraEntity issue = jiraFacade.getIssue(testCaseId);
        return issue.getId();
    }
}
