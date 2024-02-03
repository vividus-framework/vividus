/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.reportportal.jbehave;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import com.epam.reportportal.jbehave.ReportPortalScenarioStoryReporter;
import com.epam.reportportal.jbehave.ReportPortalStepStoryReporter;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.common.eventbus.EventBus;

import org.jbehave.core.reporters.DelegatingStoryReporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.reportportal.jbehave.AdaptedReportPortalFormat.TestEntity;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AdaptedReportPortalFormatTests
{
    @Mock private EventBus eventBus;
    @InjectMocks private AdaptedReportPortalFormat adaptedReportPortalFormat;

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(AdaptedReportPortalFormat.class);

    @Test
    void shouldCreateScenarioReporter()
    {
        adaptedReportPortalFormat.setTestEntity(TestEntity.SCENARIO);
        var reporter = (DelegatingStoryReporter) adaptedReportPortalFormat.createStoryReporter(null,
                null);
        assertInstanceOf(ReportPortalScenarioStoryReporter.class, reporter.getDelegates().iterator().next());
        verify(eventBus).register(reporter);
        assertThat(testLogger.getLoggingEvents(), is(empty()));
    }

    @Test
    void shouldCreateStepReporter()
    {
        adaptedReportPortalFormat.setTestEntity(TestEntity.STEP);
        var reporter = (DelegatingStoryReporter) adaptedReportPortalFormat.createStoryReporter(null,
                null);
        assertInstanceOf(ReportPortalStepStoryReporter.class, reporter.getDelegates().iterator().next());
        assertThat(testLogger.getLoggingEvents(), is(List.of(info(
                "The reporting of steps as ReportPortal test cases is deprecated. As such, the property "
                        + "'system.rp.test-entity' is deprecated and will be removed in VIVIDUS 0.8.0. "
                        + "The default behavior will be to report scenarios as test cases"
        ))));
    }

    @Test
    void shouldThrowAnExceptionWhenUnsupportedApiIsCalled()
    {
        assertThrows(UnsupportedOperationException.class,
                () -> adaptedReportPortalFormat.createReportPortalReporter(null, null));
        verifyNoInteractions(eventBus);
    }
}
