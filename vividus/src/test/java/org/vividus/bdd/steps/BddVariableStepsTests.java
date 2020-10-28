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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.comparison.ComparisonUtils.EntryComparisonResult;
import org.vividus.util.freemarker.FreemarkerProcessor;

import freemarker.template.TemplateException;

@ExtendWith(MockitoExtension.class)
class BddVariableStepsTests
{
    private static final String TABLES_ASSERT_MESSAGE = "Tables comparison result";
    private static final String TEMPLATE_NAME = "/templates/maps-comparison-table.ftl";
    private static final String RESULTS = "results";
    private static final String TABLES_ARE_EQUAL = "Tables are equal";
    private static final String VALUE2 = "value2";
    private static final String VALUE = "value";
    private static final String KEY = "key";

    @Mock private FreemarkerProcessor freemarkerProcessor;
    @Mock private IBddVariableContext bddVariableContext;
    @Mock private ISoftAssert softAssert;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @InjectMocks private BddVariableSteps bddVariableSteps;

    @SuppressWarnings({ "checkstyle:MultipleStringLiterals", "checkstyle:MultipleStringLiteralsExtended" })
    static Stream<Arguments> stringsAsNumbers()
    {
        return Stream.of(
                arguments("9", "10"),
                arguments(9,   "10"),
                arguments("9", 10),
                arguments(9,   10)
        );
    }

    @ParameterizedTest
    @MethodSource("stringsAsNumbers")
    void shouldCompareStringsAsNumbers(Object variable1, Object variable2)
    {
        bddVariableSteps.compareVariables(variable1, ComparisonRule.LESS_THAN_OR_EQUAL_TO, variable2);
        verify(softAssert).assertThat(eq("Checking if \"9\" is less than or equal to \"10\""),
                eq(BigDecimal.valueOf(9)), argThat(m -> "a value less than or equal to <10>".equals(m.toString())));
    }

    @ParameterizedTest
    @CsvSource({
            "9,       string",
            "string1, string2"
    })
    void testCompareSimpleVariablesStrings(String variable1, String variable2)
    {
        String description = "Checking if \"" + variable1 + "\" is equal to \"" + variable2 + "\"";
        when(softAssert.assertThat(eq(description), eq(variable1), any())).thenReturn(false);
        assertFalse(bddVariableSteps.compareVariables(variable1, ComparisonRule.EQUAL_TO, variable2));
    }

    @Test
    void testInitVariableWithGivenValue()
    {
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        bddVariableSteps.initVariableWithGivenValue(scopes, VALUE, VALUE);
        verify(bddVariableContext).putVariable(scopes, VALUE, VALUE);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldCompareEqualToListsOfMapsAndPublishResultTable()
    {
        List<Map<String, Object>> listOfMaps1 = List.of(Map.of(KEY, VALUE));
        List<Map<String, Object>> listOfMaps2 = List.of(Map.of(KEY, VALUE2));
        assertFalse(bddVariableSteps.compareVariables(listOfMaps1, ComparisonRule.EQUAL_TO, listOfMaps2));
        verify(softAssert).assertTrue(TABLES_ARE_EQUAL, false);
        verify(attachmentPublisher).publishAttachment(eq(TEMPLATE_NAME), argThat(results ->
        {
            EntryComparisonResult result =
                    ((Map<String, List<List<EntryComparisonResult>>>) results).get(RESULTS).get(0)
                            .get(0);
            return KEY.equals(result.getKey()) && VALUE.equals(result.getLeft()) && VALUE2.equals(result.getRight())
                    && !result.isPassed();
        }), eq(TABLES_ASSERT_MESSAGE));
        verify(softAssert, never()).assertThat(any(), any(), any());
    }

    @Test
    void shouldCompareEqualToListsOfMapsAndPublishResultTableWhenListsAreEmpty()
    {
        List<Map<String, Object>> listOfMaps1 = List.of();
        List<Map<String, Object>> listOfMaps2 = List.of();
        assertFalse(bddVariableSteps.compareVariables(listOfMaps1, ComparisonRule.EQUAL_TO, listOfMaps2));
        verify(softAssert).assertTrue(TABLES_ARE_EQUAL, true);
        verify(attachmentPublisher).publishAttachment(TEMPLATE_NAME, Map.of(RESULTS, List.of()),
                TABLES_ASSERT_MESSAGE);
        verify(softAssert, never()).assertThat(any(), any(), any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldCompareEqualToMapsAndPublishResultTable()
    {
        Map<String, Object> map1 = Map.of(KEY, 2);
        Map<String, Object> map2 = Map.of(KEY, 2L);
        BigDecimal twoBD = new BigDecimal("2");
        assertFalse(bddVariableSteps.compareVariables(map1, ComparisonRule.EQUAL_TO, map2));
        verify(softAssert).assertTrue(TABLES_ARE_EQUAL, true);
        verify(attachmentPublisher).publishAttachment(eq(TEMPLATE_NAME), argThat(results ->
        {
            EntryComparisonResult result =
                    ((Map<String, List<List<EntryComparisonResult>>>) results).get(RESULTS).get(0)
                            .get(0);
            return KEY.equals(result.getKey()) && twoBD.equals(result.getLeft()) && twoBD.equals(result.getRight())
                    && result.isPassed();
        }), eq(TABLES_ASSERT_MESSAGE));
        verify(softAssert, never()).assertThat(any(), any(), any());
    }

    @Test
    void shouldCompareEqualToNotAlignedListsRightIsLongerOfMapsAndPublishResultTable()
    {
        List<Map<String, Object>> listOfMaps1 = List.of(Map.of(KEY, VALUE));
        List<Map<String, Object>> listOfMaps2 = List.of(Map.of(KEY, VALUE), Map.of(KEY, VALUE2));
        assertFalse(bddVariableSteps.compareVariables(listOfMaps1, ComparisonRule.EQUAL_TO, listOfMaps2));
        verify(softAssert).assertTrue(TABLES_ARE_EQUAL, false);
        verify(attachmentPublisher).publishAttachment(eq(TEMPLATE_NAME), argThat(results ->
        {
            @SuppressWarnings("unchecked")
            Map<String, List<List<EntryComparisonResult>>> map =
                    (Map<String, List<List<EntryComparisonResult>>>) results;
            EntryComparisonResult result1 = map.get(RESULTS).get(0).get(0);
            EntryComparisonResult result2 = map.get(RESULTS).get(1).get(0);
            return assertResult(result1) && KEY.equals(result2.getKey()) && null == result2.getLeft()
                    && VALUE2.equals(result2.getRight()) && !result2.isPassed();
        }), eq(TABLES_ASSERT_MESSAGE));
        verify(softAssert, never()).assertThat(any(), any(), any());
    }

    @Test
    void shouldCompareEqualToNotAlignedListsLeftIsLongerOfMapsAndPublishResultTable()
    {
        List<Map<String, Object>> listOfMaps1 = List.of(Map.of(KEY, VALUE), Map.of(KEY, VALUE2));
        List<Map<String, Object>> listOfMaps2 = List.of(Map.of(KEY, VALUE));
        assertFalse(bddVariableSteps.compareVariables(listOfMaps1, ComparisonRule.EQUAL_TO, listOfMaps2));
        verify(softAssert).assertTrue(TABLES_ARE_EQUAL, false);
        verify(attachmentPublisher).publishAttachment(eq(TEMPLATE_NAME), argThat(results ->
        {
            @SuppressWarnings("unchecked")
            Map<String, List<List<EntryComparisonResult>>> map =
                    (Map<String, List<List<EntryComparisonResult>>>) results;
            EntryComparisonResult result1 = map.get(RESULTS).get(0).get(0);
            EntryComparisonResult result2 = map.get(RESULTS).get(1).get(0);
            return assertResult(result1) && KEY.equals(result2.getKey()) && VALUE2.equals(result2.getLeft())
                    && null == result2.getRight() && !result2.isPassed();
        }), eq(TABLES_ASSERT_MESSAGE));
        verify(softAssert, never()).assertThat(any(), any(), any());
    }

    @Test
    void shouldTreatNullForBothValuesAsEqual()
    {
        List<Map<String, Object>> listOfMaps = List.of(Collections.singletonMap(KEY, null));
        bddVariableSteps.compareVariables(listOfMaps, ComparisonRule.EQUAL_TO, listOfMaps);
        verify(softAssert).assertTrue(TABLES_ARE_EQUAL, true);
    }

    private boolean assertResult(EntryComparisonResult result)
    {
        return KEY.equals(result.getKey()) && VALUE.equals(result.getLeft()) && VALUE.equals(result.getRight())
                && result.isPassed();
    }

    @SuppressWarnings("checkstyle:NoWhitespaceBefore")
    static Stream<Arguments> parametersProvider()
    {
        return Stream.of(
                arguments(VALUE,                       ComparisonRule.EQUAL_TO , VALUE2),
                arguments(List.of(Map.of(KEY, VALUE)), ComparisonRule.LESS_THAN, List.of(Map.of(KEY, VALUE))),
                arguments(List.of(Map.of(KEY, VALUE)), ComparisonRule.EQUAL_TO,  Map.of(KEY, VALUE)),
                arguments(Map.of(KEY, VALUE),          ComparisonRule.EQUAL_TO,  List.of(Map.of(KEY, VALUE))),
                arguments(Map.of(KEY, VALUE),          ComparisonRule.LESS_THAN, Map.of(KEY, VALUE)),
                arguments(List.of(KEY),                ComparisonRule.EQUAL_TO,  List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("parametersProvider")
    void shouldFallBackToStringsComparisonIfOneVariableIsNotMapTable(Object var1, ComparisonRule rule, Object var2)
    {
        assertFalse(bddVariableSteps.compareVariables(var1, rule, var2));
        verifyNoInteractions(attachmentPublisher);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldCompareTablesIgnoringExtraColumns()
    {
        String key1 = "k1";
        String value1 = "v1";
        Map<String, Object> actualMap = Map.of(key1, value1);
        ExamplesTable table = new ExamplesTable("|k1|k2|\n|v1|v2|");
        bddVariableSteps.tablesAreEqualIgnoringExtraColumns(actualMap, table);
        verify(softAssert).assertTrue(TABLES_ARE_EQUAL, false);
        verify(attachmentPublisher).publishAttachment(eq(TEMPLATE_NAME), argThat(results ->
        {
            EntryComparisonResult result1 =
                    ((Map<String, List<List<EntryComparisonResult>>>) results).get(RESULTS).get(0).get(0);
            EntryComparisonResult result2 =
                    ((Map<String, List<List<EntryComparisonResult>>>) results).get(RESULTS).get(0).get(1);
            return key1.equals(result1.getKey()) && value1.equals(result1.getLeft())
                            && value1.equals(result1.getRight()) && result1.isPassed()
                    && "k2".equals(result2.getKey()) && "v2".equals(result2.getLeft())
                            && result2.getRight() == null && !result2.isPassed();
        }), eq(TABLES_ASSERT_MESSAGE));
    }

    @Test
    void shouldFailOnCompareTablesIgnoringExtraColumnsIfMapNotProvided()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> bddVariableSteps.tablesAreEqualIgnoringExtraColumns("stringValue", null));
        assertEquals("'variable' should be instance of map", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldCompareTables()
    {
        List<Map<String, Object>> listOfMaps = List.of(Map.of(KEY, VALUE));
        ExamplesTable table = new ExamplesTable("|key|\n|value|");
        bddVariableSteps.tablesAreEqual(listOfMaps, table);
        verify(softAssert).assertTrue(TABLES_ARE_EQUAL, true);
        verify(attachmentPublisher).publishAttachment(eq(TEMPLATE_NAME), argThat(results ->
        {
            EntryComparisonResult result = ((Map<String, List<List<EntryComparisonResult>>>) results).get(RESULTS)
                    .get(0).get(0);
            return KEY.equals(result.getKey()) && VALUE.equals(result.getLeft()) && VALUE.equals(result.getRight())
                    && result.isPassed();
        }), eq(TABLES_ASSERT_MESSAGE));
    }

    @Test
    void shouldFailOnCompareTablesIfListOfMapsNotProvided()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> bddVariableSteps.tablesAreEqual("", null));
        assertEquals("'variable' should be empty list or list of maps structure", exception.getMessage());
    }

    static Stream<Arguments> mapProvider()
    {
        final String header = "header";
        final String parameters = "parameters";
        final String data = "data";
        final List<String> dataList = List.of(data);
        return Stream.of(
            arguments(Map.of(header, data),     Map.of(header, dataList, parameters, Map.of(header, dataList))),
            arguments(Map.of(parameters, data), Map.of(parameters, Map.of(parameters, dataList)))
        );
    }

    @ParameterizedTest
    @MethodSource("mapProvider")
    void testInitVariableUsingTemplate(Map<String, String> dataModel, Map<String, ?> resultMap)
        throws IOException, TemplateException
    {
        String templatePath = "/templatePath";
        when(freemarkerProcessor.process(templatePath, resultMap, StandardCharsets.UTF_8)).thenReturn(VALUE);
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        String variableName = "variableName";
        bddVariableSteps.initVariableUsingTemplate(scopes, variableName, templatePath,
                new ExamplesTable("").withRows(List.of(dataModel)));
        verify(bddVariableContext).putVariable(scopes, variableName, VALUE);
    }

    @Test
    void testSaveStringVariableToFile(@TempDir Path tempDir) throws IOException
    {
        String tempFilePath = tempDir.resolve("temp").resolve("test.txt").toString();
        String fileContent = "file-content";
        bddVariableSteps.saveVariableToFile(tempFilePath, fileContent);
        assertEquals(fileContent, FileUtils.readFileToString(new File(tempFilePath), StandardCharsets.UTF_8));
    }

    @Test
    void testValueMatchesPattern()
    {
        String pattern = ".*";
        bddVariableSteps.valueMatchesPattern(VALUE, Pattern.compile(pattern));
        verify(softAssert).assertThat(eq(String.format("Value '%s' matches pattern '%s'", VALUE, pattern)),
                eq(VALUE), argThat(m -> "a string matching the pattern '.*'".equals(m.toString())));
    }

    @Test
    void testInitVariableWithGivenValues()
    {
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        ExamplesTable table = new ExamplesTable("|key0|key1|\n|value0|value1|");
        bddVariableSteps.initVariableWithGivenValues(scopes, VALUE, table);
        List<Map<String, String>> listOfMaps = List.of(Map.of("key0", "value0", "key1", "value1"));
        verify(bddVariableContext).putVariable(scopes, VALUE, listOfMaps);
    }
}
