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

package org.vividus.http.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.hc.client5.http.entity.mime.AbstractContentBody;
import org.apache.hc.client5.http.entity.mime.ByteArrayBody;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.Test;
import org.vividus.http.steps.HttpRequestSteps.RequestPartType;

class RequestPartTypeTests
{
    private static final ContentType CONTENT_TYPE = ContentType.APPLICATION_JSON;
    private static final String FILE_CONTENT = "{body}";

    @Test
    void shouldCreateStringPart() throws IOException
    {
        var value = "stringValue";
        var part = (StringBody) RequestPartType.STRING.createPart(value, CONTENT_TYPE);
        assertRequestPart(part, value, null);
    }

    @Test
    void shouldNotCreateStringPartWithFileName()
    {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> RequestPartType.STRING.createPart(null, CONTENT_TYPE, "string-filename"));
        assertEquals("'fileName' parameter is not allowed for STRING request part type", exception.getMessage());
    }

    @Test
    void shouldCreateFilePartWithDefaultFileName() throws IOException
    {
        var fileName = "requestBody.txt";
        var part = (ByteArrayBody) RequestPartType.FILE.createPart("/" + fileName, CONTENT_TYPE);
        assertRequestPart(part, FILE_CONTENT, fileName);
    }

    @Test
    void shouldCreateFilePartWithCustomFileName() throws IOException
    {
        var fileName = "file.txt";
        var part = (ByteArrayBody) RequestPartType.FILE.createPart("/requestBody.txt", CONTENT_TYPE, fileName);
        assertRequestPart(part, FILE_CONTENT, fileName);
    }

    @Test
    void shouldCreateBinaryPart() throws IOException
    {
        var data = "data";
        var fileName = "binary.txt";
        var part = (ByteArrayBody) RequestPartType.BINARY.createPart(data, CONTENT_TYPE, fileName);
        assertRequestPart(part, data, fileName);
    }

    @Test
    void shouldNotCreateBinaryPartWithoutFileName()
    {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> RequestPartType.BINARY.createPart(null, CONTENT_TYPE));
        assertEquals("'fileName' parameter is required for BINARY request part type", exception.getMessage());
    }

    private void assertRequestPart(AbstractContentBody part, String data, String fileName) throws IOException
    {
        assertEquals(CONTENT_TYPE, part.getContentType());
        try (var byteArrayOutputStream = new ByteArrayOutputStream())
        {
            part.writeTo(byteArrayOutputStream);
            assertEquals(data, byteArrayOutputStream.toString(StandardCharsets.UTF_8));
        }
        assertEquals(fileName, part.getFilename());
    }
}
