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

package org.vividus.reportportal.jbehave;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.eventbus.EventBus;

import org.jbehave.core.reporters.DelegatingStoryReporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.reportportal.jbehave.AdaptedReportPortalFormat.TestEntity;

@ExtendWith(MockitoExtension.class)
class AdaptedReportPortalFormatTests
{
    @Mock private EventBus eventBus;

    @ParameterizedTest
    @CsvSource({
            "SCENARIO, com.epam.reportportal.jbehave.ReportPortalScenarioStoryReporter",
            "STEP,     com.epam.reportportal.jbehave.ReportPortalStepStoryReporter"
    })
    void shouldCreateSpecificReporter(TestEntity testEntity, Class<?> expectedClass) throws IllegalAccessException
    {
        AdaptedReportPortalFormat adaptedReportPortalFormat = new AdaptedReportPortalFormat(eventBus);
        adaptedReportPortalFormat.setTestEntity(testEntity);
        assertEquals(expectedClass,
                ((DelegatingStoryReporter) adaptedReportPortalFormat.createStoryReporter(null,
                        null)).getDelegates().iterator().next().getClass());
    }

    @Test
    void shouldThrowAnExceptionWhenUnsupportedApiIsCalled()
    {
        AdaptedReportPortalFormat adaptedReportPortalFormat = new AdaptedReportPortalFormat(eventBus);
        assertThrows(UnsupportedOperationException.class,
                () -> adaptedReportPortalFormat.createReportPortalReporter(null, null));
    }
}
