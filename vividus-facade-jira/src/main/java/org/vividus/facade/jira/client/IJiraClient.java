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

package org.vividus.facade.jira.client;

import java.io.IOException;

import org.vividus.facade.jira.model.JiraConfiguration;

public interface IJiraClient
{
    <T> T createIssue(JiraConfiguration jiraConfiguration, String issueBody, Class<T> resultType) throws IOException;

    <T> T executePost(JiraConfiguration jiraConfiguration, String relativeUrl, String requestBody, Class<T> resultType)
            throws IOException;
}
