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
import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.facade.jira.databind.IssueDeserializer;
import org.vividus.facade.jira.model.Issue;
import org.vividus.facade.jira.model.JiraConfiguration;
import org.vividus.http.HttpMethod;
import org.vividus.http.client.ClientBuilderUtils;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.util.UriUtils;

public class JiraClient implements IJiraClient
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JiraClient.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new SimpleModule().addDeserializer(Issue.class, new IssueDeserializer()));

    private static final String ISSUE_ENDPOINT = "rest/api/latest/issue/";

    private IHttpClient httpClient;

    @Override
    public <T> T createIssue(JiraConfiguration jiraConfiguration, String issueBody, Class<T> resultType)
            throws IOException
    {
        return executePost(jiraConfiguration, ISSUE_ENDPOINT, issueBody, resultType);
    }

    @Override
    public <T> T executePost(JiraConfiguration jiraConfiguration, String relativeUrl, String requestBody,
            Class<T> resultType) throws IOException
    {
        HttpResponse httpResponse = execute(jiraConfiguration, HttpMethod.POST, relativeUrl, requestBody);
        return OBJECT_MAPPER.readValue(httpResponse.getResponseBody(), resultType);
    }

    private HttpResponse execute(JiraConfiguration jiraConfiguration, HttpMethod method, String relativeUrl,
            String requestBody) throws IOException
    {
        URI uri = URI.create(jiraConfiguration.getEndpoint() + relativeUrl);
        StringEntity entity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        HttpEntityEnclosingRequestBase request = method.createEntityEnclosingRequest(uri, entity);
        return execute(jiraConfiguration, request);
    }

    private HttpResponse execute(JiraConfiguration jiraConfiguration, HttpRequestBase httpRequest) throws IOException
    {
        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info("Jira request: {}", httpRequest.getRequestLine());
        }

        AuthCache authCache = new BasicAuthCache();
        authCache.put(HttpHost.create(UriUtils.buildNewUrl(jiraConfiguration.getEndpoint(), "").toString()),
                new BasicScheme());
        HttpClientContext httpContext = HttpClientContext.create();
        httpContext.setAuthCache(authCache);
        CredentialsProvider credentialsProvider = ClientBuilderUtils
                .createCredentialsProvider(jiraConfiguration.getUsername(), jiraConfiguration.getPassword());
        httpContext.setCredentialsProvider(credentialsProvider);
        HttpResponse httpResponse = httpClient.execute(httpRequest, httpContext);
        int status = httpResponse.getStatusCode();
        if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES)
        {
            return httpResponse;
        }
        LOGGER.error("Jira response: {}", httpResponse);
        throw new ClientProtocolException("Unexpected Jira response status: " + status);
    }

    public void setHttpClient(IHttpClient httpClient)
    {
        this.httpClient = httpClient;
    }
}
