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

package org.vividus.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.vividus.http.ConnectionDetails;
import org.vividus.http.HttpTestContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.testcontext.SimpleTestContext;

class HttpTestContextTests
{
    private static final String JSON = "{\"name\":\"value\"}";

    private final HttpTestContext httpTestContext = new HttpTestContext(new SimpleTestContext());

    @Test
    void testGetDefaultRequest()
    {
        assertEquals(Optional.empty(), httpTestContext.pullRequestEntity());
    }

    @Test
    void testPutAndGetRequest()
    {
        HttpEntity requestEntity = new StringEntity("some request", (ContentType) null);
        httpTestContext.putRequestEntity(requestEntity);
        assertEquals(Optional.of(requestEntity), httpTestContext.pullRequestEntity());
        assertEquals(Optional.empty(), httpTestContext.pullRequestEntity(), "Request is cleared");
    }

    @Test
    void testGetDefaultConnectionDetails()
    {
        assertNull(httpTestContext.getConnectionDetails());
    }

    @Test
    void testPutAndGetConnectionDetails()
    {
        ConnectionDetails connectionDetails = new ConnectionDetails();
        httpTestContext.putConnectionDetails(connectionDetails);
        assertEquals(connectionDetails, httpTestContext.getConnectionDetails());
    }

    @Test
    void testGetDefaultResponse()
    {
        assertNull(httpTestContext.getResponse());
    }

    @Test
    void testGetDefaultJsonContext()
    {
        assertNull(httpTestContext.getJsonContext());
    }

    @Test
    void testPutAndGetResponse()
    {
        HttpResponse response = new HttpResponse();
        httpTestContext.putResponse(response);
        assertEquals(response, httpTestContext.getResponse());
    }

    @Test
    void testPullDefaultRequestHeaders()
    {
        assertThat(httpTestContext.pullRequestHeaders(), empty());
    }

    @Test
    void testPutRequestHeaders()
    {
        List<Header> headers = List.of(mock(Header.class));
        httpTestContext.putRequestHeaders(headers);
        assertEquals(headers, httpTestContext.pullRequestHeaders());
        assertThat(httpTestContext.pullRequestHeaders(), empty());
    }

    @Test
    void testPutJsonContext()
    {
        HttpResponse response = new HttpResponse();
        httpTestContext.putResponse(response);
        httpTestContext.putJsonContext(JSON);
        assertEquals(JSON, httpTestContext.getJsonContext());
    }

    @Test
    void testJsonContextResetAfterPuttingResponse()
    {
        httpTestContext.putJsonContext(JSON);
        String responseBody = "{\"response\":\"data\"}";
        HttpResponse response = new HttpResponse();
        response.setResponseBody(responseBody.getBytes(StandardCharsets.UTF_8));
        httpTestContext.putResponse(response);
        assertEquals(responseBody, httpTestContext.getJsonContext());
    }
}
