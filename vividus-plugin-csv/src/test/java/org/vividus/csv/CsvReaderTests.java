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

package org.vividus.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;
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

    private final CsvReader csvReader = new CsvReader();

    @Test
    void testReadCsvFromPath() throws Exception
    {
        Path filePath = Paths.get(getCsvResource().toURI());
        List<Map<String, String>> result = csvReader.readCsvFile(filePath, FIRST_HEADER, SECOND_HEADER);
        assertEquals(CSV_RECORDS, result);
    }

    @Test
    void testReadCsvWithEscapedDataFromPath() throws Exception
    {
        Path filePath = Paths.get(getCsvResource("unittest-escaped.csv").toURI());
        List<Map<String, String>> result = new CsvReader('\\').readCsvFile(filePath, FIRST_HEADER, SECOND_HEADER);
        assertEquals(List.of(Map.of(FIRST_HEADER, FIRST_VALUE, SECOND_HEADER, "value2 with \" inside")), result);
    }

    @Test
    void testReadCsvFromStringWithoutHeaders() throws Exception
    {
        String csv = FIRST_VALUE + COMMA + SECOND_VALUE;
        List<Map<String, String>> result = csvReader.readCsvString(csv, FIRST_HEADER, SECOND_HEADER);
        assertEquals(CSV_RECORDS, result);
    }

    @Test
    void testReadCsvFromStringWithHeaders() throws Exception
    {
        String csv = FIRST_HEADER + COMMA + SECOND_HEADER + "\n" + FIRST_VALUE + COMMA + SECOND_VALUE;
        List<Map<String, String>> result = csvReader.readCsvString(csv);
        assertEquals(CSV_RECORDS, result);
    }

    @Test
    void testReadCsvFromUrl() throws Exception
    {
        URL url = getCsvResource();
        List<CSVRecord> result = csvReader.readCsvFile(url, FIRST_HEADER, SECOND_HEADER);
        assertEquals(CSV_RECORDS, result.stream().map(CSVRecord::toMap).collect(Collectors.toList()));
    }

    private URL getCsvResource()
    {
        return getCsvResource("unittest.csv");
    }

    private URL getCsvResource(String resourceName)
    {
        return getClass().getResource(resourceName);
    }
}
