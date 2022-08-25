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
import java.util.OptionalInt;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.vividus.jira.JiraClientProvider;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.Project;
import org.vividus.jira.model.Version;
import org.vividus.util.json.JsonPathUtils;
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterProperties;
import org.vividus.zephyr.model.TestCaseStatus;

@Configuration
@ConditionalOnProperty(value = "zephyr.exporter.api-type", havingValue = "SQUAD")
public class ZephyrSquadFacade extends AbstractZephyrFacade
{
    private static final String ZAPI_ENDPOINT = "/rest/zapi/latest/";

    public ZephyrSquadFacade(JiraFacade jiraFacade, JiraClientProvider jiraClientProvider,
            ZephyrExporterConfiguration zephyrExporterConfiguration, ZephyrExporterProperties zephyrExporterProperties)
    {
        super(jiraFacade, jiraClientProvider, zephyrExporterConfiguration, zephyrExporterProperties);
    }

    @Override
    public Integer createExecution(String execution) throws IOException, JiraConfigurationException
    {
        String responseBody = getJiraClient().executePost(ZAPI_ENDPOINT + "execution/", execution);
        List<Integer> executionId = JsonPathUtils.getData(responseBody, "$..id");
        return executionId.get(0);
    }

    @Override
    public void updateExecutionStatus(String executionId, String executionBody)
            throws IOException, JiraConfigurationException
    {
        getJiraClient().executePut(String.format(ZAPI_ENDPOINT + "execution/%s/execute", executionId), executionBody);
    }

    @Override
    public ZephyrConfiguration prepareConfiguration() throws IOException, JiraConfigurationException
    {
        ZephyrConfiguration zephyrConfiguration = new ZephyrConfiguration();

        Project project = getJiraFacade().getProject(getZephyrExporterConfiguration().getProjectKey());
        String projectId = project.getId();

        String versionId = findVersionId(project);
        zephyrConfiguration.setVersionId(versionId);

        String projectAndVersionUrlQuery = String.format("projectId=%s&versionId=%s", projectId, versionId);

        String cycleId = findCycleId(projectAndVersionUrlQuery);

        if (StringUtils.isNotBlank(getZephyrExporterConfiguration().getFolderName()))
        {
            String folderId = findFolderId(cycleId, projectAndVersionUrlQuery);
            zephyrConfiguration.setFolderId(folderId);
        }

        Map<TestCaseStatus, String> statusIdMap = getExecutionStatuses();

        prepareBaseConfiguration(zephyrConfiguration, cycleId, statusIdMap);

        return zephyrConfiguration;
    }

    private String findVersionId(Project project)
    {
        return project.getVersions()
                .stream()
                .filter(v -> getZephyrExporterConfiguration().getVersionName().equals(v.getName()))
                .findFirst()
                .map(Version::getId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Version with name '%s' does not exist", getZephyrExporterConfiguration().getVersionName())));
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
            if (map.getValue().get("name").equals(getZephyrExporterConfiguration().getCycleName()))
            {
                cycleId = map.getKey();
                break;
            }
        }
        notBlank(cycleId, "Cycle with name '%s' does not exist", getZephyrExporterConfiguration().getCycleName());
        return cycleId;
    }

    private String findFolderId(String cycleId, String projectAndVersionUrlQuery)
            throws IOException, JiraConfigurationException
    {
        String json = getJiraClient()
                .executeGet(ZAPI_ENDPOINT + "cycle/" + cycleId + "/folders?" + projectAndVersionUrlQuery);
        List<Integer> folderId = JsonPathUtils.getData(json, String.format("$.[?(@.folderName=='%s')].folderId",
                getZephyrExporterConfiguration().getFolderName()));
        notEmpty(folderId, "Folder with name '%s' does not exist", getZephyrExporterConfiguration().getFolderName());
        return folderId.get(0).toString();
    }

    private Map<TestCaseStatus, String> getExecutionStatuses() throws IOException, JiraConfigurationException
    {
        String json = getJiraClient().executeGet(ZAPI_ENDPOINT + "util/testExecutionStatus");
        Map<TestCaseStatus, String> testStatusPerZephyrIdMapping = new EnumMap<>(TestCaseStatus.class);
        getZephyrExporterConfiguration().getStatuses().forEach((testCaseStatus, statusName) ->
        {
            List<Integer> statusId = JsonPathUtils.getData(json, String.format("$.[?(@.name=='%s')].id", statusName));
            notEmpty(statusId, "Status '%s' does not exist", statusName);
            testStatusPerZephyrIdMapping.put(testCaseStatus, String.valueOf(statusId.get(0)));
        });
        return testStatusPerZephyrIdMapping;
    }

    @Override
    public OptionalInt findExecutionId(String issueId) throws IOException, JiraConfigurationException
    {
        String json = getJiraClient().executeGet(ZAPI_ENDPOINT + "execution?issueId=" + issueId);
        String jsonpath;
        if (StringUtils.isNotBlank(getZephyrExporterConfiguration().getFolderName()))
        {
            jsonpath = String.format("$..[?(@.versionName=='%s' && @.cycleName=='%s' && @.folderName=='%s')].id",
                    getZephyrExporterConfiguration().getVersionName(), getZephyrExporterConfiguration().getCycleName(),
                    getZephyrExporterConfiguration().getFolderName());
        }
        else
        {
            jsonpath = String.format("$..[?(@.versionName=='%s' && @.cycleName=='%s')].id",
                    getZephyrExporterConfiguration().getVersionName(), getZephyrExporterConfiguration().getCycleName());
        }
        List<Integer> executionId = JsonPathUtils.getData(json, jsonpath);
        return executionId.isEmpty() ? OptionalInt.empty() : OptionalInt.of(executionId.get(0));
    }
}
