/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.csv.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.vividus.util.ResourceUtils.findResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.csv.CsvReader;

@ExtendWith(MockitoExtension.class)
class CsvTableTransformerTests
{
    private static final String EMPTY_EXAMPLES_TABLE = "";
    private static final String EXPECTED_EXAMPLES_TABLE = "|Country|ID|Capital|Akey|\n"
            + "|Belarus|1|Minsk|11|\n"
            + "|USA|2|Washington|22|\n"
            + "|Armenia|3|Yerevan|33|";

    private final Keywords keywords = new Keywords();
    private final ParameterConverters converters = new ParameterConverters();

    @ParameterizedTest
    @ValueSource(strings = {
            "csvPath=test.csv",
            "csvPath=test-with-semicolon.csv, delimiterChar=;"
    })
    void shouldCreateExamplesTableFromCsv(String propertiesAsString)
    {
        var tableProperties = new TableProperties(propertiesAsString, keywords, converters);
        var transformer = new CsvTableTransformer(CSVFormat.DEFAULT);
        assertEquals(EXPECTED_EXAMPLES_TABLE, transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
    }

    @ParameterizedTest
    @CsvSource({
        "'',                                  'csvPath' is not set in ExamplesTable properties",
        "'csvPath= ',                         ExamplesTable property 'csvPath' is blank",
        "'csvPath=test.csv,delimiterChar=--', 'CSV delimiter must be a single char, but value ''--'' has length of 2'",
        "'csvPath=test.csv,delimiterChar= ',  'CSV delimiter must be a single char, but value '''' has length of 0'"
    })
    void shouldThrowErrorIfInvalidParametersAreProvided(String propertiesAsString, String errorMessage)
    {
        var tableProperties = new TableProperties(propertiesAsString, keywords, converters);
        var transformer = new CsvTableTransformer(CSVFormat.DEFAULT);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
        assertEquals(errorMessage, exception.getMessage());
    }

    @SuppressWarnings("try")
    @Test
    void testCsvFileReaderExceptionCatching()
    {
        var csvFileName = "test.csv";
        var tableProperties = new TableProperties("csvPath=" + csvFileName, keywords, converters);

        URL csvResource = findResource(getClass(), csvFileName);
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
