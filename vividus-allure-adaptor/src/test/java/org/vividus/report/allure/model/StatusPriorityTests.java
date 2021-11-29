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

package org.vividus.report.allure.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.softassert.model.SoftAssertionError;

import io.qameta.allure.model.Status;

class StatusPriorityTests
{
    @Test
    void testGetLowest()
    {
        assertEquals(StatusPriority.NOT_COVERED, StatusPriority.getLowest());
    }

    @Test
    void testFromStatus()
    {
        assertEquals(StatusPriority.PASSED, StatusPriority.from(Status.PASSED));
    }

    @ParameterizedTest
    @CsvSource({"false,false,FAILED", "true,false,KNOWN_ISSUES_ONLY", "true,true,FAILED"})
    void shoudCreateStatusPriorityFromAssertionFailedEvent(boolean known, boolean fixed, StatusPriority expected)
    {
        AssertionFailedEvent event = mock(AssertionFailedEvent.class);
        SoftAssertionError softAssertionError = mock(SoftAssertionError.class, withSettings().lenient());
        KnownIssue knownIssue = mock(KnownIssue.class, withSettings().lenient());
        when(softAssertionError.isKnownIssue()).thenReturn(known);
        when(softAssertionError.getKnownIssue()).thenReturn(knownIssue);
        when(knownIssue.isFixed()).thenReturn(fixed);
        when(event.getSoftAssertionError()).thenReturn(softAssertionError);
        assertEquals(expected, StatusPriority.from(event));
    }
}
