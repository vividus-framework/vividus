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

package org.vividus.bdd.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PathConverterTests
{
    private final PathConverter converter = new PathConverter();

    @Test
    void testTypeIsAccepted()
    {
        assertTrue(converter.canConvertTo(Path.class));
    }

    @Test
    void testTypeIsNotAccepted()
    {
        assertFalse(converter.canConvertTo(File.class));
    }

    @Test
    void testConversion(@TempDir Path path)
    {
        assertEquals(path, converter.convertValue(path.toString(), Path.class));
    }
}
