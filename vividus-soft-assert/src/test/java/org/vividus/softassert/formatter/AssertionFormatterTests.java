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

package org.vividus.softassert.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.softassert.model.SoftAssertionError;
import org.vividus.softassert.util.StackTraceFilter;

@ExtendWith(MockitoExtension.class)
class AssertionFormatterTests
{
    private static final String TEXT = "text";
    private static final int ASSERTION_COUNT = 1;
    private final List<SoftAssertionError> softAssertionErrors = new ArrayList<>();

    @Mock
    private KnownIssue knownIssue;

    @Mock
    private SoftAssertionError softAssertionError;

    @Mock
    private AssertionError assertionError;

    @Mock
    private PrintWriter printWriter;

    @InjectMocks
    private AssertionFormatter assertionFormatter;

    @BeforeEach
    void beforeEach()
    {
        softAssertionErrors.add(softAssertionError);
    }

    @Test
    void testGetMessage()
    {
        when(knownIssue.getStatus()).thenReturn(TEXT);
        when(knownIssue.getResolution()).thenReturn(TEXT);
        assertEquals("Known issue: null (Type: null. Status: text. Resolution: text). text",
                assertionFormatter.getMessage(TEXT, knownIssue));
    }

    @Test
    void testGetMessageWithNullStatusAndResolution()
    {
        when(knownIssue.getStatus()).thenReturn(null);
        when(knownIssue.getResolution()).thenReturn(null);
        assertEquals("Known issue: null (Type: null.). text", assertionFormatter.getMessage(TEXT, knownIssue));
    }

    @Test
    void testGetErrorsMessage()
    {
        StackTraceFilter stackTraceFilter = mock(StackTraceFilter.class);
        assertionFormatter.setStackTraceFilter(stackTraceFilter);
        when(softAssertionError.getError()).thenReturn(assertionError);
        assertionError.printStackTrace(printWriter);
        assertionFormatter.getErrorsMessage(softAssertionErrors, true);
        verify(stackTraceFilter).printFilteredStackTrace(ArgumentMatchers.eq(assertionError), any(PrintWriter.class));
    }

    @Test
    void testGetErrorsMessageDoesNotIncludeStackTraceInformation()
    {
        when(softAssertionError.getError()).thenReturn(assertionError);
        assertionFormatter.getErrorsMessage(softAssertionErrors, false);
        verify(assertionError).getMessage();
    }

    @Test
    void testGetErrorsMessageIfStackTraceFilterIsNull()
    {
        when(softAssertionError.getError()).thenReturn(assertionError);
        assertionError.printStackTrace(printWriter);
        assertionFormatter.getErrorsMessage(softAssertionErrors, true);
        verify(assertionError).printStackTrace(printWriter);
    }

    @Test
    void testGetFailedVerificationMessageIfKnownIssueIsNul()
    {
        when(softAssertionError.getKnownIssue()).thenReturn(null);
        assertEquals("Failed verification: 1 of 1 assertions failed. Known issues are not found.",
                assertionFormatter.getFailedVerificationMessage(softAssertionErrors, ASSERTION_COUNT));
    }

    @Test
    void testGetFailedVerificationMessagePotentiallyKnownIssueDoesNotExist()
    {
        when(softAssertionError.getKnownIssue()).thenReturn(knownIssue);
        when(knownIssue.isPotentiallyKnown()).thenReturn(Boolean.FALSE);
        when(knownIssue.getIdentifier()).thenReturn(TEXT);
        assertEquals("Failed verification: 1 of 1 assertions failed. Known issues: [text].",
                assertionFormatter.getFailedVerificationMessage(softAssertionErrors, ASSERTION_COUNT));
    }

    @Test
    void testGetFailedVerificationMessagePotentiallyKnownIssueExists()
    {
        when(softAssertionError.getKnownIssue()).thenReturn(knownIssue);
        when(knownIssue.isPotentiallyKnown()).thenReturn(Boolean.TRUE);
        when(knownIssue.getIdentifier()).thenReturn(TEXT);
        assertEquals("Failed verification: 1 of 1 assertions failed. Known issues are not found. Potentially "
                        + "known issues: [text].",
                assertionFormatter.getFailedVerificationMessage(softAssertionErrors, ASSERTION_COUNT));
    }

    @Test
    void testGetPassedVerificationMessage()
    {
        assertEquals("Passed verification: 1 of 1 assertions passed.",
                assertionFormatter.getPassedVerificationMessage(ASSERTION_COUNT));
    }
}
