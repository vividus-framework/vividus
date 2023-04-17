/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.parquet.expression;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.util.HadoopOutputFile;
import org.jbehave.core.expressions.BiArgExpressionProcessor;
import org.vividus.csv.CsvReader;
import org.vividus.util.ResourceUtils;

public class ConvertCsvToParquetFileExpressionProcessor extends BiArgExpressionProcessor<String>
{
    public ConvertCsvToParquetFileExpressionProcessor(CsvReader csvReader)
    {
        super("convertCsvToParquetFile", (csvPath, schemaPath) -> createParquetFile(csvReader, csvPath, schemaPath));
    }

    private static String createParquetFile(CsvReader csvReader, String csvPath, String schemaPath)
    {
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

    private static void write(File file, String avroSchemaPath, List<Map<String, String>> data) throws IOException
    {
        Schema schema = new Parser().parse(ResourceUtils.loadResource(avroSchemaPath));
        try (ParquetWriter<GenericRecord> writer = AvroParquetWriter
                .<GenericRecord>builder(HadoopOutputFile.fromPath(new Path(file.toURI()), new Configuration()))
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
