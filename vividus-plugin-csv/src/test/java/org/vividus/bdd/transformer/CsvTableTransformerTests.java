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

package org.vividus.bdd.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.vividus.util.ResourceUtils.findResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Properties;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.csv.CsvReader;

@ExtendWith(MockitoExtension.class)
class CsvTableTransformerTests
{
    private static final String CSV_PATH_PROPERTY_NAME = "csvPath";
    private static final String CSV_FILE_NAME = "test.csv";
    private static final String EMPTY_EXAMPLES_TABLE = "";

    private final CsvTableTransformer csvTableTransformer = new CsvTableTransformer(new CsvReader());

    @Test
    void testTransform()
    {
        String expectedValue = "|Country|ID|Capital|Akey|\n"
                + "|Belarus|1|Minsk|11|\n"
                + "|USA|2|Washington|22|\n"
                + "|Armenia|3|Yerevan|33|";
        Properties properties = new Properties();
        properties.setProperty(CSV_PATH_PROPERTY_NAME, CSV_FILE_NAME);
        assertEquals(expectedValue,
                csvTableTransformer.transform(EMPTY_EXAMPLES_TABLE, null, new TableProperties(properties)));
    }

    @Test
    void testNoFilePathProvided()
    {
        Properties properties = new Properties();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                csvTableTransformer.transform(EMPTY_EXAMPLES_TABLE, null, new TableProperties(properties)));
        assertEquals("'csvPath' is not set in ExamplesTable properties", exception.getMessage());
    }

    @Test
    void testEmptyFilePathProvided()
    {
        Properties properties = new Properties();
        properties.setProperty(CSV_PATH_PROPERTY_NAME, "");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                csvTableTransformer.transform(EMPTY_EXAMPLES_TABLE, null, new TableProperties(properties)));
        assertEquals("ExamplesTable property 'csvPath' is blank", exception.getMessage());
    }

    @Test
    void testCsvFileReaderExceptionCatching() throws IOException
    {
        URL csvResource = findResource(getClass(), CSV_FILE_NAME);
        Properties properties = new Properties();
        properties.setProperty(CSV_PATH_PROPERTY_NAME, CSV_FILE_NAME);
        CsvReader csvReader = mock(CsvReader.class);
        CsvTableTransformer csvTableTransformer = new CsvTableTransformer(csvReader);
        when(csvReader.readCsvFile(csvResource)).thenThrow(new IOException());
        Throwable exception = assertThrows(UncheckedIOException.class, () ->
            csvTableTransformer.transform(EMPTY_EXAMPLES_TABLE, null, new TableProperties(properties)));
        assertEquals("Problem during CSV file reading", exception.getMessage());
    }
}
