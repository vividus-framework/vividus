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

import static org.hamcrest.text.MatchesPattern.matchesPattern;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.util.MapUtils;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.comparison.ComparisonUtils;
import org.vividus.util.comparison.ComparisonUtils.EntryComparisonResult;
import org.vividus.util.freemarker.FreemarkerProcessor;

import freemarker.template.TemplateException;

public class BddVariableSteps
{
    private static final String TABLES_ARE_EQUAL = "Tables are equal";

    private FreemarkerProcessor freemarkerProcessor;
    @Inject private IBddVariableContext bddVariableContext;
    @Inject private ISoftAssert softAssert;
    @Inject private IAttachmentPublisher attachmentPublisher;

    /**
     * This step initializes BDD variable with a result of given template processing
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A name under which the value should be saved
     * @param templatePath Freemarker template file path
     * @param templateParameters Parameters processed by template. Any valid ExamplesTable.
     * <p>For example, if template file is the following:</p>
     * {<br>
     *  "id": "12345",<br>
     *  "version": 1,<br>
     *  "dateTime": "${dateTime}",<br>
     *  "adherenceDateTime": "${adherenceDateTime}",<br>
     *  "didAdhere": true<br>
     * }
     * <p>Parameters required for Freemarker template in ExamplesTable will be the following:</p>
     * <table border="1" style="width:70%">
     * <caption>Table of parameters</caption>
     * <tr>
     * <td>dateTime</td>
     * <td>adherenceDateTime</td>
     * </tr>
     * <tr>
     * <td>2016-05-19T15:30:34</td>
     * <td>2016-05-19T14:43:12</td>
     * </tr>
     * </table>
     * @throws IOException in case of any error happened at I/O operations
     * @throws TemplateException in case of any error at template processing
     */
    @Given("I initialize the $scopes variable `$variableName` using template `$templatePath` with parameters:"
            + "$templateParameters")
    public void initVariableUsingTemplate(Set<VariableScope> scopes, String variableName, String templatePath,
            ExamplesTable templateParameters) throws IOException, TemplateException
    {
        Map<String, List<String>> dataModel = MapUtils.convertExamplesTableToMap(templateParameters);
        Map<String, Object> parameters = new HashMap<>();
        String parametersKey = "parameters";
        parameters.put(parametersKey, dataModel);
        if (!dataModel.containsKey(parametersKey))
        {
            parameters.putAll(dataModel);
        }
        String value = freemarkerProcessor.process(templatePath, parameters, StandardCharsets.UTF_8);
        bddVariableContext.putVariable(scopes, variableName, value);
    }

    /**
     * Initializes BDD variable with given value in specified scope
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
        bddVariableContext.putVariable(scopes, variableName, variableValue);
    }

    /**
     * Compare the value from the first <b>variable</b> with the value from the second <b>variable</b>
     * in accordance with the <b>condition</b>
     * Could compare Maps and Lists of maps using EQUAL_TO comparison rule.
     * Other rules will fallback to strings comparison
     * <p>
     * The values of the variables should be logically comparable.
     * @param variable1 The <b>name</b> of the variable in witch the value was set
     * @param condition The rule to compare values<br>
     * (<i>Possible values:<b> less than, less than or equal to, greater than, greater than or equal to,
     * equal to</b></i>)
     * @param variable2 The <b>name</b> of the different variable in witch the value was set
     * @return true if assertion is passed, otherwise false
     */
    @Then("`$variable1` is $comparisonRule `$variable2`")
    public boolean compareVariables(Object variable1, ComparisonRule condition, Object variable2)
    {
        if (variable1 instanceof String && variable2 instanceof String)
        {
            String variable1AsString = (String) variable1;
            String variable2AsString = (String) variable2;
            if (NumberUtils.isCreatable(variable1AsString) && NumberUtils.isCreatable(variable2AsString))
            {
                BigDecimal number1 = NumberUtils.createBigDecimal(variable1AsString);
                BigDecimal number2 = NumberUtils.createBigDecimal(variable2AsString);
                return compare(number1, condition, number2);
            }
        }
        else if (ComparisonRule.EQUAL_TO.equals(condition))
        {
            if (isEmptyOrListOfMaps(variable1) && isEmptyOrListOfMaps(variable2))
            {
                return compareListsOfMaps(variable1, variable2);
            }
            else if (instanceOfMap(variable1) && instanceOfMap(variable2))
            {
                return compareListsOfMaps(List.of(variable1), List.of(variable2));
            }
        }
        return compare(variable1.toString(), condition, variable2.toString());
    }

    /**
     * Compare the map from <b>variable</b> with the provided table in <b>table</b>
     * ignoring any extra entries in <b>variable</b>
     * @param variable The <b>name</b> of the variable with map to check
     * @param table ExamplesTable with parameters to compare with
     */
    @Then("`$variable` is equal to table ignoring extra columns:$table")
    public void tablesAreEqualIgnoringExtraColumns(Object variable, ExamplesTable table)
    {
        if (!instanceOfMap(variable))
        {
            throw new IllegalArgumentException("'variable' should be instance of map");
        }
        Map<String, String> expectedMap = MapUtils.convertSingleRowExamplesTableToMap(table);
        List<EntryComparisonResult> results = ComparisonUtils.checkMapContainsSubMap((Map<?, ?>) variable, expectedMap);
        publishMapComparisonResults(List.of(results));
        softAssert.assertTrue(TABLES_ARE_EQUAL, results.stream().allMatch(EntryComparisonResult::isPassed));
    }

    /**
     * Compare empty list or list of maps from <b>variable</b> with the provided <b>table</b>
     * @param variable The variable with empty list or list of maps to check
     * @param table ExamplesTable with parameters to compare with
     */
    @Then("`$variable` is equal to table:$table")
    public void tablesAreEqual(Object variable, ExamplesTable table)
    {
        if (!isEmptyOrListOfMaps(variable))
        {
            throw new IllegalArgumentException("'variable' should be empty list or list of maps structure");
        }
        compareListsOfMaps(variable, table.getRows());
    }

    /**
     * Matches given value against specified pattern
     * @param value The value to be matched
     * @param pattern pattern
     */
    @Then("`$value` matches `$pattern`")
    public void valueMatchesPattern(String value, Pattern pattern)
    {
        softAssert.assertThat(String.format("Value '%s' matches pattern '%s'", value, pattern.pattern()), value,
                matchesPattern(pattern));
    }

    /**
     * Saves content to file with specified pathname
     * @param pathname Fully qualified file name with parent folders and extension
     * (e.g. temp/some_file.txt)
     * @param fileContent Content to be saved to file
     * @throws IOException If an I/O exception of some sort has occurred
     */
    @When("I create a file with the pathname `$pathname` and the content `$fileContent`")
    public void saveVariableToFile(String pathname, String fileContent) throws IOException
    {
        FileUtils.writeStringToFile(new File(pathname), fileContent, StandardCharsets.UTF_8);
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
                ExamplesTable examplesTable)
    {
        List<Map<String, String>> listOfMaps = examplesTable.getRowsAsParameters()
                .stream().map(Parameters::values).collect(Collectors.toList());
        bddVariableContext.putVariable(scopes, variableName, listOfMaps);
    }

    private boolean compareListsOfMaps(Object variable1, Object variable2)
    {
        List<List<EntryComparisonResult>> results = ComparisonUtils.compareListsOfMaps(variable1, variable2);
        publishMapComparisonResults(results);
        return softAssert.assertTrue(TABLES_ARE_EQUAL,
                results.stream().flatMap(List::stream).allMatch(EntryComparisonResult::isPassed));
    }

    private void publishMapComparisonResults(List<List<EntryComparisonResult>> results)
    {
        attachmentPublisher.publishAttachment("/templates/maps-comparison-table.ftl",
                Map.of("results", results), "Tables comparison result");
    }

    private boolean isEmptyOrListOfMaps(Object list)
    {
        return list instanceof List && (((List<?>) list).isEmpty() || instanceOfMap(((List<?>) list).get(0)));
    }

    private boolean instanceOfMap(Object object)
    {
        return object instanceof Map;
    }

    private <T extends Comparable<T>> boolean compare(T value1, ComparisonRule condition, T value2)
    {
        String readableCondition = condition.name().toLowerCase().replace('_', ' ');
        String description = "Checking if \"" + value1 + "\" is " + readableCondition + " \"" + value2 + "\"";
        return softAssert.assertThat(description, value1, condition.getComparisonRule(value2));
    }

    public void setFreemarkerProcessor(FreemarkerProcessor freemarkerProcessor)
    {
        this.freemarkerProcessor = freemarkerProcessor;
    }
}
