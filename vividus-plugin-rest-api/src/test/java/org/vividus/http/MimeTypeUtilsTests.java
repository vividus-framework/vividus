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

import static org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class MimeTypeUtilsTests
{
    private static final String TEXT_TYPE = "text/plain";

    static Stream<Arguments> headersWithoutContentType()
    {
        return Stream.of(arguments(new BasicHeader(CONTENT_TYPE, "")),
                         arguments(new BasicHeader("stubHeader", "stubHeaderValue")));
    }

    @ParameterizedTest
    @ValueSource(strings = { CONTENT_TYPE, "content-type" })
    void shouldGetMimeTypeFromHeaders(String contentType)
    {
        Header contentTypeHeader = new BasicHeader(contentType, TEXT_TYPE);
        String actualMimeType = MimeTypeUtils.getMimeTypeFromHeaders(contentTypeHeader).get();
        assertEquals(TEXT_TYPE, actualMimeType);
    }

    @ParameterizedTest
    @MethodSource("headersWithoutContentType")
    void shouldNotGetMimeTypeFromHeadersWithoutContentType(Header header)
    {
        Optional<String> actualMimeType = MimeTypeUtils.getMimeTypeFromHeaders(header);
        assertEquals(Optional.empty(), actualMimeType);
    }

    @Test
    void shouldGetMimeTypeFromHeadersWithDefault()
    {
        String actualMimeType = MimeTypeUtils.getMimeTypeFromHeadersWithDefault();
        assertEquals(TEXT_TYPE, actualMimeType);
    }
}
