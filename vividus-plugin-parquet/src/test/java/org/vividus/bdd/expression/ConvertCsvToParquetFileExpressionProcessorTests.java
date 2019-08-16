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

package org.vividus.bdd.expression;

import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.vividus.util.ResourceUtils;

public class ConvertCsvToParquetFileExpressionProcessorTests
{
    private final ConvertCsvToParquetFileExpressionProcessor processor =
            new ConvertCsvToParquetFileExpressionProcessor();

    @ParameterizedTest
    @ValueSource(strings = {
            "convertCsvToParquetFile(/path/test.csv, /schemas/test.avsc)",
            "convertCsvToParquetFile(test.csv, /schemas/test.avsc)"
    })
    void testGenerateParquetExpression(String input) throws IOException
    {
        String parquetPath = processor.execute(input).get();
        assertTrue(matchesPattern(".*test.*parquet").matches(parquetPath));
        byte[] expected = ResourceUtils.loadResourceAsByteArray("expected.parquet");
        assertArrayEquals(expected, FileUtils.readFileToByteArray(new File(parquetPath)));
    }

    @Test
    void testGenerateParquetExpressionNotMatched()
    {
        assertEquals(processor.execute("convertCsvToParquet(test.csv, /schemas/test.avsc)"), Optional.empty());
    }

    @Test
    void testGenerateParquetExpressionException()
    {
        UncheckedIOException exception = assertThrows(UncheckedIOException.class,
            () -> processor.execute("convertCsvToParquetFile(path/testIO.csv, /schemas/test.avsc)"));
        assertEquals("Problem during file interaction: ", exception.getMessage());
    }
}
