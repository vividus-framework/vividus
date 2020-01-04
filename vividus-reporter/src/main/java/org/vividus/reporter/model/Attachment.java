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

package org.vividus.reporter.model;

import java.net.URLConnection;

import com.google.common.io.Files;

import org.apache.commons.lang3.ArrayUtils;

public class Attachment
{
    private final byte[] content;
    private final String title;
    private final String contentType;

    public Attachment(byte[] content, String title, String contentType)
    {
        this.content = ArrayUtils.clone(content);
        this.title = title;
        this.contentType = contentType;
    }

    public Attachment(byte[] content, String fileName)
    {
        this(content, Files.getNameWithoutExtension(fileName), probeContentType(fileName));
    }

    private static String probeContentType(String fileName)
    {
        return fileName.endsWith(".json") ? "application/json" : URLConnection.guessContentTypeFromName(fileName);
    }

    public byte[] getContent()
    {
        return ArrayUtils.clone(content);
    }

    public String getTitle()
    {
        return title;
    }

    public String getContentType()
    {
        return contentType;
    }
}
