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

package org.vividus.facade.zephyr.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.vividus.facade.zephyr.ZephyrConfiguration;

public class ZephyrExecution
{
    private final ZephyrConfiguration configuration;
    private final String issueId;
    @JsonIgnore
    private final TestCaseStatus testCaseStatus;

    public ZephyrExecution(ZephyrConfiguration configuration, String issueId, TestCaseStatus testCaseStatus)
    {
        this.configuration = configuration;
        this.issueId = issueId;
        this.testCaseStatus = testCaseStatus;
    }

    public String getProjectId()
    {
        return configuration.getProjectId();
    }

    public String getVersionId()
    {
        return configuration.getVersionId();
    }

    public String getCycleId()
    {
        return configuration.getCycleId();
    }

    public String getFolderId()
    {
        return configuration.getFolderId();
    }

    public String getIssueId()
    {
        return issueId;
    }

    public TestCaseStatus getTestCaseStatus()
    {
        return testCaseStatus;
    }
}
