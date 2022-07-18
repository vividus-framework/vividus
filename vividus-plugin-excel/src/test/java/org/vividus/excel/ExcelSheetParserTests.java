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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vividus.model.CellValue;

class ExcelSheetParserTests
{
    private static final String PRICE1 = "Price1";
    private static final String PROD1 = "Product1";
    private static final String PRICE2 = "Price2";
    private static final String PROD2 = "Product2";
    private static final String TITLE_KEY_PRODUCT = "Product Title";
    private static final String TITLE_KEY_PRICE = "Price";
    private static final String AS_STRING_SHEET = "AsString";
    private static final String SHEET_NAME = "RepeatingData";
    private static final String OPEN_STATUS = "OPEN";
    private static final String NAME = "name";
    private static final String ONE_AS_STRING = "1.0";
    private static final String TWO_AS_STRING = "2.0";
    private static final String THREE_AS_STRING = "3.0";
    private static final String TRUE_AS_STRING = String.valueOf(true);
    private static final String FALSE_AS_STRING = String.valueOf(false);

    private static final int TITLE_ROW_NUMBER = 2;

    private Sheet mappingSheet;
    private IExcelSheetsExtractor extractor;
    private IExcelSheetParser sheetParser;
    private List<String> expectedTitleRowData;

    @BeforeEach
    void beforeEach() throws WorkbookParsingException, IOException
    {
        try (var inputStream = getClass().getResourceAsStream("/TestTemplate.xlsx"))
        {
            extractor = new ExcelSheetsExtractor(inputStream.readAllBytes());
        }
        mappingSheet = extractor.getSheet("Mapping").get();
        sheetParser = new ExcelSheetParser(mappingSheet, true);

        expectedTitleRowData = new LinkedList<>();
        expectedTitleRowData.add(TITLE_KEY_PRODUCT);
        expectedTitleRowData.add(TITLE_KEY_PRICE);
    }

    @Test
    void testGetSheet()
    {
        assertEquals(mappingSheet, sheetParser.getSheet());
    }

    @Test
    void testGetRowTest()
    {
        var actualRowData = sheetParser.getRow(TITLE_ROW_NUMBER);
        assertEquals(expectedTitleRowData, actualRowData);
    }

    @Test
    void testGetGetAllData()
    {
        var actualData = sheetParser.getData();
        assertEquals(5, actualData.size());
        assertEquals(expectedTitleRowData, actualData.get(TITLE_ROW_NUMBER));
    }

    @Test
    void testGetGetDataFrom()
    {
        var actualData = sheetParser.getData(4);
        List<String> expectedRowData = new LinkedList<>();
        expectedRowData.add(PROD2);
        expectedRowData.add(PRICE2);
        assertEquals(1, actualData.size());
        assertEquals(expectedRowData, actualData.get(0));
    }

    @Test
    void testGetGetDataFromWithSkipBottom()
    {
        var actualData = sheetParser.getData(2, 2);
        assertEquals(1, actualData.size());
        assertEquals(expectedTitleRowData, actualData.get(0));
    }

    @Test
    void testGetDataMapWithTitle()
    {
        List<Map<String, String>> expectedData = new LinkedList<>();
        Map<String, String> rowMap1 = new LinkedHashMap<>();
        rowMap1.put(TITLE_KEY_PRODUCT, PROD1);
        rowMap1.put(TITLE_KEY_PRICE, PRICE1);
        Map<String, String> rowMap2 = new LinkedHashMap<>();
        rowMap2.put(TITLE_KEY_PRODUCT, PROD2);
        rowMap2.put(TITLE_KEY_PRICE, PRICE2);
        expectedData.add(rowMap1);
        expectedData.add(rowMap2);

        var actualData = sheetParser.getDataWithTitle(TITLE_ROW_NUMBER);
        assertEquals(expectedData, actualData);
    }

    @Test
    void testGetDataMapWithTitleWithSkipBottom()
    {
        List<Map<String, String>> expectedData = new LinkedList<>();
        Map<String, String> rowMap1 = new LinkedHashMap<>();
        rowMap1.put(TITLE_KEY_PRODUCT, PROD1);
        rowMap1.put(TITLE_KEY_PRICE, PRICE1);
        expectedData.add(rowMap1);

        var actualData = sheetParser.getDataWithTitle(TITLE_ROW_NUMBER, 1);
        assertEquals(expectedData, actualData);
    }

    @Test
    void testTrimFalse()
    {
        sheetParser = new ExcelSheetParser(mappingSheet);
        List<Map<String, String>> expectedData = new LinkedList<>();
        Map<String, String> rowMap1 = new LinkedHashMap<>();
        rowMap1.put(TITLE_KEY_PRODUCT, "Product1 ");
        rowMap1.put(TITLE_KEY_PRICE, "Price1 ");
        expectedData.add(rowMap1);

        var actualData = sheetParser.getDataWithTitle(TITLE_ROW_NUMBER, 1);
        assertEquals(expectedData, actualData);
    }

    @Test
    void testGetCellValueWithDiffTypes()
    {
        List<List<String>> expectedStringData = new LinkedList<>();
        List<String> row1 = new LinkedList<>();
        row1.add(TRUE_AS_STRING);
        row1.add(ONE_AS_STRING);
        expectedStringData.add(row1);
        List<String> row2 = new LinkedList<>();
        row2.add(FALSE_AS_STRING);
        row2.add(TWO_AS_STRING);
        expectedStringData.add(row2);
        List<String> row3 = new LinkedList<>();
        row3.add(THREE_AS_STRING);
        expectedStringData.add(row3);

        sheetParser = new ExcelSheetParser(extractor.getSheet(AS_STRING_SHEET).get());
        var data = sheetParser.getData();
        assertEquals(expectedStringData, data);
    }

    @Test
    void testGetCellValuePreservingFormatting()
    {
        List<List<String>> expectedStringData = new LinkedList<>();
        List<String> row1 = new LinkedList<>();
        row1.add("TRUE");
        row1.add("1");
        expectedStringData.add(row1);
        List<String> row2 = new LinkedList<>();
        row2.add("FALSE");
        row2.add("2");
        expectedStringData.add(row2);
        List<String> row3 = new LinkedList<>();
        row3.add("3");
        expectedStringData.add(row3);

        var dataFormatter = new DataFormatter();
        sheetParser = new ExcelSheetParser(extractor.getSheet(AS_STRING_SHEET).get(), true, dataFormatter);
        var data = sheetParser.getData();
        assertEquals(expectedStringData, data);
    }

    @Test
    void testGetDataFromRange()
    {
        sheetParser = new ExcelSheetParser(extractor.getSheet(SHEET_NAME).get());
        var dataFromRange = sheetParser.getDataFromRange("B2:B7");
        assertEquals(6, dataFromRange.size());
        var openStatus = OPEN_STATUS;
        var closedStatus = "CLOSED";
        assertCellValue(dataFromRange.get(0), openStatus, "B2");
        assertCellValue(dataFromRange.get(1), openStatus, "B3");
        assertCellValue(dataFromRange.get(2), openStatus, "B4");
        assertCellValue(dataFromRange.get(3), "PENDING", "B5");
        assertCellValue(dataFromRange.get(4), closedStatus, "B6");
        assertCellValue(dataFromRange.get(5), closedStatus, "B7");
    }

    @Test
    void testGetDataFromTableRange()
    {
        sheetParser = new ExcelSheetParser(extractor.getSheet(SHEET_NAME).get());
        var data = sheetParser.getDataAsTable("A1:B3");
        Map<String, List<String>> expectedData = new HashMap<>();
        expectedData.put(NAME, List.of("First", "Second"));
        expectedData.put("status", List.of(OPEN_STATUS, OPEN_STATUS));
        assertEquals(expectedData, data);
    }

    @Test
    void testGetDataFromTableRangeWithDiffTypes()
    {
        Map<String, List<String>> expectedData = new LinkedHashMap<>();
        expectedData.put("Boolean", List.of(TRUE_AS_STRING, FALSE_AS_STRING));
        expectedData.put("Number", List.of(ONE_AS_STRING, TWO_AS_STRING));
        expectedData.put("String", List.of("STRING", "string"));
        expectedData.put("Formula", List.of(THREE_AS_STRING, ""));

        sheetParser = new ExcelSheetParser(extractor.getSheet("DifferentTypes").get());
        var data = sheetParser.getDataAsTable("A1:D3");
        assertEquals(expectedData, data);
    }

    private static void assertCellValue(CellValue actual, String value, String address)
    {
        assertEquals(value, actual.getValue());
        assertEquals(address, actual.getAddress());
    }

    @Test
    void testGetDataFromCell()
    {
        sheetParser = new ExcelSheetParser(extractor.getSheet(SHEET_NAME).get());
        assertEquals(NAME, sheetParser.getDataFromCell("A1"));
    }

    @Test
    void testGetDataFromNotExistingCell()
    {
        sheetParser = new ExcelSheetParser(extractor.getSheet(SHEET_NAME).get());
        var exception = assertThrows(IllegalArgumentException.class,
            () -> sheetParser.getDataFromCell("A1001"));
        assertEquals("Row at address 'A1001' doesn't exist", exception.getMessage());
    }
}
