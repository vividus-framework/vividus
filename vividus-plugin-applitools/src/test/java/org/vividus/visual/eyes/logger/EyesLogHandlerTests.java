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

package org.vividus.visual.eyes.logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.applitools.eyes.logging.TraceLevel;
import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(TestLoggerFactoryExtension.class)
class EyesLogHandlerTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(EyesLogHandler.class);

    static Stream<Arguments> loggingLevels()
    {
        return Stream.of(
                arguments(TraceLevel.Debug,  (Function<String, LoggingEvent>) LoggingEvent::debug),
                arguments(TraceLevel.Info,   (Function<String, LoggingEvent>) LoggingEvent::info),
                arguments(TraceLevel.Notice, (Function<String, LoggingEvent>) LoggingEvent::info),
                arguments(null,              (Function<String, LoggingEvent>) LoggingEvent::info),
                arguments(TraceLevel.Warn,   (Function<String, LoggingEvent>) LoggingEvent::warn),
                arguments(TraceLevel.Error,  (Function<String, LoggingEvent>) LoggingEvent::error)
        );
    }

    @ParameterizedTest
    @MethodSource("loggingLevels")
    void shouldReportMessagesAtProperLevel(TraceLevel level, Function<String, LoggingEvent> eventProducer)
    {
        String logString = "message";
        new EyesLogHandler(EyesLogHandler.class).onMessage(level, logString);
        assertThat(LOGGER.getLoggingEvents(), is(List.of(eventProducer.apply(logString))));
    }
}
