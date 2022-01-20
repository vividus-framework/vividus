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

package org.vividus.jira.model;

import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.vividus.http.client.HttpClientConfig;

public class JiraConfiguration
{
    private Pattern projectKeyRegex;
    private String endpoint;
    @JsonProperty("http")
    private HttpClientConfig httpClientConfig;
    private Map<String, String> fieldsMapping;

    public Pattern getProjectKeyRegex()
    {
        return projectKeyRegex;
    }

    public void setProjectKeyRegex(Pattern projectKeyRegex)
    {
        this.projectKeyRegex = projectKeyRegex;
    }

    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }

    public HttpClientConfig getHttpClientConfig()
    {
        return httpClientConfig;
    }

    public void setHttpClientConfig(HttpClientConfig httpClientConfig)
    {
        this.httpClientConfig = httpClientConfig;
    }

    public Map<String, String> getFieldsMapping()
    {
        return fieldsMapping;
    }

    public void setFieldsMapping(Map<String, String> fieldsMapping)
    {
        this.fieldsMapping = fieldsMapping;
    }
}
