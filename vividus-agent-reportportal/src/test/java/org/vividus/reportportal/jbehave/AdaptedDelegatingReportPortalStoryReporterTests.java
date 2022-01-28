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

package org.vividus.reportportal.jbehave;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import com.epam.reportportal.jbehave.ReportPortalStoryReporter;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.service.tree.TestItemTree.TestItemLeaf;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.google.common.eventbus.EventBus;

import org.jbehave.core.model.Step;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.model.SoftAssertionError;

import io.reactivex.Maybe;

@ExtendWith(MockitoExtension.class)
class AdaptedDelegatingReportPortalStoryReporterTests
{
    @Mock private ReportPortalStoryReporter reporter;

    @Mock private EventBus eventBus;

    @InjectMocks private AdaptedDelegatingReportPortalStoryReporter adaptedReporter;

    @Test
    void shouldRegisterToEventBus()
    {
        verify(eventBus).register(adaptedReporter);
    }

    @Test
    void shouldDelegateAfterScenarioToWrappedReporter()
    {
        adaptedReporter.afterScenario(null);
        verify(reporter).afterScenario();
    }

    @Test
    void shouldDelegateScenarioNotAllowedToWrappedReporter()
    {
        adaptedReporter.scenarioExcluded(null, null);
        verify(reporter).scenarioNotAllowed(null, null);
    }

    @Test
    void shouldDelegateBeforeStepToWrappedReporter()
    {
        String step = "When I play Starfield";
        adaptedReporter.beforeStep(new Step(StepExecutionType.EXECUTABLE, step));
        verify(reporter).beforeStep(step);
    }

    @Test
    void shouldNotCallBeforeStepForTheComments()
    {
        String step = "!-- 'It just works' T.H.";
        adaptedReporter.beforeStep(new Step(StepExecutionType.COMMENT, step));
        verifyNoInteractions(reporter);
    }

    @Test
    void shouldNotExecuteBeforeStepForSystemTests()
    {
        String step = "afterStories";
        adaptedReporter.beforeStep(new Step(StepExecutionType.EXECUTABLE, step));
        verifyNoInteractions(reporter);
    }

    @Test
    void shouldDelegateSuccessfulToWrappedReporter()
    {
        var successful = "This is successful step";
        adaptedReporter.successful(successful);
        verify(reporter).successful(successful);
    }

    @Test
    void shouldReporterStepAsFailedInCaseOfFailedAssertionError()
    {
        var event = mock(AssertionFailedEvent.class);
        when(reporter.getLastStep()).thenReturn(Optional.of(mock(TestItemLeaf.class)));
        adaptedReporter.onAssertionFailure(event);
        String failed = "When the test is failed";
        adaptedReporter.successful(failed);
        verify(reporter).failed(failed, null);
        verify(reporter, never()).successful(failed);
    }

    @Test
    void shouldSendStacktraceToTheReportPortal()
    {
        var event = mock(AssertionFailedEvent.class);
        var softAssertionError = mock(SoftAssertionError.class);
        when(event.getSoftAssertionError()).thenReturn(softAssertionError);
        when(softAssertionError.getError()).thenReturn(new AssertionError());
        var failedStep = mock(TestItemLeaf.class);
        when(reporter.getLastStep()).thenReturn(Optional.of(failedStep));
        var id = "id";
        var just = Maybe.just(id);
        when(failedStep.getItemId()).thenReturn(just);
        try (MockedStatic<ReportPortal> reportPortal = Mockito.mockStatic(ReportPortal.class))
        {
            adaptedReporter.onAssertionFailure(event);
            reportPortal.verify(() -> ReportPortal.emitLog(eq(just), argThat(f -> {
                SaveLogRQ saveLogRQ = f.apply(id);
                Assertions.assertAll(
                        () -> assertEquals(id, saveLogRQ.getItemUuid()),
                        () -> assertEquals("ERROR", saveLogRQ.getLevel()),
                        () -> assertTrue(saveLogRQ.getLogTime().getTime()
                            <= LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()),
                        () -> assertTrue(saveLogRQ.getMessage()
                            .matches("(?s)java.lang.AssertionError.+(.+\\.java.+)+.+")));
                return true;
            })));
        }
    }
}
