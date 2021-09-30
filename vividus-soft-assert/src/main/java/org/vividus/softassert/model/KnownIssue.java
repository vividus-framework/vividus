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

package org.vividus.softassert.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import org.vividus.softassert.issue.KnownIssueIdentifier;
import org.vividus.softassert.issue.KnownIssueType;

public class KnownIssue implements Serializable
{
    private static final long serialVersionUID = 5419047883822146063L;

    private static final String FIXED_ISSUE_STATUSES_STRING = "closed";
    private static final String FIXED_ISSUE_RESOLUTIONS_STRING = "fixed|done";

    private final String identifier;
    private final KnownIssueType type;
    private final boolean potentiallyKnown;
    private final boolean failTestCaseFast;
    private final boolean failTestSuiteFast;
    private final Optional<String> description;
    private String status;
    private String resolution;

    public KnownIssue(String identifier, KnownIssueIdentifier issueIdentifier, boolean potentiallyKnown)
    {
        this.identifier = identifier;
        this.type = issueIdentifier.getType();
        this.failTestCaseFast = issueIdentifier.isFailTestCaseFast();
        this.failTestSuiteFast = issueIdentifier.isFailTestSuiteFast();
        this.potentiallyKnown = potentiallyKnown;
        this.description = Optional.ofNullable(issueIdentifier.getDescription());
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public KnownIssueType getType()
    {
        return type;
    }

    public boolean isPotentiallyKnown()
    {
        return potentiallyKnown;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getResolution()
    {
        return resolution;
    }

    public void setResolution(String resolution)
    {
        this.resolution = resolution;
    }

    public boolean isClosed()
    {
        return status != null && status.length() > 0 && FIXED_ISSUE_STATUSES_STRING.contains(status.toLowerCase());
    }

    public boolean isFixed()
    {
        return isClosed() && resolution != null && resolution.length() > 0
                && FIXED_ISSUE_RESOLUTIONS_STRING.contains(resolution.toLowerCase());
    }

    public boolean isFailTestCaseFast()
    {
        return failTestCaseFast;
    }

    public boolean isFailTestSuiteFast()
    {
        return failTestSuiteFast;
    }

    public Optional<String> getDescription()
    {
        return description;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(identifier, type, potentiallyKnown, failTestCaseFast, failTestSuiteFast, status,
                description);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        KnownIssue other = (KnownIssue) obj;
        return Objects.equals(identifier, other.identifier)
                && potentiallyKnown == other.potentiallyKnown
                && failTestCaseFast == other.failTestCaseFast
                && failTestSuiteFast == other.failTestSuiteFast
                && Objects.equals(description, other.description)
                && Objects.equals(status, other.status) && type == other.type;
    }
}
