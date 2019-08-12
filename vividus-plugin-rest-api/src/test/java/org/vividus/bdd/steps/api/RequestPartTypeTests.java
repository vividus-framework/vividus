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

package org.vividus.bdd.steps.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RequestPartTypeTests
{
    private static final String CR_LF = "\r\n";
    private static final String CONTENT_TYPE = "contentType";

    // CHECKSTYLE:OFF
    static Stream<Arguments> stringContentTypes()
    {
        return Stream.of(
                Arguments.of(Optional.empty(),          ContentType.TEXT_PLAIN),
                Arguments.of(Optional.of(CONTENT_TYPE), ContentType.parse(CONTENT_TYPE))
                );
    }
    // CHECKSTYLE:ON

    @MethodSource("stringContentTypes")
    @ParameterizedTest
    void testAddStringPart(Optional<String> contentTypeAsString, ContentType contentType)
            throws IOException
    {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        String name = "stringKey";
        String value = "stringValue";
        RequestPartType.STRING.addPart(builder, name, value, contentTypeAsString);
        assertHttpEntity(builder, buildExpectedEntity(name, contentType, "8bit", value));
    }

    // CHECKSTYLE:OFF
    static Stream<Arguments> fileContentTypes()
    {
        return Stream.of(
                Arguments.of(Optional.empty(),          ContentType.APPLICATION_OCTET_STREAM),
                Arguments.of(Optional.of(CONTENT_TYPE), ContentType.parse(CONTENT_TYPE))
                );
    }
    // CHECKSTYLE:ON

    @MethodSource("fileContentTypes")
    @ParameterizedTest
    void testAddFilePart(Optional<String> contentTypeAsString, ContentType contentType) throws IOException
    {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        String name = "fileKey";
        String fileName = "requestBody.txt";
        RequestPartType.FILE.addPart(builder, name, "./" + fileName, contentTypeAsString);
        assertHttpEntity(builder, buildExpectedEntity(name + "\"; filename=\"" + fileName,
                contentType, "binary", "{body}"));
    }

    private void assertHttpEntity(MultipartEntityBuilder builder, String expectedEntity) throws IOException
    {
        assertThat(IOUtils.toString(builder.build().getContent(), StandardCharsets.UTF_8),
                containsString(expectedEntity));
    }

    private String buildExpectedEntity(String name, ContentType contentType, String contentTransferEncoding,
            String content)
    {
        // @formatter:off
        return "Content-Disposition: form-data; name=\"" + name + "\"" + CR_LF
                + "Content-Type: " + contentType + CR_LF
                + "Content-Transfer-Encoding: " + contentTransferEncoding
                + CR_LF + CR_LF + content + CR_LF;
        // @formatter:on
    }
}
