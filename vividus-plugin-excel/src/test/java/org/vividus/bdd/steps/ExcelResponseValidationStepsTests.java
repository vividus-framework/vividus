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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.model.CellRecord;
import org.vividus.http.HttpTestContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class ExcelResponseValidationStepsTests
{
    @Mock
    private HttpTestContext httpTestContext;

    @Mock
    private ISoftAssert softAssert;

    @InjectMocks
    private ExcelResponseValidationSteps steps;

    @BeforeEach
    void before()
    {
        HttpResponse response = mock(HttpResponse.class);
        byte[] body = ResourceUtils.loadResourceAsByteArray(getClass(), "TestTemplate.xlsx");
        when(httpTestContext.getResponse()).thenReturn(response);
        when(response.getResponseBody()).thenReturn(body);
    }

    static Stream<Arguments> sheetProcessors()
    {
        return Stream.of(
                Arguments.of((BiConsumer<ExcelResponseValidationSteps, List<CellRecord>>)
                    (s, r) -> s.excelSheetWithIndexHasRecords(0, r)),
                Arguments.of((BiConsumer<ExcelResponseValidationSteps, List<CellRecord>>)
                    (s, r) -> s.excelSheetWithNameHasRecords("Mapping", r))
                );
    }

    @ParameterizedTest
    @MethodSource("sheetProcessors")
    void testExcelSheetWithIndexHasRecords(BiConsumer<ExcelResponseValidationSteps, List<CellRecord>> consumer)
    {
        consumer.accept(steps, List.of(
                record("A4:B5", "(Product|Price)\\d+\\s*"),
                record("D2:D5", "\\d{2,4}\\.0"),
                record("B3", "Price"),
                record("C1:C5", null)
            ));
        verify(softAssert).recordPassedAssertion("All records at ranges A4:B5, D2:D5, B3, C1:C5 are matched in"
                + " the document");
        verifyNoMoreInteractions(softAssert);
    }

    @ParameterizedTest
    @MethodSource("sheetProcessors")
    void testExcelSheetWithIndexHasRecordsNoMatch(BiConsumer<ExcelResponseValidationSteps, List<CellRecord>> consumer)
    {
        consumer.accept(steps, List.of(
                record("C1:C3", "\\d+"),
                record("A5:B5", null)
            ));

        String expectFirst = "a string matching the pattern '\\d+'";
        verifyMissmatch("C1", null, expectFirst);
        verifyMissmatch("C2", null, expectFirst);
        verifyMissmatch("C3", null, expectFirst);

        String expectSecond = "null";
        verifyMissmatch("A5", "Product2", expectSecond);
        verifyMissmatch("B5", "Price2", expectSecond);

        verifyNoMoreInteractions(softAssert);
    }

    private void verifyMissmatch(String addr, String actual, String matcherAsString)
    {
        verify(softAssert).assertThat(eq(format("Cell at address '%s'", addr)), eq(actual),
                argThat(arg -> arg.toString().equals(matcherAsString)));
    }

    @Test
    void testExcelSheetWithIndexHasRecordsNoSheetWithIndex()
    {
        steps.excelSheetWithIndexHasRecords(10, List.of());
        verify(softAssert).recordFailedAssertion("Sheet with the index 10 doesn't exist");
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void testExcelSheetWithNameHasRecordsNoSheetWithIndex()
    {
        steps.excelSheetWithNameHasRecords("test", List.of());
        verify(softAssert).recordFailedAssertion("Sheet with the name test doesn't exist");
        verifyNoMoreInteractions(softAssert);
    }

    private static CellRecord record(String cellRange, String regex)
    {
        CellRecord record = new CellRecord();
        record.setCellsRange(cellRange);
        record.setValueRegex(Optional.ofNullable(regex).map(Pattern::compile));
        return record;
    }
}
