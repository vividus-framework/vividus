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

package org.vividus.csv;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;

public class CsvFileCreator
{
    private final CSVFormat csvFormat;
    private final File outputDirectory;

    public CsvFileCreator(File outputDirectory)
    {
        this(CSVFormat.DEFAULT, outputDirectory);
    }

    public CsvFileCreator(CSVFormat csvFormat, File outputDirectory)
    {
        this.csvFormat = csvFormat;
        this.outputDirectory = outputDirectory;
    }

    public void createCsvFiles(String subDirectory, List<CsvFileData> csvFileData) throws IOException
    {
        createCsvFiles(subDirectory, csvFileData, false);
    }

    public void createCsvFiles(String subDirectory, List<CsvFileData> csvFileData, boolean append) throws IOException
    {
        File directory = createDirectory(subDirectory);
        for (CsvFileData csvData : csvFileData)
        {
            createCsvFile(directory, csvData, append);
        }
    }

    private void createCsvFile(File directory, CsvFileData csvData, boolean append) throws IOException
    {
        File file = new File(directory, csvData.getFileName());
        boolean fileExists = file.exists();
        OpenOption[] openOptions = append ? new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.APPEND }
                : new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath(), openOptions),
                StandardCharsets.UTF_8);
                CSVPrinter printer = append && fileExists ? csvFormat.print(writer)
                        : csvFormat.builder().setHeader(csvData.getHeader()).build().print(writer))
        {
            printer.printRecords(csvData.getData());
        }
    }

    private File createDirectory(String subDirectory) throws IOException
    {
        File directory = new File(outputDirectory, subDirectory);
        if (!directory.exists())
        {
            FileUtils.forceMkdir(directory);
        }
        return directory;
    }
}
