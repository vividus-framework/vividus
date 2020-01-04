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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.inject.Named;

import org.apache.poi.ss.usermodel.Sheet;
import org.jbehave.core.model.ExamplesTableProperties;
import org.vividus.bdd.util.ExamplesTableProcessor;
import org.vividus.excel.ExcelSheetParser;
import org.vividus.excel.ExcelSheetsExtractor;
import org.vividus.excel.IExcelSheetParser;
import org.vividus.excel.IExcelSheetsExtractor;
import org.vividus.excel.WorkbookParsingException;

@Named("FROM_EXCEL")
public class ExcelTableTransformer implements ExtendedTableTransformer
{
    @Override
    public String transform(String tableAsString, ExamplesTableProperties properties)
    {
        checkTableEmptiness(tableAsString);
        String path = ExtendedTableTransformer.getMandatoryNonBlankProperty(properties, "path");
        String sheetName = ExtendedTableTransformer.getMandatoryNonBlankProperty(properties, "sheet");
        List<String> result;
        try
        {
            IExcelSheetsExtractor excelSheetsExtractor = new ExcelSheetsExtractor(path);
            Optional<Sheet> sheet = excelSheetsExtractor.getSheet(sheetName);
            if (sheet.isEmpty())
            {
                throw new IllegalArgumentException("Sheet with name '" + sheetName + "' does not exist");
            }
            IExcelSheetParser excelSheetParser = new ExcelSheetParser(sheet.get());
            String lineBreakReplacementPropertyValue = properties.getProperties().getProperty("lineBreakReplacement");
            String lineBreakReplacement = lineBreakReplacementPropertyValue == null ? ""
                : lineBreakReplacementPropertyValue;
            result = extractData(excelSheetParser, properties)
                    .stream()
                    .map(e -> e.replace("\n", lineBreakReplacement))
                    .collect(Collectors.toList());
        }
        catch (WorkbookParsingException e)
        {
            throw new IllegalStateException("Error during parsing excel workbook", e);
        }
        return build(result, properties);
    }

    private List<String> extractData(IExcelSheetParser sheetParser, ExamplesTableProperties properties)
    {
        return processCompetingMandatoryProperties(properties,
                entry("range", range -> extractDataFromRage(sheetParser, properties, range)),
                entry("addresses", addresses -> extractDataFromAddresses(sheetParser, addresses)));
    }

    private List<String> extractDataFromRage(IExcelSheetParser sheetParser, ExamplesTableProperties properties,
            String range)
    {
        List<String> data = sheetParser.getDataFromRange(range);
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

    private List<String> extractDataFromAddresses(IExcelSheetParser sheetParser, String addresses)
    {
        return Stream.of(addresses.split(";")).map(sheetParser::getDataFromCell).collect(Collectors.toList());
    }

    private String build(List<String> data, ExamplesTableProperties properties)
    {
        String columnName = ExtendedTableTransformer.getMandatoryNonBlankProperty(properties, "column");
        String joinValues = properties.getProperties().getProperty("joinValues");
        List<String> columnData = Boolean.parseBoolean(joinValues) ? List.of(String.join(" ", data)) : data;
        return ExamplesTableProcessor.buildExamplesTableFromColumns(List.of(columnName),
                List.of(columnData), properties);
    }
}
