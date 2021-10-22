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

package org.vividus.softassert;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.org.lidalia.slf4jext.Level.ERROR;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.event.AssertionPassedEvent;
import org.vividus.softassert.event.FailTestFastEvent;
import org.vividus.softassert.exception.VerificationError;
import org.vividus.softassert.formatter.IAssertionFormatter;
import org.vividus.softassert.issue.IKnownIssueChecker;
import org.vividus.softassert.model.AssertionCollection;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.softassert.model.SoftAssertionError;
import org.vividus.testcontext.TestContext;

@SuppressWarnings("checkstyle:methodcount")
@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class SoftAssertTests
{
    private static final double DELTA = 0.5;
    private static final String NULL = "Null";
    private static final String NOT_NULL = "Not null";
    private static final String TRUE = "True";
    private static final String EMPTY_STRING = "";
    private static final String FALSE = "False";
    private static final String EQUAL = "Equal";
    private static final String NOT_EQUAL = "Not equal";
    private static final String TEXT = "text";
    private static final LoggingEvent ERROR_LOG_ENTRY = error("{}{}", TEXT, TEXT);

    private final TestLogger logger = TestLoggerFactory.getTestLogger(SoftAssert.class);

    @Mock private TestContext testContext;
    @Mock private IAssertionFormatter formatter;
    @Mock private EventBus eventBus;
    @Mock private AssertionCollection assertionCollection;
    @Mock private List<SoftAssertionError> assertionErrors;
    @InjectMocks private SoftAssert softAssert;

    @SuppressWarnings("unchecked")
    private void mockAssertionCollection()
    {
        when(testContext.get(any(), any(Supplier.class))).thenReturn(assertionCollection);
    }

    @Test
    void testAssertEqualsString()
    {
        mockAssertionCollection();
        assertTrue(softAssert.assertEquals(EQUAL, TEXT, TEXT));
        verify(eventBus).post(any(AssertionPassedEvent.class));
    }

    @Test
    void testAssertEqualsBoolean()
    {
        mockAssertionCollection();
        assertTrue(softAssert.assertEquals(EQUAL, true, true));
        verify(eventBus).post(any(AssertionPassedEvent.class));
    }

    @Test
    void testAssertEqualsDouble()
    {
        mockAssertionCollection();
        assertTrue(softAssert.assertEquals(EQUAL, 1.0, 1.0));
        verify(eventBus).post(any(AssertionPassedEvent.class));
    }

    @Test
    void testAssertEqualsLong()
    {
        mockAssertionCollection();
        assertTrue(softAssert.assertEquals(EQUAL, 1L, 1L));
        verify(eventBus).post(any(AssertionPassedEvent.class));
    }

    @Test
    void testAssertFalse()
    {
        mockAssertionCollection();
        assertTrue(softAssert.assertFalse(FALSE, TEXT.equals(EMPTY_STRING)));
        verify(eventBus).post(any(AssertionPassedEvent.class));
    }

    @Test
    void testAssertFalseNot()
    {
        mockAssertionCollection();
        assertFalse(softAssert.assertFalse(FALSE, TEXT.equals(TEXT)));
    }

    @Test
    void testAssertNotEqualsString()
    {
        mockAssertionCollection();
        assertTrue(softAssert.assertNotEquals(NOT_EQUAL, TEXT, EMPTY_STRING));
        verify(eventBus).post(any(AssertionPassedEvent.class));
    }

    @Test
    void testAssertNotEqualsBoolean()
    {
        mockAssertionCollection();
        assertTrue(softAssert.assertNotEquals(NOT_EQUAL, true, false));
        verify(eventBus).post(any(AssertionPassedEvent.class));
    }

    @Test
    void testAssertNotEqualsDouble()
    {
        mockAssertionCollection();
        assertFalse(softAssert.assertNotEquals(NOT_EQUAL, 1.0, 1.0));
    }

    @Test
    void testAssertNotEqualsLong()
    {
        mockAssertionCollection();
        assertFalse(softAssert.assertNotEquals(NOT_EQUAL, 1L, 1L));
    }

    @Test
    void testAssertTrue()
    {
        mockAssertionCollection();
        assertTrue(softAssert.assertTrue(TRUE, TEXT.equals(TEXT)));
        verify(eventBus).post(any(AssertionPassedEvent.class));
    }

    @Test
    void testAssertTrueNot()
    {
        mockAssertionCollection();
        assertFalse(softAssert.assertTrue(TRUE, TEXT.equals(EMPTY_STRING)));
    }

    @Test
    void testAssertNotNull()
    {
        mockAssertionCollection();
        assertTrue(softAssert.assertNotNull(NOT_NULL, TEXT));
        verify(eventBus).post(any(AssertionPassedEvent.class));
    }

    @Test
    void testAssertNotNullFalse()
    {
        mockAssertionCollection();
        assertFalse(softAssert.assertNotNull(NOT_NULL, null));
    }

    @Test
    void testAssertNull()
    {
        mockAssertionCollection();
        assertTrue(softAssert.assertNull(NULL, null));
        verify(eventBus).post(any(AssertionPassedEvent.class));
    }

    @Test
    void testAssertNullFalse()
    {
        mockAssertionCollection();
        assertFalse(softAssert.assertNull(NULL, TEXT));
    }

    @Test
    void testAssertThat()
    {
        mockAssertionCollection();
        assertTrue(softAssert.assertThat("Text contains 't'", TEXT, containsString("t")));
        verify(eventBus).post(any(AssertionPassedEvent.class));
    }

    @Test
    void testAssertThatFalse()
    {
        mockAssertionCollection();
        assertFalse(softAssert.assertThat("Text contains 'a'", TEXT, containsString("a")));
    }

    @Test
    void testRecordFailedAssertion()
    {
        testRecordFailedAssertion(softAssert::recordFailedAssertion);
    }

    @Test
    void shouldRecordFailedAssertionAndFailTestCaseFast()
    {
        softAssert.setFailTestCaseFast(true);
        testRecordFailedAssertion(softAssert::recordFailedAssertion);
        verify(eventBus).post(argThat(event -> {
            if (event instanceof FailTestFastEvent)
            {
                FailTestFastEvent failTestFastEvent = (FailTestFastEvent) event;
                return failTestFastEvent.isFailTestCaseFast() && !failTestFastEvent.isFailTestSuiteFast();
            }
            return false;
        }));
        verifyNoMoreInteractions(eventBus);
    }

    @Test
    void testRecordAssertionWithFailedFlag()
    {
        testRecordFailedAssertion(description -> softAssert.recordAssertion(false, description));
    }

    @ParameterizedTest
    @CsvSource({
            "false",
            "true"
    })
    void shouldRecordFailedAssertionAsKnownIssue(boolean globalFailTestCaseFast)
    {
        var knownIssue = mock(KnownIssue.class);
        testRecordFailedAssertionAsKnownIssue(globalFailTestCaseFast, knownIssue);
        verifyNoMoreInteractions(eventBus);
    }

    @Test
    void shouldRecordFailedAssertionAsKnownIssueAndFailTestCaseFast()
    {
        var knownIssue = mock(KnownIssue.class);
        when(knownIssue.isFailTestCaseFast()).thenReturn(true);
        testRecordFailedAssertionAsKnownIssue(false, knownIssue);
        verify(eventBus).post(argThat(event -> {
            if (event instanceof FailTestFastEvent)
            {
                FailTestFastEvent failTestFastEvent = (FailTestFastEvent) event;
                return failTestFastEvent.isFailTestCaseFast() && !failTestFastEvent.isFailTestSuiteFast();
            }
            return false;
        }));
        verifyNoMoreInteractions(eventBus);
    }

    @Test
    void shouldRecordFailedAssertionAsKnownIssueAndFailTestSuiteFast()
    {
        var knownIssue = mock(KnownIssue.class);
        when(knownIssue.isFailTestSuiteFast()).thenReturn(true);
        testRecordFailedAssertionAsKnownIssue(false, knownIssue);
        verify(eventBus).post(argThat(event -> {
            if (event instanceof FailTestFastEvent)
            {
                FailTestFastEvent failTestFastEvent = (FailTestFastEvent) event;
                return !failTestFastEvent.isFailTestCaseFast() && failTestFastEvent.isFailTestSuiteFast();
            }
            return false;
        }));
        verifyNoMoreInteractions(eventBus);
    }

    private void testRecordFailedAssertion(Predicate<String> testMethod)
    {
        mockAssertionCollection();
        String description = "Failed";
        assertFalse(testMethod.test(description));
        assertLoggingOfFailedAssertion(description);
        verifyAssertionFailedEventPosted(false);
    }

    private void testRecordFailedAssertionAsKnownIssue(boolean globalFailTestCaseFast, KnownIssue knownIssue)
    {
        mockAssertionCollection();
        var knownIssueChecker = mock(IKnownIssueChecker.class);
        softAssert.setKnownIssueChecker(knownIssueChecker);
        softAssert.setFailTestCaseFast(globalFailTestCaseFast);
        var description = "failure";
        when(knownIssue.isPotentiallyKnown()).thenReturn(false);
        when(knownIssueChecker.getKnownIssue(description)).thenReturn(knownIssue);
        var descriptionWithKnownIssue = "failure as known issue";
        when(formatter.getMessage(description, knownIssue)).thenReturn(descriptionWithKnownIssue);
        assertFalse(softAssert.recordFailedAssertion(description));
        assertLoggingOfFailedAssertion(descriptionWithKnownIssue);
        verifyAssertionFailedEventPosted(true);
    }

    private void verifyAssertionFailedEventPosted(boolean knownIssue)
    {
        verify(eventBus).post(argThat(event -> {
            if (event instanceof AssertionFailedEvent)
            {
                AssertionFailedEvent failedEvent = (AssertionFailedEvent) event;
                SoftAssertionError softAssertionError = failedEvent.getSoftAssertionError();
                return softAssertionError != null && knownIssue == softAssertionError.isKnownIssue()
                        && softAssertionError.getError() != null;
            }
            return false;
        }));
    }

    private void assertLoggingOfFailedAssertion(String description)
    {
        assertThat(logger.getLoggingEvents(), equalTo(List.of(error("Fail: {}", description))));
    }

    @Test
    void testRecordFailedAssertionExceptionAndDescription()
    {
        mockAssertionCollection();
        assertFalse(softAssert.recordFailedAssertion(TEXT, new IllegalStateException("The state is illegal")));
    }

    @Test
    void testRecordFailedAssertionException()
    {
        mockAssertionCollection();
        assertFalse(softAssert.recordFailedAssertion(new IllegalStateException("Error")));
    }

    @Test
    void testRecordFailedAssertionExceptionWithNullMessage()
    {
        mockAssertionCollection();
        assertFalse(softAssert.recordFailedAssertion(new IllegalStateException()));
    }

    @Test
    void testRecordPassedAssertion()
    {
        testRecordPassedAssertion(softAssert::recordPassedAssertion);
    }

    @Test
    void testRecordAssertionWithPassedFlag()
    {
        testRecordPassedAssertion(description -> softAssert.recordAssertion(true, description));
    }

    private void testRecordPassedAssertion(Predicate<String> testMethod)
    {
        mockAssertionCollection();
        String description = "Passed";
        assertTrue(testMethod.test(description));
        verify(eventBus).post(any(AssertionPassedEvent.class));
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info("Pass: {}", description))));
    }

    @Test
    void testAssertEqualsDoubleDeltaFalse()
    {
        mockAssertionCollection();
        assertFalse(softAssert.assertEquals(EQUAL, 1.0, 2.0, DELTA));
    }

    @Test
    void testAssertEqualsDoubleDeltaTrue()
    {
        mockAssertionCollection();
        assertTrue(softAssert.assertEquals(EQUAL, 1.0, 1.1, DELTA));
        verify(eventBus).post(any(AssertionPassedEvent.class));
    }

    @Test
    void testAssertEqualsDoubleDelta()
    {
        mockAssertionCollection();
        assertTrue(softAssert.assertEquals(EQUAL, 1.0, 1.0, DELTA));
        verify(eventBus).post(any(AssertionPassedEvent.class));
    }

    @Test
    void testAssertNotEqualsDoubleTrue()
    {
        mockAssertionCollection();
        assertTrue(softAssert.assertNotEquals(NOT_EQUAL, 1.0, 2.0, DELTA));
        verify(eventBus).post(any(AssertionPassedEvent.class));
    }

    @Test
    void testAssertNotEqualsDoubleFalse()
    {
        mockAssertionCollection();
        assertFalse(softAssert.assertNotEquals(NOT_EQUAL, 1.0, 1.3, DELTA));
    }

    @Test
    void testVerifyIfInfoEnabled()
    {
        mockAssertionCollection();
        int count = 1;
        when(assertionCollection.getAssertionsCount()).thenReturn(count);
        when(formatter.getPassedVerificationMessage(count)).thenReturn(TEXT);
        softAssert.verify();
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TEXT))));
    }

    @Test
    void testVerifyIfInfoDisabled()
    {
        mockAssertionCollection();
        logger.setEnabledLevels();
        softAssert.verify();
        assertThat(logger.getLoggingEvents(), equalTo(List.of()));
    }

    @Test
    void testVerifyIfErrorEnabled()
    {
        mockAssertionCollection();
        int count = 1;
        when(assertionCollection.getAssertionErrors()).thenReturn(assertionErrors);
        when(assertionErrors.isEmpty()).thenReturn(false);
        when(formatter.getErrorsMessage(assertionErrors, true)).thenReturn(TEXT);
        when(assertionCollection.getAssertionsCount()).thenReturn(count);
        when(formatter.getFailedVerificationMessage(assertionErrors, count)).thenReturn(TEXT);
        logger.setEnabledLevels(ERROR);
        assertThrows(VerificationError.class, softAssert:: verify);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(ERROR_LOG_ENTRY)));
    }

    @Test
    void shouldVerifyIfPatternMatches()
    {
        mockAssertionCollection();
        int count = 1;
        SoftAssertionError softAssertionError = mock(SoftAssertionError.class);
        AssertionError assertionError = mock(AssertionError.class);
        when(softAssertionError.getError()).thenReturn(assertionError);
        when(assertionError.getMessage()).thenReturn(TEXT);
        List<SoftAssertionError> assertionErrors = List.of(softAssertionError);
        when(assertionCollection.getAssertionErrors()).thenReturn(assertionErrors);
        when(formatter.getErrorsMessage(assertionErrors, true)).thenReturn(TEXT);
        when(assertionCollection.getAssertionsCount()).thenReturn(count);
        when(formatter.getFailedVerificationMessage(assertionErrors, count)).thenReturn(TEXT);
        logger.setEnabledLevels(ERROR);
        assertThrows(VerificationError.class, () -> softAssert.verify(Pattern.compile(TEXT)));
        assertThat(logger.getLoggingEvents(), equalTo(List.of(ERROR_LOG_ENTRY)));
    }

    @Test
    void shouldNotVerifyIfPatternNotMatches()
    {
        mockAssertionCollection();
        SoftAssertionError softAssertionError = mock(SoftAssertionError.class);
        AssertionError assertionError = mock(AssertionError.class);
        when(softAssertionError.getError()).thenReturn(assertionError);
        when(assertionError.getMessage()).thenReturn(TEXT);
        List<SoftAssertionError> assertionErrors = List.of(softAssertionError);
        when(assertionCollection.getAssertionErrors()).thenReturn(assertionErrors);
        logger.setEnabledLevels(ERROR);
        softAssert.verify(Pattern.compile("bazinga"));
        assertThat(logger.getLoggingEvents(), empty());
    }

    @Test
    void testVerifyIfErrorDisabled()
    {
        mockAssertionCollection();
        logger.setEnabledLevels();
        when(assertionCollection.getAssertionErrors()).thenReturn(assertionErrors);
        when(assertionErrors.isEmpty()).thenReturn(false);
        assertThrows(VerificationError.class, softAssert:: verify);
        assertThat(logger.getLoggingEvents(), equalTo(List.of()));
    }

    @Test
    void testInit()
    {
        softAssert.init();
        verify(testContext).putInitValueSupplier(eq(AssertionCollection.class),
                argThat(argument -> argument.get() instanceof AssertionCollection));
    }
}
