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
    private final transient Optional<String> description;
    private transient Optional<String> status;
    private transient Optional<String> resolution;

    public KnownIssue(String identifier, KnownIssueIdentifier issueIdentifier, boolean potentiallyKnown)
    {
        this.identifier = identifier;
        this.type = issueIdentifier.getType();
        this.failTestCaseFast = issueIdentifier.isFailTestCaseFast();
        this.failTestSuiteFast = issueIdentifier.isFailTestSuiteFast();
        this.potentiallyKnown = potentiallyKnown;
        this.description = Optional.ofNullable(issueIdentifier.getDescription());
        this.status = Optional.empty();
        this.resolution = Optional.empty();
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

    public Optional<String> getStatus()
    {
        return status;
    }

    public void setStatus(Optional<String> status)
    {
        this.status = status;
    }

    public Optional<String> getResolution()
    {
        return resolution;
    }

    public void setResolution(Optional<String> resolution)
    {
        this.resolution = resolution;
    }

    public boolean isClosed()
    {
        return contains(status, FIXED_ISSUE_STATUSES_STRING);
    }

    public boolean isFixed()
    {
        return isClosed() && contains(resolution, FIXED_ISSUE_RESOLUTIONS_STRING);
    }

    private boolean contains(Optional<String> value, String values)
    {
        return value.filter(v -> v.length() > 0).filter(v -> values.contains(v.toLowerCase())).isPresent();
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
        return Objects.hash(identifier, type, potentiallyKnown, failTestCaseFast, failTestSuiteFast);
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
                && type == other.type;
    }
}
