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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.vividus.csv.CsvReader;
import org.vividus.util.ResourceUtils;

public class ConvertCsvToParquetFileExpressionProcessor extends AbstractExpressionProcessor<String>
{
    private static final Pattern CONVERT_CSV_TO_PARQUET_PATTERN = Pattern
            .compile("^convertCsvToParquetFile\\((.+), (.+)\\)$");

    private static final int CSV_PATH_GROUP = 1;
    private static final int SCHEMA_PATH_GROUP = 2;

    private final CsvReader csvReader;

    public ConvertCsvToParquetFileExpressionProcessor(CsvReader csvReader)
    {
        super(CONVERT_CSV_TO_PARQUET_PATTERN);
        this.csvReader = csvReader;
    }

    @Override
    protected String evaluateExpression(Matcher expressionMatcher)
    {
        String csvPath = expressionMatcher.group(CSV_PATH_GROUP);
        String schemaPath = expressionMatcher.group(SCHEMA_PATH_GROUP);

        try
        {
            List<Map<String, String>> csvData = csvReader.readCsvString(ResourceUtils.loadResource(csvPath));
            File temporaryFile = ResourceUtils.createTempFile(FilenameUtils.getBaseName(csvPath), ".parquet", null)
                    .toFile();
            write(temporaryFile, schemaPath, csvData);
            return temporaryFile.getPath();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException("Problem during file interaction: ", e);
        }
    }

    private void write(File file, String avroSchemaPath, List<Map<String, String>> data) throws IOException
    {
        Schema schema = new Parser().parse(ResourceUtils.loadResource(avroSchemaPath));
        try (ParquetWriter<GenericRecord> writer = AvroParquetWriter
                .<GenericRecord>builder(new Path(file.toURI()))
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .withDataModel(GenericData.get())
                .withSchema(schema)
                .build())
        {
            for (Map<String, String> map : data)
            {
                GenericRecord record = new GenericData.Record(schema);
                map.forEach(record::put);
                writer.write(record);
            }
        }
    }
}
