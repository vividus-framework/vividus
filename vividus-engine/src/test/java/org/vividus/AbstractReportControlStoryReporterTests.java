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

package org.vividus;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.ReportControlContext;
import org.vividus.context.RunContext;

@ExtendWith(MockitoExtension.class)
class AbstractReportControlStoryReporterTests
{
    @Mock private ReportControlContext reportControlContext;
    @Mock private RunContext runContext;
    @Mock private Runnable runnable;
    @InjectMocks private TestReportControlStoryReporter reporter;

    @ParameterizedTest
    @CsvSource({
        "true, true, 1",
        "true, false, 0",
        "false, true, 0"
    })
    void shouldPerformRunnable(boolean reportEnabled, boolean runCompleted, int times)
    {
        when(reportControlContext.isReportingEnabled()).thenReturn(reportEnabled);
        lenient().when(runContext.isRunInProgress()).thenReturn(runCompleted);

        reporter.perform(runnable);

        verify(runnable, times(times)).run();
    }

    private static final class TestReportControlStoryReporter extends AbstractReportControlStoryReporter
    {
        TestReportControlStoryReporter(ReportControlContext reportControlContext, RunContext runContext)
        {
            super(reportControlContext, runContext);
        }
    }
}
