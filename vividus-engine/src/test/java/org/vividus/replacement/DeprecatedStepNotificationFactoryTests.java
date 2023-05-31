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

package org.vividus.replacement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.RunTestContext;
import org.vividus.model.RunningStory;

@ExtendWith(MockitoExtension.class)
class DeprecatedStepNotificationFactoryTests
{
    private static final String REMOVE_VERSION = "0.7.0";

    private static final String NOT_PARAMETERIZED_STEP_VALUE = "step without parameters deprecated";
    private static final String NOT_PARAMETERIZED_STEP_DEPR = "When step without parameters deprecated";
    private static final String NOT_PARAMETERIZED_STEP_ACTUAL = "When step without parameters actual";

    private static final String PARAMETERIZED_STEP_VALUE = "deprecated step with parameter '$param'";
    private static final String PARAMETERIZED_STEP_PATTERN = "deprecated\\sstep\\swith\\sparameter\\s'(.*)'";
    private static final String PARAMETERIZED_STEP_DEPR = "Then deprecated step with parameter 'param'";
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

    @Mock private RunTestContext runTestContext;
    @Mock private Configuration configuration;
    @Mock private CollectingStepPatternsMonitor collectingStepPatternsMonitor;
    @InjectMocks private DeprecatedStepNotificationFactory deprecatedStepNotificationFactory;

    static Stream<Arguments> dataForStepReplacing()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
                Arguments.of(NOT_PARAMETERIZED_STEP_VALUE,   NOT_PARAMETERIZED_STEP_VALUE,     NOT_PARAMETERIZED_STEP_DEPR,   NOT_PARAMETERIZED_STEP_ACTUAL,   NOT_PARAMETERIZED_STEP_ACTUAL),
                Arguments.of(PARAMETERIZED_STEP_VALUE,       PARAMETERIZED_STEP_PATTERN,       PARAMETERIZED_STEP_DEPR,       PARAMETERIZED_STEP_ACTUAL,       PARAMETERIZED_STEP_REPLACE_PATTERN),
                Arguments.of(MULTI_PARAMETERIZED_STEP_VALUE, MULTI_PARAMETERIZED_STEP_PATTERN, MULTI_PARAMETERIZED_STEP_DEPR, MULTI_PARAMETERIZED_STEP_ACTUAL, MULTI_PARAMETERIZED_STEP_REPLACE_PATTERN)
        );
        // CHECKSTYLE:ON
    }

    @MethodSource("dataForStepReplacing")
    @ParameterizedTest
    void shouldGetNotificationForRunningDeprecatedStep(String deprecatedStepValue, String deprecatedStepPattern,
            String deprecatedStep, String expectedActualStep, String formatPattern)
    {
        Map<String, Pattern> stepPatternsCache = new ConcurrentHashMap<>();
        stepPatternsCache.put(deprecatedStepValue, Pattern.compile(deprecatedStepPattern));
        stepPatternsCache.put("otherStepValue", Pattern.compile("otherStepPattern"));
        when(configuration.keywords()).thenReturn(new Keywords());
        when(collectingStepPatternsMonitor.getRegisteredStepPatterns()).thenReturn(stepPatternsCache);

        mockRunTestContext(deprecatedStep);
        String expectedNotification = String.format(
                "The step: \"%s\" is deprecated and will be removed in VIVIDUS %s. Use step: \"%s\"", deprecatedStep,
                REMOVE_VERSION, expectedActualStep);
        assertEquals(expectedNotification, deprecatedStepNotificationFactory
                .createDeprecatedStepNotification(REMOVE_VERSION, formatPattern));
    }

    private void mockRunTestContext(String deprecatedStep)
    {
        var runningStory = mock(RunningStory.class);
        when(runTestContext.getRunningStory()).thenReturn(runningStory);
        when(runningStory.getRunningSteps()).thenReturn(new LinkedList<>(List.of(deprecatedStep)));
    }
}
