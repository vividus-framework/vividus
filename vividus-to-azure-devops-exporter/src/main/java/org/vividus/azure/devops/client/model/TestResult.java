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

package org.vividus.azure.devops.client.model;

import java.time.OffsetDateTime;

public class TestResult
{
    private OffsetDateTime startedDate;
    private OffsetDateTime completedDate;
    private String outcome;
    private String state;
    private String testCaseTitle;
    private int revision;
    private ShallowReference testPoint;

    public OffsetDateTime getStartedDate()
    {
        return startedDate;
    }

    public void setStartedDate(OffsetDateTime startedDate)
    {
        this.startedDate = startedDate;
    }

    public OffsetDateTime getCompletedDate()
    {
        return completedDate;
    }

    public void setCompletedDate(OffsetDateTime completedDate)
    {
        this.completedDate = completedDate;
    }

    public String getOutcome()
    {
        return outcome;
    }

    public void setOutcome(String outcome)
    {
        this.outcome = outcome;
    }

    public String getState()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public String getTestCaseTitle()
    {
        return testCaseTitle;
    }

    public void setTestCaseTitle(String testCaseTitle)
    {
        this.testCaseTitle = testCaseTitle;
    }

    public int getRevision()
    {
        return revision;
    }

    public void setRevision(int revision)
    {
        this.revision = revision;
    }

    public ShallowReference getTestPoint()
    {
        return testPoint;
    }

    public void setTestPoint(ShallowReference testPoint)
    {
        this.testPoint = testPoint;
    }
}
