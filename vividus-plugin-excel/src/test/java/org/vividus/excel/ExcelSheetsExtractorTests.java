/*
 * Copyright 2019-2022 the original author or authors.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class ExcelSheetsExtractorTests
{
    private static final String TEMPLATE_PATH = "/TestTemplate.xlsx";

    private static final int EXPECTED_SHEETS_COUNT = 4;

    @Test
    void testGetSheetAtNumberSuccess() throws WorkbookParsingException, IOException
    {
        try (var inputStream = getClass().getResourceAsStream(TEMPLATE_PATH))
        {
            var excelSheetsExtractor = new ExcelSheetsExtractor(inputStream.readAllBytes());
            assertEquals(EXPECTED_SHEETS_COUNT, excelSheetsExtractor.getSheets().size());
            Optional<Sheet> sheetOpt = excelSheetsExtractor.getSheet(0);
            assertTrue(sheetOpt.isPresent());
            assertThat("Mapping", equalTo(sheetOpt.get().getSheetName()));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void testGetSheetAtNumberOutOfRange(int number) throws WorkbookParsingException, IOException
    {
        try (var inputStream = getClass().getResourceAsStream(TEMPLATE_PATH))
        {
            var excelSheetsExtractor = new ExcelSheetsExtractor(inputStream.readAllBytes());
            Optional<Sheet> sheetOpt = excelSheetsExtractor.getSheet(EXPECTED_SHEETS_COUNT + number);
            assertFalse(sheetOpt.isPresent());
        }
    }

    @Test
    void testGetSheetByName() throws WorkbookParsingException, IOException
    {
        try (var inputStream = getClass().getResourceAsStream(TEMPLATE_PATH))
        {
            var excelSheetsExtractor = new ExcelSheetsExtractor(inputStream.readAllBytes());
            assertAll(
                () -> {
                    Optional<Sheet> sheet = excelSheetsExtractor.getSheet("AsString");
                    assertTrue(sheet.isPresent());
                },
                () -> {
                    Optional<Sheet> sheet = excelSheetsExtractor.getSheet("Nonexistent");
                    assertFalse(sheet.isPresent());
                }
            );
        }
    }

    @Test
    void testGetSheetsWithNames() throws WorkbookParsingException, IOException
    {
        try (var inputStream = getClass().getResourceAsStream(TEMPLATE_PATH))
        {
            var excelSheetsExtractor = new ExcelSheetsExtractor(inputStream.readAllBytes());
            Map<String, Sheet> actualMap = excelSheetsExtractor.getSheetsWithNames();
            actualMap.forEach((key, value) -> assertThat(key, equalTo(value.getSheetName())));
        }
    }

    static List<Class<? extends Throwable>> exceptionDataProvider()
    {
        return List.of(EncryptedDocumentException.class, IOException.class);
    }

    @ParameterizedTest
    @ValueSource(classes = { EncryptedDocumentException.class, IOException.class })
    void testCreateFromBytesException(Class<? extends Throwable> exceptionClazz)
    {
        try (MockedStatic<WorkbookFactory> workbookFactory = Mockito.mockStatic(WorkbookFactory.class))
        {
            workbookFactory.when(() -> WorkbookFactory.create(any(ByteArrayInputStream.class))).thenThrow(
                    exceptionClazz);
            var workbookParsingException = assertThrows(WorkbookParsingException.class,
                    () -> new ExcelSheetsExtractor(new byte[0]));
            assertEquals("Unable to parse workbook", workbookParsingException.getMessage());
            assertThat(workbookParsingException.getCause(), instanceOf(exceptionClazz));
        }
    }
}
