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

package org.vividus.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Test;

class CsvReaderTests
{
    private static final String FIRST_HEADER = "header1";
    private static final String SECOND_HEADER = "header2";
    private static final String FIRST_VALUE = "value1";
    private static final String SECOND_VALUE = "value2";
    private static final String COMMA = ",";
    private static final Map<String, String> CSV_RECORD = Map.of(FIRST_HEADER, FIRST_VALUE, SECOND_HEADER,
            SECOND_VALUE);
    private static final List<Map<String, String>> CSV_RECORDS = List.of(CSV_RECORD);

    @Test
    void testReadCsvWithEscapedDataFromPath() throws IOException
    {
        try (var inputStream = getClass().getResourceAsStream("unittest-escaped.csv"))
        {
            var csvFormat = CSVFormat.DEFAULT.builder().setDelimiter(',').setEscape('\\').build();
            var result = new CsvReader(csvFormat).readCsvStream(inputStream, FIRST_HEADER, SECOND_HEADER);
            assertEquals(List.of(Map.of(FIRST_HEADER, FIRST_VALUE, SECOND_HEADER, "value2 with \" inside")), result);
        }
    }

    @Test
    void testReadCsvFromStringWithoutHeaders() throws IOException
    {
        var csv = FIRST_VALUE + COMMA + SECOND_VALUE;
        var result = new CsvReader().readCsvString(csv, FIRST_HEADER, SECOND_HEADER);
        assertEquals(CSV_RECORDS, result);
    }

    @Test
    void testReadCsvFromStringWithHeaders() throws IOException
    {
        var csv = FIRST_HEADER + COMMA + SECOND_HEADER + "\n" + FIRST_VALUE + COMMA + SECOND_VALUE;
        var result = new CsvReader().readCsvString(csv);
        assertEquals(CSV_RECORDS, result);
    }
}
