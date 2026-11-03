/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.xray.facade;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jspecify.annotations.Nullable;
import org.vividus.jira.JiraClient;
import org.vividus.jira.JiraClientProvider;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.util.json.JsonPathUtils;
import org.vividus.xray.model.AddOperationRequest;

public class XrayServerClient implements XrayClient
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setDefaultPropertyInclusion(Include.NON_NULL);
    private static final String TEST_REPOSITORY_ENDPOINT = "/rest/raven/1.0/api/testrepository/";
    private static final String FOLDERS = "folders";
    private static final String NAME = "name";
    private static final String PATH_SEPARATOR = "/";

    private final JiraClientProvider jiraClientProvider;
    @Nullable private final String jiraInstanceKey;

    public XrayServerClient(JiraClientProvider jiraClientProvider, @Nullable String jiraInstanceKey)
    {
        this.jiraClientProvider = jiraClientProvider;
        this.jiraInstanceKey = jiraInstanceKey;
    }

    @Override
    public String importExecution(String executionJson) throws IOException
    {
        try
        {
            String response = jiraClientProvider.getByJiraConfigurationKey(Optional.ofNullable(jiraInstanceKey))
                    .executePost("/rest/raven/1.0/import/execution", executionJson);
            return JsonPathUtils.getData(response, "$.testExecIssue.key");
        }
        catch (JiraConfigurationException e)
        {
            throw new IOException(e);
        }
    }

    @Override
    public void addTestsToTestSet(String testSetKey, List<String> testCaseKeys) throws IOException
    {
        try
        {
            String requestBody = OBJECT_MAPPER.writeValueAsString(new AddOperationRequest(testCaseKeys));
            jiraClientProvider.getByIssueKey(testSetKey)
                    .executePost("/rest/raven/1.0/api/testset/" + testSetKey + "/test", requestBody);
        }
        catch (JiraConfigurationException e)
        {
            throw new IOException(e);
        }
    }

    @Override
    public void addTestToRepository(String testCaseKey, String path) throws IOException
    {
        String normalizedPath = normalizePath(path);
        try
        {
            JiraClient jiraClient = jiraClientProvider.getByIssueKey(testCaseKey);
            String projectKey = JsonPathUtils.getData(jiraClient.executeGet(
                    "/rest/api/latest/issue/" + testCaseKey + "?fields=project"), "$.fields.project.key");
            JsonNode folderTree = OBJECT_MAPPER.readTree(jiraClient.executeGet(
                    TEST_REPOSITORY_ENDPOINT + projectKey + PATH_SEPARATOR + FOLDERS));
            JsonNode folders = folderTree.isArray() ? folderTree : folderTree.path(FOLDERS);
            String folderId = normalizedPath.isEmpty() ? "-1" : findFolderId(folders, "", normalizedPath);
            if (folderId == null)
            {
                throw new IOException("Xray Test Repository folder path '" + path + "' does not exist");
            }
            String requestBody = OBJECT_MAPPER.writeValueAsString(new AddOperationRequest(List.of(testCaseKey)));
            jiraClient.executePut(TEST_REPOSITORY_ENDPOINT + projectKey + "/folders/" + folderId + "/tests",
                    requestBody);
        }
        catch (JiraConfigurationException e)
        {
            throw new IOException(e);
        }
    }

    private static String findFolderId(JsonNode folders, String parentPath, String targetPath)
    {
        if (!folders.isArray())
        {
            return null;
        }
        for (JsonNode folder : folders)
        {
            String folderName = folder.path(NAME).asText();
            String folderPath = parentPath.isEmpty() ? folderName : parentPath + PATH_SEPARATOR + folderName;
            if (folderPath.equals(targetPath))
            {
                return folder.path("id").asText();
            }
            String folderId = findFolderId(folder.path(FOLDERS), folderPath, targetPath);
            if (folderId != null)
            {
                return folderId;
            }
        }
        return null;
    }

    private static String normalizePath(String path) throws IOException
    {
        String normalizedPath = path.replaceAll("^" + PATH_SEPARATOR + "+|" + PATH_SEPARATOR + "+$", "");
        if (normalizedPath.isEmpty())
        {
            return "";
        }
        for (String segment : normalizedPath.split(PATH_SEPARATOR, -1))
        {
            if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment))
            {
                throw new IOException("Invalid Xray Test Repository folder path: " + path);
            }
        }
        return normalizedPath;
    }
}
