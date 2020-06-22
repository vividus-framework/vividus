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

package org.vividus.zephyr;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notEmpty;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.vividus.jira.JiraClient;
import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.Project;
import org.vividus.jira.model.Version;
import org.vividus.util.json.JsonPathUtils;

public class ZephyrFacade implements IZephyrFacade
{
    private static final String ZAPI_ENDPOINT = "/rest/zapi/latest/";
    private static final String GET_CYCLE_ID_ENDPOINT = ZAPI_ENDPOINT + "cycle?projectId=%s&versionId=%s";
    private static final String CREATE_EXECUTION_ENDPOINT = ZAPI_ENDPOINT + "execution/";
    private static final String UPDATE_EXECUTION_STATUS_ENDPOINT = ZAPI_ENDPOINT + "execution/%s/execute";

    private static final String FOLDER_ID_JSON_PATH = "$.[?(@.folderName=='%s')].folderId";
    private static final String EXECUTION_ID_JSON_PATH = "$..id";

    private final JiraFacade jiraFacade;
    private final JiraClient client;
    private final ZephyrConfiguration zephyrConfiguration;

    public ZephyrFacade(JiraFacade jiraFacade, JiraClient client, ZephyrConfiguration zephyrConfiguration)
    {
        this.jiraFacade = jiraFacade;
        this.client = client;
        this.zephyrConfiguration = zephyrConfiguration;
    }

    @Override
    public Integer createExecution(String execution) throws IOException
    {
        String responseBody = client.executePost(CREATE_EXECUTION_ENDPOINT, execution);
        List<Integer> executionId = JsonPathUtils.getData(responseBody, EXECUTION_ID_JSON_PATH);
        return executionId.get(0);
    }

    @Override
    public void updateExecutionStatus(int executionId, String executionBody) throws IOException
    {
        client.executePut(String.format(UPDATE_EXECUTION_STATUS_ENDPOINT, executionId), executionBody);
    }

    @Override
    public ZephyrConfiguration prepareConfiguration() throws IOException
    {
        notBlank(zephyrConfiguration.getProjectKey(), "Property 'zephyr.project-key=' should not be empty");
        notBlank(zephyrConfiguration.getVersionName(), "Property 'zephyr.version-name=' should not be empty");
        notBlank(zephyrConfiguration.getCycleName(), "Property 'zephyr.cycle-name=' should not be empty");
        notBlank(zephyrConfiguration.getFolderName(), "Property 'zephyr.folder-name=' should not be empty");
        findProjectAndVersionId();
        findCycleId();
        findFolderId(zephyrConfiguration.getCycleId(), zephyrConfiguration.getProjectId(),
                zephyrConfiguration.getVersionId());
        return zephyrConfiguration;
    }

    private void findProjectAndVersionId() throws IOException
    {
        Project project = jiraFacade.getProject(zephyrConfiguration.getProjectKey());
        Optional<Version> version = project.getVersions().stream().filter(
            v -> zephyrConfiguration.getVersionName().equals(v.getName())).findFirst();
        isTrue(version.isPresent(), "Version with name '%s' does not exist", zephyrConfiguration.getVersionName());
        zephyrConfiguration.setProjectId(project.getId());
        zephyrConfiguration.setVersionId(version.get().getId());
    }

    private void findCycleId() throws IOException
    {
        String json = client.executeGet(String.format(GET_CYCLE_ID_ENDPOINT,
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
        notBlank(cycleId, "Cycle with name '%s' does not exist", zephyrConfiguration.getCycleName());
        zephyrConfiguration.setCycleId(cycleId);
    }

    private void findFolderId(String cycleId, String projectId, String versionId) throws IOException
    {
        String json = client.executeGet(String.format(ZAPI_ENDPOINT +  "cycle/%s/folders?projectId=%s&versionId=%s",
                cycleId, projectId, versionId));
        List<Integer> folderId = JsonPathUtils.getData(json, String.format(FOLDER_ID_JSON_PATH,
                zephyrConfiguration.getFolderName()));
        notEmpty(folderId, "Folder with name '%s' does not exist", zephyrConfiguration.getFolderName());
        zephyrConfiguration.setFolderId(folderId.get(0).toString());
    }
}
