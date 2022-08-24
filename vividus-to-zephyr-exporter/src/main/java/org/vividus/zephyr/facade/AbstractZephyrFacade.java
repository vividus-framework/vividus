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
import java.util.Map;

import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.Project;
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterConfiguration;
import org.vividus.zephyr.model.TestCaseStatus;

public abstract class AbstractZephyrFacade implements IZephyrFacade
{
    private final JiraFacade jiraFacade;
    private final ZephyrExporterConfiguration zephyrExporterConfiguration;

    public AbstractZephyrFacade(JiraFacade jiraFacade, ZephyrExporterConfiguration zephyrExporterConfiguration)
    {
        this.jiraFacade = jiraFacade;
        this.zephyrExporterConfiguration = zephyrExporterConfiguration;
    }

    public ZephyrConfiguration prepareBaseConfiguration(ZephyrConfiguration zephyrConfiguration,
                                                        String cycleId,
                                                        Map<TestCaseStatus, String> statusMap)
            throws IOException, JiraConfigurationException
    {
        Project project = jiraFacade.getProject(zephyrExporterConfiguration.getProjectKey());
        String projectId = project.getId();
        zephyrConfiguration.setProjectId(projectId);
        zephyrConfiguration.setCycleId(cycleId);
        zephyrConfiguration.setTestStatusPerZephyrStatusMapping(statusMap);

        return zephyrConfiguration;
    }

    public JiraFacade getJiraFacade()
    {
        return jiraFacade;
    }

    public ZephyrExporterConfiguration getZephyrExporterConfiguration()
    {
        return zephyrExporterConfiguration;
    }
}
