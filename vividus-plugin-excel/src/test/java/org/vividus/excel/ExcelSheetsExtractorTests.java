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

package org.vividus.excel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.vividus.util.ResourceUtils;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(DataProviderRunner.class)
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*",
        "org.xml.*", "org.w3c.dom.*" })
public class ExcelSheetsExtractorTests
{
    private static final String TEMPLATE_PATH = "/TestTemplate.xlsx";

    private static final String EXCEPTION_MSG = "Unable to parse workbook";

    private static final int EXPECTED_SHEETS_COUNT = 3;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testGetSheetsFromFile() throws WorkbookParsingException
    {
        IExcelSheetsExtractor excelSheetsExtractor = new ExcelSheetsExtractor(TEMPLATE_PATH);
        assertThat(excelSheetsExtractor.getSheets().size(), Matchers.equalTo(EXPECTED_SHEETS_COUNT));
    }

    @Test
    public void testGetSheetsFromBytes() throws WorkbookParsingException, IOException
    {
        File excelFile = ResourceUtils.loadFile(this.getClass(), TEMPLATE_PATH);
        IExcelSheetsExtractor excelSheetsExtractor = new ExcelSheetsExtractor(Files.readAllBytes(excelFile.toPath()));
        assertThat(excelSheetsExtractor.getSheets().size(), Matchers.equalTo(EXPECTED_SHEETS_COUNT));
    }

    @Test
    public void testGetSheetAtNumberSuccess() throws WorkbookParsingException
    {
        IExcelSheetsExtractor excelSheetsExtractor = new ExcelSheetsExtractor(TEMPLATE_PATH);
        Optional<Sheet> sheetOpt = excelSheetsExtractor.getSheet(0);
        assertTrue(sheetOpt.isPresent());
        assertThat("Mapping", Matchers.equalTo(sheetOpt.get().getSheetName()));
    }

    @Test
    public void testGetSheetAtNumberOutOfRange() throws WorkbookParsingException
    {
        IExcelSheetsExtractor excelSheetsExtractor = new ExcelSheetsExtractor(TEMPLATE_PATH);
        Optional<Sheet> sheetOpt = excelSheetsExtractor.getSheet(EXPECTED_SHEETS_COUNT + 1);
        assertFalse(sheetOpt.isPresent());
    }

    @Test
    public void testGetSheetByNameSuccess() throws WorkbookParsingException
    {
        IExcelSheetsExtractor excelSheetsExtractor = new ExcelSheetsExtractor(TEMPLATE_PATH);
        Optional<Sheet> sheetOpt = excelSheetsExtractor.getSheet("AsString");
        assertTrue(sheetOpt.isPresent());
    }

    @Test
    public void testGetSheetByNameNotExisted() throws WorkbookParsingException
    {
        IExcelSheetsExtractor excelSheetsExtractor = new ExcelSheetsExtractor(TEMPLATE_PATH);
        Optional<Sheet> sheetOpt = excelSheetsExtractor.getSheet("Taxonomies");
        assertFalse(sheetOpt.isPresent());
    }

    @Test
    public void testGetSheetsWithNames() throws WorkbookParsingException
    {
        IExcelSheetsExtractor excelSheetsExtractor = new ExcelSheetsExtractor(TEMPLATE_PATH);
        Map<String, Sheet> actualMap = excelSheetsExtractor.getSheetsWithNames();
        actualMap.forEach((key, value) -> assertThat(key, Matchers.equalTo(value.getSheetName())));
    }

    @DataProvider
    public static Object[][] exceptionDataProvider()
    {
        // @formatter:off
        return new Object[][] {
            { EncryptedDocumentException.class },
            { IOException.class }};
        // @formatter:on
    }

    @PrepareForTest({ WorkbookFactory.class, ResourceUtils.class, ExcelSheetsExtractor.class })
    @Test
    @UseDataProvider("exceptionDataProvider")
    public void testCreateFromFileException(Class<? extends Throwable> exceptionClazz)
            throws WorkbookParsingException, IOException
    {
        prepareExceptionTest(exceptionClazz);
        PowerMockito.when(WorkbookFactory.create(ArgumentMatchers.any(File.class))).thenThrow(exceptionClazz);
        new ExcelSheetsExtractor(TEMPLATE_PATH);
    }

    @PrepareForTest({ WorkbookFactory.class, ResourceUtils.class, ExcelSheetsExtractor.class })
    @Test
    @UseDataProvider("exceptionDataProvider")
    public void testCreateFromBytesException(Class<? extends Throwable> exceptionClazz)
            throws WorkbookParsingException, IOException
    {
        prepareExceptionTest(exceptionClazz);
        PowerMockito.when(WorkbookFactory.create(ArgumentMatchers.any(ByteArrayInputStream.class)))
                .thenThrow(exceptionClazz);
        new ExcelSheetsExtractor(new byte[0]);
    }

    private void prepareExceptionTest(Class<? extends Throwable> exceptionClazz)
    {
        PowerMockito.mockStatic(WorkbookFactory.class);
        expectedException.expect(WorkbookParsingException.class);
        expectedException.expectMessage(EXCEPTION_MSG);
        expectedException.expectCause(Matchers.instanceOf(exceptionClazz));
    }
}
