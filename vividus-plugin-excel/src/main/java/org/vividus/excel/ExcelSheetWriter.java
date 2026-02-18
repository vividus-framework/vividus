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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.commons.lang3.EnumUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jbehave.core.model.ExamplesTable;
import org.vividus.model.CellType;
import org.vividus.util.DateUtils;

public class ExcelSheetWriter
{
    private static final Pattern HEADER_WITH_TYPE_PATTERN = Pattern
            .compile("#\\{withColumnCellsType\\(([^,]+),\\h*(.*)\\)}");
    private static final Pattern CELL_WITH_TYPE_PATTERN = Pattern.compile("#\\{withCellType\\(([^,]+),\\h*(.*)\\)}");

    private final DateUtils dateUtils;

    public ExcelSheetWriter(DateUtils dateUtils)
    {
        this.dateUtils = dateUtils;
    }

    /**
    * Create an Excel document containing one sheet with the optional name and specified content.
    * @param path path of Excel file to create
    * @param sheetName sheet name, if not specified the default name will be determined by underlying excel library
    * @param content any valid ExamplesTable
    */
    public void createExcel(Path path, Optional<String> sheetName, ExamplesTable content) throws IOException
    {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                OutputStream fileOut = Files.newOutputStream(path))
        {
            XSSFSheet sheet = sheetName.map(workbook::createSheet).orElseGet(workbook::createSheet);
            fillData(content, sheet, workbook);
            workbook.write(fileOut);
        }
    }

    /**
    * Add a sheet with the specified name and content to the existing excel file.
    * @param path path of existing Excel file
    * @param sheetName sheet name
    * @param content any valid ExamplesTable
    */
    public void addSheetToExcel(Path path, String sheetName, ExamplesTable content) throws IOException
    {
        try (InputStream fileInput = Files.newInputStream(path);
                XSSFWorkbook workbook = new XSSFWorkbook(fileInput);
                OutputStream fileOut = Files.newOutputStream(path))
        {
            fillData(content, workbook.createSheet(sheetName), workbook);
            workbook.write(fileOut);
        }
    }

    private void fillData(ExamplesTable content, XSSFSheet sheet, XSSFWorkbook workbook)
    {
        ArrayList<CellType> columTypes = new ArrayList<>();
        fillRow(sheet, workbook, 0, content.getHeaders(), columTypes);
        IntStream.range(0, content.getRowCount()).forEach(rowIndex -> {
            List<String> cells = content.getRowValues(rowIndex, true);
            fillRow(sheet, workbook, rowIndex + 1, cells, columTypes);
        });
    }

    private void fillRow(XSSFSheet sheet, XSSFWorkbook workbook, int rowIndex, List<String> cells,
            ArrayList<CellType> columTypes)
    {
        Row row = sheet.createRow(rowIndex);
        IntStream.range(0, cells.size()).forEach(index ->
        {
            if (rowIndex == 0)
            {
                String headerFromExampleTable = cells.get(index);
                Cell cellExcel = row.createCell(index);
                Matcher headerTypeMatcher = HEADER_WITH_TYPE_PATTERN.matcher(headerFromExampleTable);

                if (headerTypeMatcher.matches())
                {
                    columTypes.add(EnumUtils.getEnumIgnoreCase(CellType.class, headerTypeMatcher.group(1)));
                    cellExcel.setCellValue(headerTypeMatcher.group(2));
                }
                else
                {
                    cellExcel.setCellValue(headerFromExampleTable);
                    columTypes.add(CellType.STRING);
                }
            }
            else
            {
                String cellContentFromExampleTable = cells.get(index);
                Cell cellExcel = row.createCell(index);
                Matcher cellTypeMatcher = CELL_WITH_TYPE_PATTERN.matcher(cellContentFromExampleTable);
                if (cellTypeMatcher.matches())
                {
                    CellType type = EnumUtils.getEnumIgnoreCase(CellType.class, cellTypeMatcher.group(1));
                    String cellContent = cellTypeMatcher.group(2);
                    type.setCellValue(workbook, cellExcel, cellContent, dateUtils);
                }
                else
                {
                    CellType type = columTypes.get(index);
                    type.setCellValue(workbook, cellExcel, cellContentFromExampleTable, dateUtils);
                }
            }
        });
    }
}
