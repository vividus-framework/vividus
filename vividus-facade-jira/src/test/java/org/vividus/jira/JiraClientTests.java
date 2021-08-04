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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;

@ExtendWith(MockitoExtension.class)
class JiraClientTests
{
    private static final String JIRA_URI = "https://jira.com";
    private static final String RESPONSE_BODY_AS_STRING = "{test}";

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
    }

    @Test
    void testExecutePost() throws IOException
    {
        String relativeUrl = "/testPost";
        String body = mockHttpMethodExecution(HttpPost.class, HttpStatus.SC_OK, relativeUrl);
        assertEquals(body, jiraClient.executePost(relativeUrl, "{\"key\":\"value\"}"));
    }

    @Test
    void testExecutePut() throws IOException
    {
        String relativeUrl = "/testPut";
        mockHttpMethodExecution(HttpPut.class, HttpStatus.SC_OK, relativeUrl);
        jiraClient.executePut(relativeUrl, "{\"status\":\"1\"}");
        verify(httpClient).execute(argThat(httpRequest -> httpRequest instanceof HttpPut
                && "https://jira.com/testPut".equals(httpRequest.getURI().toString())));
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
}
