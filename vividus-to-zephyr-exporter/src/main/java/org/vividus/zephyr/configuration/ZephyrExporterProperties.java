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

package org.vividus.zephyr.configuration;

import java.nio.file.Path;
import java.util.List;

import javax.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.vividus.zephyr.model.TestCaseEntity;
import org.vividus.zephyr.model.TestCaseStatus;

@ConfigurationProperties("zephyr-exporter")
public class ZephyrExporterProperties
{
    private String jiraInstanceKey;

    private boolean exportResults;

    @NotBlank(message = "Property 'zephyr-exporter.source-directory' must not be blank")
    private Path sourceDirectory;

    private boolean updateExecutionStatusesOnly;

    private List<TestCaseStatus> statusesOfTestCasesToAddToExecution;

    private TestCaseEntity entity;

    private boolean updateCasesOnExport;

    private String statusForUpdatedTestCases;

    public String getJiraInstanceKey()
    {
        return jiraInstanceKey;
    }

    public void setJiraInstanceKey(String jiraInstanceKey)
    {
        this.jiraInstanceKey = jiraInstanceKey;
    }

    public boolean getExportResults()
    {
        return exportResults;
    }

    public void setExportResults(boolean exportResults)
    {
        this.exportResults = exportResults;
    }

    public Path getSourceDirectory()
    {
        return sourceDirectory;
    }

    public void setSourceDirectory(Path sourceDirectory)
    {
        this.sourceDirectory = sourceDirectory;
    }

    public boolean getUpdateExecutionStatusesOnly()
    {
        return updateExecutionStatusesOnly;
    }

    public void setUpdateExecutionStatusesOnly(boolean updateExecutionStatusesOnly)
    {
        this.updateExecutionStatusesOnly = updateExecutionStatusesOnly;
    }

    public List<TestCaseStatus> getStatusesOfTestCasesToAddToExecution()
    {
        return statusesOfTestCasesToAddToExecution;
    }

    public void setStatusesOfTestCasesToAddToExecution(List<TestCaseStatus> statusesOfTestCasesToAddToExecution)
    {
        this.statusesOfTestCasesToAddToExecution = statusesOfTestCasesToAddToExecution;
    }

    public TestCaseEntity getEntity()
    {
        return entity;
    }

    public void setEntity(TestCaseEntity entity)
    {
        this.entity = entity;
    }

    public boolean isUpdateCasesOnExport()
    {
        return updateCasesOnExport;
    }

    public void setUpdateCasesOnExport(boolean updateCasesOnExport)
    {
        this.updateCasesOnExport = updateCasesOnExport;
    }

    public String getStatusForUpdatedTestCases()
    {
        return statusForUpdatedTestCases;
    }

    public void setStatusForUpdatedTestCases(String statusForUpdatedTestCases)
    {
        this.statusForUpdatedTestCases = statusForUpdatedTestCases;
    }
}
