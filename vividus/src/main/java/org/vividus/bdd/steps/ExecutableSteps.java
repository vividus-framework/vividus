/*
 * Copyright 2019 the original author or authors.
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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import javax.inject.Inject;

import com.google.common.base.Preconditions;

import org.apache.commons.lang3.mutable.MutableInt;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;

public class ExecutableSteps
{
    public static final int EXECUTIONS_NUMBER_THRESHOLD = 50;

    @Inject private ISubStepExecutorFactory subStepExecutorFactory;
    @Inject private IBddVariableContext bddVariableContext;

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
    @Alias(value = "the condition '$condition' is true I do$stepsToExecute")
    public void performAllStepsIfConditionIsTrue(boolean condition, ExamplesTable stepsToExecute)
    {
        if (condition)
        {
            ISubStepExecutor subStepExecutor = subStepExecutorFactory.createSubStepExecutor(stepsToExecute);
            subStepExecutor.execute(Optional.empty());
        }
    }

    /** If the variable with name is not set into context executes steps.
     * <b>Example:</b>
     * <br> When variable 'token' is not set perform:
     * <br> |When I login|
     * @param name variable name to check
     * @param stepsToExecute steps to execute
     */
    @When("variable '$name' is not set I do:$stepsToExecute")
    public void ifVariableNotSetPerformSteps(String name, ExamplesTable stepsToExecute)
    {
        if (bddVariableContext.getVariable(name) == null)
        {
            subStepExecutorFactory.createSubStepExecutor(stepsToExecute).execute(Optional.empty());
        }
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
    public void performStepsNumberTimes(int number, ExamplesTable stepsToExecute)
    {
        int minimum = 0;
        checkArgument(minimum <= number && number <= EXECUTIONS_NUMBER_THRESHOLD,
            "Please, specify executions number in the range from %s to %s", minimum, EXECUTIONS_NUMBER_THRESHOLD);
        ISubStepExecutor subStepExecutor = subStepExecutorFactory.createSubStepExecutor(stepsToExecute);
        IntStream.range(0, number).forEach(i -> subStepExecutor.execute(Optional.empty()));
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
     * @param comparisonRule comparison rule to match counter against
     * @param limit counter limit
     * @param increment number to add to the counter on each iteration
     * @param seed initial counter value
     * @param stepsToExecute steps to execute
     */
    @When("I execute steps while counter is $comparisonRule `$limit` with increment `$increment` starting from `$seed`"
            + ":$stepsToExecute")
    public void executeStepsWhileConditionIsTrueWithStep(ComparisonRule comparisonRule, int limit, int increment,
            int seed, ExamplesTable stepsToExecute)
    {
        Matcher<Integer> limitMatcher = comparisonRule.getComparisonRule(limit);
        checkForLimit(seed, limitMatcher, increment);
        ISubStepExecutor subStepExecutor = subStepExecutorFactory.createSubStepExecutor(stepsToExecute);
        iterate(seed, limitMatcher, increment, iterationVariable ->
        {
            bddVariableContext.putVariable(VariableScope.STEP, "iterationVariable", iterationVariable);
            subStepExecutor.execute(Optional.empty());
        });
    }

    private static void checkForLimit(int seed, Matcher<Integer> limitMatcher, int increment)
    {
        MutableInt iterationsCounter = new MutableInt();
        iterate(seed, limitMatcher, increment,
            index -> Preconditions.checkArgument(iterationsCounter.incrementAndGet() < EXECUTIONS_NUMBER_THRESHOLD,
                    "Number of iterations has exceeded allowable limit " + EXECUTIONS_NUMBER_THRESHOLD));
    }

    private static void iterate(int seed, Matcher<Integer> limitMatcher, int increment, Consumer<Integer> body)
    {
        IntStream.iterate(seed, limitMatcher::matches, index -> index + increment).forEach(body::accept);
    }
}
