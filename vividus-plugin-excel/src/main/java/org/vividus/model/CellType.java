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

package org.vividus.model;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.vividus.util.DateUtils;

public enum CellType
{
    STRING
    {
        @Override
        public void setCellValue(XSSFWorkbook workbook, Cell cell, String value, DateUtils dateUtils)
        {
            cell.setCellValue(value);
        }
    },
    FORMULA
    {
        @Override
        public void setCellValue(XSSFWorkbook workbook, Cell cell, String value, DateUtils dateUtils)
        {
            cell.setCellFormula(value);
        }
    },
    DATE
    {
        @Override
        public void setCellValue(XSSFWorkbook workbook, Cell cell, String value, DateUtils dateUtils)
        {
            Matcher valueMatcher = INCOMING_VALUE_PATTERN.matcher(value);
            if (!valueMatcher.matches())
            {
                throw new IllegalArgumentException("The date value isn't specified");
            }
            Matcher inputFormatMatcher = INPUT_FORMAT_PATTERN.matcher(value);
            Matcher cellFormatMatcher = CELL_FORMAT_PATTERN.matcher(value);

            String inputFormat = inputFormatMatcher.matches() ? inputFormatMatcher.group(1).trim() : "MM/dd/yyyy";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(inputFormat, Locale.ENGLISH);
            ZonedDateTime zonedDateTime = dateUtils.parseDateTime(valueMatcher.group(1).trim(), formatter);
            LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
            cell.setCellValue(localDateTime);

            CreationHelper createHelper = workbook.getCreationHelper();
            CellStyle cellStyle = workbook.createCellStyle();
            String cellFormat = cellFormatMatcher.matches() ? cellFormatMatcher.group(1).trim() : "m/d/yy";
            cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(cellFormat));
            cell.setCellStyle(cellStyle);
        }
    },
    BOOLEAN
    {
        @Override
        public void setCellValue(XSSFWorkbook workbook, Cell cell, String value, DateUtils dateUtils)
        {
            cell.setCellValue(Boolean.parseBoolean(value));
        }
    },
    NUMERIC
    {
        @Override
        public void setCellValue(XSSFWorkbook workbook, Cell cell, String value, DateUtils dateUtils)
        {
            Matcher valueMatcher = INCOMING_VALUE_PATTERN.matcher(value);
            if (!valueMatcher.matches())
            {
                throw new IllegalArgumentException("The numeric value isn't specified");
            }
            Matcher cellFormatMatcher = CELL_FORMAT_PATTERN.matcher(value);

            cell.setCellValue(Double.parseDouble(valueMatcher.group(1).trim()));

            CreationHelper createHelper = workbook.getCreationHelper();
            CellStyle cellStyle = workbook.createCellStyle();
            String cellFormat = cellFormatMatcher.matches() ? cellFormatMatcher.group(1).trim() : "#.0";
            cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(cellFormat));
            cell.setCellStyle(cellStyle);
        }
    };

    private static final Pattern INCOMING_VALUE_PATTERN = Pattern.compile("^(.+?)(?=format|input|$).*");
    private static final Pattern INPUT_FORMAT_PATTERN = Pattern.compile(".+input\\h+(.*?)(?=format|$).*");
    private static final Pattern CELL_FORMAT_PATTERN = Pattern.compile(".+format\\h+(.*?)(?=input|$).*");

    public abstract void setCellValue(XSSFWorkbook workbook, Cell cell, String value, DateUtils dateUtils);
}
