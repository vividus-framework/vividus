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

package org.vividus.reporter.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AttachmentTests
{
    private static final byte[] CONTENT = "content".getBytes(StandardCharsets.UTF_8);
    private static final String TITLE = "title";
    private static final String CONTENT_TYPE = "text/html";

    private final Attachment attachment = new Attachment(CONTENT, TITLE, CONTENT_TYPE);

    @Test
    void testGetContent()
    {
        assertArrayEquals(CONTENT, attachment.getContent());
    }

    @Test
    void testGetTitle()
    {
        assertEquals(TITLE, attachment.getTitle());
    }

    @Test
    void testGetContentType()
    {
        assertEquals(CONTENT_TYPE, attachment.getContentType());
    }

    @Test
    void testParseTitle()
    {
        String title = "file";
        assertEquals(title, new Attachment(CONTENT, title + ".html").getTitle());
    }

    @ParameterizedTest
    @CsvSource({ "text/html, file.html", "application/json, file.json"})
    void testDetectContentType(String expectedContentType, String fileName)
    {
        assertEquals(expectedContentType, new Attachment(CONTENT, fileName).getContentType());
    }
}
