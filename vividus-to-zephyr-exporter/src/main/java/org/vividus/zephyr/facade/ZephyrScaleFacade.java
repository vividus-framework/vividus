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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.vividus.jira.JiraClientProvider;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraFacade;
import org.vividus.util.json.JsonPathUtils;
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterProperties;

@Configuration
@ConditionalOnProperty(value = "zephyr.exporter.api-type", havingValue = "SCALE")
public class ZephyrScaleFacade extends AbstractZephyrFacade
{
    private static final String BASE_ENDPOINT = "/rest/atm/1.0";
    private static final String TEST_RESULT_ENDPOINT = BASE_ENDPOINT + "/testrun/%s/testcase/%s/testresult";

    private ZephyrConfiguration zephyrConfiguration;

    public ZephyrScaleFacade(JiraFacade jiraFacade, JiraClientProvider jiraClientProvider,
            ZephyrExporterConfiguration zephyrExporterConfiguration, ZephyrExporterProperties zephyrExporterProperties)
    {
        super(jiraFacade, jiraClientProvider, zephyrExporterConfiguration, zephyrExporterProperties);
    }

    @Override
    public Integer createExecution(String execution) throws IOException, JiraConfigurationException
    {
        String testCaseResultUrl = String.format(TEST_RESULT_ENDPOINT,
                zephyrConfiguration.getCycleId(),
                execution);
        String responseBody = getJiraClient().executePost(
                testCaseResultUrl,
                "{}");
        return JsonPathUtils.getData(responseBody, "$.id");
    }

    @Override
    public void updateExecutionStatus(String executionId, String executionBody)
            throws IOException, JiraConfigurationException
    {
        String testCaseResultUrl = String.format(TEST_RESULT_ENDPOINT,
                zephyrConfiguration.getCycleId(),
                executionId);
        getJiraClient().executePut(testCaseResultUrl, executionBody);
    }

    @Override
    public ZephyrConfiguration prepareConfiguration() throws IOException, JiraConfigurationException
    {
        zephyrConfiguration = new ZephyrConfiguration();

        String cycleId = createTestCycle(
                getZephyrExporterConfiguration().getCycleName(),
                getZephyrExporterConfiguration().getProjectKey(),
                getZephyrExporterConfiguration().getFolderName());

        prepareBaseConfiguration(zephyrConfiguration, cycleId, getZephyrExporterConfiguration().getStatuses());

        return zephyrConfiguration;
    }

    private String createTestCycle(String cycleName,
                                   String projectKey,
                                   String folderName) throws IOException, JiraConfigurationException
    {
        Map<String, Object> params = new HashMap<>();
        params.put("name", cycleName);
        params.put("projectKey", projectKey);
        params.put("folder", "/" + folderName);
        String body = new ObjectMapper().writeValueAsString(params);
        String json = getJiraClient().executePost(BASE_ENDPOINT + "/testrun", body);
        return JsonPathUtils.getData(json, "$.key");
    }

    @Override
    public OptionalInt findExecutionId(String issueId)
    {
        // Execution ID is not applicable for Zephyr Scale. All interaction occurs using the id of the test case.
        throw new UnsupportedOperationException("Execution ID is not applicable for Zephyr Scale."
                + " All interaction occurs using the id of the test case");
    }
}
