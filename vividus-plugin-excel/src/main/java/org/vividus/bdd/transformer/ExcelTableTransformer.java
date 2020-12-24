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

package org.vividus.bdd.transformer;

import static java.util.Map.entry;
import static org.apache.commons.lang3.Validate.notBlank;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.inject.Named;

import org.apache.poi.ss.usermodel.Sheet;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.vividus.bdd.model.CellValue;
import org.vividus.bdd.util.ExamplesTableProcessor;
import org.vividus.excel.ExcelSheetParser;
import org.vividus.excel.ExcelSheetsExtractor;
import org.vividus.excel.IExcelSheetParser;
import org.vividus.excel.IExcelSheetsExtractor;
import org.vividus.excel.WorkbookParsingException;

@Named("FROM_EXCEL")
public class ExcelTableTransformer implements ExtendedTableTransformer
{
    private static final String RANGE = "range";

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        checkTableEmptiness(tableAsString);
        String path = ExtendedTableTransformer.getMandatoryNonBlankProperty(properties, "path");
        String sheetName = ExtendedTableTransformer.getMandatoryNonBlankProperty(properties, "sheet");
        try
        {
            IExcelSheetsExtractor excelSheetsExtractor = new ExcelSheetsExtractor(path);
            Optional<Sheet> sheet = excelSheetsExtractor.getSheet(sheetName);
            if (sheet.isEmpty())
            {
                throw new IllegalArgumentException("Sheet with name '" + sheetName + "' does not exist");
            }
            IExcelSheetParser excelSheetParser = new ExcelSheetParser(sheet.get());
            String column = properties.getProperties().getProperty("column");
            if (column != null)
            {
                notBlank(column, "Table property 'column' is blank");
                String joinValues = properties.getProperties().getProperty("joinValues");
                List<String> result = extractData(excelSheetParser, properties);
                List<String> data = Boolean.parseBoolean(joinValues) ? List.of(String.join(" ", result)) : result;
                return build(List.of(column), List.of(data), properties);
            }
            String range = ExtendedTableTransformer.getMandatoryNonBlankProperty(properties, RANGE);
            Map<String, List<String>> exactDataTable = excelSheetParser.getDataAsTable(range);
            return build(exactDataTable.keySet(), exactDataTable.values(), properties);
        }
        catch (WorkbookParsingException e)
        {
            throw new IllegalStateException("Error during parsing excel workbook", e);
        }
    }

    private List<String> extractData(IExcelSheetParser sheetParser, TableProperties properties)
    {
        return processCompetingMandatoryProperties(properties,
                entry(RANGE, range -> extractDataFromRange(sheetParser, properties, range)),
                entry("addresses", addresses -> extractDataFromAddresses(sheetParser, addresses)));
    }

    private List<String> extractDataFromRange(IExcelSheetParser sheetParser, TableProperties properties, String range)
    {
        List<String> data = extractValues(sheetParser, range);
        String incrementAsString = properties.getProperties().getProperty("increment");
        if (incrementAsString != null)
        {
            return IntStream.range(0, data.size())
                    .filter(n -> n % Integer.parseInt(incrementAsString) == 0)
                    .mapToObj(data::get)
                    .collect(Collectors.toList());
        }
        return data;
    }

    private List<String> extractValues(IExcelSheetParser sheetParser, String range)
    {
        return sheetParser.getDataFromRange(range).stream().map(CellValue::getValue).collect(Collectors.toList());
    }

    private List<String> extractDataFromAddresses(IExcelSheetParser sheetParser, String addresses)
    {
        return Stream.of(addresses.split(";")).map(sheetParser::getDataFromCell).collect(Collectors.toList());
    }

    private String build(Collection<String> headers, Collection<List<String>> data, TableProperties properties)
    {
        String lineBreakReplacementPropertyValue = properties.getProperties().getProperty("lineBreakReplacement");
        String lineBreakReplacement = lineBreakReplacementPropertyValue == null ? ""
            : lineBreakReplacementPropertyValue;
        List<List<String>> result = data.stream()
                                          .map(element -> replaceLineBreaks(element, lineBreakReplacement))
                                          .collect(Collectors.toList());
        return ExamplesTableProcessor.buildExamplesTableFromColumns(headers, result, properties);
    }

    private List<String> replaceLineBreaks(List<String> list, String lineBreakReplacement)
    {
        return list.stream()
                     .map(e -> e.replace("\n", lineBreakReplacement))
                     .collect(Collectors.toList());
    }
}
