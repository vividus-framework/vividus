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

package org.vividus.facade.zephyr;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notEmpty;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.vividus.facade.jira.JiraConfiguration;
import org.vividus.facade.jira.client.IJiraClient;
import org.vividus.util.json.JsonPathUtils;

public class ZephyrFacade implements IZephyrFacade
{
    private static final String GET_PROJECT_ID_AND_VERSION_ID_ENDPOINT = "/rest/api/latest/project/";
    private static final String ZAPI_ENDPOINT = "/rest/zapi/latest/";
    private static final String GET_CYCLE_ID_ENDPOINT = ZAPI_ENDPOINT + "cycle?projectId=%s&versionId=%s";
    private static final String GET_FOLDER_ID_ENDPOINT = ZAPI_ENDPOINT +  "cycle/%s/folders?projectId=%s&versionId=%s";
    private static final String CREATE_EXECUTION_ENDPOINT = ZAPI_ENDPOINT + "execution/";
    private static final String UPDATE_EXECUTION_STATUS_ENDPOINT = ZAPI_ENDPOINT + "execution/%s/execute";

    private static final String PROJECT_OR_ISSUE_ID_JSON_PATH = "$.id";
    private static final String VERSION_ID_JSON_PATH = "$.versions.[?(@.name=='%s')].id";
    private static final String FOLDER_ID_JSON_PATH = "$.[?(@.folderName=='%s')].folderId";
    private static final String EXECUTION_ID_JSON_PATH = "$..id";

    private IJiraClient client;
    private JiraConfiguration jiraConfiguration;
    private ZephyrConfiguration zephyrConfiguration;

    @Override
    public Integer createExecution(String execution)
    {
        String responseBody = client.executePost(jiraConfiguration, CREATE_EXECUTION_ENDPOINT, execution);
        List<Integer> executionId = JsonPathUtils.getData(responseBody, EXECUTION_ID_JSON_PATH);
        return executionId.get(0);
    }

    @Override
    public void updateExecutionStatus(int executionId, String executionBody)
    {
        client.executePut(jiraConfiguration, String.format(UPDATE_EXECUTION_STATUS_ENDPOINT, executionId),
                executionBody);
    }

    @Override
    public ZephyrConfiguration prepareConfiguration()
    {
        notBlank(jiraConfiguration.getUsername(), "Property 'jira.username=' should not be empty");
        notBlank(jiraConfiguration.getPassword(), "Property 'jira.password=' should not be empty");
        notBlank(jiraConfiguration.getEndpoint().toString(),
                "Property 'jira.endpoint=' should not be empty");
        notBlank(zephyrConfiguration.getProjectKey(),
                "Property 'zephyr.project-key=' should not be empty");
        notBlank(zephyrConfiguration.getVersionName(),
                "Property 'zephyr.version-name=' should not be empty");
        notBlank(zephyrConfiguration.getCycleName(),
                "Property 'zephyr.cycle-name=' should not be empty");
        notBlank(zephyrConfiguration.getFolderName(),
                "Property 'zephyr.folder-name=' should not be empty");
        findProjectAndVersionId();
        findCycleId();
        findFolderId();
        return zephyrConfiguration;
    }

    private void findProjectAndVersionId()
    {
        String json = client.executeGet(jiraConfiguration, GET_PROJECT_ID_AND_VERSION_ID_ENDPOINT
                + zephyrConfiguration.getProjectKey());
        String projectId = JsonPathUtils.getData(json, PROJECT_OR_ISSUE_ID_JSON_PATH);
        List<String> versionId = JsonPathUtils.getData(json,
                String.format(VERSION_ID_JSON_PATH, zephyrConfiguration.getVersionName()));
        notEmpty(versionId, String.format("Version by name '%s' does not exist", zephyrConfiguration.getVersionName()));
        zephyrConfiguration.setProjectId(projectId);
        zephyrConfiguration.setVersionId(versionId.get(0));
    }

    private void findCycleId()
    {
        String json = client.executeGet(jiraConfiguration, String.format(GET_CYCLE_ID_ENDPOINT,
                zephyrConfiguration.getProjectId(), zephyrConfiguration.getVersionId()));
        Map<String, Map<String, String>> cycles = JsonPathUtils.getData(json, "$");
        cycles.remove("recordsCount");
        Iterator<Map.Entry<String, Map<String, String>>> itr = cycles.entrySet().iterator();
        String cycleId = "";
        while (itr.hasNext())
        {
            Map.Entry<String, Map<String, String>> map = itr.next();
            if (map.getValue().get("name").equals(zephyrConfiguration.getCycleName()))
            {
                cycleId = map.getKey();
                break;
            }
        }
        notBlank(cycleId, String.format("Cycle by name '%s' does not exist", zephyrConfiguration.getCycleName()));
        zephyrConfiguration.setCycleId(cycleId);
    }

    private void findFolderId()
    {
        String json = client.executeGet(jiraConfiguration, String.format(GET_FOLDER_ID_ENDPOINT,
                zephyrConfiguration.getCycleId(), zephyrConfiguration.getProjectId(),
                zephyrConfiguration.getVersionId()));
        List<Integer> folderId = JsonPathUtils.getData(json, String.format(FOLDER_ID_JSON_PATH,
                zephyrConfiguration.getFolderName()));
        notEmpty(folderId, String.format("Folder by name '%s' does not exist", zephyrConfiguration.getFolderName()));
        zephyrConfiguration.setFolderId(folderId.get(0).toString());
    }

    public void setClient(IJiraClient client)
    {
        this.client = client;
    }

    public void setJiraConfiguration(JiraConfiguration jiraConfiguration)
    {
        this.jiraConfiguration = jiraConfiguration;
    }

    public void setZephyrConfiguration(ZephyrConfiguration zephyrConfiguration)
    {
        this.zephyrConfiguration = zephyrConfiguration;
    }
}
