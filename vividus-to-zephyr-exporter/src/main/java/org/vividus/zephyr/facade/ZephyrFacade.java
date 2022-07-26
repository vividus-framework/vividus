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

package org.vividus.zephyr.facade;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notEmpty;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.exporter.converter.CucumberExamplesConverter;
import org.vividus.jira.JiraClient;
import org.vividus.jira.JiraClientProvider;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.Project;
import org.vividus.jira.model.Version;
import org.vividus.model.jbehave.Examples;
import org.vividus.model.jbehave.Scenario;
import org.vividus.model.jbehave.Step;
import org.vividus.model.jbehave.Story;
import org.vividus.util.json.JsonPathUtils;
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterProperties;
import org.vividus.zephyr.databind.TestCaseSerializer;
import org.vividus.zephyr.model.TestCaseStatus;
import org.vividus.zephyr.model.TestStep;
import org.vividus.zephyr.model.ZephyrTestCase;

public class ZephyrFacade implements IZephyrFacade
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ZephyrFacade.class);
    private static final String ZAPI_ENDPOINT = "/rest/zapi/latest/";
    private static final String TEST_STEP_CREATED = "{} test step has been created";
    private static final String TESTSTEP = "teststep/%s";

    private final JiraFacade jiraFacade;
    private final JiraClientProvider jiraClientProvider;
    private final ZephyrExporterConfiguration zephyrExporterConfiguration;
    private final ZephyrExporterProperties zephyrExporterProperties;
    private final ObjectMapper objectMapper;

    public ZephyrFacade(JiraFacade jiraFacade, JiraClientProvider jiraClientProvider,
                        ZephyrExporterConfiguration zephyrExporterConfiguration,
                        ZephyrExporterProperties zephyrExporterProperties, TestCaseSerializer testCaseSerializer)
    {
        this.jiraFacade = jiraFacade;
        this.jiraClientProvider = jiraClientProvider;
        this.zephyrExporterConfiguration = zephyrExporterConfiguration;
        this.zephyrExporterProperties = zephyrExporterProperties;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModule(new SimpleModule().addSerializer(ZephyrTestCase.class, testCaseSerializer));
    }

    @Override
    public Integer createExecution(String execution) throws IOException, JiraConfigurationException
    {
        String responseBody = getJiraClient().executePost(ZAPI_ENDPOINT + "execution/", execution);
        List<Integer> executionId = JsonPathUtils.getData(responseBody, "$..id");
        return executionId.get(0);
    }

    @Override
    public void updateTestCase(String testCaseId, ZephyrTestCase zephyrTest)
            throws IOException, JiraConfigurationException
    {
        String updateTestRequest = objectMapper.writeValueAsString(zephyrTest);
        LOGGER.atInfo().addArgument(testCaseId)
                .addArgument(updateTestRequest)
                .log("Updating Test Case with ID {}: {}");
        jiraFacade.updateIssue(testCaseId, updateTestRequest);
        jiraFacade.changeIssueStatus(testCaseId, zephyrExporterProperties.getStatusForUpdatedTestCases());
        LOGGER.atInfo().addArgument(testCaseId)
                .log("Test with key {} has been updated");
    }

    @Override
    public String createTestCase(ZephyrTestCase zephyrTest) throws IOException, JiraConfigurationException
    {
        zephyrTest.setProjectKey(zephyrExporterConfiguration.getProjectKey());
        String createTestRequest = objectMapper.writeValueAsString(zephyrTest);
        LOGGER.atInfo().addArgument(createTestRequest).log("Creating Test Case: {}");
        String response = jiraFacade
                .createIssue(createTestRequest, Optional.ofNullable(zephyrExporterProperties.getJiraInstanceKey()));
        String issueKey = JsonPathUtils.getData(response, "$.key");
        LOGGER.atInfo().addArgument(issueKey).log("Test with key {} has been created");
        return issueKey;
    }

    @Override
    public void createTestSteps(Scenario scenario, String issueId) throws IOException, JiraConfigurationException
    {
        List<Step> steps = scenario.collectSteps();
        for (int i = 0; i < steps.size(); i++)
        {
            LOGGER.atInfo().addArgument(i + 1).log(TEST_STEP_CREATED);
            String executionBody = objectMapper.writeValueAsString(new TestStep(steps.get(i).getValue(), null));
            getJiraClient().executePost(String.format(ZAPI_ENDPOINT + TESTSTEP, issueId), executionBody);
        }
        Examples examples = scenario.getExamples();

        if (Objects.nonNull(examples))
        {
            String examplesStr = CucumberExamplesConverter
                    .buildScenarioExamplesTableWithoutName(examples.getParameters());
            String executionBody = objectMapper.writeValueAsString(new TestStep("Examples:", examplesStr));
            getJiraClient().executePost(String.format(ZAPI_ENDPOINT + TESTSTEP, issueId), executionBody);
        }
    }

    @Override
    public void createTestSteps(Story story, String issueId) throws IOException, JiraConfigurationException
    {
        List<Scenario> scenarios = story.getScenarios();
        for (int i = 0; i < scenarios.size(); i++)
        {
            LOGGER.atInfo().addArgument(i + 1).log(TEST_STEP_CREATED);
            String executionBody = objectMapper
                    .writeValueAsString(new TestStep(scenarios.get(i).getTitle(), null));
            getJiraClient().executePost(String.format(ZAPI_ENDPOINT + TESTSTEP, issueId), executionBody);
        }
    }

    @Override
    public void updateExecutionStatus(int executionId, String executionBody)
            throws IOException, JiraConfigurationException
    {
        getJiraClient().executePut(String.format(ZAPI_ENDPOINT + "execution/%s/execute", executionId), executionBody);
    }

    @Override
    public ZephyrConfiguration prepareConfiguration() throws IOException, JiraConfigurationException
    {
        ZephyrConfiguration zephyrConfiguration = new ZephyrConfiguration();

        Project project = jiraFacade.getProject(zephyrExporterConfiguration.getProjectKey());
        String projectId = project.getId();
        zephyrConfiguration.setProjectId(projectId);

        String versionId = findVersionId(project);
        zephyrConfiguration.setVersionId(versionId);

        String projectAndVersionUrlQuery = String.format("projectId=%s&versionId=%s", projectId, versionId);

        String cycleId = findCycleId(projectAndVersionUrlQuery);
        zephyrConfiguration.setCycleId(cycleId);

        if (StringUtils.isNotBlank(zephyrExporterConfiguration.getFolderName()))
        {
            String folderId = findFolderId(cycleId, projectAndVersionUrlQuery);
            zephyrConfiguration.setFolderId(folderId);
        }

        Map<TestCaseStatus, Integer> statusIdMap = getExecutionStatuses();
        zephyrConfiguration.setTestStatusPerZephyrIdMapping(statusIdMap);

        return zephyrConfiguration;
    }

    private String findVersionId(Project project)
    {
        return project.getVersions()
                .stream()
                .filter(v -> zephyrExporterConfiguration.getVersionName().equals(v.getName()))
                .findFirst()
                .map(Version::getId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Version with name '%s' does not exist", zephyrExporterConfiguration.getVersionName())));
    }

    private String findCycleId(String projectAndVersionUrlQuery) throws IOException, JiraConfigurationException
    {
        String json = getJiraClient().executeGet(ZAPI_ENDPOINT + "cycle?" + projectAndVersionUrlQuery);
        Map<String, Map<String, String>> cycles = JsonPathUtils.getData(json, "$");
        cycles.remove("recordsCount");
        Iterator<Map.Entry<String, Map<String, String>>> itr = cycles.entrySet().iterator();
        String cycleId = "";
        while (itr.hasNext())
        {
            Map.Entry<String, Map<String, String>> map = itr.next();
            if (map.getValue().get("name").equals(zephyrExporterConfiguration.getCycleName()))
            {
                cycleId = map.getKey();
                break;
            }
        }
        notBlank(cycleId, "Cycle with name '%s' does not exist", zephyrExporterConfiguration.getCycleName());
        return cycleId;
    }

    private String findFolderId(String cycleId, String projectAndVersionUrlQuery)
            throws IOException, JiraConfigurationException
    {
        String json = getJiraClient()
                .executeGet(ZAPI_ENDPOINT + "cycle/" + cycleId + "/folders?" + projectAndVersionUrlQuery);
        List<Integer> folderId = JsonPathUtils.getData(json, String.format("$.[?(@.folderName=='%s')].folderId",
                zephyrExporterConfiguration.getFolderName()));
        notEmpty(folderId, "Folder with name '%s' does not exist", zephyrExporterConfiguration.getFolderName());
        return folderId.get(0).toString();
    }

    @Override
    public OptionalInt findExecutionId(String issueId) throws IOException, JiraConfigurationException
    {
        String json = getJiraClient().executeGet(ZAPI_ENDPOINT + "execution?issueId=" + issueId);
        String jsonpath;
        if (StringUtils.isNotBlank(zephyrExporterConfiguration.getFolderName()))
        {
            jsonpath = String.format("$..[?(@.versionName=='%s' && @.cycleName=='%s' && @.folderName=='%s')].id",
                    zephyrExporterConfiguration.getVersionName(), zephyrExporterConfiguration.getCycleName(),
                    zephyrExporterConfiguration.getFolderName());
        }
        else
        {
            jsonpath = String.format("$..[?(@.versionName=='%s' && @.cycleName=='%s')].id",
                    zephyrExporterConfiguration.getVersionName(), zephyrExporterConfiguration.getCycleName());
        }
        List<Integer> executionId = JsonPathUtils.getData(json, jsonpath);
        return executionId.size() != 0 ? OptionalInt.of(executionId.get(0)) : OptionalInt.empty();
    }

    private Map<TestCaseStatus, Integer> getExecutionStatuses() throws IOException, JiraConfigurationException
    {
        String json = getJiraClient().executeGet(ZAPI_ENDPOINT + "util/testExecutionStatus");
        Map<TestCaseStatus, Integer> testStatusPerZephyrIdMapping = new EnumMap<>(TestCaseStatus.class);
        zephyrExporterConfiguration.getStatuses().forEach((key, value) -> {
            List<Integer> statusId = JsonPathUtils.getData(json, String.format("$.[?(@.name=='%s')].id", value));
            notEmpty(statusId, "Status '%s' does not exist", value);
            testStatusPerZephyrIdMapping.put(key, statusId.get(0));
        });
        return testStatusPerZephyrIdMapping;
    }

    private JiraClient getJiraClient() throws JiraConfigurationException
    {
        return jiraClientProvider
                .getByJiraConfigurationKey(Optional.ofNullable(zephyrExporterProperties.getJiraInstanceKey()));
    }
}
