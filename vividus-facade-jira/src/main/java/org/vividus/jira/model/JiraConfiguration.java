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

package org.vividus.jira.model;

import java.util.regex.Pattern;

import org.vividus.http.client.HttpClientConfig;

public class JiraConfiguration
{
    private Pattern projectCodePattern;
    private String endpoint;
    private String username;
    private String password;
    private HttpClientConfig http;

    public Pattern getProjectCodePattern()
    {
        return projectCodePattern;
    }

    public void setProjectCodePattern(Pattern projectCodePattern)
    {
        this.projectCodePattern = projectCodePattern;
    }

    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public HttpClientConfig getHttp()
    {
        return http;
    }

    public void setHttp(HttpClientConfig http)
    {
        this.http = http;
    }
}
