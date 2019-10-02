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

package org.vividus.http.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IdempotentMethodsRetryHandlerTests
{
    private static final String POST = "POST";
    private static final String GET = "GET";

    static Stream<Arguments> dataProvider()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
            Arguments.of(List.of(POST), GET , false, 1),
            Arguments.of(List.of(POST), POST, true , 1),
            Arguments.of(null         , GET , true , 0)
        );
        // CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void testHandleAsIdempotent(List<String> methods, String method, boolean expected, int times)
    {
        IdempotentMethodsRetryHandler handler = new IdempotentMethodsRetryHandler();
        handler.setIdempotentMethodsSendingRequestBody(methods);
        HttpRequest httpRequest = mockHttpRequest(method);
        assertEquals(expected, handler.handleAsIdempotent(httpRequest));
        verify(httpRequest, times(times)).getRequestLine();
    }

    @Test
    void testHandleAsIdempotentParentIdempotent()
    {
        IdempotentMethodsRetryHandler handler = new IdempotentMethodsRetryHandler();
        HttpRequest httpRequest = mock(HttpRequest.class);
        assertTrue(handler.handleAsIdempotent(httpRequest));
        verifyNoInteractions(httpRequest);
    }

    private HttpRequest mockHttpRequest(String method)
    {
        HttpEntityEnclosingRequest httpRequest = mock(HttpEntityEnclosingRequest.class);
        RequestLine requestLine = mock(RequestLine.class);
        when(httpRequest.getRequestLine()).thenReturn(requestLine);
        when(requestLine.getMethod()).thenReturn(method);
        return httpRequest;
    }
}
