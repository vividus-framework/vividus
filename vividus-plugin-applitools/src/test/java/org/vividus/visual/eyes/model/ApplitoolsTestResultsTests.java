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

package org.vividus.visual.eyes.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.applitools.eyes.AccessibilityGuidelinesVersion;
import com.applitools.eyes.AccessibilityLevel;
import com.applitools.eyes.AccessibilityStatus;
import com.applitools.eyes.SessionAccessibilityStatus;
import com.applitools.eyes.StepInfo;
import com.applitools.eyes.StepInfo.AppUrls;
import com.applitools.eyes.TestResults;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplitoolsTestResultsTests
{
    @Mock private TestResults testResults;

    @ParameterizedTest
    @CsvSource({
        "Passed, true",
        "Failed, false"
    })
    void shouldConsiderAccessibilityStatus(AccessibilityStatus accessibilityStatus, boolean passed)
    {
        when(testResults.isPassed()).thenReturn(true);
        SessionAccessibilityStatus status = mock(SessionAccessibilityStatus.class);
        when(status.getLevel()).thenReturn(AccessibilityLevel.AA);
        when(status.getStatus()).thenReturn(accessibilityStatus);
        when(status.getVersion()).thenReturn(AccessibilityGuidelinesVersion.WCAG_2_0);
        when(testResults.getAccessibilityStatus()).thenReturn(status);
        StepInfo info = mock(StepInfo.class);
        when(testResults.getStepsInfo()).thenReturn(new StepInfo[] { info });
        AppUrls appUrls = mock(AppUrls.class);
        when(info.getAppUrls()).thenReturn(appUrls);
        when(appUrls.getStepEditor()).thenReturn("http://example.com");

        assertEquals(passed, new ApplitoolsTestResults(testResults).isPassed());
    }

    @Test
    void shouldBePassed()
    {
        when(testResults.isPassed()).thenReturn(true);
        assertTrue(new ApplitoolsTestResults(testResults).isPassed());
    }
}
