/*
 * Copyright 2019-2020 the original author or authors.
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

import org.vividus.jira.JiraClient;
import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.Project;
import org.vividus.jira.model.Version;
import org.vividus.util.json.JsonPathUtils;
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterConfiguration;
import org.vividus.zephyr.model.TestCaseStatus;

public class ZephyrFacade implements IZephyrFacade
{
    private static final String ZAPI_ENDPOINT = "/rest/zapi/latest/";

    private final JiraFacade jiraFacade;
    private final JiraClient client;
    private final ZephyrExporterConfiguration zephyrExporterConfiguration;

    public ZephyrFacade(JiraFacade jiraFacade, JiraClient jiraClient,
            ZephyrExporterConfiguration zephyrExporterConfiguration)
    {
        this.jiraFacade = jiraFacade;
        this.client = jiraClient;
        this.zephyrExporterConfiguration = zephyrExporterConfiguration;
    }

    @Override
    public Integer createExecution(String execution) throws IOException
    {
        String responseBody = client.executePost(ZAPI_ENDPOINT + "execution/", execution);
        List<Integer> executionId = JsonPathUtils.getData(responseBody, "$..id");
        return executionId.get(0);
    }

    @Override
    public void updateExecutionStatus(int executionId, String executionBody) throws IOException
    {
        client.executePut(String.format(ZAPI_ENDPOINT + "execution/%s/execute", executionId), executionBody);
    }

    @Override
    public ZephyrConfiguration prepareConfiguration() throws IOException
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

        String folderId = findFolderId(cycleId, projectAndVersionUrlQuery);
        zephyrConfiguration.setFolderId(folderId);

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

    private String findCycleId(String projectAndVersionUrlQuery) throws IOException
    {
        String json = client.executeGet(ZAPI_ENDPOINT + "cycle?" + projectAndVersionUrlQuery);
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

    private String findFolderId(String cycleId, String projectAndVersionUrlQuery) throws IOException
    {
        String json = client.executeGet(ZAPI_ENDPOINT + "cycle/" + cycleId + "/folders?" + projectAndVersionUrlQuery);
        List<Integer> folderId = JsonPathUtils.getData(json, String.format("$.[?(@.folderName=='%s')].folderId",
                zephyrExporterConfiguration.getFolderName()));
        notEmpty(folderId, "Folder with name '%s' does not exist", zephyrExporterConfiguration.getFolderName());
        return folderId.get(0).toString();
    }

    private Map<TestCaseStatus, Integer> getExecutionStatuses() throws IOException
    {
        String json = client.executeGet(ZAPI_ENDPOINT + "util/testExecutionStatus");
        Map<TestCaseStatus, Integer> testStatusPerZephyrIdMapping = new EnumMap<>(TestCaseStatus.class);
        zephyrExporterConfiguration.getStatuses().entrySet().forEach(s ->
        {
            List<Integer> statusId = JsonPathUtils.getData(json, String.format("$.[?(@.name=='%s')].id", s.getValue()));
            notEmpty(statusId, "Status '%s' does not exist", s.getValue());
            testStatusPerZephyrIdMapping.put(s.getKey(), statusId.get(0));
        });
        return testStatusPerZephyrIdMapping;
    }
}
