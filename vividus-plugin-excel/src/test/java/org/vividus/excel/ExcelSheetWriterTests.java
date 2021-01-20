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

package org.vividus.excel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.vividus.util.ResourceUtils;

class ExcelSheetWriterTests
{
    @Test
    void testCreateExcel() throws IOException
    {
        IExcelSheetParser sheetParser;
        Path pathTemp = ResourceUtils.createTempFile("test", ".xlsx", null);
        ExamplesTable content = new ExamplesTable("|name|status|\n|First|OPEN|\n|Second|closed|");
        ExcelSheetWriter.createExcel(pathTemp, content);
        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(FileUtils.openInputStream(new File(pathTemp.toString()))))
        {
            XSSFSheet sheet = myExcelBook.getSheetAt(0);
            sheetParser = new ExcelSheetParser(sheet);
            Map<String, List<String>> actualData = sheetParser.getDataAsTable("A1:B3");
            Map<String, List<String>> expectedData = new HashMap<>();
            expectedData.put("name", List.of("First", "Second"));
            expectedData.put("status", List.of("OPEN", "closed"));
            assertEquals(expectedData, actualData);
        }
    }
}
