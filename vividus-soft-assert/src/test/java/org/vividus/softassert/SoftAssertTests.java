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

package org.vividus.softassert;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.org.lidalia.slf4jext.Level.ERROR;

import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.event.AssertionPassedEvent;
import org.vividus.softassert.exception.VerificationError;
import org.vividus.softassert.formatter.IAssertionFormatter;
import org.vividus.softassert.model.AssertionCollection;
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

    @Mock
    private TestContext testContext;

    @Mock
    private IAssertionFormatter formatter;

    @Mock
    private EventBus eventBus;

    @Mock
    private AssertionCollection assertionCollection;

    @Mock
    private List<SoftAssertionError> assertionErrors;

    @InjectMocks
    private SoftAssert softAssert;

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
        mockAssertionCollection();
        assertFalse(softAssert.recordFailedAssertion("Failed"));
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
        mockAssertionCollection();
        assertTrue(softAssert.recordPassedAssertion("Passed"));
        verify(eventBus).post(any(AssertionPassedEvent.class));
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
