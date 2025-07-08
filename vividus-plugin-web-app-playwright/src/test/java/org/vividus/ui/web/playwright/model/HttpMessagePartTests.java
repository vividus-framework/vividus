/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;

class HttpMessagePartTests
{
    private static final String URL = "www.test.com";
    private static final String KEY1 = "key1";
    private static final String VALUE1 = "value1";
    private static final String KEY2 = "key2";
    private static final String VALUE2 = "value2";
    private static final String URL_WITH_QUERY = String.format("%s/?%s=%s&%s=%s&%s=%s", URL, KEY1, VALUE1, KEY1, VALUE2,
            KEY2, VALUE2);
    private static final String RESOURCE_TYPE = "resourceType";
    private static final String RESOURCE_TYPE_VALUE = "document";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE = "contentType";
    private static final String TEXT = "text";
    private static final String NOT_DEFINED = "not defined";

    @Test
    void shouldReturnRequestUrl()
    {
        Request request = mock();
        when(request.url()).thenReturn(URL);
        assertEquals(URL, HttpMessagePart.URL.get(request));
    }

    @Test
    void shouldReturnUrlQuery()
    {
        Request request = mock();
        when(request.url()).thenReturn(URL_WITH_QUERY);
        Map<String, List<String>> queryParameters = Map.of(
            KEY1, List.of(VALUE1, VALUE2),
            KEY2, List.of(VALUE2)
        );
        assertEquals(queryParameters, HttpMessagePart.URL_QUERY.get(request));
    }

    @Test
    void shouldTryReturnUrlQueryAndThrowExceptionInCaseOfInvalidUrl()
    {
        Request request = mock();
        when(request.url()).thenReturn("c:\\Users");
        var illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> HttpMessagePart.URL_QUERY.get(request));
        var expectedError = "java.net.URISyntaxException: Illegal character in opaque part at index 2: c:\\Users";
        assertEquals(expectedError, illegalArgumentException.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnRequestData()
    {
        Request request = mock();
        when(request.url()).thenReturn(URL_WITH_QUERY);
        when(request.resourceType()).thenReturn(RESOURCE_TYPE_VALUE);
        when(request.headerValue(CONTENT_TYPE_HEADER)).thenReturn(CONTENT_TYPE);
        when(request.postData()).thenReturn(TEXT);
        Response response = mock();
        when(request.response()).thenReturn(response);
        when(response.status()).thenReturn(HttpStatus.SC_OK);
        when(request.postData()).thenReturn(TEXT);

        Map<String, Object> requestData = (Map<String, Object>) HttpMessagePart.REQUEST_DATA.get(request);
        Map<String, List<String>> urlQuery = (Map<String, List<String>>) requestData.get("query");
        Map<String, String> requestBody = (Map<String, String>) requestData.get("requestBody");
        Integer responseStatus = (Integer) requestData.get("responseStatus");
        assertAll(
            () -> assertEquals(List.of(VALUE1, VALUE2), urlQuery.get(KEY1)),
            () -> assertEquals(List.of(VALUE2), urlQuery.get(KEY2)),
            () -> assertEquals(RESOURCE_TYPE_VALUE, requestData.get(RESOURCE_TYPE)),
            () -> assertEquals(CONTENT_TYPE, requestBody.get(CONTENT_TYPE)),
            () -> assertEquals(TEXT, requestBody.get(TEXT)),
            () -> assertEquals(HttpStatus.SC_OK, responseStatus)
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnResponseData()
    {
        Request request = mock();
        Response response = mock();
        when(request.response()).thenReturn(response);

        Map<String, Object> responseData = (Map<String, Object>) HttpMessagePart.RESPONSE_DATA.get(request);
        Map<String, String> responseBody = (Map<String, String>) responseData.get("responseBody");
        assertAll(
                () -> assertEquals(NOT_DEFINED, responseBody.get(CONTENT_TYPE)),
                () -> assertEquals(NOT_DEFINED, responseBody.get(TEXT))
        );
    }
}
