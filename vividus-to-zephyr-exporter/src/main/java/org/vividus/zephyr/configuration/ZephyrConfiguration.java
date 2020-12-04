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

package org.vividus.zephyr.configuration;

import java.util.Map;

import org.vividus.zephyr.model.TestCaseStatus;

public class ZephyrConfiguration
{
    private String projectId;
    private String versionId;
    private String cycleId;
    private String folderId;
    private Map<TestCaseStatus, Integer> testStatusPerZephyrIdMapping;

    public void setProjectId(String projectId)
    {
        this.projectId = projectId;
    }

    public void setVersionId(String versionId)
    {
        this.versionId = versionId;
    }

    public void setCycleId(String cycleId)
    {
        this.cycleId = cycleId;
    }

    public void setFolderId(String folderId)
    {
        this.folderId = folderId;
    }

    public void setTestStatusPerZephyrIdMapping(Map<TestCaseStatus, Integer> statusIdMap)
    {
        this.testStatusPerZephyrIdMapping = statusIdMap;
    }

    public String getProjectId()
    {
        return projectId;
    }

    public String getVersionId()
    {
        return versionId;
    }

    public String getCycleId()
    {
        return cycleId;
    }

    public String getFolderId()
    {
        return folderId;
    }

    public Map<TestCaseStatus, Integer> getTestStatusPerZephyrIdMapping()
    {
        return testStatusPerZephyrIdMapping;
    }
}
