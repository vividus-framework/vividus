/*
 * Copyright 2019 the original author or authors.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class CsvReader
{
    private final CSVFormat csvFormat;

    public CsvReader()
    {
        this(CSVFormat.DEFAULT);
    }

    public CsvReader(CSVFormat csvFormat)
    {
        this.csvFormat = csvFormat;
    }

    public List<Map<String, String>> readCsvFile(Path path, String... header) throws IOException
    {
        try (Reader reader = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8))
        {
            return collectCsv(reader, header);
        }
    }

    public List<Map<String, String>> readCsvString(String csvString, String... header) throws IOException
    {
        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(csvString.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8))
        {
            return collectCsv(reader, header);
        }
    }

    public List<CSVRecord> readCsvFile(URL resourceUrl, String... header) throws IOException
    {
        try (Reader reader = new InputStreamReader(resourceUrl.openStream(), StandardCharsets.UTF_8))
        {
            return readCsvFile(reader, header).collect(Collectors.toList());
        }
    }

    private List<Map<String, String>> collectCsv(Reader reader, String... header) throws IOException
    {
        return readCsvFile(reader, header).map(CSVRecord::toMap).collect(Collectors.toList());
    }


    private Stream<CSVRecord> readCsvFile(Reader reader, String... header) throws IOException
    {
        return StreamSupport.stream(csvFormat.withHeader(header).parse(reader).spliterator(), false);
    }
}
