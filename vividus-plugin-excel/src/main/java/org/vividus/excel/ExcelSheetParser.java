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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.cellwalk.CellWalk;

public class ExcelSheetParser implements IExcelSheetParser
{
    private final Sheet sheet;

    private final int rowsToParseCount;

    private final int columnsToParseCount;

    private boolean trimCellValues;

    private DataFormatter dataFormatter;

    private FormulaEvaluator formulaEvaluator;

    public ExcelSheetParser(Sheet sheet)
    {
        this.sheet = sheet;
        SheetDataLimits sheetDataLimits = getSheetDataLimits(sheet.getLastRowNum());
        rowsToParseCount = sheetDataLimits.getLastRowIndex() + 1;
        columnsToParseCount = sheetDataLimits.getLastColumnIndex() + 1;
    }

    public ExcelSheetParser(Sheet sheet, boolean trimCellValues)
    {
        this(sheet);
        this.trimCellValues = trimCellValues;
    }

    public ExcelSheetParser(Sheet sheet, boolean trimCellValues, DataFormatter dataFormatter)
    {
        this(sheet, trimCellValues);
        this.dataFormatter = dataFormatter;
        formulaEvaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
    }

    private SheetDataLimits getSheetDataLimits(int lastRowCandidate)
    {
        SheetDataLimits sheetDataLimits = new SheetDataLimits();
        boolean lastRowFound = false;
        for (int rowIndex = lastRowCandidate; rowIndex >= 0; rowIndex--)
        {
            Row row = sheet.getRow(rowIndex);
            if (null != row)
            {
                for (Cell cell : row)
                {
                    if (null != cell && !CellUtils.getCellValueAsString(cell).isEmpty())
                    {
                        if (!lastRowFound)
                        {
                            sheetDataLimits.setLastRowIndex(rowIndex);
                            lastRowFound = true;
                        }
                        int cellIndex = cell.getColumnIndex();
                        if (cellIndex > sheetDataLimits.getLastColumnIndex())
                        {
                            sheetDataLimits.setLastColumnIndex(cellIndex);
                        }
                    }
                }
            }
        }
        return sheetDataLimits;
    }

    @Override
    public List<String> getRow(int rowNumber)
    {
        return StreamSupport.stream(sheet.getRow(rowNumber).spliterator(), false)
                .map(this::getCellValueAndTrimIfNeeded).collect(Collectors.toList());
    }

    @Override
    public List<List<String>> getData()
    {
        return getData(0, 0);
    }

    @Override
    public List<List<String>> getData(int from)
    {
        return getData(from, 0);
    }

    @Override
    public List<List<String>> getData(int from, int skipBottomRows)
    {
        int to = rowsToParseCount - skipBottomRows;
        return IntStream.range(from, to)
                .mapToObj(sheet::getRow)
                .filter(Objects::nonNull)
                .map(row -> IntStream.range(0, getCellsCount(row.getLastCellNum()))
                        .mapToObj(row::getCell)
                        .filter(Objects::nonNull)
                        .map(this::getCellValueAndTrimIfNeeded)
                        .collect(toLinkedList()))
                .collect(toLinkedList());
    }

    private int getCellsCount(int lastCellIndex)
    {
        return Math.min(lastCellIndex + 1, columnsToParseCount);
    }

    private static <T> Collector<T, ?, LinkedList<T>> toLinkedList()
    {
        return Collectors.toCollection(LinkedList::new);
    }

    @Override
    public List<Map<String, String>> getDataWithTitle(int titleRowNumber)
    {
        return getDataWithTitle(titleRowNumber, 0);
    }

    @Override
    public List<Map<String, String>> getDataWithTitle(int titleRowNumber, int skipBottomRows)
    {
        int to = rowsToParseCount - skipBottomRows;
        Row titleRow = sheet.getRow(titleRowNumber);
        List<Map<String, String>> resultData = new LinkedList<>();
        for (int rowIndex = titleRowNumber + 1; rowIndex < to; rowIndex++)
        {
            Row dataRow = sheet.getRow(rowIndex);
            if (dataRow != null)
            {
                Map<String, String> entryData = new LinkedHashMap<>();
                for (int j = 0; j < getCellsCount(titleRow.getLastCellNum()); j++)
                {
                    Cell titleCell = titleRow.getCell(j);
                    if (null != titleCell)
                    {
                        String key = getCellValueAndTrimIfNeeded(titleCell);
                        Cell dataCell = dataRow.getCell(j);
                        if (null != dataCell && !key.isEmpty())
                        {
                            entryData.put(key, getCellValueAndTrimIfNeeded(dataCell));
                        }
                    }
                }
                resultData.add(entryData);
            }
        }
        return resultData;
    }

    private String getCellValueAndTrimIfNeeded(Cell cell)
    {
        String cellValue;
        if (dataFormatter != null)
        {
            cellValue = dataFormatter.formatCellValue(cell, formulaEvaluator);
        }
        else
        {
            cellValue = CellUtils.getCellValueAsString(cell);
        }
        return trimCellValues ? cellValue.trim() : cellValue;
    }

    @Override
    public Sheet getSheet()
    {
        return sheet;
    }

    @Override
    public List<String> getDataFromRange(String range)
    {
        CellRangeAddress cellRangeAddress = CellRangeAddress.valueOf(range);
        CellWalk cellWalk = new CellWalk(sheet, cellRangeAddress);
        List<String> resultData = new ArrayList<>();
        cellWalk.traverse((cell, ctx) -> Optional.ofNullable(CellUtils.getCellValueAsString(cell))
                .ifPresent(resultData::add));
        return resultData;
    }

    @Override
    public String getDataFromCell(String address)
    {
        CellReference cellReference = new CellReference(address);
        Row row = sheet.getRow(cellReference.getRow());
        if (row == null)
        {
            throw new IllegalArgumentException(String.format("Row at address '%s' doesn't exist", address));
        }
        Cell cell = row.getCell(cellReference.getCol());
        return CellUtils.getCellValueAsString(cell);
    }

    private static class SheetDataLimits
    {
        private int lastRowIndex = -1;
        private int lastColumnIndex = -1;

        public void setLastRowIndex(int lastRowIndex)
        {
            this.lastRowIndex = lastRowIndex;
        }

        public int getLastRowIndex()
        {
            return lastRowIndex;
        }

        public void setLastColumnIndex(int lastColumnIndex)
        {
            this.lastColumnIndex = lastColumnIndex;
        }

        public int getLastColumnIndex()
        {
            return lastColumnIndex;
        }
    }
}
