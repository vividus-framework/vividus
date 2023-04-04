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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.function.Consumer;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(TestLoggerFactoryExtension.class)
class LoggingStoryReporterTests
{
    private final TestLogger logger = TestLoggerFactory.getTestLogger(LoggingStoryReporter.class);

    private final LoggingStoryReporter reporter = new LoggingStoryReporter();

    static Stream<Arguments> invocationsToSkipLogging()
    {
        return Stream.of(
                arguments((Consumer<LoggingStoryReporter>) reporter -> reporter.pendingMethods(null)),
                arguments((Consumer<LoggingStoryReporter>) reporter -> reporter.beforeStorySteps(null, null)),
                arguments((Consumer<LoggingStoryReporter>) reporter -> reporter.afterStorySteps(null, null)),
                arguments((Consumer<LoggingStoryReporter>) reporter -> reporter.beforeScenarioSteps(null, null)),
                arguments((Consumer<LoggingStoryReporter>) reporter -> reporter.afterScenarioSteps(null, null))
        );
    }

    @MethodSource("invocationsToSkipLogging")
    @ParameterizedTest
    void shouldNotLogAnythingForInvocation(Consumer<LoggingStoryReporter> test)
    {
        test.accept(reporter);
        assertThat(logger.getLoggingEvents(), is(empty()));
    }
}
