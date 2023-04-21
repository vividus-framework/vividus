/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.jira;

import java.io.IOException;
import java.util.List;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestBuilder;
import org.vividus.http.client.IHttpClient;

public class JiraClient
{
    private final String endpoint;
    private final IHttpClient httpClient;

    public JiraClient(String endpoint, IHttpClient httpClient)
    {
        this.endpoint = endpoint;
        this.httpClient = httpClient;
    }

    public String executeGet(String relativeUrl) throws IOException
    {
        return execute(HttpMethod.GET, relativeUrl, List.of(), (HttpEntity) null);
    }

    public String executePost(String relativeUrl, String requestBody) throws IOException
    {
        return execute(HttpMethod.POST, relativeUrl, requestBody);
    }

    public String executePost(String relativeUrl, List<Header> headers, HttpEntity content) throws IOException
    {
        return execute(HttpMethod.POST, relativeUrl, headers, content);
    }

    public String executePut(String relativeUrl, String requestBody) throws IOException
    {
        return execute(HttpMethod.PUT, relativeUrl, requestBody);
    }

    private String execute(HttpMethod method, String relativeUrl, String requestBody) throws IOException
    {
        return execute(method, relativeUrl, List.of(), new StringEntity(requestBody, ContentType.APPLICATION_JSON));
    }

    private String execute(HttpMethod method, String relativeUrl, List<Header> headers, HttpEntity content)
            throws IOException
    {
        ClassicHttpRequest httpRequest = HttpRequestBuilder.create()
                .withHttpMethod(method)
                .withEndpoint(endpoint)
                .withRelativeUrl(relativeUrl)
                .withHeaders(headers)
                .withContent(content)
                .build();

        return httpClient.execute(httpRequest).getResponseBodyAsString();
    }
}
