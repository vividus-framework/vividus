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

package org.vividus.log;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.common.collect.ImmutableList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.vividus.StatisticsStoryReporter;
import org.vividus.model.Failure;
import org.vividus.model.NodeType;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.model.Statistic;
import org.vividus.util.ResourceUtils;

@ExtendWith(TestLoggerFactoryExtension.class)
class TestInfoLoggerTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(TestInfoLogger.class);
    private static final String FORMAT = "{}={}";
    private static final String MESSAGE = "This is a very long message that should be wrapped to defined cell size "
            + "and with special char: %";

    @ParameterizedTest
    @ValueSource(strings = {
            "secure.access-key",
            "secure.admin-password",
            "secure.api-key",
            "secure.access-token",
            "secure.secret-phrase",
            "secure.private-key"
    })
    void testLogPropertiesSecurely(String key)
    {
        Properties properties = new Properties();
        properties.put(key, "value");
        TestInfoLogger.logPropertiesSecurely(properties);
        assertThat(LOGGER.getLoggingEvents(), is(List.of(info(FORMAT, key, "****"))));
    }

    @Test
    void testLogPropertiesSecurelyPlainProperties()
    {
        String keyString = "simple.ignore-failure";
        String valueString = "string";
        String keyInt = "simple.int";
        int valueInt = 2;
        Properties properties = new Properties();
        properties.put(keyString, valueString);
        properties.put(keyInt, valueInt);
        TestInfoLogger.logPropertiesSecurely(properties);
        assertThat(LOGGER.getLoggingEvents(), is(List.of(
                info(FORMAT, keyString, valueString),
                info(FORMAT, keyInt, valueInt)
        )));
    }

    @SuppressWarnings({ "MultipleStringLiterals", "MultipleStringLiteralsExtended", "LineLength"})
    private static Stream<Arguments> sourceOfFailures()
    {
        return Stream.of(
                Arguments.of("", null),
                Arguments.of(String.format("%n   No Failures & Errors!"), List.of()),
                Arguments.of(
        "\\s+ Failures & Errors:\\s+"
        + "┌───────┬────────────────────────────────────────────────────┬───────────┬────────────────────────────────────────────────────┐\\s+"
        + "\\│ STORY \\│ SCENARIO                                           \\│ STEP      \\│ ERROR MESSAGE                                      \\│\\s+"
        + "\\│       \\│                                                    \\│           \\│                                                    \\│\\s+"
        + "├───────┼────────────────────────────────────────────────────┼───────────┼────────────────────────────────────────────────────┤\\s+"
        + "\\│ aaaaa \\│ This is a very long message that should be wrapped \\│ When I do \\│ This is a very long message that should be wrapped \\│\\s+"
        + "\\│       \\│ to defined cell size and with special char: %      \\│ \\|k\\|       \\│ to defined cell size and with special char: %      \\│\\s+"
        + "\\│       \\│                                                    \\│ \\|v\\|       \\│                                                    \\│\\s+"
        + "\\│       \\│                                                    \\│           \\│                                                    \\│\\s+"
        + "\\│ first \\│ verify                                             \\│ do        \\│ failure                                            \\│\\s+"
        + "\\│       \\│                                                    \\│           \\│                                                    \\│\\s+"
        + "└───────┴────────────────────────────────────────────────────┴───────────┴────────────────────────────────────────────────────┘",
                        Arrays.asList(createFailure("first", "verify", "do", "failure"),
                                      createFailure("aaaaa", MESSAGE, "When I do\n\r|k|\n|v|", MESSAGE))));
    }

    @ParameterizedTest
    @MethodSource("sourceOfFailures")
    @SuppressWarnings({ "MultipleStringLiterals", "MultipleStringLiteralsExtended"})
    void shouldLogMetadata(String failuresMessage, List<Failure> failures)
    {
        Statistic statistic = new Statistic();
        statistic.incrementBroken();
        statistic.incrementFailed();
        statistic.incrementKnownIssue();
        statistic.incrementPassed();
        try (MockedStatic<StatisticsStoryReporter> reporter = mockStatic(StatisticsStoryReporter.class))
        {
            reporter.when(StatisticsStoryReporter::getStatistics).thenReturn(
                    Map.of(NodeType.STORY, statistic,
                           NodeType.SCENARIO, statistic,
                           NodeType.STEP, statistic));
            reporter.when(StatisticsStoryReporter::getFailures).thenReturn(failures);
            TestInfoLogger.logEnvironmentMetadata();
            ImmutableList<LoggingEvent> loggingEvents = LOGGER.getLoggingEvents();
            assertThat(loggingEvents, hasSize(1));
            assertThat(loggingEvents.get(0).getMessage(), matchesRegex(
                      "(?s)\\s+"
                      + "-{60}\\s+"
                      + " Configuration:\\s+"
                      + "-{60}\\s+"
                      + " Profile:\\s+"
                      + "-{60}\\s+"
                      + " Suite:\\s+"
                      + "-{60}\\s+"
                      + " Environment:\\s+"
                      + "-{60}\\s+"
                      + " Vividus:.*"
                      + " Execution statistics:\\s+.+"
                      + "-{40}\\s+"
                      + "               Story   Scenario     Step\\s+"
                      + "-{40}\\s+"
                      + "Passed            1          1        1\\s+"
                      + "Failed            1          1        1\\s+"
                      + "Broken            1          1        1\\s+"
                      + "Known Issue       1          1        1\\s+"
                      + "Pending           0          0        0\\s+"
                      + "Skipped           0          0        0\\s+"
                      + "-{40}\\s+"
                      + "TOTAL             4          4        4"
                      + failuresMessage));
        }
    }

    private static Failure createFailure(String storyName, String scenatioTitle, String step, String message)
    {
        RunningStory runningStory = mock(RunningStory.class);
        RunningScenario runningScenario = mock(RunningScenario.class);
        when(runningStory.getRunningScenario()).thenReturn(runningScenario);
        when(runningScenario.getTitle()).thenReturn(scenatioTitle);
        when(runningStory.getName()).thenReturn(storyName);
        when(runningStory.getRunningSteps()).thenReturn(new LinkedList<>(List.of(step)));
        return Failure.from(runningStory, message);
    }

    @Test
    void shouldPrintBanner()
    {
        TestInfoLogger.drawBanner();
        assertThat(LOGGER.getLoggingEvents(), is(List.of(info("\n{}", ResourceUtils.loadResource("banner.vividus")))));
    }

    @Test
    void shouldPringExecutionPlan()
    {
        Map<String, List<String>> executionPlan = new LinkedHashMap<>();
        executionPlan.put("batch-1", List.of("path1"));
        executionPlan.put("batch-2", List.of("path2", "path3"));
        executionPlan.put("batch-3", List.of());

        TestInfoLogger.logExecutionPlan(executionPlan);

        ImmutableList<LoggingEvent> loggingEvents = LOGGER.getLoggingEvents();
        assertThat(loggingEvents, hasSize(1));
        assertThat(loggingEvents.get(0).getMessage(), matchesRegex(
                "(?s)\\s*"
                + "-{60}\\s+"
                + " Execution plan \\(before filtering by meta\\):\\s*"
                + "   batch-1:\\s*"
                + "     path1\\s*"
                + "   batch-2:\\s*"
                + "     path2\\s*"
                + "     path3\\s*"
                + "   batch-3:\\s*"
                + "     \\[no stories found\\]\\s*"
                + "-{60}"));
    }
}
