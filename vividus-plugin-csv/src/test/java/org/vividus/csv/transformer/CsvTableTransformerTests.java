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

package org.vividus.csv.transformer;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.csv.CSVFormat;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.csv.CsvReader;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class CsvTableTransformerTests
{
    private static final String EMPTY_EXAMPLES_TABLE = "";
    private static final String EXPECTED_EXAMPLES_TABLE = """
            |Country|ID|Capital|Akey|
            |Belarus|1|Minsk|11|
            |USA|2|Washington|22|
            |Armenia|3|Yerevan|33|""";
    private static final String VARIABLE_CSV = """
            Country,Capital
            Belarus,Minsk""";
    private static final String VARIABLE_CSV_CUSTOM_DELIMITER = """
            Country;Capital
            Belarus;Minsk""";
    private static final String EXPECTED_VARIABLE_TABLE = """
            |Country|Capital|
            |Belarus|Minsk|""";

    private final Keywords keywords = new Keywords();
    private final ParameterConverters converters = new ParameterConverters();
    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(CsvTableTransformer.class);
    @Mock private VariableContext variableContext;

    @ParameterizedTest
    @ValueSource(strings = {
            "csvPath=org/vividus/csv/transformer/test.csv",
            "csvPath=org/vividus/csv/transformer/test-with-semicolon.csv, delimiterChar=;"
    })
    void shouldCreateExamplesTableFromCsvDeprecatedProperty(String propertiesAsString)
    {
        var tableProperties = new TableProperties(propertiesAsString, keywords, converters);
        var transformer = new CsvTableTransformer(CSVFormat.DEFAULT, variableContext);
        assertEquals(EXPECTED_EXAMPLES_TABLE, transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
        assertThat(testLogger.getLoggingEvents(), is(List.of(warn(
                "The 'csvPath' transformer parameter is deprecated and will be removed VIVIDUS 0.7.0, "
                        + "please use 'path' parameter instead."
        ))));
    }

    @ParameterizedTest
    @MethodSource("variableProvider")
    void shouldCreateExamplesTableFromVariable(String propertiesAsString, String variableValue)
    {
        when(variableContext.getVariable("csvVar")).thenReturn(variableValue);
        var tableProperties = new TableProperties(propertiesAsString, keywords, converters);
        var transformer = new CsvTableTransformer(CSVFormat.DEFAULT, variableContext);
        assertEquals(EXPECTED_VARIABLE_TABLE, transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
    }

    static Stream<Arguments> variableProvider()
    {
        return Stream.of(
                arguments("variableName=csvVar", VARIABLE_CSV),
                arguments("variableName=csvVar, delimiterChar=;", VARIABLE_CSV_CUSTOM_DELIMITER)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "path=org/vividus/csv/transformer/test.csv",
            "path=org/vividus/csv/transformer/test-with-semicolon.csv, delimiterChar=;"
    })
    void shouldCreateExamplesTableFromCsv(String propertiesAsString)
    {
        var tableProperties = new TableProperties(propertiesAsString, keywords, converters);
        var transformer = new CsvTableTransformer(CSVFormat.DEFAULT, variableContext);
        assertEquals(EXPECTED_EXAMPLES_TABLE, transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
    }

    @ParameterizedTest
    @CsvSource({
        "'path=org/vividus/csv/transformer/test.csv,delimiterChar=--',"
                + "'CSV delimiter must be a single char, but value ''--'' has length of 2'",
        "'path=org/vividus/csv/transformer/test.csv,delimiterChar= ',"
                + "'CSV delimiter must be a single char, but value '''' has length of 0'"
    })
    void shouldThrowErrorIfInvalidParametersAreProvided(String propertiesAsString, String errorMessage)
    {
        var tableProperties = new TableProperties(propertiesAsString, keywords, converters);
        var transformer = new CsvTableTransformer(CSVFormat.DEFAULT, variableContext);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
        assertEquals(errorMessage, exception.getMessage());
    }

    @SuppressWarnings("try")
    @Test
    void testCsvFileReaderExceptionCatching()
    {
        var csvFileName = "org/vividus/csv/transformer/test.csv";
        var tableProperties = new TableProperties("path=" + csvFileName, keywords, converters);

        var ioException = new IOException();
        try (MockedConstruction<CsvReader> ignored = mockConstruction(CsvReader.class,
                (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(CSVFormat.DEFAULT), context.arguments());
                    when(mock.readCsvStream(any())).thenThrow(ioException);
                }))
        {
            var transformer = new CsvTableTransformer(CSVFormat.DEFAULT, variableContext);
            var exception = assertThrows(UncheckedIOException.class,
                    () -> transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
            assertException(exception, ioException);
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testEmptyVariableValue(String variableValue)
    {
        when(variableContext.getVariable("emptyVar")).thenReturn(variableValue);
        var tableProperties = new TableProperties("variableName=emptyVar", keywords, converters);
        var transformer = new CsvTableTransformer(CSVFormat.DEFAULT, variableContext);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
        assertEquals("Variable 'emptyVar' is not set or empty. Please check that variable is defined"
                + " and has 'global' or 'next_batches' scope", exception.getMessage());
    }

    @Test
    void testWrongVariableValueFormat()
    {
        when(variableContext.getVariable("wrongVar")).thenReturn("text value. Not csv");
        var tableProperties = new TableProperties("variableName=wrongVar", keywords, converters);
        var transformer = new CsvTableTransformer(CSVFormat.DEFAULT, variableContext);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
        assertEquals("Unable to create examples table based on 'wrongVar' variable value."
                + " Please check that value has proper csv format", exception.getMessage());
    }

    @SuppressWarnings("try")
    @Test
    void testCsvReaderReadCsvStringExceptionCatching()
    {
        var tableProperties = new TableProperties("variableName=myVar", keywords, converters);
        var variableValue = "some value";
        when(variableContext.getVariable("myVar")).thenReturn(variableValue);

        var ioException = new IOException();
        try (MockedConstruction<CsvReader> ignored = mockConstruction(CsvReader.class,
                (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(CSVFormat.DEFAULT), context.arguments());
                    when(mock.readCsvString(variableValue)).thenThrow(ioException);
                }))
        {
            var transformer = new CsvTableTransformer(CSVFormat.DEFAULT, variableContext);
            var exception = assertThrows(UncheckedIOException.class,
                    () -> transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
            assertException(exception, ioException);
        }
    }

    private static void assertException(UncheckedIOException exception, IOException expectedCause)
    {
        assertEquals("Problem during CSV data reading", exception.getMessage());
        assertEquals(expectedCause, exception.getCause());
    }
}
