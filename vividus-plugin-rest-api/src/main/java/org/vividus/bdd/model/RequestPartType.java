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

package org.vividus.bdd.model;

import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.vividus.util.ResourceUtils;

public enum RequestPartType
{
    STRING
    {
        @Override
        public void addPart(MultipartEntityBuilder builder, String name, String value, Optional<String> contentType)
        {
            contentType.ifPresentOrElse(type -> builder.addTextBody(name, value, ContentType.parse(type)),
                () -> builder.addTextBody(name, value));
        }
    },
    FILE
    {
        @Override
        public void addPart(MultipartEntityBuilder builder, String name, String value, Optional<String> contentType)
        {
            byte[] byteArray = ResourceUtils.loadResourceAsByteArray(getClass(), value);
            String fileName = FilenameUtils.getName(value);
            builder.addBinaryBody(name, byteArray,
                    contentType.map(ContentType::parse).orElse(ContentType.DEFAULT_BINARY),
                    fileName);
        }
    };

    public abstract void addPart(MultipartEntityBuilder builder, String name, String value,
            Optional<String> contentType);
}
