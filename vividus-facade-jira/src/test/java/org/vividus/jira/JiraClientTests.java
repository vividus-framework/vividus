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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class})
class JiraClientTests
{
    private static final String JIRA_URI = "https://jira.com";
    private static final String RESPONSE_BODY_AS_STRING = "{test}";
    private static final String GET = "GET";

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(JiraClient.class);

    @Mock private IHttpClient httpClient;

    private JiraClient jiraClient;

    @BeforeEach
    void init()
    {
        jiraClient = new JiraClient(JIRA_URI, httpClient);
    }

    @Test
    void testExecuteGet() throws IOException
    {
        String relativeUrl = "/testGet";
        String body = mockHttpMethodExecution(HttpGet.class, HttpStatus.SC_OK, relativeUrl);
        assertEquals(body, jiraClient.executeGet(relativeUrl));
        checkLogger(GET, relativeUrl);
    }

    @ParameterizedTest
    @ValueSource(ints = { HttpStatus.SC_PROCESSING, HttpStatus.SC_MULTIPLE_CHOICES })
    void testExecuteGetThrownUnexpectedStatusCode(int statusCode) throws IOException
    {
        String relativeUrl = "/testGetUnexpectedStatusCode";
        mockHttpMethodExecution(HttpGet.class, statusCode, relativeUrl);
        IOException exception = assertThrows(IOException.class, () -> jiraClient.executeGet(relativeUrl));
        assertEquals("Unexpected status code: " + statusCode,
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
        String body = mockHttpMethodExecution(HttpPost.class, HttpStatus.SC_OK, relativeUrl);
        assertEquals(body, jiraClient.executePost(relativeUrl, "{\"key\":\"value\"}"));
        checkLogger("POST", relativeUrl);
    }

    @Test
    void testExecutePut() throws IOException
    {
        String relativeUrl = "/testPut";
        mockHttpMethodExecution(HttpPut.class, HttpStatus.SC_OK, relativeUrl);
        jiraClient.executePut(relativeUrl, "{\"status\":\"1\"}");
        verify(httpClient).execute(argThat(httpRequest -> httpRequest instanceof HttpPut
                && "https://jira.com/testPut".equals(httpRequest.getURI().toString())));
        checkLogger("PUT", relativeUrl);
    }

    private String mockHttpMethodExecution(Class<? extends HttpRequestBase> requestClass, int statusCode,
            String relativeUrl) throws IOException
    {
        HttpResponse response = new HttpResponse();
        response.setStatusCode(statusCode);
        response.setResponseBody(RESPONSE_BODY_AS_STRING.getBytes(StandardCharsets.UTF_8));
        String expectedUrl = JIRA_URI + relativeUrl;
        when(httpClient.execute(argThat(httpRequest -> requestClass.isInstance(httpRequest)
                && expectedUrl.equals(httpRequest.getURI().toString()))))
                        .thenReturn(response);
        return response.getResponseBodyAsString();
    }

    private void checkLogger(String method, String relativeUrl)
    {
        assertEquals(1, testLogger.getLoggingEvents().size());
        assertEquals("Jira request: " + method + " https://jira.com" + relativeUrl + " HTTP/1.1",
                testLogger.getLoggingEvents().get(0).getFormattedMessage());
    }
}
