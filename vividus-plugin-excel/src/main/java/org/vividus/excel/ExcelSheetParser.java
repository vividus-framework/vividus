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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.vividus.model.CellValue;

public class ExcelSheetParser implements IExcelSheetParser
{
    private final Sheet sheet;

    private final int rowsToParseCount;

    private final int columnsToParseCount;

    private final boolean preserveCellFormatting;

    private DataFormatter dataFormatter;

    private FormulaEvaluator formulaEvaluator;

    public ExcelSheetParser(Sheet sheet, boolean preserveCellFormatting)
    {
        this.sheet = sheet;
        this.preserveCellFormatting = preserveCellFormatting;
        SheetDataLimits sheetDataLimits = getSheetDataLimits(sheet.getLastRowNum());
        rowsToParseCount = sheetDataLimits.getLastRowIndex() + 1;
        columnsToParseCount = sheetDataLimits.getLastColumnIndex() + 1;
        if (preserveCellFormatting)
        {
            dataFormatter = new DataFormatter();
            formulaEvaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        }
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
                .map(this::getCellValue).toList();
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
                        .map(this::getCellValue)
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
                        String key = getCellValue(titleCell);
                        Cell dataCell = dataRow.getCell(j);
                        if (null != dataCell && !key.isEmpty())
                        {
                            entryData.put(key, getCellValue(dataCell));
                        }
                    }
                }
                resultData.add(entryData);
            }
        }
        return resultData;
    }

    private String getCellValue(Cell cell)
    {
        return preserveCellFormatting ? dataFormatter.formatCellValue(cell, formulaEvaluator)
                                      : CellUtils.getCellValueAsString(cell);
    }

    @Override
    public Sheet getSheet()
    {
        return sheet;
    }

    @Override
    public List<CellValue> getDataFromRange(String range)
    {
        return StreamSupport.stream(CellRangeAddress.valueOf(range).spliterator(), false)
                .map(CellAddress::formatAsString)
                .map(addr -> new CellValue(getDataFromCell(addr), addr))
                .toList();
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
        return Optional.ofNullable(row.getCell(cellReference.getCol())).map(this::getCellValue)
                .orElse(null);
    }

    @Override
    public Map<String, List<String>> getDataAsTable(String range)
    {
        List<String> rangeList = Stream.of(StringUtils.split(range, ';'))
                .map(String::trim)
                .toList();

        Map<String, List<String>> data = parseRowsWithHeader(CellRangeAddress.valueOf(rangeList.get(0)));
        int numOfHeaders = data.size();

        rangeList.stream().skip(1).forEach(r ->
        {
            CellRangeAddress address = CellRangeAddress.valueOf(r);
            int numOfColumnsInAddress = address.getLastColumn() - address.getFirstColumn() + 1;
            if (numOfHeaders != numOfColumnsInAddress)
            {
                throw new IllegalArgumentException(String.format(
                "The number of columns (%d) in the \"%s\" range must correspond to the number of table headers (%d)",
                        numOfColumnsInAddress, r, numOfHeaders));
            }
            addAdditionalRows(data, address);
        });
        return data;
    }

    private Map<String, List<String>> parseRowsWithHeader(CellRangeAddress address)
    {
        Map<String, List<String>> data = new LinkedHashMap<>();

        for (int colIndex = address.getFirstColumn(); colIndex <= address.getLastColumn(); colIndex++)
        {
            String columnName = sheet.getRow(address.getFirstRow()).getCell(colIndex).getStringCellValue();
            List<String> columnsData = new ArrayList<>();
            for (int rowIndex = address.getFirstRow() + 1; rowIndex <= address.getLastRow(); rowIndex++)
            {
                Row row = sheet.getRow(rowIndex);
                String cellValue = Optional.ofNullable(row.getCell(colIndex))
                                           .map(this::getCellValue)
                                           .orElse(null);
                columnsData.add(cellValue);
            }
            data.put(columnName, columnsData);
        }
        return data;
    }

    @SuppressWarnings("checkstyle:MultipleVariableDeclarationsExtended")
    private void addAdditionalRows(Map<String, List<String>> data, CellRangeAddress address)
    {
        for (int colIndex = address.getFirstColumn(), i = 0; colIndex <= address.getLastColumn(); colIndex++, i++)
        {
            List<String> colValues = data.get(new ArrayList<>(data.keySet()).get(i));
            for (int rowIndex = address.getFirstRow(); rowIndex <= address.getLastRow(); rowIndex++)
            {
                Row row = sheet.getRow(rowIndex);
                colValues.add(getCellValue(row.getCell(colIndex)));
            }
        }
    }

    private static final class SheetDataLimits
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
