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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.facade.jira.model.Issue;
import org.vividus.facade.jira.model.JiraConfiguration;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.util.ResourceUtils;

import uk.org.lidalia.slf4jext.Level;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class JiraClientTests
{
    private static final String ISSUE_KEY = "HSM-1000";
    private static final String ISSUE_PATH = "issue/";
    private static final String JIRA_HOST = "https://www.jirahost.com";
    private static final String JIRA_RESPONSE_JSON = "/jira_response.json";
    private static final String UNEXPECTED_RESPONSE_STATUS = "Unexpected Jira response status: ";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(JiraClient.class);

    private final JiraConfiguration jiraConfiguration = new JiraConfiguration();

    @Mock
    private IHttpClient httpclient;

    @InjectMocks
    private JiraClient jiraClient;

    @BeforeEach
    void beforeEach()
    {
        jiraConfiguration.setPassword("password");
        jiraConfiguration.setUsername("username");
        jiraConfiguration.setEndpoint(JIRA_HOST);
    }

    @Test
    void testCreateIssue() throws IOException
    {
        String issueBody = ResourceUtils.loadResource(this.getClass(), JIRA_RESPONSE_JSON);
        String expectedUrl = mockHttpMethodExecution(HttpPost.class, JIRA_RESPONSE_JSON, ISSUE_PATH);
        logger.setEnabledLevels(Level.INFO);
        Issue actualIssue = jiraClient.createIssue(jiraConfiguration, issueBody, Issue.class);
        assertEquals(actualIssue.getKey(), ISSUE_KEY);
        assertLogging("POST", expectedUrl);
    }

    @Test
    void testNotInfoLogLevel() throws IOException
    {
        String issueBody = ResourceUtils.loadResource(this.getClass(), JIRA_RESPONSE_JSON);
        mockHttpMethodExecution(HttpPost.class, JIRA_RESPONSE_JSON, ISSUE_PATH);
        logger.setEnabledLevels(Level.WARN);
        jiraClient.createIssue(jiraConfiguration, issueBody, Issue.class);
        assertThat(logger.getLoggingEvents(), equalTo(List.of()));
    }

    @ParameterizedTest
    @ValueSource(ints = { HttpStatus.SC_PROCESSING, HttpStatus.SC_MULTIPLE_CHOICES })
    void testExceptionIsThrownWhenUnexpectedStatusCodeIsReturned(int statusCode) throws IOException
    {
        mockHttpMethodExecution(HttpPost.class, ISSUE_PATH, statusCode, null, JIRA_HOST);
        ClientProtocolException exception = assertThrows(ClientProtocolException.class,
            () -> jiraClient.createIssue(jiraConfiguration, StringUtils.EMPTY, Issue.class));
        assertEquals(UNEXPECTED_RESPONSE_STATUS + statusCode, exception.getMessage());
    }

    private String mockHttpMethodExecution(Class<? extends HttpRequestBase> requestClass, String responseResourceName,
            String expectedRelativeUrl) throws IOException
    {
        byte[] responseBody = IOUtils.toByteArray(this.getClass().getResourceAsStream(responseResourceName));
        return mockHttpMethodExecution(requestClass, expectedRelativeUrl, HttpStatus.SC_OK, responseBody, JIRA_HOST);
    }

    private String mockHttpMethodExecution(Class<? extends HttpRequestBase> requestClass, String expectedRelativeUrl,
            int statusCode, byte[] responseBody, String host) throws IOException
    {
        String expectedUrl = jiraConfiguration.getEndpoint() + "rest/api/latest/" + expectedRelativeUrl;
        HttpResponse response = new HttpResponse();
        response.setStatusCode(statusCode);
        response.setResponseBody(responseBody);
        when(httpclient.execute(argThat(httpRequest -> requestClass.isInstance(httpRequest)
                && expectedUrl.equals(httpRequest.getURI().toString())), argThat(httpContext ->
                {
                    Object authCacheObj = httpContext.getAttribute(HttpClientContext.AUTH_CACHE);
                    if (authCacheObj != null)
                    {
                        AuthCache authCache = (AuthCache) authCacheObj;
                        return authCache.get(HttpHost.create(host)) instanceof BasicScheme;
                    }
                    return false;
                }))).thenReturn(response);
        return expectedUrl;
    }

    private void assertLogging(String httpMethod, String expectedUrl)
    {
        List<LoggingEvent> loggingEvents = logger.getLoggingEvents();
        assertThat(loggingEvents.size(), equalTo(1));
        LoggingEvent loggingEvent = loggingEvents.get(0);
        assertThat(loggingEvent.getLevel(), equalTo(Level.INFO));
        assertThat(loggingEvent.getMessage(), equalTo("Jira request: {}"));
        ImmutableList<Object> arguments = loggingEvent.getArguments();
        assertThat(arguments.size(), equalTo(1));
        assertThat(arguments.get(0).toString(), equalTo(httpMethod + " " + expectedUrl + " HTTP/1.1"));
    }
}
