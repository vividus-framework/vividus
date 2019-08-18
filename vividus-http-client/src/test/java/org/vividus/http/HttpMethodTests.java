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

package org.vividus.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class HttpMethodTests
{
    static Stream<Arguments> successfulEmptyRequestCreation()
    {
        return Stream.of(
                Arguments.of(HttpMethod.GET, HttpGet.class),
                Arguments.of(HttpMethod.HEAD, HttpHead.class),
                Arguments.of(HttpMethod.DELETE, HttpDelete.class),
                Arguments.of(HttpMethod.OPTIONS, HttpOptions.class),
                Arguments.of(HttpMethod.TRACE, HttpTrace.class),
                Arguments.of(HttpMethod.PUT, HttpPutWithoutBody.class),
                Arguments.of(HttpMethod.DEBUG, HttpDebug.class)
        );
    }

    static Stream<Arguments> successfulEmptyEnclosingEntityRequestCreation()
    {
        return Stream.of(
                Arguments.of(HttpMethod.DELETE, HttpDeleteWithBody.class),
                Arguments.of(HttpMethod.PATCH, HttpPatch.class),
                Arguments.of(HttpMethod.POST, HttpPost.class),
                Arguments.of(HttpMethod.PUT, HttpPut.class)
        );
    }

    @ParameterizedTest
    @MethodSource("successfulEmptyRequestCreation")
    void testSuccessfulEmptyRequestCreation(HttpMethod httpMethod, Class<? extends HttpRequestBase> requestClass)
    {
        assertThat(httpMethod.createEmptyRequest(), instanceOf(requestClass));
    }

    @ParameterizedTest
    @EnumSource(value = HttpMethod.class, names = { "PATCH", "POST" })
    void testFailedEmptyRequestCreation(HttpMethod httpMethod)
    {
        IllegalStateException exception = assertThrows(IllegalStateException.class, httpMethod::createEmptyRequest);
        assertEquals(createExceptionMessage(httpMethod, "must"), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("successfulEmptyEnclosingEntityRequestCreation")
    void testSuccessfulEmptyEnclosingEntityRequestCreation(HttpMethod httpMethod,
            Class<? extends HttpRequestBase> requestClass)
    {
        assertThat(httpMethod.createEmptyEnclosingEntityRequest(), instanceOf(requestClass));
    }

    @ParameterizedTest
    @EnumSource(value = HttpMethod.class, names = { "GET", "HEAD", "OPTIONS", "TRACE", "DEBUG" })
    void testFailedEmptyEnclosingEntityRequestCreation(HttpMethod httpMethod)
    {
        assertThrows(IllegalStateException.class, httpMethod::createEmptyEnclosingEntityRequest,
                createExceptionMessage(httpMethod, "can't"));
    }

    private static String createExceptionMessage(HttpMethod httpMethod, String verb)
    {
        return "HTTP " + httpMethod + " request " + verb + " include body";
    }
}
