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

package org.vividus.excel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.vividus.util.DateUtils;
import org.vividus.util.ResourceUtils;

class ExcelSheetWriterTests
{
    private static final ExamplesTable CONTENT = new ExamplesTable("""
            |name|status|name|
            |First |OPEN   |MoreFirst|
            |Second|closed|MoreSecond|""");
    private static final String NAME = "name";
    private static final List<List<String>> EXPECTED_DATA = List.of(
            List.of(NAME, "status", NAME),
            List.of("First", "OPEN", "MoreFirst"),
            List.of("Second", "closed", "MoreSecond")
    );
    private static final String TEST_SHEET_NAME = "test_sheet_name";

    private static final ExcelSheetWriter EXCEL_SHEET_WRITER = new ExcelSheetWriter(
            new DateUtils(ZoneId.systemDefault()));

    @CsvSource({
        "User-defined sheet name, User-defined sheet name",
        ", Sheet0"
    })
    @ParameterizedTest
    void shouldCreateExcel(String inputSheetName, String actualSheetName) throws IOException
    {
        Path pathTemp = createExcelFile();
        EXCEL_SHEET_WRITER.createExcel(pathTemp, Optional.ofNullable(inputSheetName), CONTENT);
        assertDataInSheet(pathTemp, 0, actualSheetName, EXPECTED_DATA);
    }

    @Test
    void shouldCreateExcelWithDifferentTypes() throws IOException
    {
        // CHECKSTYLE:OFF
        ExamplesTable content = new ExamplesTable("""
        |numeric                                       |formula                              |date                                                                              |boolean                         |#{withColumnCellsType(date, multi-type col)} |
        |#{withCellType(Numeric, 100500 format #.000)} |#{withCellType(formula, DOLLAR(A2))} |#{withCellType(DATE,  03/31/2024)}                                                |#{withCellType(BOOLEAN, true)}  |28-Feb-2024 input dd-MMM-yyyy                |
        |#{withCellType(Numeric, 99500)}               |#{withCellType(formula,A2+A3)}       |#{withCellType(DATE, 31-Jan-2024 input dd-MMM-yyyy)}                              |#{withCellType(BOOLEAN, false)} |#{withCellType(Numeric, 100500)}             |
        |                                              |                                     |#{withCellType(DATE, 03/31/2024 format m.d.yyyy h:mm)}                            |                                |#{withCellType(String, 128)}                 |
        |                                              |                                     |#{withCellType(DATE, 2024/03/31 13:04 input yyyy/MM/dd HH:mm format m.d.yy h:mm)} |                                |#{withCellType(String, Empty)}               |
        |                                              |                                     |#{withCellType(DATE, 2024,03,31 format m.d.yy h:mm input yyyy,MM,dd)}             |                                |#{withCellType(String, not allowed)}         |
        """);
        // CHECKSTYLE:ON

        Path pathTemp = createExcelFile();
        EXCEL_SHEET_WRITER.createExcel(pathTemp, Optional.of(TEST_SHEET_NAME), content);

        List<List<String>> expectedData = List.of(
                List.of("numeric",    "formula",      "date",             "boolean",   "multi-type col"),
                List.of("100500.000", "$100,500.00",  "3/31/24",          "TRUE",      "2/28/24"),
                List.of("99500.0",    "200000",       "1/31/24",          "FALSE",     "100500.0"),
                List.of("",           "",             "3.31.2024 0:00",   "",          "128"),
                List.of("",           "",             "3.31.24 13:04",    "",          "Empty"),
                List.of("",           "",             "3.31.24 0:00",     "",          "not allowed")
        );
        assertDataInSheet(pathTemp, 0, TEST_SHEET_NAME, expectedData);
    }

    @ValueSource(strings = { "date", "numeric" })
    @ParameterizedTest
    void shouldThrowExceptionIfValueOfTypeNotSpecified(String dataType) throws IOException
    {
        Path pathTemp = createExcelFile();
        var exception = assertThrows(IllegalArgumentException.class,
                () -> EXCEL_SHEET_WRITER.createExcel(pathTemp, Optional.of(TEST_SHEET_NAME),
                        new ExamplesTable(String.format("|#{withColumnCellsType(%s, name,value)}|%n||", dataType))));
        assertEquals(String.format("The %s value isn't specified", dataType), exception.getMessage());
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
        EXCEL_SHEET_WRITER.addSheetToExcel(pathTemp, sheetA, CONTENT);
        String sheetB = "sheet b";
        EXCEL_SHEET_WRITER.addSheetToExcel(pathTemp, sheetB, CONTENT);

        assertDataInSheet(pathTemp, 0, sheetA, EXPECTED_DATA);
        assertDataInSheet(pathTemp, 1, sheetB, EXPECTED_DATA);
    }

    private Path createExcelFile() throws IOException
    {
        return ResourceUtils.createTempFile("test", ".xlsx", null);
    }

    @SuppressWarnings({ "checkstyle:MultipleStringLiterals", "checkstyle:MultipleStringLiteralsExtended" })
    private void assertDataInSheet(Path path, int index, String name, List<List<String>> expectedData)
            throws IOException
    {
        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(FileUtils.openInputStream(new File(path.toString()))))
        {
            XSSFSheet sheet = myExcelBook.getSheetAt(index);
            assertEquals(sheet.getSheetName(), name);
            IExcelSheetParser sheetParser = new ExcelSheetParser(sheet, true);
            List<List<String>> actualData = sheetParser.getData();
            assertEquals(expectedData, actualData);
        }
    }
}
