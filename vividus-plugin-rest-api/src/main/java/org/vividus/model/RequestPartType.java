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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.vividus.util.ResourceUtils;

public enum RequestPartType
{
    STRING
    {
        @Override
        public ContentType getDefaultContentType()
        {
            return ContentType.DEFAULT_TEXT;
        }

        @Override
        public ContentBody createPart(String value, ContentType contentType)
        {
            return new StringBody(value, contentType);
        }

        @Override
        public ContentBody createPart(String value, ContentType contentType, String fileName)
        {
            throw new IllegalArgumentException(
                    String.format("'fileName' parameter is not allowed for %s request part type", name()));
        }
    },
    FILE
    {
        @Override
        public ContentType getDefaultContentType()
        {
            return ContentType.DEFAULT_BINARY;
        }

        @Override
        public ContentBody createPart(String value, ContentType contentType) throws IOException
        {
            String fileName = FilenameUtils.getName(value);
            return createPart(value, contentType, fileName);
        }

        @Override
        public ContentBody createPart(String value, ContentType contentType, String fileName) throws IOException
        {
            byte[] byteArray = ResourceUtils.loadResourceOrFileAsByteArray(value);
            return new ByteArrayBody(byteArray, contentType, fileName);
        }
    },
    BINARY
    {
        @Override
        public ContentType getDefaultContentType()
        {
            return ContentType.DEFAULT_BINARY;
        }

        @Override
        public ContentBody createPart(String value, ContentType contentType)
        {
            throw new IllegalArgumentException(
                    String.format("'fileName' parameter is required for %s request part type", name()));
        }

        @Override
        public ContentBody createPart(String value, ContentType contentType, String fileName)
        {
            byte[] byteArray = value.getBytes(StandardCharsets.UTF_8);
            return new ByteArrayBody(byteArray, contentType, fileName);
        }
    };

    public abstract ContentType getDefaultContentType();

    public abstract ContentBody createPart(String value, ContentType contentType) throws IOException;

    public abstract ContentBody createPart(String value, ContentType contentType, String fileName) throws IOException;
}
