/*
 * Copyright 2019-2025 the original author or authors.
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
import com.fasterxml.jackson.databind.ObjectMapper;

import org.vividus.jira.JiraClientProvider;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.util.json.JsonPathUtils;
import org.vividus.xray.model.AddOperationRequest;

public class XrayServerClient implements XrayClient
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setDefaultPropertyInclusion(Include.NON_NULL);

    private final JiraClientProvider jiraClientProvider;
    private final Optional<String> jiraInstanceKey;

    public XrayServerClient(JiraClientProvider jiraClientProvider, Optional<String> jiraInstanceKey)
    {
        this.jiraClientProvider = jiraClientProvider;
        this.jiraInstanceKey = jiraInstanceKey;
    }

    @Override
    public String importExecution(String executionJson) throws IOException
    {
        try
        {
            String response = jiraClientProvider.getByJiraConfigurationKey(jiraInstanceKey)
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
}
