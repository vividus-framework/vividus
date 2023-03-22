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

package org.vividus.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestBuilder;
import org.vividus.http.exception.HttpRequestBuildException;

class HttpRequestRetryStrategyTests
{
    private static final String POST = "POST";
    private static final StringEntity REQUEST_ENTITY = new StringEntity("request", StandardCharsets.UTF_8);

    static Stream<Arguments> dataProvider()
    {
        return Stream.of(
                arguments(List.of(POST), HttpMethod.GET,  null,           true),
                arguments(List.of(POST), HttpMethod.POST, REQUEST_ENTITY, true),
                arguments(List.of(),     HttpMethod.POST, REQUEST_ENTITY, false),
                arguments(null,          HttpMethod.GET,  null,           true),
                arguments(null,          HttpMethod.POST, REQUEST_ENTITY, false)
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void testHandleAsIdempotent(List<String> methods, HttpMethod method, HttpEntity entity, boolean expected)
            throws HttpRequestBuildException
    {
        var handler = new HttpRequestRetryStrategy(1, Duration.ofMillis(1), null);
        handler.setIdempotentMethodsSendingRequestBody(methods);
        HttpRequest httpRequest = HttpRequestBuilder.create()
                .withEndpoint("https://vividus-framework.vividus/")
                .withHttpMethod(method)
                .withContent(entity)
                .build();
        assertEquals(expected, handler.handleAsIdempotent(httpRequest));
    }

    @Test
    void shouldSetRetryInterval()
    {
        var retryInterval = 123;
        var strategy = new HttpRequestRetryStrategy(1, Duration.ofMillis(retryInterval), null);
        var expected = TimeValue.of(retryInterval, TimeUnit.MILLISECONDS);
        assertEquals(expected, strategy.getRetryInterval(mock(HttpResponse.class), 1, mock(HttpContext.class)));
    }

    @ParameterizedTest
    @CsvSource({
            "1, 500, true",
            "1, 418, false",
            "2, 500, false"
    })
    void shouldTakeIntoAccountParametersWhileCheckingForRetry(int executionCount, int statusCode, boolean retry)
    {
        var strategy = new HttpRequestRetryStrategy(1, Duration.ofMillis(1), Set.of(statusCode));
        var response = mock(HttpResponse.class);
        when(response.getCode()).thenReturn(500);
        assertEquals(retry, strategy.retryRequest(response, executionCount, null));
    }
}
