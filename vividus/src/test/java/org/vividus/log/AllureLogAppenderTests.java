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

package org.vividus.log;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.SystemStreamTests;
import org.vividus.report.allure.AllureStoryReporter;

@ExtendWith(MockitoExtension.class)
class AllureLogAppenderTests extends SystemStreamTests
{
    private static final String NAME = "Name";

    @Mock private Filter filter;
    @Mock private Layout<?> layout;

    @Test
    void shouldNotCreateAppenderWithNullName()
    {
        AllureLogAppender appender = AllureLogAppender.createAppender(null, filter, layout);
        assertThat(getErrStreamContent(), containsString("No name provided for AllureLogAppender"));
        assertNull(appender);
    }

    @Test
    void shouldCreateAppenderWithNonNullName()
    {
        AllureLogAppender appender = AllureLogAppender.createAppender(NAME, filter, layout);
        assertNotNull(appender);
        assertEquals(NAME, appender.getName());
        assertEquals(filter, appender.getFilter());
        assertEquals(layout, appender.getLayout());
    }

    @Test
    void shouldBeSingleton()
    {
        AllureLogAppender appender = AllureLogAppender.createAppender(NAME, filter, layout);
        assertNotNull(appender);
        assertEquals(appender, AllureLogAppender.getInstance());
    }

    @Test
    void shouldNotAppendLogEventIfAllureStoryReporterIsNotSet()
    {
        AllureLogAppender appender = AllureLogAppender.createAppender(NAME, filter, layout);
        LogEvent logEvent = mock(LogEvent.class);
        appender.append(logEvent);
        verifyNoInteractions(logEvent);
    }

    @Test
    void shouldAddLogStepIfAllureStoryReporterIsSet()
    {
        AllureLogAppender appender = AllureLogAppender.createAppender(NAME, filter, layout);
        final AllureStoryReporter allureStoryReporter = mock(AllureStoryReporter.class);
        appender.setAllureStoryReporter(allureStoryReporter);
        LogEvent logEvent = mock(LogEvent.class);
        final String logEntry = "message";
        final Level logLevel = Level.INFO;
        when(layout.toByteArray(logEvent)).thenReturn(logEntry.getBytes(StandardCharsets.UTF_8));
        when(logEvent.getLevel()).thenReturn(logLevel);
        appender.append(logEvent);
        verify(allureStoryReporter).addLogStep(logLevel.name(), logEntry);
    }
}
