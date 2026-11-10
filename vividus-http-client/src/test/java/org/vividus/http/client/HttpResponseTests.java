/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;

class HttpResponseTests
{
    private static final String HEADER_NAME = "name";
    private static final String OTHER_HEADER_NAME = "other_name";

    private final HttpResponse httpResponse = new HttpResponse();

    @Test
    void testToString()
    {
        int statusCode = HttpStatus.SC_OK;
        String responseBody = "response body";
        httpResponse.setStatusCode(statusCode);
        httpResponse.setResponseBody(responseBody.getBytes(StandardCharsets.UTF_8));
        assertEquals(statusCode + " : " + responseBody, httpResponse.toString());
    }

    @Test
    void testToStringForHead()
    {
        httpResponse.setStatusCode(HttpStatus.SC_OK);
        assertEquals(HttpStatus.SC_OK + " : null", httpResponse.toString());
    }

    @Test
    void testGetNullResponseBody()
    {
        assertNull(httpResponse.getResponseBody());
    }

    @Test
    void testGetNullResponseHeaders()
    {
        assertNull(httpResponse.getResponseHeaders());
    }

    @Test
    void testGetHeaderByName()
    {
        Header header = mock(Header.class);
        when(header.getName()).thenReturn(HEADER_NAME);
        Header[] headers = { header };
        httpResponse.setResponseHeaders(headers);
        assertEquals(Optional.of(header), httpResponse.getHeaderByName(HEADER_NAME));
        assertEquals(Optional.of(header), httpResponse.getHeaderByName("NaMe"));
    }

    @Test
    void testGetHeadersByName()
    {
        Header header = mock(Header.class);
        when(header.getName()).thenReturn(HEADER_NAME);
        Header[] headers = { header, header };
        httpResponse.setResponseHeaders(headers);
        assertEquals(List.of(headers), httpResponse.getHeadersByName(HEADER_NAME).toList());
    }

    @Test
    void testGetHeadersByNameNotFound()
    {
        Header header = mock(Header.class);
        when(header.getName()).thenReturn(OTHER_HEADER_NAME);
        Header[] headers = { header, header };
        httpResponse.setResponseHeaders(headers);
        assertEquals(List.of(), httpResponse.getHeadersByName(HEADER_NAME).toList());
    }

    @Test
    void testGetHeaderByNameNotFound()
    {
        Header header = mock(Header.class);
        when(header.getName()).thenReturn(OTHER_HEADER_NAME);
        Header[] headers = { header };
        httpResponse.setResponseHeaders(headers);
        assertEquals(Optional.empty(), httpResponse.getHeaderByName(HEADER_NAME));
    }
}
