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

package org.vividus.ui.action;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.Wait;
import org.vividus.softassert.ISoftAssert;

@ExtendWith(MockitoExtension.class)
class WaitActionsTests
{
    private static final long TIMEOUT_VALUE = 1;
    private static final Duration TIMEOUT_SECONDS = Duration.ofSeconds(TIMEOUT_VALUE);
    private static final Duration TIMEOUT_MILLIS = Duration.ofMillis(1);
    private static final String EXCEPTION_TIMEOUT_MESSAGE = "mocked timeout exception message";
    private static final String MOCKED_WAIT_DESCRIPTION = "mocked wait description";

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private IWaitFactory waitFactory;

    @Mock
    private Wait<SearchContext> wait;

    @Mock
    private Function<SearchContext, ?> isTrue;

    @Mock
    private SearchContext searchContext;

    @InjectMocks
    private WaitActions waitActions;

    @Test
    void testWaitWith4Parameters()
    {
        when(wait.toString()).thenReturn(MOCKED_WAIT_DESCRIPTION);
        when(waitFactory.createWait(searchContext, TIMEOUT_SECONDS)).thenReturn(wait);
        waitActions.wait(searchContext, TIMEOUT_SECONDS, isTrue);
        verifySuccess();
    }

    @Test
    void testWaitWith3Parameters()
    {
        when(wait.toString()).thenReturn(MOCKED_WAIT_DESCRIPTION);
        when(waitFactory.createWait(searchContext)).thenReturn(wait);
        waitActions.wait(searchContext, isTrue, true);
        verifySuccess();
    }

    @Test
    void testWaitWith2Parameters()
    {
        when(wait.toString()).thenReturn(MOCKED_WAIT_DESCRIPTION);
        when(waitFactory.createWait(searchContext)).thenReturn(wait);
        waitActions.wait(searchContext, isTrue);
        verifySuccess();
    }

    @Test
    void testWaitWithNullInput()
    {
        SearchContext searchContext = null;
        waitActions.wait(searchContext, isTrue);
        verifyNoInteractions(waitFactory);
        verify(softAssert).assertNotNull("The input value to pass to the wait condition", searchContext);
    }

    @Test
    void testWaitWith4ParametersFailWithTimeoutException()
    {
        when(wait.toString()).thenReturn(MOCKED_WAIT_DESCRIPTION);
        when(waitFactory.createWait(searchContext, TIMEOUT_SECONDS)).thenReturn(wait);
        mockException(TimeoutException.class);
        waitActions.wait(searchContext, TIMEOUT_SECONDS, isTrue);
        verifyFailureRecording();
    }

    @Test
    void testWaitWith4ParametersFailWithTimeoutExceptionAndDoNotRecordAssertionFailure()
    {
        when(waitFactory.createWait(searchContext, TIMEOUT_SECONDS)).thenReturn(wait);
        mockException(TimeoutException.class);
        waitActions.wait(searchContext, TIMEOUT_SECONDS, isTrue, false);
        verifyNoInteractions(softAssert);
    }

    @Test
    void testWaitWith5ParametersFailWithTimeoutExceptionAndDoNotRecordAssertionFailure()
    {
        when(waitFactory.createWait(searchContext, TIMEOUT_SECONDS, TIMEOUT_MILLIS)).thenReturn(wait);
        mockException(TimeoutException.class);
        waitActions.wait(searchContext, TIMEOUT_SECONDS, TIMEOUT_MILLIS, isTrue, false);
        verifyNoInteractions(softAssert);
    }

    @Test
    void testWaitWith4ParametersFailWithNoSuchElementException()
    {
        when(wait.toString()).thenReturn(MOCKED_WAIT_DESCRIPTION);
        when(waitFactory.createWait(searchContext, TIMEOUT_SECONDS)).thenReturn(wait);
        mockException(NoSuchElementException.class);
        waitActions.wait(searchContext, TIMEOUT_SECONDS, isTrue);
        verifyFailureRecording();
    }

    private void mockException(Class<? extends Exception> clazz)
    {
        Exception exception = mock(clazz);
        Mockito.lenient().when(exception.getMessage()).thenReturn(EXCEPTION_TIMEOUT_MESSAGE);
        when(wait.until(isTrue)).thenThrow(exception);
    }

    private void verifyFailureRecording()
    {
        verify(softAssert, never()).recordPassedAssertion(MOCKED_WAIT_DESCRIPTION);
        verify(softAssert).recordFailedAssertion(MOCKED_WAIT_DESCRIPTION + ". Error: " + EXCEPTION_TIMEOUT_MESSAGE);
    }

    private void verifySuccess()
    {
        verify(wait).until(isTrue);
    }
}
