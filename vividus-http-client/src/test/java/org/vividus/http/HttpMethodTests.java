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

package org.vividus.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpOptions;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpTrace;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.http.exception.HttpRequestBuildException;

class HttpMethodTests
{
    private static final URI URI_EXAMPLE = URI.create("https://any.example/");

    static Stream<Arguments> successfulEmptyRequestCreation()
    {
        return Stream.of(
                Arguments.of(HttpMethod.GET, HttpGet.class),
                Arguments.of(HttpMethod.HEAD, HttpHead.class),
                Arguments.of(HttpMethod.DELETE, HttpDelete.class),
                Arguments.of(HttpMethod.OPTIONS, HttpOptions.class),
                Arguments.of(HttpMethod.TRACE, HttpTrace.class),
                Arguments.of(HttpMethod.POST, HttpPost.class),
                Arguments.of(HttpMethod.PUT, HttpPut.class),
                Arguments.of(HttpMethod.DEBUG, HttpDebug.class)
        );
    }

    static Stream<Arguments> successfulEmptyEnclosingEntityRequestCreation()
    {
        return Stream.of(
                Arguments.of(HttpMethod.DELETE, HttpDelete.class),
                Arguments.of(HttpMethod.PATCH, HttpPatch.class),
                Arguments.of(HttpMethod.POST, HttpPost.class),
                Arguments.of(HttpMethod.PUT, HttpPut.class)
        );
    }

    @ParameterizedTest
    @MethodSource("successfulEmptyRequestCreation")
    void testSuccessfulEmptyRequestCreation(HttpMethod httpMethod, Class<? extends ClassicHttpRequest> requestClass)
            throws HttpRequestBuildException, URISyntaxException
    {
        var request = httpMethod.createRequest(URI_EXAMPLE);
        assertThat(request, instanceOf(requestClass));
        assertEquals(URI_EXAMPLE, request.getUri());
    }

    @Test
    void testFailedEmptyRequestCreationForHttpPatch()
    {
        var exception = assertThrows(HttpRequestBuildException.class,
            () -> HttpMethod.PATCH.createRequest(URI_EXAMPLE));
        assertEquals("HTTP PATCH request must include body", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("successfulEmptyEnclosingEntityRequestCreation")
    void testSuccessfulEmptyEnclosingEntityRequestCreation(HttpMethod httpMethod,
            Class<? extends ClassicHttpRequest> requestClass) throws HttpRequestBuildException, URISyntaxException
    {
        @SuppressWarnings("PMD.CloseResource")
        var entity = new StringEntity("body", StandardCharsets.UTF_8);
        var request = httpMethod.createEntityEnclosingRequest(URI_EXAMPLE, entity);
        assertThat(request, instanceOf(requestClass));
        assertEquals(URI_EXAMPLE, request.getUri());
        assertEquals(entity, request.getEntity());
    }

    @ParameterizedTest
    @EnumSource(value = HttpMethod.class, names = { "GET", "HEAD", "OPTIONS", "TRACE", "DEBUG" })
    void testFailedEmptyEnclosingEntityRequestCreation(HttpMethod httpMethod)
    {
        var exception = assertThrows(HttpRequestBuildException.class,
            () -> httpMethod.createEntityEnclosingRequest(URI_EXAMPLE));
        assertEquals("HTTP " + httpMethod + " request can't include body", exception.getMessage());
    }
}
