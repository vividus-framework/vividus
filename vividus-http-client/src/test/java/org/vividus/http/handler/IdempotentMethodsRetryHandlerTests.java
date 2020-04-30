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

package org.vividus.http.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestBuilder;
import org.vividus.http.exception.HttpRequestBuildException;

class IdempotentMethodsRetryHandlerTests
{
    private static final String POST = "POST";
    private static final StringEntity REQUEST_ENTITY = new StringEntity("request", StandardCharsets.UTF_8);

    static Stream<Arguments> dataProvider()
    {
        return Stream.of(
                Arguments.of(List.of(POST), HttpMethod.GET,  null,           true),
                Arguments.of(List.of(POST), HttpMethod.POST, REQUEST_ENTITY, true),
                Arguments.of(List.of(),     HttpMethod.POST, REQUEST_ENTITY, false),
                Arguments.of(null,          HttpMethod.GET,  null,           true),
                Arguments.of(null,          HttpMethod.POST, REQUEST_ENTITY, false)
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void testHandleAsIdempotent(List<String> methods, HttpMethod method, HttpEntity entity, boolean expected)
            throws HttpRequestBuildException
    {
        IdempotentMethodsRetryHandler handler = new IdempotentMethodsRetryHandler();
        handler.setIdempotentMethodsSendingRequestBody(methods);
        HttpRequest httpRequest = HttpRequestBuilder.create()
                .withEndpoint("https://vividus-framework.vividus/")
                .withHttpMethod(method)
                .withContent(entity)
                .build();
        assertEquals(expected, handler.handleAsIdempotent(httpRequest));
    }
}
