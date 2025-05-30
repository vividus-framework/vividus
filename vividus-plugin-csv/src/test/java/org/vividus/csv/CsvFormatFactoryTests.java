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

package org.vividus.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Test;

class CsvFormatFactoryTests
{
    @Test
    void shouldCreateCsvFormat()
    {
        var delimiter = ';';
        var escapeChar = '\\';
        var actual = new CsvFormatFactory(delimiter, escapeChar).getCsvFormat();
        assertEquals(CSVFormat.DEFAULT.builder().setDelimiter(delimiter).setEscape(escapeChar).get(), actual);
    }
}
