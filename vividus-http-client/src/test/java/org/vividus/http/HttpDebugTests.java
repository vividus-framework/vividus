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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

class HttpDebugTests
{
    private static final String RESOURCE_URI = "http://somewh.ere/";

    @Test
    void testRequestCreationWithURI() throws URISyntaxException
    {
        URI uri = URI.create(RESOURCE_URI);
        HttpDebug httpDebug = new HttpDebug(uri);
        assertAll(
                () -> assertEquals(uri, httpDebug.getUri()),
                () -> assertEquals("DEBUG", httpDebug.getMethod())
        );
    }

    @Test
    void testRequestCreationWithStringURI() throws URISyntaxException
    {
        assertEquals(URI.create(RESOURCE_URI), new HttpDebug(RESOURCE_URI).getUri());
    }
}
