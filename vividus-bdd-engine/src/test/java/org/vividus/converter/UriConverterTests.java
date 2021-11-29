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

package org.vividus.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import org.junit.jupiter.api.Test;

class UriConverterTests
{
    private final UriConverter converter = new UriConverter();

    @Test
    void testTypeIsAccepted()
    {
        assertTrue(converter.canConvertTo(URI.class));
    }

    @Test
    void testTypeIsNotAccepted()
    {
        assertFalse(converter.canConvertTo(String.class));
    }

    @Test
    void testConversion()
    {
        String url = "http://some.url/";
        assertEquals(URI.create(url), converter.convertValue(url, URI.class));
    }
}
