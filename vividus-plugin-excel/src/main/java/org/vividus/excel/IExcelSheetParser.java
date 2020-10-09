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

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.vividus.bdd.model.CellValue;

public interface IExcelSheetParser
{
    List<String> getRow(int rowNumber);

    List<List<String>> getData();

    List<List<String>> getData(int from);

    List<List<String>> getData(int from, int skipBottomRows);

    List<Map<String, String>> getDataWithTitle(int titleRowNumber);

    List<Map<String, String>> getDataWithTitle(int titleRowNumber, int skipBottomRows);

    Sheet getSheet();

    List<CellValue> getDataFromRange(String range);

    String getDataFromCell(String cellAddress);

    Map<String, List<String>> getDataAsTable(String range);
}
