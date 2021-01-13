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

package org.vividus.selenium.cloud;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Supplier;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.cloud.AbstractCloudTestStatusManager.UpdateCloudTestStatusException;
import org.vividus.selenium.event.BeforeWebDriverQuitEvent;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.softassert.model.SoftAssertionError;
import org.vividus.testcontext.TestContext;
import org.vividus.testcontext.ThreadedTestContext;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AbstractCloudTestStatusManagerTests
{
    private static final String PASSED = "passed";

    @Mock private IWebDriverProvider webDriverProvider;
    @Spy private ThreadedTestContext testContext;
    @InjectMocks
    @Spy private TestCloudTestStatusManager statusManager;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AbstractCloudTestStatusManager.class);

    @SuppressWarnings("unchecked")
    @Test
    void shouldUpdateCloudTestStatusToFailure() throws UpdateCloudTestStatusException
    {
        SoftAssertionError assertionError = mock(SoftAssertionError.class);

        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(assertionError.isKnownIssue()).thenReturn(false);

        statusManager.setCloudTestStatusToFailure(new AssertionFailedEvent(assertionError));
        statusManager.setCloudTestStatusToFailure(new AssertionFailedEvent(assertionError));
        statusManager.setCloudTestStatusToFailure(new AssertionFailedEvent(assertionError));
        statusManager.updateCloudTestStatus(new BeforeWebDriverQuitEvent());

        verify(statusManager).updateCloudTestStatus("failed");
        verify(testContext, times(5)).get(any(Class.class), any(Supplier.class));
        verify(testContext).remove(any(Class.class));
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldLogExceptionOccurredWhileCloudTestStatusUpdate() throws UpdateCloudTestStatusException
    {
        UpdateCloudTestStatusException exception = mock(UpdateCloudTestStatusException.class);

        doThrow(exception).when(statusManager).updateCloudTestStatus(PASSED);

        statusManager.updateCloudTestStatus(new BeforeWebDriverQuitEvent());

        verify(testContext).get(any(Class.class), any(Supplier.class));
        verify(testContext).remove(any(Class.class));
        assertThat(logger.getLoggingEvents(),
                is(List.of(error(exception, "Unable to update cloud test status to {}", PASSED))));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldUpdateCloudTestStatusToSuccess() throws UpdateCloudTestStatusException
    {
        statusManager.updateCloudTestStatus(new BeforeWebDriverQuitEvent());

        verify(statusManager).updateCloudTestStatus(PASSED);
        verify(testContext).get(any(Class.class), any(Supplier.class));
        verify(testContext).remove(any(Class.class));
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @Test
    void shouldSetCloudTestStatusToFailureIfWebDriverIsNotInitialized()
    {
        SoftAssertionError assertionError = mock(SoftAssertionError.class);

        when(webDriverProvider.isWebDriverInitialized()).thenReturn(false);

        statusManager.setCloudTestStatusToFailure(new AssertionFailedEvent(assertionError));

        verifyNoInteractions(assertionError, testContext);
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @CsvSource({
        "true, 2",
        "false, 1"
    })
    void shouldManageCloudTestStatusIfKnownIssue(boolean fixedKnownIssue, int invocations)
    {
        SoftAssertionError assertionError = mock(SoftAssertionError.class);
        KnownIssue knownIssue = mock(KnownIssue.class);

        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(assertionError.isKnownIssue()).thenReturn(true);
        when(assertionError.getKnownIssue()).thenReturn(knownIssue);
        when(knownIssue.isFixed()).thenReturn(fixedKnownIssue);

        statusManager.setCloudTestStatusToFailure(new AssertionFailedEvent(assertionError));

        verify(testContext, times(invocations)).get(any(Class.class), any(Supplier.class));
        verifyNoMoreInteractions(testContext);
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    private static final class TestCloudTestStatusManager extends AbstractCloudTestStatusManager
    {
        TestCloudTestStatusManager(IWebDriverProvider webDriverProvider, TestContext testContext)
        {
            super(webDriverProvider, testContext);
        }

        @Override
        protected void updateCloudTestStatus(String status) throws UpdateCloudTestStatusException
        {
        }
    }
}
