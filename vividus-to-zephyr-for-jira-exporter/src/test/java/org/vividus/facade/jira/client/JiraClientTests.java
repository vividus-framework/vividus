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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.facade.jira.JiraConfiguration;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class})
class JiraClientTests
{
    private static final URI JIRA_URI = URI.create("https://jira.com");
    private static final String RESPONSE_BODY_AS_STRING = "{test}";
    private static final String GET = "GET";

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(JiraClient.class);
    private final JiraConfiguration jiraConfiguration = new JiraConfiguration();

    @Mock
    private IHttpClient httpClient;

    @InjectMocks
    private JiraClient jiraClient;

    @BeforeEach
    void beforeEach()
    {
        jiraConfiguration.setPassword("password");
        jiraConfiguration.setUsername("username");
        jiraConfiguration.setEndpoint(JIRA_URI);
    }

    @Test
    void testExecuteGet() throws IOException
    {
        String relativeUrl = "/testGet";
        assertEquals(mockHttpMethodExecution(HttpGet.class, HttpStatus.SC_OK, relativeUrl),
                jiraClient.executeGet(jiraConfiguration, relativeUrl));
        checkLogger(GET, relativeUrl);
    }

    @ParameterizedTest
    @ValueSource(ints = { HttpStatus.SC_PROCESSING, HttpStatus.SC_MULTIPLE_CHOICES })
    void testExecuteGetThrownUnexpectedStatusCode(int statusCode) throws IOException
    {
        String relativeUrl = "/testGetUnexpectedStatusCode";
        mockHttpMethodExecution(HttpGet.class, statusCode, relativeUrl);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> jiraClient.executeGet(jiraConfiguration, relativeUrl));
        assertEquals("org.apache.http.client.ClientProtocolException: Unexpected status code: " + statusCode,
                exception.getMessage());
        assertEquals(2, testLogger.getLoggingEvents().size());
        assertEquals(String.format("Jira request: GET https://jira.com%s HTTP/1.1", relativeUrl),
                testLogger.getLoggingEvents().get(0).getFormattedMessage());
        assertEquals("Jira response: " + statusCode + " : {test}",
                testLogger.getLoggingEvents().get(1).getFormattedMessage());
    }

    @Test
    void testExecutePost() throws IOException
    {
        String relativeUrl = "/testPost";
        assertEquals(mockHttpMethodExecution(HttpPost.class, HttpStatus.SC_OK, relativeUrl),
                jiraClient.executePost(jiraConfiguration, relativeUrl, "{\"key\":\"value\"}"));
        checkLogger("POST", relativeUrl);
    }

    @Test
    void testExecutePut() throws IOException
    {
        String relativeUrl = "/testPut";
        mockHttpMethodExecution(HttpPut.class, HttpStatus.SC_OK, relativeUrl);
        jiraClient.executePut(jiraConfiguration, relativeUrl, "{\"status\":\"1\"}");
        verify(httpClient).execute(argThat(httpRequest -> httpRequest instanceof HttpPut
                && "https://jira.com/testPut".equals(httpRequest.getURI().toString())),
                argThat(httpContext -> httpContext instanceof HttpClientContext));
        checkLogger("PUT", relativeUrl);
    }

    private String mockHttpMethodExecution(Class<? extends HttpRequestBase> requestClass, int statusCode,
                                           String relativeUrl) throws IOException
    {
        HttpResponse response = new HttpResponse();
        response.setStatusCode(statusCode);
        response.setResponseBody(RESPONSE_BODY_AS_STRING.getBytes(StandardCharsets.UTF_8));
        String expectedUrl = jiraConfiguration.getEndpoint() + relativeUrl;
        when(httpClient.execute(argThat(httpRequest -> requestClass.isInstance(httpRequest)
                && expectedUrl.equals(httpRequest.getURI().toString())), argThat(httpContext ->
                {
                    Object authCacheObj = httpContext.getAttribute(HttpClientContext.AUTH_CACHE);
                    return authCacheObj != null;
                }))).thenReturn(response);
        return response.getResponseBodyAsString();
    }

    private void checkLogger(String method, String relativeUrl)
    {
        assertEquals(1, testLogger.getLoggingEvents().size());
        assertEquals("Jira request: " + method + " https://jira.com" + relativeUrl + " HTTP/1.1",
                testLogger.getLoggingEvents().get(0).getFormattedMessage());
    }
}
