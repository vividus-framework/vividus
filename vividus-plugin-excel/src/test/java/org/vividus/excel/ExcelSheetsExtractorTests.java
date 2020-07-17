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

package org.vividus.excel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.Mockito;
import org.vividus.util.ResourceUtils;

class ExcelSheetsExtractorTests
{
    private static final String TEMPLATE_PATH = "/TestTemplate.xlsx";

    private static final int EXPECTED_SHEETS_COUNT = 3;

    @Test
    void testGetSheetsFromFile() throws WorkbookParsingException
    {
        IExcelSheetsExtractor excelSheetsExtractor = new ExcelSheetsExtractor(TEMPLATE_PATH);
        assertThat(excelSheetsExtractor.getSheets().size(), Matchers.equalTo(EXPECTED_SHEETS_COUNT));
    }

    @Test
    void testGetSheetsFromBytes() throws WorkbookParsingException, IOException
    {
        File excelFile = ResourceUtils.loadFile(this.getClass(), TEMPLATE_PATH);
        IExcelSheetsExtractor excelSheetsExtractor = new ExcelSheetsExtractor(Files.readAllBytes(excelFile.toPath()));
        assertThat(excelSheetsExtractor.getSheets().size(), Matchers.equalTo(EXPECTED_SHEETS_COUNT));
    }

    @Test
    void testGetSheetAtNumberSuccess() throws WorkbookParsingException
    {
        IExcelSheetsExtractor excelSheetsExtractor = new ExcelSheetsExtractor(TEMPLATE_PATH);
        Optional<Sheet> sheetOpt = excelSheetsExtractor.getSheet(0);
        assertTrue(sheetOpt.isPresent());
        assertThat("Mapping", Matchers.equalTo(sheetOpt.get().getSheetName()));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void testGetSheetAtNumberOutOfRange(int number) throws WorkbookParsingException
    {
        IExcelSheetsExtractor excelSheetsExtractor = new ExcelSheetsExtractor(TEMPLATE_PATH);
        Optional<Sheet> sheetOpt = excelSheetsExtractor.getSheet(EXPECTED_SHEETS_COUNT + number);
        assertFalse(sheetOpt.isPresent());
    }

    @Test
    void testGetSheetByNameSuccess() throws WorkbookParsingException
    {
        IExcelSheetsExtractor excelSheetsExtractor = new ExcelSheetsExtractor(TEMPLATE_PATH);
        Optional<Sheet> sheetOpt = excelSheetsExtractor.getSheet("AsString");
        assertTrue(sheetOpt.isPresent());
    }

    @Test
    void testGetSheetByNameNotExisted() throws WorkbookParsingException
    {
        IExcelSheetsExtractor excelSheetsExtractor = new ExcelSheetsExtractor(TEMPLATE_PATH);
        Optional<Sheet> sheetOpt = excelSheetsExtractor.getSheet("Taxonomies");
        assertFalse(sheetOpt.isPresent());
    }

    @Test
    void testGetSheetsWithNames() throws WorkbookParsingException
    {
        IExcelSheetsExtractor excelSheetsExtractor = new ExcelSheetsExtractor(TEMPLATE_PATH);
        Map<String, Sheet> actualMap = excelSheetsExtractor.getSheetsWithNames();
        actualMap.forEach((key, value) -> assertThat(key, Matchers.equalTo(value.getSheetName())));
    }

    static Stream<Class<? extends Throwable>> exceptionDataProvider()
    {
        return Stream.of(EncryptedDocumentException.class, IOException.class);
    }

    @ParameterizedTest
    @MethodSource("exceptionDataProvider")
    void testCreateFromFileException(Class<? extends Throwable> exceptionClazz)
    {
        assertWorkbookParsingException(() -> WorkbookFactory.create(ArgumentMatchers.any(File.class)), exceptionClazz,
            () -> new ExcelSheetsExtractor(TEMPLATE_PATH));
    }

    @ParameterizedTest
    @MethodSource("exceptionDataProvider")
    void testCreateFromBytesException(Class<? extends Throwable> exceptionClazz)
    {
        assertWorkbookParsingException(() -> WorkbookFactory.create(ArgumentMatchers.any(ByteArrayInputStream.class)),
                exceptionClazz, () -> new ExcelSheetsExtractor(new byte[0]));
    }

    private void assertWorkbookParsingException(Verification staticMethodMock,
            Class<? extends Throwable> exceptionClazz, Executable executable)
    {
        try (MockedStatic<WorkbookFactory> workbookFactory = Mockito.mockStatic(WorkbookFactory.class))
        {
            workbookFactory.when(staticMethodMock).thenThrow(exceptionClazz);
            WorkbookParsingException workbookParsingException = assertThrows(WorkbookParsingException.class,
                    executable);
            assertEquals("Unable to parse workbook", workbookParsingException.getMessage());
            assertThat(workbookParsingException.getCause(), instanceOf(exceptionClazz));
        }
    }
}
