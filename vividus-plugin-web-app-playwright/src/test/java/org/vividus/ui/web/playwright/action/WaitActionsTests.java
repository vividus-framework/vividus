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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.function.Supplier;

import com.microsoft.playwright.TimeoutError;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.SoftAssert;
import org.vividus.ui.web.playwright.assertions.PlaywrightSoftAssert;

@ExtendWith(MockitoExtension.class)
class WaitActionsTests
{
    private static final String CHECKED_CONDITION = "checked condition";
    private static final String TIMEOUT_ERROR_MESSAGE = "Timeout error message";
    private static final String FAILED_ASSERTION_DESCRIPTION = "Failed wait condition: " + CHECKED_CONDITION;
    private static final String PASSED_ASSERTION_DESCRIPTION = "Passed wait condition: " + CHECKED_CONDITION;

    @Mock private Runnable timeoutOperation;

    @Mock private SoftAssert softAssert;
    @Mock private PlaywrightSoftAssert playwrightSoftAssert;
    @InjectMocks private WaitActions waitActions;

    @Test
    void shouldRunWithTimeoutAssertionWithSuccessfullyAssertion()
    {
        waitActions.runWithTimeoutAssertion(CHECKED_CONDITION, timeoutOperation);
        verify(timeoutOperation).run();
        verify(softAssert).recordPassedAssertion(PASSED_ASSERTION_DESCRIPTION);
    }

    @Test
    void shouldRunWithTimeoutAssertionWithFailedAssertion()
    {
        var timeoutError = new TimeoutError(TIMEOUT_ERROR_MESSAGE);
        doThrow(timeoutError).when(timeoutOperation).run();
        waitActions.runWithTimeoutAssertion(CHECKED_CONDITION, timeoutOperation);
        verify(timeoutOperation).run();
        verify(softAssert).recordFailedAssertion(
                String.format("%s. %s", FAILED_ASSERTION_DESCRIPTION, TIMEOUT_ERROR_MESSAGE), timeoutError);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldRunTimeoutPlaywrightAssertion()
    {
        doNothing().when(playwrightSoftAssert).runAssertion(
                argThat((Supplier<String> description) -> description.get().equals(FAILED_ASSERTION_DESCRIPTION)),
                argThat(runnable -> {
                    runnable.run();
                    return true;
                }));
        waitActions.runTimeoutPlaywrightAssertion(() -> CHECKED_CONDITION, timeoutOperation);
        verify(softAssert).recordPassedAssertion(PASSED_ASSERTION_DESCRIPTION);
    }
}
