/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;

class ExamplesTableToDefaultHttpHeadersConverterTests
{
    private final ExamplesTableToDefaultHttpHeadersConverter converter =
            new ExamplesTableToDefaultHttpHeadersConverter();

    @Test
    void shouldConvertExamplesTableToADefaultHeaders()
    {
        var examplesTable = new ExamplesTable("""
                |name|value|
                |key |value|
                """);
        var headers = converter.convertValue(examplesTable, Object.class);
        assertEquals(1, headers.size());
        assertEquals("value", headers.get("key"));
    }

    @Test
    void shouldConvertExamplesTableToAnEmptyDefaultHeaders()
    {
        var headers = converter.convertValue(ExamplesTable.empty(), Object.class);
        assertEquals(0, headers.size());
    }
}
