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

package org.vividus.bdd.expression;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.vividus.csv.CsvReader;

class ConvertCsvToParquetFileExpressionProcessorTests
{
    private final ConvertCsvToParquetFileExpressionProcessor processor =
            new ConvertCsvToParquetFileExpressionProcessor(new CsvReader());

    @ParameterizedTest
    @ValueSource(strings = {
            "/path/test.csv",
            "test.csv"
    })
    void testConvertCsvToParquetFileExpression(String csvFilePath) throws IOException
    {
        String expression = String.format("convertCsvToParquetFile(%s, /schemas/test.avsc)", csvFilePath);
        String parquetPath = processor.execute(expression).get();
        assertThat(parquetPath, matchesPattern(".*test.*parquet"));
        GenericRecord actualRecord = readActualRecord(parquetPath);
        assertEquals("value1", actualRecord.get("field1").toString());
        assertEquals("value2", actualRecord.get("field2").toString());
    }

    private GenericRecord readActualRecord(String parquetPath) throws IOException
    {
        try (ParquetReader<GenericRecord> reader = AvroParquetReader
                .<GenericRecord>builder(
                        HadoopInputFile.fromPath(new Path(new File(parquetPath).toURI()), new Configuration()))
                .build())
        {
            return reader.read();
        }
    }

    @Test
    void testConvertCsvToParquetFileExpressionNotMatched()
    {
        assertEquals(processor.execute("convertCsvToParquet(test.csv, /schemas/test.avsc)"), Optional.empty());
    }

    @Test
    void testConvertCsvToParquetFileExpressionException()
    {
        UncheckedIOException exception = assertThrows(UncheckedIOException.class,
            () -> processor.execute("convertCsvToParquetFile(path/testIO.csv, /schemas/test.avsc)"));
        assertEquals("Problem during file interaction: ", exception.getMessage());
    }
}
