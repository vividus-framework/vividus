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
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatcher;
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
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(JiraClient.class);

    @Mock private IHttpClient httpClient;

    private JiraClient jiraClient;

    @Test
    void testExecuteGet() throws IOException
    {
        init(null, null);
        String relativeUrl = "/testGet";
        String body = mockHttpMethodExecution(HttpGet.class, HttpStatus.SC_OK, relativeUrl, ctx -> ctx == null);
        assertEquals(body, jiraClient.executeGet(relativeUrl));
        checkLogger(GET, relativeUrl);
    }

    @ParameterizedTest
    @ValueSource(ints = { HttpStatus.SC_PROCESSING, HttpStatus.SC_MULTIPLE_CHOICES })
    void testExecuteGetThrownUnexpectedStatusCode(int statusCode) throws IOException
    {
        init(USERNAME, PASSWORD);
        String relativeUrl = "/testGetUnexpectedStatusCode";
        mockHttpMethodExecution(HttpGet.class, statusCode, relativeUrl, contextMatcher());
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
        init(USERNAME, PASSWORD);
        String relativeUrl = "/testPost";
        String body = mockHttpMethodExecution(HttpPost.class, HttpStatus.SC_OK, relativeUrl, contextMatcher());
        assertEquals(body, jiraClient.executePost(relativeUrl, "{\"key\":\"value\"}"));
        checkLogger("POST", relativeUrl);
    }

    @Test
    void testExecutePut() throws IOException
    {
        init(USERNAME, PASSWORD);
        String relativeUrl = "/testPut";
        mockHttpMethodExecution(HttpPut.class, HttpStatus.SC_OK, relativeUrl, contextMatcher());
        jiraClient.executePut(relativeUrl, "{\"status\":\"1\"}");
        verify(httpClient).execute(argThat(httpRequest -> httpRequest instanceof HttpPut
                && "https://jira.com/testPut".equals(httpRequest.getURI().toString())),
                argThat(contextMatcher()));
        checkLogger("PUT", relativeUrl);
    }

    @ParameterizedTest
    @CsvSource({
        "username,,The JIRA password is missing",
        ",password,The JIRA username is missing"
    })
    void testExecuteRequestWithWrongSettings(String username, String password, String message) throws IOException
    {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> new JiraClient(JIRA_URI, username, password, httpClient));
        assertEquals(message, thrown.getMessage());
    }

    private String mockHttpMethodExecution(Class<? extends HttpRequestBase> requestClass, int statusCode,
            String relativeUrl, ArgumentMatcher<HttpContext> contextMatcher) throws IOException
    {
        HttpResponse response = new HttpResponse();
        response.setStatusCode(statusCode);
        response.setResponseBody(RESPONSE_BODY_AS_STRING.getBytes(StandardCharsets.UTF_8));
        String expectedUrl = JIRA_URI + relativeUrl;
        when(httpClient.execute(argThat(httpRequest -> requestClass.isInstance(httpRequest)
                && expectedUrl.equals(httpRequest.getURI().toString())), argThat(contextMatcher)))
                        .thenReturn(response);
        return response.getResponseBodyAsString();
    }

    private ArgumentMatcher<HttpContext> contextMatcher()
    {
        return context ->
        {
            CredentialsProvider provider = (CredentialsProvider) context.getAttribute(HttpClientContext.CREDS_PROVIDER);
            Credentials credentials = provider.getCredentials(AuthScope.ANY);
            return USERNAME.equals(credentials.getUserPrincipal().getName())
                    && PASSWORD.equals(credentials.getPassword());
        };
    }

    private void checkLogger(String method, String relativeUrl)
    {
        assertEquals(1, testLogger.getLoggingEvents().size());
        assertEquals("Jira request: " + method + " https://jira.com" + relativeUrl + " HTTP/1.1",
                testLogger.getLoggingEvents().get(0).getFormattedMessage());
    }

    private void init(String username, String password)
    {
        jiraClient = new JiraClient(JIRA_URI, username, password, httpClient);
    }
}
