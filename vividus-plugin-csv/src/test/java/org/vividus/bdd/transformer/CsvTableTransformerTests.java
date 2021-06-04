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

package org.vividus.bdd.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.vividus.util.ResourceUtils.findResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.csv.CsvReader;

@ExtendWith(MockitoExtension.class)
class CsvTableTransformerTests
{
    private static final String CSV_PATH_PROPERTY_NAME = "csvPath";
    private static final String DELIMITER_PROPERTY_NAME = "delimiterChar";

    private static final String CSV_FILE_NAME = "test.csv";
    private static final String EMPTY_EXAMPLES_TABLE = "";
    private static final String EXPECTED_EXAMPLES_TABLE = "|Country|ID|Capital|Akey|\n"
            + "|Belarus|1|Minsk|11|\n"
            + "|USA|2|Washington|22|\n"
            + "|Armenia|3|Yerevan|33|";

    private final ParameterConverters converters = new ParameterConverters();

    @Test
    void shouldCreateExamplesTableFromCsv()
    {
        var properties = new Properties();
        properties.setProperty(CSV_PATH_PROPERTY_NAME, CSV_FILE_NAME);
        var tableProperties = new TableProperties(converters, properties);
        var transformer = new CsvTableTransformer(CSVFormat.DEFAULT);
        assertEquals(EXPECTED_EXAMPLES_TABLE, transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
    }

    @Test
    void shouldCreateExamplesTableFromCsvWithSemicolonSeparator()
    {
        var properties = new Properties();
        properties.setProperty(CSV_PATH_PROPERTY_NAME, "test-with-semicolon.csv");
        properties.setProperty(DELIMITER_PROPERTY_NAME, ";");
        var tableProperties = new TableProperties(converters, properties);
        var transformer = new CsvTableTransformer(CSVFormat.DEFAULT);
        assertEquals(EXPECTED_EXAMPLES_TABLE, transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
    }

    @Test
    void testNoFilePathProvided()
    {
        var tableProperties = new TableProperties(converters, new Properties());
        var transformer = new CsvTableTransformer(CSVFormat.DEFAULT);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
        assertEquals("'csvPath' is not set in ExamplesTable properties", exception.getMessage());
    }

    @Test
    void testEmptyFilePathProvided()
    {
        var properties = new Properties();
        properties.setProperty(CSV_PATH_PROPERTY_NAME, "");
        var tableProperties = new TableProperties(converters, properties);
        var transformer = new CsvTableTransformer(CSVFormat.DEFAULT);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
        assertEquals("ExamplesTable property 'csvPath' is blank", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"--", ""})
    void shouldThrowAnErrorIfInvalidDelimiterIsProvided(String invalidDelimiter)
    {
        var properties = new Properties();
        properties.setProperty(CSV_PATH_PROPERTY_NAME, CSV_FILE_NAME);
        properties.setProperty(DELIMITER_PROPERTY_NAME, invalidDelimiter);
        var tableProperties = new TableProperties(converters, properties);
        var transformer = new CsvTableTransformer(CSVFormat.DEFAULT);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
        assertEquals("CSV delimiter must be a single char, but value '" + invalidDelimiter + "' has length of "
                + invalidDelimiter.length(), exception.getMessage());
    }

    @SuppressWarnings("try")
    @Test
    void testCsvFileReaderExceptionCatching()
    {
        var properties = new Properties();
        properties.setProperty(CSV_PATH_PROPERTY_NAME, CSV_FILE_NAME);
        var tableProperties = new TableProperties(converters, properties);

        URL csvResource = findResource(getClass(), CSV_FILE_NAME);
        var ioException = new IOException();
        try (MockedConstruction<CsvReader> ignored = mockConstruction(CsvReader.class,
                (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(CSVFormat.DEFAULT), context.arguments());
                    when(mock.readCsvFile(csvResource)).thenThrow(ioException);
                }))
        {
            var transformer = new CsvTableTransformer(CSVFormat.DEFAULT);
            var exception = assertThrows(UncheckedIOException.class,
                    () -> transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
            assertEquals("Problem during CSV file reading", exception.getMessage());
            assertEquals(ioException, exception.getCause());
        }
    }
}
