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

package org.vividus.log;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.reporter.environment.EnvironmentConfigurer;
import org.vividus.reporter.environment.PropertyCategory;
import org.vividus.results.ResultsProvider;
import org.vividus.results.model.ExecutableEntity;
import org.vividus.results.model.Failure;
import org.vividus.results.model.Statistic;
import org.vividus.util.ResourceUtils;

@ExtendWith(TestLoggerFactoryExtension.class)
class TestInfoLoggerTests
{
    private static final String FORMAT = "Properties and environment variables:{}";
    private static final String NEW_LINE = System.lineSeparator();
    private static final char EQUAL_SIGN = '=';
    private static final String MESSAGE = "This is a very long message that should be wrapped to defined cell size "
            + "and with special char: %";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(TestInfoLogger.class);

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
        var properties = new Properties();
        properties.put(key, "value");
        TestInfoLogger.logPropertiesSecurely(properties);
        assertThat(logger.getLoggingEvents(), is(List.of(info(FORMAT, NEW_LINE + key + EQUAL_SIGN + "****"))));
    }

    @Test
    void testLogPropertiesSecurelyPlainProperties()
    {
        var keyString = "simple.ignore-failure";
        var valueString = "string";
        var keyInt = "simple.keys-int";
        int valueInt = 2;
        var properties = new Properties();
        properties.put(keyString, valueString);
        properties.put(keyInt, valueInt);
        TestInfoLogger.logPropertiesSecurely(properties);
        assertThat(logger.getLoggingEvents(), is(List.of(info(FORMAT,
                NEW_LINE + keyString + EQUAL_SIGN + valueString + NEW_LINE + keyInt + EQUAL_SIGN + valueInt)
        )));
    }

    @SuppressWarnings({ "MultipleStringLiterals", "MultipleStringLiteralsExtended", "LineLength"})
    private static Stream<Arguments> sourceOfFailures()
    {
        return Stream.of(
                Arguments.of("", Optional.empty()),
                Arguments.of(String.format("%n%n No Failures & Errors!"), Optional.of(List.of())),
                Arguments.of(
        "\\s+ Failures & Errors:\\s+"
        + "\\+-------\\+-----------------------------------\\+-----------\\+----------------------------------------------------\\+\\s+"
        + "\\| STORY \\| SCENARIO                          \\| STEP      \\| ERROR MESSAGE                                      \\|\\s+"
        + "\\|       \\|                                   \\|           \\|                                                    \\|\\s+"
        + "\\+-------\\+-----------------------------------\\+-----------\\+----------------------------------------------------\\+\\s+"
        + "\\| aaaaa \\| This is a very long message that  \\| When I do \\| This is a very long message that should be wrapped \\|\\s+"
        + "\\|       \\| should be wrapped to defined cell \\| \\|k\\|       \\| to defined cell size and with special char: %      \\|\\s+"
        + "\\|       \\| size and with special char: %     \\| \\|v\\|       \\|                                                    \\|\\s+"
        + "\\|       \\|                                   \\|           \\|                                                    \\|\\s+"
        + "\\| first \\| verify                            \\| do        \\| failure                                            \\|\\s+"
        + "\\|       \\|                                   \\|           \\|                                                    \\|\\s+"
        + "\\+-------\\+-----------------------------------\\+-----------\\+----------------------------------------------------\\+\\s+",
                        Optional.of(Arrays.asList(createFailure("first", "verify", "do", "failure"),
                                      createFailure("aaaaa", MESSAGE, "When I do\n\r|k|\n|v|", MESSAGE)))));
    }

    @ParameterizedTest
    @MethodSource("sourceOfFailures")
    @Order(1)
    void shouldLogMetadata(String failuresMessage, Optional<List<Failure>> failures)
    {
        shouldLogMetadata(" Configuration:\\s+", failuresMessage, failures);
    }

    @Test
    @Order(2)
    @SuppressWarnings({ "MultipleStringLiterals", "MultipleStringLiteralsExtended", "PMD.AvoidDuplicateLiterals"})
    void shouldLogMetadataWithConfigurationSet()
    {
        EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION.get(PropertyCategory.CONFIGURATION).put("Set", "active");
        shouldLogMetadata(" Configuration set:\\s+active\\s+", "", Optional.empty());
    }

    @SuppressWarnings({ "MultipleStringLiterals", "MultipleStringLiteralsExtended", "PMD.AvoidDuplicateLiterals"})
    void shouldLogMetadata(String configurationLine, String failuresMessage, Optional<List<Failure>> failures)
    {
        var statistic = new Statistic();
        statistic.incrementBroken();
        statistic.incrementFailed();
        statistic.incrementKnownIssue();
        statistic.incrementPassed();
        var statisticsProvider = mock(ResultsProvider.class);
        when(statisticsProvider.getStatistics()).thenReturn(
                Map.of(ExecutableEntity.STORY, statistic,
                       ExecutableEntity.SCENARIO, statistic,
                       ExecutableEntity.STEP, statistic)
        );
        when(statisticsProvider.getFailures()).thenReturn(failures);
        new TestInfoLogger(statisticsProvider).logTestExecutionResults();
        var loggingEvents = logger.getLoggingEvents();
        assertThat(loggingEvents, hasSize(1));
        assertThat(loggingEvents.get(0).getMessage(), matchesRegex(
                "(?s)\\s+"
                        + "-{80}\\s+"
                        + configurationLine
                        + "-{80}\\s+"
                        + " Profile:\\s+"
                        + "-{80}\\s+"
                        + " Suite:\\s+"
                        + "-{80}\\s+"
                        + " Environment:\\s+"
                        + "-{80}\\s+"
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
                        + "TOTAL             4          4        4\\s+"
                        + "-{40}"
                        + failuresMessage));
    }

    private static Failure createFailure(String storyName, String scenarioTitle, String step, String message)
    {
        var runningStory = mock(RunningStory.class);
        var runningScenario = mock(RunningScenario.class);
        when(runningStory.getRunningScenario()).thenReturn(runningScenario);
        when(runningScenario.getTitle()).thenReturn(scenarioTitle);
        when(runningStory.getName()).thenReturn(storyName);
        when(runningStory.getRunningSteps()).thenReturn(new LinkedList<>(List.of(step)));
        return Failure.from(runningStory, message);
    }

    @Test
    void shouldPrintBanner()
    {
        TestInfoLogger.drawBanner();
        assertThat(logger.getLoggingEvents(), is(List.of(info("\n{}", ResourceUtils.loadResource("banner.vividus")))));
    }

    @Test
    void shouldPrintExecutionPlan()
    {
        Map<String, List<String>> executionPlan = new LinkedHashMap<>();
        executionPlan.put("batch-1", List.of("path1"));
        executionPlan.put("batch-2", List.of("path2", "path3"));
        executionPlan.put("batch-3", List.of());

        TestInfoLogger.logExecutionPlan(executionPlan);

        var loggingEvents = logger.getLoggingEvents();
        assertThat(loggingEvents, hasSize(1));
        assertThat(loggingEvents.get(0).getMessage(), matchesRegex(
                "(?s)\\s*"
                + "-{80}\\s+"
                + " Execution plan \\(before filtering by meta\\):\\s*"
                + "   batch-1:\\s*"
                + "     path1\\s*"
                + "   batch-2:\\s*"
                + "     path2\\s*"
                + "     path3\\s*"
                + "   batch-3:\\s*"
                + "     \\[no stories found\\]\\s*"
                + "-{80}"));
    }
}
