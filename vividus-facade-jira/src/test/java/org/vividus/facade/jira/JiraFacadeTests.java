/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.facade.jira;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.facade.jira.client.IJiraClient;
import org.vividus.facade.jira.model.Issue;
import org.vividus.facade.jira.model.JiraConfiguration;

@ExtendWith(MockitoExtension.class)
class JiraFacadeTests
{
    private static final String PROJECT_KEY = "HSM";
    private static final String ISSUE_BODY = "issueBody";

    @Mock
    private IJiraClient client;

    @Mock
    private IJiraConfigurationProvider jiraConfigurationProvider;

    @InjectMocks
    private JiraFacade jiraFacade;

    @Test
    void testCreateIssue() throws IOException
    {
        Issue issue = new Issue();
        JiraConfiguration jiraConfiguration = new JiraConfiguration();
        when(jiraConfigurationProvider.getConfigurationByProjectKey(PROJECT_KEY)).thenReturn(jiraConfiguration);
        when(client.createIssue(jiraConfiguration, ISSUE_BODY, Issue.class)).thenReturn(issue);
        Issue actualIssue = jiraFacade.createIssue(PROJECT_KEY, ISSUE_BODY);
        assertEquals(issue, actualIssue);
    }
}
