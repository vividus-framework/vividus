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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
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
        String body = mockHttpMethodExecution(HttpGet.class, relativeUrl, List.of(), e -> e == null);
        assertEquals(body, jiraClient.executeGet(relativeUrl));
    }

    @Test
    void testExecutePost() throws IOException
    {
        String relativeUrl = "/testPost";
        String body = mockHttpMethodExecution(HttpPost.class, relativeUrl);
        assertEquals(body, jiraClient.executePost(relativeUrl, "{\"key\":\"value\"}"));
    }

    @Test
    void testExecutePostWithEntity() throws IOException
    {
        HttpEntity entity = mock(HttpEntity.class);
        Header header = mock(Header.class);
        String relativeUrl = "/testPostWithEntity";
        String body = mockHttpMethodExecution(HttpPost.class, relativeUrl, List.of(header), e -> e.equals(entity));
        assertEquals(body, jiraClient.executePost(relativeUrl, List.of(header), entity));
    }

    @Test
    void testExecutePut() throws IOException
    {
        String relativeUrl = "/testPut";
        String body = mockHttpMethodExecution(HttpPut.class, relativeUrl);
        assertEquals(body, jiraClient.executePut(relativeUrl, "{\"status\":\"1\"}"));
    }

    private String mockHttpMethodExecution(Class<? extends ClassicHttpRequest> requestClass, String relativeUrl)
            throws IOException
    {
        return mockHttpMethodExecution(requestClass, relativeUrl, List.of(), e -> e instanceof StringEntity);
    }

    private String mockHttpMethodExecution(Class<? extends ClassicHttpRequest> requestClass, String relativeUrl,
            List<Header> headers, Predicate<HttpEntity> entityTest) throws IOException
    {
        HttpResponse response = new HttpResponse();
        response.setStatusCode(HttpStatus.SC_OK);
        response.setResponseBody(RESPONSE_BODY_AS_STRING.getBytes(StandardCharsets.UTF_8));
        URI expectedUrl = URI.create(JIRA_URI + relativeUrl);
        when(httpClient.execute(argThat(httpRequest -> {
            try
            {
                return requestClass.isInstance(httpRequest) && expectedUrl.equals(httpRequest.getUri())
                        && entityTest.test(httpRequest.getEntity())
                        && Arrays.asList(httpRequest.getHeaders()).equals(headers);
            }
            catch (URISyntaxException e)
            {
                throw new IllegalArgumentException(e);
            }
        }))).thenReturn(response);
        return response.getResponseBodyAsString();
    }
}
