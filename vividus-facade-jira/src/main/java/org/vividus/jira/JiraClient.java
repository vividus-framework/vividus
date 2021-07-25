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

package org.vividus.jira;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestBuilder;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;

public class JiraClient
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JiraClient.class);

    private final String endpoint;
    private final IHttpClient httpClient;

    public JiraClient(String endpoint, IHttpClient httpClient)
    {
        this.endpoint = endpoint;
        this.httpClient = httpClient;
    }

    public String executeGet(String relativeUrl) throws IOException
    {
        return execute(HttpMethod.GET, relativeUrl, (HttpEntity) null);
    }

    public String executePost(String relativeUrl, String requestBody) throws IOException
    {
        return execute(HttpMethod.POST, relativeUrl, requestBody);
    }

    public String executePut(String relativeUrl, String requestBody) throws IOException
    {
        return execute(HttpMethod.PUT, relativeUrl, requestBody);
    }

    private String execute(HttpMethod method, String relativeUrl, String requestBody) throws IOException
    {
        return execute(method, relativeUrl, new StringEntity(requestBody, ContentType.APPLICATION_JSON));
    }

    private String execute(HttpMethod method, String relativeUrl, HttpEntity content) throws IOException
    {
        HttpRequestBase httpRequest = HttpRequestBuilder.create()
                .withHttpMethod(method)
                .withEndpoint(endpoint)
                .withRelativeUrl(relativeUrl)
                .withContent(content)
                .build();

        LOGGER.atInfo().addArgument(httpRequest::getRequestLine).log("Jira request: {}");

        HttpResponse httpResponse = httpClient.execute(httpRequest);
        int status = httpResponse.getStatusCode();
        if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES)
        {
            return httpResponse.getResponseBodyAsString();
        }
        LOGGER.atError().addArgument(httpResponse).log("Jira response: {}");
        throw new IOException("Unexpected status code: " + status);
    }
}
