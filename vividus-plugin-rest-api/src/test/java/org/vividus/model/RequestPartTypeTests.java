/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.junit.jupiter.api.Test;

class RequestPartTypeTests
{
    private static final ContentType CONTENT_TYPE = ContentType.APPLICATION_JSON;
    private static final String FILE_CONTENT = "{body}";

    @Test
    void shouldCreateStringPart() throws IOException
    {
        String value = "stringValue";
        StringBody part = (StringBody) RequestPartType.STRING.createPart(value, CONTENT_TYPE);
        assertRequestPart(part, value, null);
    }

    @Test
    void shouldNotCreateStringPartWithFileName()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> RequestPartType.STRING.createPart(null, CONTENT_TYPE, "string-filename"));
        assertEquals("'fileName' parameter is not allowed for STRING request part type", exception.getMessage());
    }

    @Test
    void shouldCreateFilePartWithDefaultFileName() throws IOException
    {
        String fileName = "requestBody.txt";
        ByteArrayBody part = (ByteArrayBody) RequestPartType.FILE.createPart("/" + fileName, CONTENT_TYPE);
        assertRequestPart(part, FILE_CONTENT, fileName);
    }

    @Test
    void shouldCreateFilePartWithCustomFileName() throws IOException
    {
        String fileName = "file.txt";
        ByteArrayBody part = (ByteArrayBody) RequestPartType.FILE.createPart("/requestBody.txt", CONTENT_TYPE,
                fileName);
        assertRequestPart(part, FILE_CONTENT, fileName);
    }

    @Test
    void shouldCreateBinaryPart() throws IOException
    {
        String data = "data";
        String fileName = "binary.txt";
        ByteArrayBody part = (ByteArrayBody) RequestPartType.BINARY.createPart(data, CONTENT_TYPE, fileName);
        assertRequestPart(part, data, fileName);
    }

    @Test
    void shouldNotCreateBinaryPartWithoutFileName()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> RequestPartType.BINARY.createPart(null, CONTENT_TYPE));
        assertEquals("'fileName' parameter is required for BINARY request part type", exception.getMessage());
    }

    private void assertRequestPart(AbstractContentBody part, String data, String fileName) throws IOException
    {
        assertEquals(CONTENT_TYPE, part.getContentType());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        part.writeTo(byteArrayOutputStream);
        assertEquals(data, byteArrayOutputStream.toString(StandardCharsets.UTF_8));
        assertEquals(fileName, part.getFilename());
    }
}
