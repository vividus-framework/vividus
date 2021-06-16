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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.epam.reportportal.jbehave.JBehaveUtils;

import org.jbehave.core.model.Step;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AdaptedReportPortalStoryReporterTests
{
    private static final String STEP = "When I play Starfield";

    private final AdaptedReportPortalStoryReporter reporter = new AdaptedReportPortalStoryReporter();

    @Test
    void shouldDelegateToParentWithAStepAsAString()
    {
        try (MockedStatic<JBehaveUtils> jbehaveUtils = mockStatic(JBehaveUtils.class))
        {
            Step step = mock(Step.class);
            when(step.getStepAsString()).thenReturn(STEP);
            reporter.beforeStep(step);
            jbehaveUtils.verify(() -> JBehaveUtils.startStep(STEP));
        }
    }
}
