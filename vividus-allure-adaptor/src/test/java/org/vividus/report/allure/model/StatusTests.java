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

package org.vividus.report.allure.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.exception.VerificationError;
import org.vividus.softassert.issue.KnownIssueIdentifier;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.softassert.model.SoftAssertionError;

class StatusTests
{
    @ParameterizedTest
    @CsvSource({
        "BROKEN,0",
        "FAILED,1",
        "PENDING,2",
        "KNOWN_ISSUES_ONLY,3",
        "SKIPPED,4",
        "PASSED,5",
        "NOT_COVERED,6"
    })
    void shouldReturnPriority(Status status, int expectedPriority)
    {
        assertEquals(expectedPriority, status.getPriority());
    }

    @Test
    void shouldReturnNotCoveredAsALowestPriority()
    {
        assertEquals(Status.NOT_COVERED, Status.getLowest());
    }

    @ParameterizedTest
    @CsvSource({
            "false, FAILED",
            "true,  KNOWN_ISSUES_ONLY"
    })
    void shouldCreateStatusFromAssertionFailedEvent(boolean notFixedKnownIssue, Status expected)
    {
        var event = mock(AssertionFailedEvent.class);
        var softAssertionError = mock(SoftAssertionError.class);
        when(softAssertionError.isNotFixedKnownIssue()).thenReturn(notFixedKnownIssue);
        when(event.getSoftAssertionError()).thenReturn(softAssertionError);
        assertEquals(expected, Status.from(event));
    }

    @Test
    void shouldCreateBrokenStatusFromException()
    {
        assertEquals(Status.BROKEN, Status.from(new IOException()));
    }

    @Test
    void shouldCreateFailedStatusFromAssertionError()
    {
        assertEquals(Status.FAILED, Status.from(new UUIDExceptionWrapper(new AssertionError())));
    }

    @Test
    void shouldCreateKnownIssuesOnlyStatusFromVerificationError()
    {
        var softAssertionError = new SoftAssertionError(null);
        softAssertionError.setKnownIssue(new KnownIssue(null, new KnownIssueIdentifier(), false));
        var verificationError = new VerificationError(null, List.of(softAssertionError));
        assertEquals(Status.KNOWN_ISSUES_ONLY, Status.from(new UUIDExceptionWrapper(verificationError)));
    }

    @Test
    void shouldCreateFailedStatusFromVerificationErrorWithoutKnownIssue()
    {
        var verificationError = new VerificationError(null, List.of(new SoftAssertionError(null)));
        assertEquals(Status.FAILED, Status.from(new UUIDExceptionWrapper(verificationError)));
    }
}
