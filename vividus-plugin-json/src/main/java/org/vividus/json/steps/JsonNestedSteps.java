/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.json.steps;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.json.JsonContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.SubSteps;
import org.vividus.util.json.JsonUtils;

public class JsonNestedSteps extends AbstractJsonSteps
{
    private final JsonUtils jsonUtils;
    private final VariableContext variableContext;
    private final ISoftAssert softAssert;

    public JsonNestedSteps(VariableContext variableContext, JsonContext jsonContext, JsonSteps jsonSteps,
            ISoftAssert softAssert, JsonUtils jsonUtils)
    {
        super(jsonContext, softAssert, jsonSteps);
        this.jsonUtils = jsonUtils;
        this.variableContext = variableContext;
        this.softAssert = softAssert;
    }

    /**
     * Step designed to perform steps against all elements found by JSON path in current json context or response
     * <b>if</b> they are matching comparison rule.
     * Actions performed by step:
     * <ul>
     * <li>Searches for elements using JSON path</li>
     * <li>Checks that elements quantity matches comparison rule and elements number</li>
     * <li>Passes if the comparison rule matches and the elements number is 0</li>
     * <li>For each element switches JSON context and performs all steps. No steps will be performed
     * in case of comparison rule mismatch</li>
     * <li>Restores previously set context</li>
     * </ul>
     * <br> Usage example:
     * <code>
     * <br>When I find equal to `1` JSON elements by `$.[?(@.parent_target_id=="")]` and for each element do
     * <br>|step                                                      |
     * <br>|Then number of JSON elements by JSON path `$..name` is = 3|
     * </code>
     * @param comparisonRule The rule to match the quantity of elements. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param elementsNumber The expected number of elements
     * @param jsonPath       A JSON path
     * @param stepsToExecute Examples table with steps to execute for each found elements
     */
    @SuppressWarnings("MagicNumber")
    @When(value = "I find $comparisonRule `$elementsNumber` JSON elements by `$jsonPath` and for each element do"
            + "$stepsToExecute", priority = 6)
    @Alias("I find $comparisonRule '$elementsNumber' JSON elements by '$jsonPath' and for each element"
            + " doa$stepsToExecute")
    public void performAllStepsForJsonIfFound(ComparisonRule comparisonRule, int elementsNumber, String jsonPath,
            SubSteps stepsToExecute)
    {
        performAllStepsForProvidedJsonIfFound(comparisonRule, elementsNumber, getActualJson(), jsonPath,
                stepsToExecute);
    }

    /**
     * Step designed to perform steps against all elements found by JSON path in provided json
     * <b>if</b> they are matching comparison rule.
     * Actions performed by step:
     * <ul>
     * <li>Searches for elements using JSON path</li>
     * <li>Checks that elements quantity matches comparison rule and elements number</li>
     * <li>Passes if the comparison rule matches and the elements number is 0</li>
     * <li>For each element switches JSON context and performs all steps. No steps will be performed
     * in case of comparison rule mismatch</li>
     * <li>Restores previously set context</li>
     * </ul>
     * <br> Usage example:
     * <code>
     * <br>When I find equal to `1` JSON elements from `{"parent_id":"","elements":[{"name": "1"},{"name": "2"}]}`
     *       by `$.[?(@.parent_id=="")]` and for each element do
     * <br>|step                                                      |
     * <br>|Then number of JSON elements by JSON path `$..name` is = 2|
     * </code>
     * @param comparisonRule The rule to match the quantity of elements. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param elementsNumber The expected number of elements
     * @param json           A JSON element
     * @param jsonPath       A JSON path
     * @param stepsToExecute Examples table with steps to execute for each found elements
     */
    @When("I find $comparisonRule `$elementsNumber` JSON elements from `$json` by `$jsonPath` and for each element do"
            + "$stepsToExecute")
    @Alias("I find $comparisonRule '$elementsNumber' JSON elements from '$json' by '$jsonPath' and for"
            + " each element do$stepsToExecute")
    public void performAllStepsForProvidedJsonIfFound(ComparisonRule comparisonRule, int elementsNumber, String json,
            String jsonPath, SubSteps stepsToExecute)
    {
        performStepsForEachJsonEntry(json, jsonPath, comparisonRule, elementsNumber, stepsToExecute, () -> false);
    }

    /**
     * Step designed to perform steps against all elements found by JSON path in provided json
     * <b>if</b> they are matching comparison rule and <b>until</b> context variable value by specified name not set
     * or matches expected rule.
     * Actions performed by step:
     * <ul>
     * <li>Searches for elements using JSON path</li>
     * <li>Checks that elements quantity matches comparison rule and elements number</li>
     * <li>Passes if the comparison rule matches and the elements number is 0</li>
     * <li>For each element switches JSON context and performs all steps until variable not set or mismatches
     * expected value. No steps will be performed in case of comparison rule mismatch</li>
     * <li>Restores previously set context</li>
     * <li>Step will fail if variable never be set along the iterations</li>
     * </ul>
     * <br> Usage example:
     * <code>
     * <br>When I find > `1` JSON elements in `${json}` by `$.store.book` and until variable `title` matches `M.+`
     * for each element I do:
     * <br>|step|
     * <br>|When I save JSON element value from context by JSON path `$.title` to scenario variable `title`|
     * </code>
     * @param comparisonRule  The rule to match the quantity of elements. The supported rules:
     *                        <ul>
     *                        <li>less than (&lt;)</li>
     *                        <li>less than or equal to (&lt;=)</li>
     *                        <li>greater than (&gt;)</li>
     *                        <li>greater than or equal to (&gt;=)</li>
     *                        <li>equal to (=)</li>
     *                        <li>not equal to (!=)</li>
     *                        </ul>
     * @param elementsNumber  The expected number of elements
     * @param json            A JSON element
     * @param jsonPath        A JSON path
     * @param variableName    A variable name to check
     * @param variableMatcher The rule to match the quantity of elements. The supported rules:
     *                        <ul>
     *                        <li>is equal to</li>
     *                        <li>contains</li>
     *                        <li>does not contain</li>
     *                        <li>matches</li>
     *                        </ul>
     * @param expectedValue   The expected variable value
     * @param stepsToExecute  Examples table with steps to execute for each found elements
     */
    @When("I find $comparisonRule `$elementsNumber` JSON elements in `$json` by `$jsonPath`"
            + " and until variable `$variableName` $variableMatcher `$expectedValue`"
            + " for each element I do:$stepsToExecute")
    @Alias("I find $comparisonRule '$elementsNumber' JSON elements in '$json' by '$jsonPath' and"
            + " until variable '$variableName' $variableMatcher '$expectedValue' for each element I do:$stepsToExecute")
    public void performAllStepsForJsonEntriesExpectingVariable(ComparisonRule comparisonRule, int elementsNumber,
        String json, String jsonPath, String variableName, StringComparisonRule variableMatcher, String expectedValue,
            SubSteps stepsToExecute)
    {
        Matcher<String> matcher = variableMatcher.createMatcher(expectedValue);
        performStepsForEachJsonEntry(json, jsonPath, comparisonRule, elementsNumber, stepsToExecute, () -> {
            Object variable = variableContext.getVariable(variableName);
            return variable != null && matcher.matches(variable.toString());
        });
        Object variable = variableContext.getVariable(variableName);
        if (variable == null)
        {
            softAssert.recordFailedAssertion(String.format("Variable `%s` was not initialized", variableName));
        }
    }

    /**
     * Step designed to perform steps against all elements found by JSON path in provided json
     * <b>if</b> they are matching comparison rule and <b>until</b> context variable value by specified name not set
     * or matches expected rule.
     * Actions performed by step:
     * <ul>
     * <li>Searches for elements using JSON path</li>
     * <li>Checks that elements quantity matches comparison rule and elements number</li>
     * <li>Passes if the comparison rule matches and the elements number is 0</li>
     * <li>For each element switches JSON context and performs all steps until variable not set or mismatches
     * expected value. No steps will be performed in case of comparison rule mismatch</li>
     * <li>Restores previously set context</li>
     * <li>Step will fail if variable never be set along the iterations</li>
     * </ul>
     * <br> Usage example:
     * <code>
     * <br>When I find > `1` JSON elements in context by `$.store.book` and until variable `title` matches `M.+`
     * for each element I do:
     * <br>|step|
     * <br>|When I save JSON element value from context by JSON path `$.title` to scenario variable `title`|
     * </code>
     * @param comparisonRule  The rule to match the quantity of elements. The supported rules:
     *                        <ul>
     *                        <li>less than (&lt;)</li>
     *                        <li>less than or equal to (&lt;=)</li>
     *                        <li>greater than (&gt;)</li>
     *                        <li>greater than or equal to (&gt;=)</li>
     *                        <li>equal to (=)</li>
     *                        <li>not equal to (!=)</li>
     *                        </ul>
     * @param elementsNumber  The expected number of elements
     * @param jsonPath        A JSON path
     * @param variableName    A variable name to check
     * @param variableMatcher The rule to match the quantity of elements. The supported rules:
     *                        <ul>
     *                        <li>is equal to</li>
     *                        <li>contains</li>
     *                        <li>does not contain</li>
     *                        <li>matches</li>
     *                        </ul>
     * @param expectedValue   The expected variable value
     * @param stepsToExecute  Examples table with steps to execute for each found elements
     */
    @When("I find $comparisonRule `$elementsNumber` JSON elements in context by `$jsonPath` and until variable "
            + "`$variableName` $variableMatcher `$expectedValue` for each element I do:$stepsToExecute")
    @Alias("I find $comparisonRule '$elementsNumber' JSON elements in context by '$jsonPath' and until"
            + " variable '$variableName' $variableMatcher '$expectedValue' for each element I do:$stepsToExecute")
    public void performAllStepsForJsonEntriesExpectingVariable(ComparisonRule comparisonRule, int elementsNumber,
            String jsonPath, String variableName, StringComparisonRule variableMatcher, String expectedValue,
            SubSteps stepsToExecute)
    {
        performAllStepsForJsonEntriesExpectingVariable(comparisonRule, elementsNumber, getActualJson(), jsonPath,
            variableName, variableMatcher, expectedValue, stepsToExecute);
    }

    private void performStepsForEachJsonEntry(String json, String jsonPath, ComparisonRule comparisonRule,
            int elementsNumber, SubSteps stepsToExecute, BooleanSupplier breakCondition)
    {
        Optional<List<?>> jsonElements = getElements(json, jsonPath);
        int count = countElementsNumber(jsonElements);
        if (assertJsonElementsNumber(jsonPath, count, comparisonRule, elementsNumber) && count != 0)
        {
            String jsonContext = getActualJson();
            try
            {
                for (Object o : jsonElements.get())
                {
                    if (breakCondition.getAsBoolean())
                    {
                        break;
                    }
                    String jsonElement = jsonUtils.toJson(o);
                    getJsonContext().putJsonContext(jsonElement);
                    stepsToExecute.execute(Optional.empty());
                }
            }
            finally
            {
                getJsonContext().putJsonContext(jsonContext);
            }
        }
    }
}
