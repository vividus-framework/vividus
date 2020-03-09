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

import static com.github.valfirst.slf4jtest.LoggingEvent.debug;
import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestLoggerFactoryExtension.class)
class EyesLogHandlerTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(EyesLogHandler.class);

    private final EyesLogHandler eyesLogHandler = new EyesLogHandler(EyesLogHandler.class);

    @Test
    void shouldReportVerboseMessagesAtDebugLevel()
    {
        String logString = "debug message";
        eyesLogHandler.onMessage(true, logString);
        assertThat(LOGGER.getLoggingEvents(), is(List.of(debug(logString))));
    }

    @Test
    void shouldReportNotVerboseMessagesAtInfoLevel()
    {
        String logString = "info message";
        eyesLogHandler.onMessage(false, logString);
        assertThat(LOGGER.getLoggingEvents(), is(List.of(info(logString))));
    }
}
