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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.StdErr;
import org.junitpioneer.jupiter.StdIo;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.report.allure.AllureStoryReporter;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
class AllureLogAppenderTests
{
    private static final String NAME = "Name";

    @Mock private Filter filter;
    @Mock private Layout<?> layout;

    @Test
    @StdIo
    // Need to be sure static variable org.apache.logging.log4j.status.StatusLogger.STATUS_LOGGER was not initialized
    // with default value of System.err
    @Order(1)
    void shouldNotCreateAppenderWithNullName(StdErr stdErr)
    {
        var appender = AllureLogAppender.createAppender(null, filter, layout);
        assertThat(stdErr.capturedLines(),
                arrayContaining("ERROR StatusLogger No name provided for AllureLogAppender"));
        assertNull(appender);
    }

    @Test
    void shouldCreateAppenderWithNonNullName()
    {
        var appender = AllureLogAppender.createAppender(NAME, filter, layout);
        assertNotNull(appender);
        assertEquals(NAME, appender.getName());
        assertEquals(filter, appender.getFilter());
        assertEquals(layout, appender.getLayout());
    }

    @Test
    void shouldBeSingleton()
    {
        var appender = AllureLogAppender.createAppender(NAME, filter, layout);
        assertNotNull(appender);
        assertEquals(appender, AllureLogAppender.getInstance());
    }

    @Test
    void shouldNotAppendLogEventIfAllureStoryReporterIsNotSet()
    {
        var appender = AllureLogAppender.createAppender(NAME, filter, layout);
        var logEvent = mock(LogEvent.class);
        appender.append(logEvent);
        verifyNoInteractions(logEvent);
    }

    @Test
    void shouldAddLogStepIfAllureStoryReporterIsSet()
    {
        var appender = AllureLogAppender.createAppender(NAME, filter, layout);
        final var allureStoryReporter = mock(AllureStoryReporter.class);
        appender.setAllureStoryReporter(allureStoryReporter);
        var logEvent = mock(LogEvent.class);
        final var logEntry = "message";
        final var logLevel = Level.INFO;
        when(layout.toByteArray(logEvent)).thenReturn(logEntry.getBytes(StandardCharsets.UTF_8));
        when(logEvent.getLevel()).thenReturn(logLevel);
        appender.append(logEvent);
        verify(allureStoryReporter).addLogStep(logLevel.name(), logEntry);
    }
}
