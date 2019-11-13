/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.steps.ui.web.validation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.common.eventbus.EventBus;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.SoftAssert;
import org.vividus.softassert.formatter.IAssertionFormatter;
import org.vividus.softassert.issue.IKnownIssueChecker;
import org.vividus.softassert.model.AssertionCollection;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.testcontext.TestContext;

import uk.org.lidalia.slf4jext.Level;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class DescriptiveSoftAssertTests
{
    private static final String TEST = "test";

    private static final String BUSINESS_DESCRIPTION = "Business description";
    private static final String SYSTEM_DESCRIPTION = "System description";

    private static final String FAILED_ASSERTION = " [Expected: \"\" Actual: was \"test\"]";
    private static final String FAILED_BUSINESS_ASSERTION = BUSINESS_DESCRIPTION + FAILED_ASSERTION;
    private static final String FAILED_SYSTEM_ASSERTION = SYSTEM_DESCRIPTION + FAILED_ASSERTION;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(SoftAssert.class);

    @Mock
    private TestContext testContext;

    @Mock
    private IAssertionFormatter formatter;

    @Mock
    private IKnownIssueChecker knownIssueChecker;

    @Mock
    private AssertionCollection assertionCollection;

    @Mock
    private EventBus eventBus;

    @InjectMocks
    private DescriptiveSoftAssert descriptiveSoftAssert;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void before()
    {
        when(testContext.get(any(), any(Supplier.class))).thenReturn(assertionCollection);
    }

    @Test
    void testAssertThat()
    {
        LoggingEvent expectedEvent = new LoggingEvent(Level.DEBUG, "Pass: {}",
                SYSTEM_DESCRIPTION + StringUtils.SPACE + "\"test\"");
        assertTrue(
                descriptiveSoftAssert.assertThat(BUSINESS_DESCRIPTION, SYSTEM_DESCRIPTION, TEST, equalTo(TEST)));
        verify(assertionCollection).addPassed();
        assertTrue(logger.getAllLoggingEvents().contains(expectedEvent));
    }

    @Test
    void testAssertThatMessageAssertion()
    {
        assertTrue(
                descriptiveSoftAssert.assertThat(BUSINESS_DESCRIPTION, SYSTEM_DESCRIPTION, TEST, equalTo(TEST)));
    }

    @Test
    void testAssertThatFalse()
    {
        assertFalse(
                descriptiveSoftAssert.assertThat(BUSINESS_DESCRIPTION, SYSTEM_DESCRIPTION, TEST, equalTo("")));
    }

    @Test
    void testAssertThatWithKnownSystemIssue()
    {
        KnownIssue issue = mock(KnownIssue.class);
        when(knownIssueChecker.getKnownIssue(FAILED_BUSINESS_ASSERTION)).thenReturn(null);
        when(knownIssueChecker.getKnownIssue(FAILED_SYSTEM_ASSERTION)).thenReturn(issue);
        descriptiveSoftAssert.assertThat(BUSINESS_DESCRIPTION, SYSTEM_DESCRIPTION, TEST, equalTo(""));
        verify(formatter).getMessage(FAILED_SYSTEM_ASSERTION, issue);
    }

    @Test
    void testAssertThatWithKnownBusinessIssue()
    {
        KnownIssue issue = mock(KnownIssue.class);
        when(knownIssueChecker.getKnownIssue(FAILED_BUSINESS_ASSERTION)).thenReturn(issue);
        descriptiveSoftAssert.assertThat(BUSINESS_DESCRIPTION, SYSTEM_DESCRIPTION, TEST, equalTo(""));
        verify(formatter).getMessage(FAILED_BUSINESS_ASSERTION, issue);
    }

    @Test
    void testAssertThatWithNullKnownIssue()
    {
        descriptiveSoftAssert.assertThat(BUSINESS_DESCRIPTION, SYSTEM_DESCRIPTION, TEST, equalTo(""));
        verifyNoInteractions(formatter);
    }

    @Test
    void testAssertThatWithNullKnownIssueChecker()
    {
        DescriptiveSoftAssert spy = Mockito.spy(descriptiveSoftAssert);
        spy.setKnownIssueChecker(null);
        spy.assertThat(BUSINESS_DESCRIPTION, SYSTEM_DESCRIPTION, TEST, equalTo(""));
        verifyNoInteractions(formatter);
    }
}
