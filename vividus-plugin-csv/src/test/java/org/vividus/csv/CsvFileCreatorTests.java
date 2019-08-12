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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CsvFileCreatorTests
{
    private static final String SUB_DIRECTORY = "subDirectory";
    private static final String NO_SUB_DIRECTORY = "";

    private static final String FILE_NAME = "fileName";
    private static final String[] HEADER = { "header1", "header2" };
    private static final String[] DATA = { "text1", "text2" };
    private static final String[] DATA_2 = { "text3", "text4" };

    private static String dataToCsvLine(String... data)
    {
        return String.join(",", data);
    }

    @ParameterizedTest
    @ValueSource(strings = { SUB_DIRECTORY, NO_SUB_DIRECTORY })
    void shouldCreateCsvFilesInAnyDirectory(String subDirectory, @TempDir Path outputDirectory) throws IOException
    {
        new CsvFileCreator(outputDirectory.toFile()).createCsvFiles(subDirectory, createCsvFileData(DATA));
        verifyCsvFile(outputDirectory, subDirectory, List.of(dataToCsvLine(HEADER), dataToCsvLine(DATA)));
    }

    @Test
    void shouldCreateCsvFilesWithAppendToNonExistingFile(@TempDir Path outputDirectory) throws IOException
    {
        CsvFileCreator csvFileCreator = new CsvFileCreator(outputDirectory.toFile());
        String subDirectory = NO_SUB_DIRECTORY;
        csvFileCreator.createCsvFiles(subDirectory, createCsvFileData(DATA), true);
        verifyCsvFile(outputDirectory, subDirectory, List.of(dataToCsvLine(HEADER), dataToCsvLine(DATA)));
    }

    @Test
    void shouldCreateCsvFilesWithAppend(@TempDir Path outputDirectory) throws IOException
    {
        CsvFileCreator csvFileCreator = new CsvFileCreator(outputDirectory.toFile());
        String subDirectory = NO_SUB_DIRECTORY;
        csvFileCreator.createCsvFiles(subDirectory, createCsvFileData(DATA), false);
        List<CsvFileData> csvFileData2 = createCsvFileData(DATA_2);
        csvFileCreator.createCsvFiles(subDirectory, csvFileData2, true);
        verifyCsvFile(outputDirectory, subDirectory,
                List.of(dataToCsvLine(HEADER), dataToCsvLine(DATA), dataToCsvLine(DATA_2)));
    }

    @Test
    void shouldCreateCsvFilesWithOverwrite(@TempDir Path outputDirectory) throws IOException
    {
        CsvFileCreator csvFileCreator = new CsvFileCreator(outputDirectory.toFile());
        String subDirectory = NO_SUB_DIRECTORY;
        csvFileCreator.createCsvFiles(subDirectory, createCsvFileData(DATA), false);
        List<CsvFileData> csvFileData2 = createCsvFileData(DATA_2);
        csvFileCreator.createCsvFiles(subDirectory, csvFileData2, false);
        verifyCsvFile(outputDirectory, subDirectory, List.of(dataToCsvLine(HEADER), dataToCsvLine(DATA_2)));
    }

    private List<CsvFileData> createCsvFileData(String... data)
    {
        CsvFileData csvFileData = new CsvFileData();
        csvFileData.setFileName(FILE_NAME);
        csvFileData.setHeader(HEADER);
        csvFileData.setData(List.<Object[]>of(data));

        return List.of(csvFileData);
    }

    private void verifyCsvFile(Path outputDirectory, String subDirectory, List<String> expectedLines) throws IOException
    {
        Path csvFile = outputDirectory.resolve(subDirectory).resolve(FILE_NAME);
        assertEquals(expectedLines, Files.readAllLines(csvFile));
    }
}
