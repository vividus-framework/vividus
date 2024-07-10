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

package org.vividus.ui.web.playwright.action;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.microsoft.playwright.TimeoutError;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.SoftAssert;

@ExtendWith(MockitoExtension.class)
class WaitActionsTests
{
    private static final String CHECKED_CONDITION = "checked condition";
    private static final String TIMEOUT_ERROR_MESSAGE = "Timeout error message";

    @Mock private Runnable timeoutOperation;

    @Mock private SoftAssert softAssert;
    @InjectMocks private WaitActions waitActions;

    @Test
    void shouldRunWithTimeoutAssertionWithSuccessfullyAssertion()
    {
        waitActions.runWithTimeoutAssertion(CHECKED_CONDITION, timeoutOperation);
        verify(timeoutOperation).run();
        verify(softAssert).recordPassedAssertion("Passed wait condition: " + CHECKED_CONDITION);
    }

    @Test
    void shouldRunWithTimeoutAssertionWithFailedAssertion()
    {
        var timeoutError = new TimeoutError(TIMEOUT_ERROR_MESSAGE);
        doThrow(timeoutError).when(timeoutOperation).run();
        waitActions.runWithTimeoutAssertion(CHECKED_CONDITION, timeoutOperation);
        verify(timeoutOperation).run();
        verify(softAssert).recordFailedAssertion(
                String.format("Failed wait condition: %s. %s", CHECKED_CONDITION, TIMEOUT_ERROR_MESSAGE), timeoutError);
        verifyNoMoreInteractions(softAssert);
    }
}
