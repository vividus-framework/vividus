/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.softassert.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.softassert.model.SoftAssertionError;

@ExtendWith(MockitoExtension.class)
class VerificationErrorTests
{
    private static final String TEXT = "text";
    private final List<SoftAssertionError> softAssertionErrors = new ArrayList<>();
    private final Set<KnownIssue> knownIssues = new HashSet<>();

    private VerificationError verificationError;

    @Mock
    private KnownIssue knownIssue;

    @Mock
    private SoftAssertionError softAssertionError;

    @BeforeEach
    void beforeEach()
    {
        verificationError = new VerificationError(TEXT, softAssertionErrors);
        softAssertionErrors.add(softAssertionError);
    }

    @Test
    void testGetErrorsMessage()
    {
        knownIssues.add(knownIssue);
        when(softAssertionError.getKnownIssue()).thenReturn(knownIssue);
        assertEquals(verificationError.getKnownIssues(), knownIssues);
    }

    @Test
    void testGetErrorsMessageIfKnownIssueIsNull()
    {
        when(softAssertionError.getKnownIssue()).thenReturn(null);
        assertEquals(verificationError.getKnownIssues(), knownIssues);
    }

    @Test
    void testIsOngoingKnownIssuesOnlyIsFalse()
    {
        when(softAssertionError.isKnownIssue()).thenReturn(false);
        assertFalse(verificationError.isOngoingKnownIssuesOnly());
    }

    @Test
    void testIsOngoingKnownIssuesOnlyIsTrueWhenKnownIssueDoesNotFixed()
    {
        when(softAssertionError.isKnownIssue()).thenReturn(true);
        when(softAssertionError.getKnownIssue()).thenReturn(knownIssue);
        when(knownIssue.isFixed()).thenReturn(false);
        assertTrue(verificationError.isOngoingKnownIssuesOnly());
    }

    @Test
    void testIsOngoingKnownIssuesOnlyIsFalseWhenKnownIssueIsFixed()
    {
        when(softAssertionError.isKnownIssue()).thenReturn(true);
        when(softAssertionError.getKnownIssue()).thenReturn(knownIssue);
        when(knownIssue.isFixed()).thenReturn(true);
        assertFalse(verificationError.isOngoingKnownIssuesOnly());
    }
}
