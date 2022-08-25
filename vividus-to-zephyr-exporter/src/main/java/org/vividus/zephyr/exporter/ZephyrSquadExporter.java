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

import java.io.IOException;
import java.util.OptionalInt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.JiraEntity;
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterProperties;
import org.vividus.zephyr.facade.ZephyrFacade;
import org.vividus.zephyr.model.ExecutionStatus;
import org.vividus.zephyr.model.TestCase;
import org.vividus.zephyr.model.ZephyrExecution;
import org.vividus.zephyr.parser.TestCaseParser;

@Configuration
@ConditionalOnProperty(value = "zephyr.exporter.api-type", havingValue = "SQUAD")
public class ZephyrSquadExporter extends AbstractZephyrExporter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ZephyrSquadExporter.class);

    private final JiraFacade jiraFacade;
    private final ZephyrExporterProperties zephyrExporterProperties;

    public ZephyrSquadExporter(JiraFacade jiraFacade, ZephyrFacade zephyrFacade, TestCaseParser testCaseParser,
                               ZephyrExporterProperties zephyrExporterProperties)
    {
        super(zephyrFacade, testCaseParser);
        this.jiraFacade = jiraFacade;
        this.zephyrExporterProperties = zephyrExporterProperties;
    }

    @Override
    protected void exportTestExecution(TestCase testCase, ZephyrConfiguration configuration)
            throws IOException, JiraConfigurationException
    {
        JiraEntity issue = jiraFacade.getIssue(testCase.getKey());
        ZephyrExecution execution = new ZephyrExecution(configuration, issue.getId(), testCase.getStatus());
        OptionalInt executionId;

        if (zephyrExporterProperties.getUpdateExecutionStatusesOnly())
        {
            executionId = getZephyrFacade().findExecutionId(issue.getId());
        }
        else
        {
            String createExecution = getObjectMapper().writeValueAsString(execution);
            executionId = OptionalInt.of(getZephyrFacade().createExecution(createExecution));
        }
        if (executionId.isPresent())
        {
            String executionBody = getObjectMapper().writeValueAsString(new ExecutionStatus(
                String.valueOf(configuration.getTestStatusPerZephyrStatusMapping()
                        .get(execution.getTestCaseStatus()))));
            getZephyrFacade().updateExecutionStatus(String.valueOf(executionId.getAsInt()), executionBody);
        }
        else
        {
            LOGGER.atInfo().addArgument(testCase::getKey).log("Test case result for {} was not exported, "
                    + "because execution does not exist");
        }
    }
}
