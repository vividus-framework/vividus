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

package org.vividus.facade.jira.client;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.facade.jira.JiraConfiguration;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestBuilder;
import org.vividus.http.client.ClientBuilderUtils;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.http.exception.HttpRequestBuildException;

public class JiraClient implements IJiraClient
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JiraClient.class);

    private IHttpClient httpClient;

    @Override
    public String executeGet(JiraConfiguration jiraConfiguration, String relativeUrl)
    {
        URI uri = URI.create(jiraConfiguration.getEndpoint() + relativeUrl);
        HttpRequestBase request = HttpMethod.GET.createRequest(uri);
        return execute(jiraConfiguration, request).getResponseBodyAsString();
    }

    @Override
    public String executePost(JiraConfiguration jiraConfiguration, String relativeUrl, String requestBody)
    {
        HttpResponse response = execute(jiraConfiguration, HttpMethod.POST, relativeUrl, requestBody);
        return response.getResponseBodyAsString();
    }

    @Override
    public void executePut(JiraConfiguration jiraConfiguration, String relativeUrl, String requestBody)
    {
        execute(jiraConfiguration, HttpMethod.PUT, relativeUrl, requestBody);
    }

    private HttpResponse execute(JiraConfiguration jiraConfiguration, HttpMethod method, String relativeUrl,
                                 String requestBody)
    {
        StringEntity entity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        try
        {
            HttpRequestBuilder httpRequestBuilder = HttpRequestBuilder.create()
                    .withHttpMethod(method)
                    .withEndpoint(jiraConfiguration.getEndpoint().toString())
                    .withRelativeUrl(relativeUrl)
                    .withContent(entity);
            return execute(jiraConfiguration, httpRequestBuilder.build());
        }
        catch (HttpRequestBuildException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    private HttpResponse execute(JiraConfiguration jiraConfiguration, HttpRequestBase httpRequest)
    {
        if (LOGGER.isInfoEnabled())
        {
            LOGGER.atInfo().addArgument(httpRequest::getRequestLine).log("Jira request: {}");
        }

        AuthCache authCache = new BasicAuthCache();
        authCache.put(HttpHost.create(jiraConfiguration.getEndpoint().toString()), new BasicScheme());
        HttpClientContext httpContext = HttpClientContext.create();
        httpContext.setAuthCache(authCache);
        CredentialsProvider credentialsProvider = ClientBuilderUtils
                .createCredentialsProvider(jiraConfiguration.getUsername(), jiraConfiguration.getPassword());
        httpContext.setCredentialsProvider(credentialsProvider);
        HttpResponse httpResponse;
        try
        {
            httpResponse = httpClient.execute(httpRequest, httpContext);
            int status = httpResponse.getStatusCode();
            if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES)
            {
                return httpResponse;
            }
            LOGGER.error("Jira response: {}", httpResponse);
            throw new ClientProtocolException("Unexpected status code: " + status);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    public void setHttpClient(IHttpClient httpClient)
    {
        this.httpClient = httpClient;
    }
}
