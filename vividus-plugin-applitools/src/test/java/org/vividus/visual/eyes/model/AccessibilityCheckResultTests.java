/*
 * Copyright 2019-2023 the original author or authors.
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.applitools.eyes.AccessibilityGuidelinesVersion;
import com.applitools.eyes.AccessibilityLevel;
import com.applitools.eyes.AccessibilityStatus;
import com.applitools.eyes.SessionAccessibilityStatus;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AccessibilityCheckResultTests
{
    @ParameterizedTest
    @CsvSource({
        "WCAG_2_0, WCAG 2.0, Passed, true, passed",
        "WCAG_2_1, WCAG 2.1, Failed, false, failed"
    })
    void shouldCreateAccessibilityCheckResult(AccessibilityGuidelinesVersion standard, String name,
            AccessibilityStatus accessibilityStatus, boolean passed, String statusAsString)
    {
        SessionAccessibilityStatus status = mock(SessionAccessibilityStatus.class);
        when(status.getLevel()).thenReturn(AccessibilityLevel.AA);
        when(status.getStatus()).thenReturn(accessibilityStatus);
        when(status.getVersion()).thenReturn(standard);

        AccessibilityCheckResult check = new AccessibilityCheckResult("https://accessibility-testing.com", status);

        assertEquals(name + " - AA", check.getGuideline());
        assertEquals(passed, check.isPassed());
        assertEquals("https://accessibility-testing.com?accessibility=true", check.getUrl());
        assertEquals(statusAsString, check.getStatus());
    }
}
