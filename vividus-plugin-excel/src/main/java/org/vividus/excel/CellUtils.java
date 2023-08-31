/*
 * Copyright 2019-2023 the original author or authors.
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

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public final class CellUtils
{
    private CellUtils()
    {
    }

    public static String getCellValueAsString(Cell cell)
    {
        return getCellValueAsString(cell, cell.getCellType());
    }

    private static String getCellValueAsString(Cell cell, CellType cellType)
    {
        return switch (cellType)
        {
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> getCellValueAsString(cell, cell.getCachedFormulaResultType());
            case STRING -> cell.getStringCellValue();
            default -> StringUtils.EMPTY;
        };
    }
}
