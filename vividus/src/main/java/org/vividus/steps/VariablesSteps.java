/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.steps;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.hamcrest.text.MatchesPattern.matchesPattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.vividus.context.VariableContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.EnumUtils;
import org.vividus.util.comparison.ComparisonUtils;
import org.vividus.util.comparison.ComparisonUtils.EntryComparisonResult;
import org.vividus.variable.VariableScope;

public class VariablesSteps
{
    private static final String TABLES_ARE_EQUAL = "Tables are equal";

    private final VariableContext variableContext;
    private final ISoftAssert softAssert;
    private final IAttachmentPublisher attachmentPublisher;
    private final VariableComparator variableComparator;

    public VariablesSteps(VariableContext variableContext, ISoftAssert softAssert,
            IAttachmentPublisher attachmentPublisher)
    {
        this.variableContext = variableContext;
        this.softAssert = softAssert;
        this.attachmentPublisher = attachmentPublisher;
        this.variableComparator = new VariableComparator()
        {
            @Override
            protected <T extends Comparable<T>> boolean compareValues(T value1, ComparisonRule condition, T value2)
            {
                String readableCondition = EnumUtils.toHumanReadableForm(condition);
                String description = "Checking if \"" + value1 + "\" is " + readableCondition + " \"" + value2 + "\"";
                return softAssert.assertThat(description, value1, condition.getComparisonRule(value2));
            }

            @Override
            protected boolean compareListsOfMaps(Object variable1, Object variable2)
            {
                List<List<EntryComparisonResult>> results = ComparisonUtils.compareListsOfMaps(variable1, variable2);
                publishMapComparisonResults(results);
                return softAssert.assertTrue(TABLES_ARE_EQUAL, areAllResultsPassed(results));
            }
        };
    }

    /**
     * Initializes a variable with given value in specified scope
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A name under which the value should be saved
     * @param variableValue A value to be saved
     */
    @When("I initialize the $scopes variable `$variableName` with value `$variableValue`")
    public void initVariableWithGivenValue(Set<VariableScope> scopes, String variableName, String variableValue)
    {
        variableContext.putVariable(scopes, variableName, variableValue);
    }

    /**
     * Compare the value from the first <b>variable</b> with the value from the second <b>variable</b>
     * in accordance with the <b>condition</b>
     * Could compare Maps and Lists of maps using EQUAL_TO comparison rule.
     * Other rules will fallback to strings comparison
     * <p>
     * The values of the variables should be logically comparable.
     * @param variable1 The <b>name</b> of the variable in witch the value was set
     * @param condition The rule to match the variable value. The supported rules:
     *                  <ul>
     *                  <li>less than (&lt;)</li>
     *                  <li>less than or equal to (&lt;=)</li>
     *                  <li>greater than (&gt;)</li>
     *                  <li>greater than or equal to (&gt;=)</li>
     *                  <li>equal to (=)</li>
     *                  <li>not equal to (!=)</li>
     *                  </ul>
     * @param variable2 The <b>name</b> of the different variable in witch the value was set
     * @return true if assertion is passed, otherwise false
     */
    @Then("`$variable1` is $comparisonRule `$variable2`")
    public boolean compareVariables(Object variable1, ComparisonRule condition, Object variable2)
    {
        return variableComparator.compare(variable1, condition, variable2);
    }

    /**
     * Compare the map from <b>variable</b> with the provided table in <b>table</b>
     * ignoring any extra entries in <b>variable</b>
     * @param variable The <b>name</b> of the variable with map to check
     * @param table ExamplesTable with parameters to compare with
     */
    @Then("`$variable` is equal to table ignoring extra columns:$table")
    @SuppressWarnings("unchecked")
    public void tablesAreEqualIgnoringExtraColumns(Object variable, ExamplesTable table)
    {
        isTrue(variable instanceof Map, "'variable' should be an instance of map");
        Map<Object, Object> actualMap = expandListValuesWithSingleElement((Map<Object, Object>) variable);

        List<Parameters> rows = table.getRowsAsParameters(true);
        int rowCount = rows.size();
        isTrue(rowCount == 1, "Input ExamplesTable should contain 1 row, but was %d", rowCount);

        Map<String, String> expectedMap =  rows.get(0).values();
        List<EntryComparisonResult> results = ComparisonUtils.checkMapContainsSubMap(actualMap, expectedMap);
        publishMapComparisonResults(List.of(results));
        softAssert.assertTrue(TABLES_ARE_EQUAL, results.stream().allMatch(EntryComparisonResult::isPassed));
    }

    private Map<Object, Object> expandListValuesWithSingleElement(Map<Object, Object> map)
    {
        Map<Object, Object> expandedMap = new HashMap<>();

        for (Entry<?, Object> entry : map.entrySet())
        {
            if (entry.getValue() instanceof List)
            {
                List<?> valueAsList = (List<?>) entry.getValue();
                if (valueAsList.size() == 1)
                {
                    expandedMap.put(entry.getKey(), valueAsList.get(0));
                    continue;
                }
            }
            return map;
        }
        return expandedMap;
    }

    /**
     * Compare empty list or list of maps from <b>variable</b> with the provided <b>table</b>
     * @param variable The variable with empty list or list of maps to check
     * @param table ExamplesTable with parameters to compare with
     */
    @Then("`$variable` is equal to table:$table")
    public void tablesAreEqual(Object variable, ExamplesTable table)
    {
        if (!variableComparator.isEmptyOrListOfMaps(variable))
        {
            throw new IllegalArgumentException("'variable' should be empty list or list of maps structure");
        }
        variableComparator.compareListsOfMaps(variable, table.getRows());
    }

    /**
     * Asserts that the given value matches the specified regular expression
     * @param value The value to assert
     * @param regex The regular expression
     */
    @Then("`$value` matches `$regex`")
    public void assertMatchesRegex(String value, String regex)
    {
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        softAssert.assertThat(String.format("Value '%s' matches pattern '%s'", value, regex), value,
                matchesPattern(pattern));
    }

    /**
     * Saves examples table as list of maps to variable
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A name of variable to assign the values
     * @param examplesTable A table for saving in variable
     */
    @When("I initialize $scopes variable `$variableName` with values:$examplesTable")
    public void initVariableWithGivenValues(Set<VariableScope> scopes, String variableName,
            List<Map<String, String>> examplesTable)
    {
        variableContext.putVariable(scopes, variableName, examplesTable);
    }

    private void publishMapComparisonResults(List<List<EntryComparisonResult>> results)
    {
        attachmentPublisher.publishAttachment("/templates/maps-comparison-table.ftl",
                Map.of("results", results), "Tables comparison result");
    }
}
