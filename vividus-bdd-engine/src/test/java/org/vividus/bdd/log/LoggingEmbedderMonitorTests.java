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

package org.vividus.bdd.log;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Properties;
import java.util.concurrent.ExecutorService;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.failures.BatchFailures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestLoggerFactoryExtension.class)
class LoggingEmbedderMonitorTests
{
    private final TestLogger logger = TestLoggerFactory.getTestLogger(LoggingEmbedderMonitor.class);

    private final LoggingEmbedderMonitor monitor = new LoggingEmbedderMonitor();

    @Test
    void shouldSkipLoggingOfBatchFailed()
    {
        var batchFailures = mock(BatchFailures.class);
        monitor.batchFailed(batchFailures);
        assertThat(logger.getLoggingEvents(), is(empty()));
        verifyNoInteractions(batchFailures);
    }

    @Test
    void shouldSkipLoggingOfRunningStory()
    {
        monitor.runningStory("story/path");
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @Test
    void shouldSkipLoggingOfProcessingSystemProperties()
    {
        var properties = mock(Properties.class);
        monitor.processingSystemProperties(properties);
        assertThat(logger.getLoggingEvents(), is(empty()));
        verifyNoInteractions(properties);
    }

    @Test
    void shouldSkipLoggingOfUsingExecutorService()
    {
        var executorService = mock(ExecutorService.class);
        monitor.usingExecutorService(executorService);
        assertThat(logger.getLoggingEvents(), is(empty()));
        verifyNoInteractions(executorService);
    }

    @Test
    void shouldSkipLoggingOfUsingControls()
    {
        var embedderControls = mock(EmbedderControls.class);
        monitor.usingControls(embedderControls);
        assertThat(logger.getLoggingEvents(), is(empty()));
        verifyNoInteractions(embedderControls);
    }
}
