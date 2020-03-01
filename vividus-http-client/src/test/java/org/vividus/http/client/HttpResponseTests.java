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

package org.vividus.http.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HttpResponseTests
{
    private static final String HEADER_NAME = "name";
    private static final String EXCEPTION_MSG_PATTERN = "Service returned response with unexpected status code: [%d]. ";
    private static final String EXCEPTION_MSG_IN_LIST = EXCEPTION_MSG_PATTERN + "Expected code is one of: ";
    private static final String EXCEPTION_MSG_IN_RANGE = EXCEPTION_MSG_PATTERN + "Expected code from range [%d - %d]";
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
    }

    @Test
    void testGetHeadersByName()
    {
        Header header = mock(Header.class);
        when(header.getName()).thenReturn(HEADER_NAME);
        Header[] headers = { header, header };
        httpResponse.setResponseHeaders(headers);
        assertEquals(List.of(headers), httpResponse.getHeadersByName(HEADER_NAME).collect(Collectors.toList()));
    }

    @Test
    void testGetHeadersByNameNotFound()
    {
        Header header = mock(Header.class);
        when(header.getName()).thenReturn(OTHER_HEADER_NAME);
        Header[] headers = { header, header };
        httpResponse.setResponseHeaders(headers);
        assertEquals(List.of(), httpResponse.getHeadersByName(HEADER_NAME).collect(Collectors.toList()));
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

    @Test
    void testVerifyOneStatusCodeSuccess()
    {
        httpResponse.setStatusCode(HttpStatus.SC_OK);
        HttpResponse actual = httpResponse.verifyStatusCode(HttpStatus.SC_OK);
        assertThat(actual, Matchers.equalTo(httpResponse));
    }

    @Test
    void testVerifyOneStatusCodeEmpty()
    {
        httpResponse.setStatusCode(HttpStatus.SC_OK);
        ExternalServiceException exception = assertThrows(ExternalServiceException.class,
                httpResponse::verifyStatusCode);
        assertEquals(String.format(EXCEPTION_MSG_IN_LIST, HttpStatus.SC_OK), exception.getMessage());
    }

    @Test
    void testVerifyOneStatusCodeNull()
    {
        httpResponse.setStatusCode(HttpStatus.SC_OK);
        ExternalServiceException exception = assertThrows(ExternalServiceException.class,
            () -> httpResponse.verifyStatusCode(null));
        assertEquals(String.format(EXCEPTION_MSG_IN_LIST, HttpStatus.SC_OK), exception.getMessage());
    }

    @Test
    void testVerifyNoExpectedStatusCodeFromCollection()
    {
        httpResponse.setStatusCode(HttpStatus.SC_ACCEPTED);
        ExternalServiceException exception = assertThrows(ExternalServiceException.class,
            () -> httpResponse.verifyStatusCode(HttpStatus.SC_OK, HttpStatus.SC_CREATED));
        assertEquals(String.format(EXCEPTION_MSG_IN_LIST, HttpStatus.SC_ACCEPTED) + HttpStatus.SC_OK + ", "
                + HttpStatus.SC_CREATED, exception.getMessage());
    }

    @Test
    void testVerifySeveralStatusCodesSuccess()
    {
        httpResponse.setStatusCode(HttpStatus.SC_CREATED);
        HttpResponse actual = httpResponse.verifyStatusCode(HttpStatus.SC_OK, HttpStatus.SC_CREATED);
        assertThat(actual, Matchers.equalTo(httpResponse));
    }

    @ParameterizedTest
    @ValueSource(ints = { HttpStatus.SC_PROCESSING, HttpStatus.SC_UNAUTHORIZED, HttpStatus.SC_GATEWAY_TIMEOUT })
    void testVerifyStatusCodeException(int code)
    {
        httpResponse.setStatusCode(code);
        String expectedMessage = String.format(EXCEPTION_MSG_IN_RANGE, code, HttpStatus.SC_OK,
                HttpStatus.SC_BAD_REQUEST);
        ExternalServiceException exception = assertThrows(ExternalServiceException.class,
            () -> httpResponse.verifyStatusCodeInRange(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST));
        assertEquals(expectedMessage, exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = { HttpStatus.SC_OK, HttpStatus.SC_CREATED, HttpStatus.SC_BAD_REQUEST })
    void testVerifyOneStatusCodeInRangeSuccess(Integer code)
    {
        httpResponse.setStatusCode(code);
        HttpResponse actual = httpResponse.verifyStatusCodeInRange(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST);
        assertThat(actual, Matchers.equalTo(httpResponse));
    }

    @ParameterizedTest
    @ValueSource(ints = { HttpStatus.SC_PROCESSING, HttpStatus.SC_UNAUTHORIZED, HttpStatus.SC_GATEWAY_TIMEOUT })
    void testVerifyOneStatusCodeInRangeException(int code)
    {
        httpResponse.setStatusCode(code);
        String expectedMessage = String.format(EXCEPTION_MSG_IN_RANGE, code, HttpStatus.SC_OK,
                HttpStatus.SC_BAD_REQUEST);
        ExternalServiceException exception = assertThrows(ExternalServiceException.class,
            () -> httpResponse.verifyStatusCodeInRange(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST));
        assertEquals(expectedMessage, exception.getMessage());
    }
}
