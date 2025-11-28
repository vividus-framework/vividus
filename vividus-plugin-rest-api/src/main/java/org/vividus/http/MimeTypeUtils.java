/*
 * Copyright 2019-2025 the original author or authors.
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

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.MessageSupport;

public final class MimeTypeUtils
{
    private MimeTypeUtils()
    {
    }

    /**
     * Tries to get value of the "Content-Type" header (case-insensitive).
     * If the header does not exist or empty, returns text ("text/plain") MIME type
     * @param headers Headers to get MIME type
     * @return MIME type
     */
    public static String getMimeTypeFromHeadersWithDefault(Header... headers)
    {
        return getMimeTypeFromHeaders(headers).orElseGet(ContentType.DEFAULT_TEXT::getMimeType);
    }

    public static Optional<String> getMimeTypeFromHeaders(Header... headers)
    {
        return Stream.of(headers)
                .filter(h -> HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(h.getName())
                        && StringUtils.isNotBlank(h.getValue()))
                .findFirst()
                .map(MessageSupport::parseElements)
                .map(elements -> elements.get(0))
                .map(HeaderElement::getName);
    }
}
