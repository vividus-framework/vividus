/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.annotation;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.StepPattern;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.RunTestContext;
import org.vividus.model.RunningStory;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class ReplacementMonitorTests
{
    private static final String LOGGED_MESSAGE_PATTERN =
            "The step: \"{}\" is deprecated and will be removed in VIVIDUS {}. Use step: \"{}\"";
    private static final String REMOVE_VERSION = "0.7.0";
    private static final String PARAMETERIZED_STEP_METHOD = "parameterizedStep";

    private static final String NOT_PARAMETERIZED_STEP_VALUE = "step without parameters deprecated";
    private static final String NOT_PARAMETERIZED_STEP_DEPR = "When step without parameters deprecated";
    private static final String NOT_PARAMETERIZED_STEP_ACTUAL = "When step without parameters actual";

    private static final String PARAMETERIZED_STEP_VALUE = "deprecated step with parameter '$param'";
    private static final String PARAMETERIZED_STEP_PATTERN = "deprecated\\sstep\\swith\\sparameter\\s'(.*)'";
    private static final String PARAMETERIZED_STEP_VALUE_ALIAS = "deprecated step with parameter `$param`";
    private static final String PARAMETERIZED_STEP_ALIAS_PATTERN = "deprecated\\sstep\\swith\\sparameter\\s`(.*)`";
    private static final String PARAMETERIZED_STEP_DEPR = "Then deprecated step with parameter 'param'";
    private static final String PARAMETERIZED_STEP_DEPR_ALIAS = "Then deprecated step with parameter `param`";
    private static final String PARAMETERIZED_STEP_ACTUAL = "Then actual step with parameter `param`";
    private static final String PARAMETERIZED_STEP_REPLACE_PATTERN = "Then actual step with parameter `%1$s`";

    private static final String MULTI_PARAMETERIZED_STEP_VALUE =
            "deprecated step with parameters '$param1' & '$param2'";
    private static final String MULTI_PARAMETERIZED_STEP_PATTERN =
            "deprecated\\sstep\\swith\\sparameters\\s'(.*)'\\s&\\s'(.*)'";
    private static final String MULTI_PARAMETERIZED_STEP_DEPR =
            "Given deprecated step with parameters 'param1' & 'param2'";
    private static final String MULTI_PARAMETERIZED_STEP_ACTUAL =
            "Given actual step with parameters `param2` & `param1`";
    private static final String MULTI_PARAMETERIZED_STEP_REPLACE_PATTERN =
            "Given actual step with parameters `%2$s` & `%1$s`";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(ReplacementMonitor.class);

    @Mock private RunTestContext runTestContext;
    @InjectMocks private ReplacementMonitor replacementMonitor;

    static Stream<Arguments> dataForStepReplacing()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
                Arguments.of(NOT_PARAMETERIZED_STEP_VALUE,   NOT_PARAMETERIZED_STEP_VALUE,     NOT_PARAMETERIZED_STEP_DEPR,   NOT_PARAMETERIZED_STEP_ACTUAL,   "notParameterizedStep"),
                Arguments.of(PARAMETERIZED_STEP_VALUE,       PARAMETERIZED_STEP_PATTERN,       PARAMETERIZED_STEP_DEPR,       PARAMETERIZED_STEP_ACTUAL,       PARAMETERIZED_STEP_METHOD),
                Arguments.of(PARAMETERIZED_STEP_VALUE,       PARAMETERIZED_STEP_ALIAS_PATTERN, PARAMETERIZED_STEP_DEPR_ALIAS, PARAMETERIZED_STEP_ACTUAL,       "parameterizedStepWithAlias"),
                Arguments.of(MULTI_PARAMETERIZED_STEP_VALUE, MULTI_PARAMETERIZED_STEP_PATTERN, MULTI_PARAMETERIZED_STEP_DEPR, MULTI_PARAMETERIZED_STEP_ACTUAL, "multiParameterizedStep")
        );
        // CHECKSTYLE:ON
    }

    @MethodSource("dataForStepReplacing")
    @ParameterizedTest
    void shouldLogDeprecatedStep(String deprecatedStepValue, String deprecatedStepPattern, String deprecatedStep,
            String actualStep, String methodName) throws NoSuchMethodException
    {
        StepPattern stepPattern = new StepPattern(null, deprecatedStepValue, deprecatedStepPattern);
        StepPattern stepPatternOtherStep = new StepPattern(null, "otherStepValue", "otherStepPattern");
        replacementMonitor.stepMatchesPattern("notMatchedStep", false, null, null, null);
        replacementMonitor.stepMatchesPattern("otherStep", true, stepPatternOtherStep, null, null);
        replacementMonitor.stepMatchesPattern(deprecatedStepValue, true, stepPattern, null, null);
        mockRunTestContext(deprecatedStep);
        var method = ReplacementMonitorTests.class.getDeclaredMethod(methodName);
        replacementMonitor.beforePerforming(null, true, method);
        assertThat(logger.getLoggingEvents(),
                is(List.of(info(LOGGED_MESSAGE_PATTERN, deprecatedStep, REMOVE_VERSION, actualStep))));
    }

    private void mockRunTestContext(String deprecatedStep)
    {
        var runningStory = mock(RunningStory.class);
        when(runTestContext.getRunningStory()).thenReturn(runningStory);
        when(runningStory.getRunningSteps()).thenReturn(new LinkedList<>(List.of(deprecatedStep)));
    }

    @Replacement(versionToRemoveStep = REMOVE_VERSION, replacementFormatPattern = NOT_PARAMETERIZED_STEP_ACTUAL)
    @When(NOT_PARAMETERIZED_STEP_VALUE)
    void notParameterizedStep()
    {
        // nothing to do
    }

    @Replacement(versionToRemoveStep = REMOVE_VERSION, replacementFormatPattern = PARAMETERIZED_STEP_REPLACE_PATTERN)
    @Then(PARAMETERIZED_STEP_VALUE)
    void parameterizedStep()
    {
        // nothing to do
    }

    @Replacement(versionToRemoveStep = REMOVE_VERSION, replacementFormatPattern = PARAMETERIZED_STEP_REPLACE_PATTERN)
    @Then(PARAMETERIZED_STEP_VALUE)
    @Alias(PARAMETERIZED_STEP_VALUE_ALIAS)
    void parameterizedStepWithAlias()
    {
        // nothing to do
    }

    @Replacement(versionToRemoveStep = REMOVE_VERSION,
                 replacementFormatPattern = MULTI_PARAMETERIZED_STEP_REPLACE_PATTERN)
    @Given(MULTI_PARAMETERIZED_STEP_VALUE)
    void multiParameterizedStep()
    {
        // nothing to do
    }
}
