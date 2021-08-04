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

package org.vividus.bdd.steps;

import static org.apache.commons.lang3.Validate.inclusiveBetween;
import static org.apache.commons.lang3.Validate.isTrue;

import java.time.Duration;
import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import org.apache.commons.lang3.mutable.MutableInt;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.When;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.util.wait.MaxTimesBasedWaiter;

public class ExecutableSteps
{
    public static final int EXECUTIONS_NUMBER_THRESHOLD = 1000;

    private final IBddVariableContext bddVariableContext;
    private final VariableComparator variableComparator;

    public ExecutableSteps(IBddVariableContext bddVariableContext)
    {
        this.bddVariableContext = bddVariableContext;
        this.variableComparator = new VariableComparator()
        {
            @Override
            protected <T extends Comparable<T>> boolean compareValues(T value1, ComparisonRule condition, T value2)
            {
                return condition.getComparisonRule(value2).matches(value1);
            }
        };
    }

    /**
     * Steps designed to perform steps if condition is true
     * <b>if</b> condition is true
     * Performs all steps. No steps will be performed
     * in case of condition is false
     * <br> Usage example:
     * <code>
     * <br>When the condition 'true' is true I do
     * <br>|step|
     * <br>|When I compare against baseline with name 'test_composit1_step'|
     * <br>|When I click on all elements by xpath './/a[@title='Close']'|
     * </code>
     * @param condition verifiable condition
     * @param stepsToExecute examples table with steps to execute if condition is true
     */
    @SuppressWarnings("MagicNumber")
    @When(value = "the condition `$condition` is true I do$stepsToExecute", priority = 5)
    @Alias("the condition '$condition' is true I do$stepsToExecute")
    public void performAllStepsIfConditionIsTrue(boolean condition, SubSteps stepsToExecute)
    {
        if (condition)
        {
            stepsToExecute.execute(Optional.empty());
        }
    }

    /** If the variable with name is not set into context executes steps.
     * <b>Example:</b>
     * <br> When variable 'token' is not set perform:
     * <br> |When I login|
     * @param name variable name to check
     * @param stepsToExecute steps to execute
     */
    @When("variable `$name` is not set I do:$stepsToExecute")
    @Alias("variable '$name' is not set I do:$stepsToExecute")
    public void ifVariableNotSetPerformSteps(String name, SubSteps stepsToExecute)
    {
        if (bddVariableContext.getVariable(name) == null)
        {
            stepsToExecute.execute(Optional.empty());
        }
    }

    /**
     * Executes the steps while variable matches the comparison rule or until the maximum number of iterations is
     * reached.<br>
     * <b>Example:</b><br>
     * <code>
     * When I execute steps at most 5 times while `var` is &lt; `3`:<br>
     * |step                                                                        |<br>
     * |When I click on element located `id(counter)`                               |<br>
     * |When I set the text found in search context to the 'scenario' variable 'var'|<br>
     * </code>
     *
     * @param max            The maximum number of iterations
     * @param name           The name of the variable to check
     * @param comparisonRule The rule to match the variable value. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param expectedValue  The expected value of the variable
     * @param stepsToExecute The ExamplesTable with a single column containing the steps to execute
     */
    @When("I execute steps at most $max times while variable "
            + "`$variableName` is $comparisonRule `$expectedValue`:$stepsToExecute")
    @Alias("I execute steps at most $max times while variable "
            + "'$variableName' is $comparisonRule '$expectedValue':$stepsToExecute")
    public void executeStepsWhile(int max, String name, ComparisonRule comparisonRule, Object expectedValue,
            SubSteps stepsToExecute)
    {
        int iterationsLeft = max;
        while (iterationsLeft > 0 && doesVariableValueMatch(name, comparisonRule, expectedValue))
        {
            --iterationsLeft;
            stepsToExecute.execute(Optional.empty());
        }
    }

    /**
     * Executes the steps while variable matches the comparison rule or until the maximum number of iterations is
     * reached. The delay is used to define the amount of time to wait between iterations.<br>
     * <b>Example:</b><br>
     * <code>
     * When I execute steps with delay `PT10S` at most 5 times while `var` is &lt; `3`:<br>
     * |step                                                                        |<br>
     * |When I click on element located `id(counter)`                               |<br>
     * |When I set the text found in search context to the 'scenario' variable 'var'|<br>
     * </code>
     *
     * @param delay          The delay between iterations
     * @param max            The maximum number of iterations
     * @param name           The name of the variable to check
     * @param comparisonRule The rule to match the variable value. Allowed options:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than(&gt;)</li>
     *                       <li>greater than or equal to(&gt;=)</li>
     *                       <li>equal to(=))</li>
     *                       </ul>
     * @param expectedValue  The expected value of the variable
     * @param stepsToExecute The ExamplesTable with a single column containing the steps to execute
     */
    @When("I execute steps with delay `$delay` at most $max times while variable "
            + "`$variableName` is $comparisonRule `$expectedValue`:$stepsToExecute")
    @Alias("I execute steps with delay '$delay' at most $max times while variable "
            + "'$variableName' is $comparisonRule '$expectedValue':$stepsToExecute")
    public void executeStepsWithPollingInterval(Duration delay, int max, String name,
            ComparisonRule comparisonRule, Object expectedValue, SubSteps stepsToExecute)
    {
        new MaxTimesBasedWaiter(delay, max).wait(
                () -> stepsToExecute.execute(Optional.empty()),
                () -> !doesVariableValueMatch(name, comparisonRule, expectedValue)
        );
    }

    private boolean doesVariableValueMatch(String name, ComparisonRule comparisonRule, Object expectedValue)
    {
        Object variable = bddVariableContext.getVariable(name);
        return variable == null || variableComparator.compare(variable, comparisonRule, expectedValue);
    }

    /**
     * Steps designed to perform steps <b>number</b> times.
     * Executions number must be in the range from 0 to 50.
     * <br> Usage example:
     * <code>
     * <br>When I `2` times do:
     * <br>|step|
     * <br>|When I enter `text` in field located `By.xpath(//*[@id="identifierId"])`|
     * </code>
     * @param number executions number
     * @param stepsToExecute examples table with steps to execute <b>number</b> times
     */
    @When("I `$number` times do:$stepsToExecute")
    @Alias("I '$number' times do:$stepsToExecute")
    public void performStepsNumberTimes(int number, SubSteps stepsToExecute)
    {
        int minimum = 0;
        inclusiveBetween(minimum, EXECUTIONS_NUMBER_THRESHOLD, number,
            "Please, specify executions number in the range from %s to %s", minimum, EXECUTIONS_NUMBER_THRESHOLD);
        IntStream.range(0, number).forEach(i -> stepsToExecute.execute(Optional.empty()));
    }

    /**
     * Step is designed to perform <b>stepsToExecute</b> while counter matches <b>comparisonRule</b>. The limit for the
     * counter is set by <b>limit</b> variable. On each iteration the counter is increased on <b>increment</b> value,
     * which is allowed to be either positive or negative. The <b>seed</b> value is used as a starting point for
     * iteration. Current iteration index is available within <b>stepsToExecute</b> as <code>${iterationVariable}
     * </code>.
     * <br>
     * Step fails if iteration counter reaches maximum iteration limit which is <i>50</i>.
     * <br>
     * Example:
     * <br>
     * <code>
     * When I execute steps while counter is not equal to `10` with increment `2` starting from `0`
     * <br>
     * |step|
     * <br>
     * |Then `${iterationVariable}` matches `\\d+`|
     * </code>
     * @param comparisonRule The rule to match the counter against. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param limit          The counter limit
     * @param increment      The number to add to the counter on each iteration
     * @param seed           The initial counter value
     * @param stepsToExecute Steps to execute
     */
    @When("I execute steps while counter is $comparisonRule `$limit` with increment `$increment` starting from `$seed`"
            + ":$stepsToExecute")
    @Alias("I execute steps while counter is $comparisonRule '$limit' with increment '$increment' starting from '$seed'"
            + ":$stepsToExecute")
    public void executeStepsWhileConditionIsTrueWithStep(ComparisonRule comparisonRule, int limit, int increment,
            int seed, SubSteps stepsToExecute)
    {
        Matcher<Integer> limitMatcher = comparisonRule.getComparisonRule(limit);
        checkForLimit(seed, limitMatcher, increment);
        iterate(seed, limitMatcher, increment, iterationVariable ->
        {
            bddVariableContext.putVariable(VariableScope.STEP, "iterationVariable", iterationVariable);
            stepsToExecute.execute(Optional.empty());
        });
    }

    private static void checkForLimit(int seed, Matcher<Integer> limitMatcher, int increment)
    {
        MutableInt iterationsCounter = new MutableInt();
        iterate(seed, limitMatcher, increment,
            index -> isTrue(iterationsCounter.incrementAndGet() < EXECUTIONS_NUMBER_THRESHOLD,
                    "Number of iterations has exceeded allowable limit " + EXECUTIONS_NUMBER_THRESHOLD));
    }

    private static void iterate(int seed, Matcher<Integer> limitMatcher, int increment, IntConsumer body)
    {
        IntStream.iterate(seed, limitMatcher::matches, index -> index + increment).forEach(body);
    }
}
