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

package org.vividus.excel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vividus.util.ResourceUtils;

class ExcelSheetWriterTests
{
    private static final ExamplesTable CONTENT = new ExamplesTable("|name|status|name|\n"
            + "|First |OPEN   |MoreFirst|\n"
            + "|Second|closed|MoreSecond|");

    @CsvSource({
        "User-defined sheet name, User-defined sheet name",
        ", Sheet0"
    })
    @ParameterizedTest
    void shouldCreateExcel(String inputSheetName, String actualSheetName) throws IOException
    {
        Path pathTemp = createExcelFile();
        ExcelSheetWriter.createExcel(pathTemp, Optional.ofNullable(inputSheetName), CONTENT);
        assertDataInSheet(pathTemp, 0, actualSheetName);
    }

    @Test
    void shouldAddSheetToExcel() throws IOException
    {
        Path pathTemp = createExcelFile();
        try (XSSFWorkbook workbook = new XSSFWorkbook(); OutputStream os = Files.newOutputStream(pathTemp))
        {
            workbook.write(os);
        }

        String sheetA = "sheet a";
        ExcelSheetWriter.addSheetToExcel(pathTemp, sheetA, CONTENT);
        String sheetB = "sheet b";
        ExcelSheetWriter.addSheetToExcel(pathTemp, sheetB, CONTENT);

        assertDataInSheet(pathTemp, 0, sheetA);
        assertDataInSheet(pathTemp, 1, sheetB);
    }

    private Path createExcelFile() throws IOException
    {
        return ResourceUtils.createTempFile("test", ".xlsx", null);
    }

    @SuppressWarnings({ "checkstyle:MultipleStringLiterals", "checkstyle:MultipleStringLiteralsExtended" })
    private void assertDataInSheet(Path path, int index, String name) throws IOException
    {
        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(FileUtils.openInputStream(new File(path.toString()))))
        {
            XSSFSheet sheet = myExcelBook.getSheetAt(index);
            assertEquals(sheet.getSheetName(), name);
            IExcelSheetParser sheetParser = new ExcelSheetParser(sheet);
            List<List<String>> actualData = sheetParser.getData();
            List<List<String>> expectedData = List.of(
                    List.of("name", "status", "name"),
                    List.of("First", "OPEN", "MoreFirst"),
                    List.of("Second", "closed", "MoreSecond")
            );
            assertEquals(expectedData, actualData);
        }
    }
}
