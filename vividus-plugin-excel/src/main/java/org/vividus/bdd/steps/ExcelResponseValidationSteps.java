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

package org.vividus.bdd.steps;

import static java.lang.String.format;
import static java.util.Map.entry;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Sheet;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsNull;
import org.jbehave.core.annotations.Then;
import org.vividus.bdd.model.CellRecord;
import org.vividus.bdd.model.CellValue;
import org.vividus.excel.ExcelSheetParser;
import org.vividus.excel.ExcelSheetsExtractor;
import org.vividus.excel.IExcelSheetParser;
import org.vividus.excel.IExcelSheetsExtractor;
import org.vividus.excel.WorkbookParsingException;
import org.vividus.http.HttpTestContext;
import org.vividus.softassert.ISoftAssert;

public class ExcelResponseValidationSteps
{
    private final HttpTestContext httpTestContext;
    private final ISoftAssert softAssert;

    public ExcelResponseValidationSteps(HttpTestContext httpTestContext, ISoftAssert softAssert)
    {
        this.httpTestContext = httpTestContext;
        this.softAssert = softAssert;
    }

    /**
     * Checks that API response contains excel sheet with index <b>index</b> and records <b>records</b>
     * @param index Index of the sheet (0-based)
     * @param records Table of records with specified <b>cellsRange</b> and <b>valueRegex</b> that should be
     * matched against cells in excel sheet:
     * <ul>
     * <li>cellsRange - range of cells (e.g. "B1:D8", "A1", "C1:C5").
     * <li>valueRegex - regular expression to match a value against</li>
     * </ul>
     * <code>
     * Then response contains excel sheet with index `2` and records:
     * |cellsRange|valueRegex      |<br>
     * |A1:E8     |\\w+            |<br>
     * |D11:H25   |                |<br>
     * |A1:H1     |header_\\d+_\\w+|<br>
     * </code>
     */
    @Then("response contains excel sheet with index `$index` and records:$records")
    public void excelSheetWithIndexHasRecords(int index, List<CellRecord> records)
    {
        checkRecords(records, e -> e.getSheet(index), "index " + index);
    }

    /**
     * Checks that API response contains excel sheet with name <b>name</b> and records <b>records</b>
     * @param name Name of the sheet
     * @param records Table of records with specified <b>cellsRange</b> and <b>valueRegex</b> that should be
     * matched against cells in excel sheet:
     * <ul>
     * <li>cellsRange - range of cells (e.g. "B1:D8", "A1", "C1:C5").
     * <li>valueRegex - regular expression to match a value against</li>
     * </ul>
     * <code>
     * Then response contains excel sheet with name `products_140220` and records:
     * |cellsRange|valueRegex      |<br>
     * |A1:E8     |\\w+            |<br>
     * |D11:H25   |                |<br>
     * |A1:H1     |header_\\d+_\\w+|<br>
     * </code>
     */
    @Then("response contains excel sheet with name `$name` and records:$records")
    public void excelSheetWithNameHasRecords(String name, List<CellRecord> records)
    {
        checkRecords(records, e -> e.getSheet(name), "name " + name);
    }

    private void checkRecords(List<CellRecord> records, Function<IExcelSheetsExtractor, Optional<Sheet>> sheetMapper,
            String errorKey)
    {
        sheetMapper.apply(getExtractor()).ifPresentOrElse(s ->
        {
            IExcelSheetParser parser = new ExcelSheetParser(s);
            records.stream()
                .map(r -> parser.getDataFromRange(r.getCellsRange())
                        .stream()
                        .map(cv -> entry(cv, r.getValueRegex())))
                .flatMap(Function.identity())
                .filter(filterMatched())
                .collect(Collectors.collectingAndThen(Collectors.toList(), v ->
                {
                    reportResults(records.stream().map(CellRecord::getCellsRange), v);
                    return null;
                }));
        }, () -> softAssert.recordFailedAssertion(format("Sheet with the %s doesn't exist", errorKey)));
    }

    private void reportResults(Stream<String> ranges, List<Entry<CellValue, Optional<Pattern>>> failedRecords)
    {
        if (failedRecords.isEmpty())
        {
            softAssert.recordPassedAssertion(format("All records at ranges %s are matched in the document",
                    ranges.collect(Collectors.joining(", "))));
            return;
        }
        failedRecords.forEach(e ->
        {
            CellValue cellValue = e.getKey();
            Matcher<String> matcher = e.getValue().map(Matchers::matchesPattern).orElseGet(IsNull<String>::new);
            softAssert.assertThat(format("Cell at address '%s'", cellValue.getAddress()), cellValue.getValue(),
                    matcher);
        });
    }

    private IExcelSheetsExtractor getExtractor()
    {
        try
        {
            return new ExcelSheetsExtractor(httpTestContext.getResponse().getResponseBody());
        }
        catch (WorkbookParsingException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static Predicate<Entry<CellValue, Optional<Pattern>>> filterMatched()
    {
        return e ->
        {
            Optional<Pattern> pattern = e.getValue();
            CellValue cellValue = e.getKey();
            if (pattern.isPresent() && cellValue.getValue() != null)
            {
                return !pattern.get().asMatchPredicate().test(cellValue.getValue());
            }
            return !pattern.isEmpty() || cellValue.getValue() != null;
        };
    }
}
