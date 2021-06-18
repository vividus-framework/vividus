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

package org.vividus.jira.connector;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.JiraIssue;
import org.vividus.softassert.issue.IIssueStateProvider;

public final class JiraIssueStateProvider implements IIssueStateProvider
{
    private final List<String> statuses;
    private final List<String> resolutions;
    private final JiraFacadeProvider jiraFacadeProvider;

    public JiraIssueStateProvider(List<String> statuses, List<String> resolutions,
            JiraFacadeProvider jiraFacadeProvider)
    {
        this.statuses = statuses;
        this.resolutions = resolutions;
        this.jiraFacadeProvider = jiraFacadeProvider;
    }

    @Override
    public IssueState getIssueState(String btsKey, String issueKey)
    {
        try
        {
            JiraFacade jiraFacade = btsKey != null ? jiraFacadeProvider.findByBtsKey(btsKey)
                    : jiraFacadeProvider.findByJiraCode(issueKey);
            JiraIssue jiraIssue = jiraFacade.getIssue(issueKey);
            Map<String, String> details = Map.of(
                "Status", jiraIssue.getStatus(),
                "Resolution", jiraIssue.getResolution()
            );
            return new IssueState(isFixed(issueKey), details);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private boolean isFixed(String resolution)
    {
        return isClosed(resolution) && resolution != null && resolution.length() > 0
                && resolutions.contains(resolution.toLowerCase());
    }

    private boolean isClosed(String status)
    {
        return status != null && status.length() > 0 && statuses.contains(status.toLowerCase());
    }
}
