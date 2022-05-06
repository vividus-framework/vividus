/*
 * Copyright 2019-2021 the original author or authors.
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

import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraFacade;
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterProperties;
import org.vividus.zephyr.facade.ZephyrFacadeFactory;
import org.vividus.zephyr.model.ExecutionStatus;
import org.vividus.zephyr.model.TestCase;
import org.vividus.zephyr.model.ZephyrExecution;
import org.vividus.zephyr.parser.TestCaseParser;

public class ZephyrScaleExporter extends ZephyrExporter
{
    public ZephyrScaleExporter(JiraFacade jiraFacade, ZephyrFacadeFactory zephyrFacadeFactory,
                               TestCaseParser testCaseParser, ZephyrExporterProperties zephyrExporterProperties)
    {
        super(jiraFacade, zephyrFacadeFactory, testCaseParser, zephyrExporterProperties);
    }

    @Override
    public void exportTestExecution(TestCase testCase, ZephyrConfiguration configuration)
            throws IOException, JiraConfigurationException
    {
        ZephyrExecution execution = new ZephyrExecution(configuration, testCase.getKey(), testCase.getStatus());
        getZephyrFacade().createExecution(testCase.getKey());
        String executionBody = getObjectMapper().writeValueAsString(new ExecutionStatus(
                    configuration.getTestStatusPerZephyrMapping().get(execution.getTestCaseStatus())));
        getZephyrFacade().updateExecutionStatus(testCase.getKey(), executionBody);
    }
}
