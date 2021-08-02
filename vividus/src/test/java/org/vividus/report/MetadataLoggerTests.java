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

package org.vividus.report;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
import org.vividus.bdd.StatisticsStoryReporter;
import org.vividus.bdd.model.Failure;
import org.vividus.bdd.model.NodeType;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.bdd.model.Statistic;
import org.vividus.util.ResourceUtils;

@ExtendWith(TestLoggerFactoryExtension.class)
class MetadataLoggerTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(MetadataLogger.class);
    private static final String FORMAT = "{}={}";
    private static final String MESSAGE = "This is a very long message that should be wrapped to defined cell size";

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
        MetadataLogger.logPropertiesSecurely(properties);
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
        MetadataLogger.logPropertiesSecurely(properties);
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
        + "\\│       \\│ to defined cell size                               \\│ \\|k\\|       \\│ to defined cell size                               \\│\\s+"
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
            MetadataLogger.logEnvironmentMetadata();
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
        MetadataLogger.drawBanner();
        assertThat(LOGGER.getLoggingEvents(), is(List.of(info("\n{}", ResourceUtils.loadResource("banner.vividus")))));
    }
}
