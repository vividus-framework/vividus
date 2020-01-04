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

package org.vividus.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.URI;

import org.junit.jupiter.api.Test;

public class HttpDebugTests
{
    private static final String RESOURCE_URI = "http://somewh.ere/";

    @Test
    void testEmptyRequestCreation()
    {
        assertNull(new HttpDebug().getURI());
    }

    @Test
    void testRequestCreationWithURI()
    {
        URI uri = URI.create(RESOURCE_URI);
        assertEquals(uri, new HttpDebug(uri).getURI());
    }

    @Test
    void testRequestCreationWithStringURI()
    {
        assertEquals(URI.create(RESOURCE_URI), new HttpDebug(RESOURCE_URI).getURI());
    }

    @Test
    void testMethodName()
    {
        assertEquals("DEBUG", new HttpDebug().getMethod());
    }
}
