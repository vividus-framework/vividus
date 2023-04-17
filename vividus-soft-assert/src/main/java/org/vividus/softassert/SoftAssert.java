/*
 * Copyright 2019-2023 the original author or authors.
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

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.google.common.eventbus.EventBus;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.event.AssertionPassedEvent;
import org.vividus.softassert.exception.VerificationError;
import org.vividus.softassert.formatter.IAssertionFormatter;
import org.vividus.softassert.issue.IKnownIssueChecker;
import org.vividus.softassert.model.AssertionCollection;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.softassert.model.SoftAssertionError;
import org.vividus.testcontext.TestContext;

/**
 * Implementation of soft assertions. When an assertion fails, it doesn't throw an exception, but records the failure
 * and count the assertions.
 */
@SuppressWarnings({ "checkstyle:methodcount", "PMD.GodClass" })
public class SoftAssert implements ISoftAssert
{
    protected static final String FAIL = "Fail: {}";
    protected static final String PASS = "Pass: {}";
    private static final String EXPECTED = "Expected: ";
    private static final String ACTUAL = " Actual: ";

    // Assertion descriptions
    private static final String IS_TRUE = "The condition is true";
    private static final String IS_FALSE = "The condition is false";
    private static final String IS_NULL = "The object is null";
    private static final String IS_NOT_NULL = "The object is not null";

    private static final Logger LOGGER = LoggerFactory.getLogger(SoftAssert.class);

    private TestContext testContext;

    private IAssertionFormatter formatter;
    private IKnownIssueChecker knownIssueChecker;
    private FailTestFastHandler failTestFastHandler;
    private FailTestFastManager failTestFastManager;

    private EventBus eventBus;

    @Override
    public boolean assertTrue(final String description, final boolean condition)
    {
        return assertTrue(description, condition, null);
    }

    @Override
    public boolean assertTrue(String description, boolean condition, Consumer<Boolean> resultConsumer)
    {
        return recordAssertionWithFinally(condition, description, condition ? IS_TRUE : IS_FALSE, resultConsumer);
    }

    @Override
    public boolean assertFalse(final String description, final boolean condition)
    {
        return recordAssertion(!condition, description, condition ? IS_TRUE : IS_FALSE);
    }

    @Override
    public boolean assertEquals(final String description, final Object expected, final Object actual)
    {
        return assertEquality(description, true, expected, actual);
    }

    @Override
    public boolean assertNotEquals(final String description, final Object expected, final Object actual)
    {
        return assertEquality(description, false, expected, actual);
    }

    private boolean
            assertEquality(final String description, boolean equals, final Object expected, final Object actual)
    {
        if (expected == null && actual == null || expected != null && expected.equals(actual))
        {
            return recordAssertion(equals, description, createAssertionDescription(expected, actual));
        }

        String expectedString = String.valueOf(expected);
        String actualString = String.valueOf(actual);
        boolean areValuesEqual = expectedString.equals(actualString);
        StringBuilder assertionDescription = new StringBuilder();
        append(assertionDescription, EXPECTED, areValuesEqual, expected, expectedString);
        append(assertionDescription, ACTUAL, areValuesEqual, actual, actualString);
        return recordAssertion(!equals, description, assertionDescription.toString());
    }

    private static void append(StringBuilder builder, String prompt, boolean areValuesEqual, final Object obj,
            String objValue)
    {
        builder.append(prompt);
        if (areValuesEqual)
        {
            builder.append(obj.getClass().getName());
        }
        appendWrapped(builder, objValue);
    }

    @Override
    public boolean
            assertEquals(final String description, final double expected, final double actual, final double delta)
    {
        return assertEquality(description, true, expected, actual, delta);
    }

    @Override
    public boolean assertNotEquals(final String description, final double expected, final double actual, double delta)
    {
        return assertEquality(description, false, expected, actual, delta);
    }

    private boolean assertEquality(final String description, boolean equals, final double expected,
            final double actual, final double delta)
    {
        String expectedString = Double.toString(expected);
        String actualString = Double.toString(actual);
        if (Double.compare(expected, actual) == 0)
        {
            return recordAssertion(equals, description, createAssertionDescription(expectedString, actualString));
        }
        boolean equalByDelta = Math.abs(expected - actual) <= delta;
        StringBuilder assertionDescription = new StringBuilder("|Expected ");
        appendWrapped(assertionDescription, expectedString);
        assertionDescription.append(" - actual ");
        appendWrapped(assertionDescription, actualString);
        assertionDescription.append("| is");
        if (!equalByDelta)
        {
            assertionDescription.append(" not");
        }
        assertionDescription.append(" more than delta ");
        appendWrapped(assertionDescription, Double.toString(delta));
        return recordAssertion(equalByDelta == equals, description, assertionDescription.toString());
    }

    @Override
    public boolean assertEquals(final String description, final long expected, final long actual)
    {
        return assertEquality(description, true, expected, actual);
    }

    @Override
    public boolean assertNotEquals(final String description, final long expected, final long actual)
    {
        return assertEquality(description, false, expected, actual);
    }

    private boolean assertEquality(final String description, boolean equals, final long expected, final long actual)
    {
        String assertionDescription = createAssertionDescription(Long.toString(expected), Long.toString(actual));
        return recordAssertion(equals == (expected == actual), description, assertionDescription);
    }

    @Override
    public boolean assertEquals(final String description, final boolean expected, final boolean actual)
    {
        return assertEquality(description, true, expected, actual);
    }

    @Override
    public boolean assertNotEquals(final String description, final boolean expected, final boolean actual)
    {
        return assertEquality(description, false, expected, actual);
    }

    private boolean assertEquality(final String description, boolean equals, final boolean expected,
            final boolean actual)
    {
        String assertionDescription = createAssertionDescription(Boolean.toString(expected), Boolean.toString(actual));
        return recordAssertion(equals == (expected == actual), description, assertionDescription);
    }

    @Override
    public boolean assertNotNull(String description, final Object object)
    {
        boolean condition = object != null;
        return recordAssertion(condition, description, getNullAssertionDescription(object));
    }

    @Override
    public boolean assertNull(String description, final Object object)
    {
        boolean condition = object == null;
        return recordAssertion(condition, description, getNullAssertionDescription(object));
    }

    @Override
    public <T> boolean assertThat(String description, T actual, Matcher<? super T> matcher)
    {
        return assertThat(description, actual, matcher, null);
    }

    @Override
    public <T> boolean assertThat(String description, T actual, Matcher<? super T> matcher,
            Consumer<Boolean> resultConsumer)
    {
        boolean matches = matcher.matches(actual);
        String matcherDescriptionString = getAssertionDescriptionString(actual, matcher);
        return recordAssertionWithFinally(matches, description, matcherDescriptionString, resultConsumer);
    }

    protected String getNullAssertionDescription(final Object object)
    {
        return object != null ? IS_NOT_NULL : IS_NULL;
    }

    protected <T> String getAssertionDescriptionString(T actual, Matcher<? super T> matcher)
    {
        StringDescription stringDescription = new StringDescription();
        stringDescription.appendText(EXPECTED).appendDescriptionOf(matcher);

        StringDescription mismatchStringDescription = new StringDescription();
        matcher.describeMismatch(actual, mismatchStringDescription);

        String mismatchString = mismatchStringDescription.toString();
        if (!mismatchString.isEmpty())
        {
            stringDescription.appendText(ACTUAL).appendText(mismatchString);
        }
        return stringDescription.toString();
    }

    @Override
    public boolean recordPassedAssertion(String description)
    {
        return recordAssertion(true, description);
    }

    @Override
    public boolean recordFailedAssertion(String description)
    {
        return recordAssertion(false, description);
    }

    @Override
    public boolean recordFailedAssertion(Throwable exception)
    {
        String message = exception.getMessage();
        return recordAssertion(false, message != null ? message : exception.toString(), exception);
    }

    @Override
    public boolean recordFailedAssertion(String description, Throwable exception)
    {
        return recordAssertion(false, description, exception);
    }

    @Override
    public boolean recordAssertion(boolean passed, String description)
    {
        return recordAssertion(passed, description, (Throwable) null);
    }

    @Override
    public void verify() throws VerificationError
    {
        AssertionCollection assertionCollection = getAssertionCollection();
        try
        {
            List<SoftAssertionError> assertionErrors = assertionCollection.getAssertionErrors();
            if (assertionErrors.isEmpty())
            {
                LOGGER.atInfo().log(() -> formatter.getPassedVerificationMessage(assertionCollection
                        .getAssertionsCount()));
            }
            else
            {
                String errorsMessage = formatter.getErrorsMessage(assertionErrors, true);
                LOGGER.atError().addArgument(formatter.getFailedVerificationMessage(assertionErrors,
                        assertionCollection.getAssertionsCount())).addArgument(errorsMessage).log("{}{}");
                throw new VerificationError(errorsMessage, assertionErrors);
            }
        }
        finally
        {
            assertionCollection.clear();
        }
    }

    @Override
    public void verify(Pattern pattern) throws VerificationError
    {
        if (getAssertionCollection().getAssertionErrors().stream()
                                                         .map(SoftAssertionError::getError)
                                                         .map(AssertionError::getMessage)
                                                         .noneMatch(pattern.asMatchPredicate()))
        {
            return;
        }
        verify();
    }

    @Override
    public <E extends Exception> void runIgnoringTestFailFast(FailableRunnable<E> runnable) throws E
    {
        boolean testCaseFailFast = failTestFastManager.isFailTestCaseFast();
        if (testCaseFailFast)
        {
            failTestFastManager.disableTestCaseFailFast();
        }

        try
        {
            runnable.run();
        }
        finally
        {
            if (testCaseFailFast)
            {
                failTestFastManager.enableTestCaseFailFast();
                verify();
            }
        }
    }

    public void init()
    {
        testContext.putInitValueSupplier(AssertionCollection.class, AssertionCollection::new);
    }

    private boolean recordAssertion(boolean passed, String description, String assertionDescription)
    {
        return recordAssertion(passed, format(description, assertionDescription));
    }

    private boolean recordAssertionWithFinally(boolean passed, String description, String assertionDescription,
            Consumer<Boolean> resultConsumer)
    {
        try
        {
            return recordAssertion(passed, format(description, assertionDescription));
        }
        finally
        {
            if (resultConsumer != null)
            {
                resultConsumer.accept(passed);
            }
        }
    }

    private boolean recordAssertion(boolean passed, String description, Throwable cause)
    {
        if (passed)
        {
            LOGGER.atInfo().addArgument(description).log(PASS);
            getAssertionCollection().addPassed();
            eventBus.post(new AssertionPassedEvent());
        }
        else
        {
            KnownIssue issue = getKnownIssue(description);
            String message = getKnownIssueMessage(issue, description);
            LOGGER.atError().addArgument(message).log(FAIL);
            recordAssertionError(issue, message, cause);
        }
        return passed;
    }

    protected String getKnownIssueMessage(KnownIssue issue, String description)
    {
        return issue != null ? formatter.getMessage(description, issue) : description;
    }

    protected KnownIssue getKnownIssue(String description)
    {
        if (knownIssueChecker != null)
        {
            return knownIssueChecker.getKnownIssue(description);
        }
        return null;
    }

    protected void recordAssertionError(KnownIssue issue, String message, Throwable cause)
    {
        SoftAssertionError assertionError = new SoftAssertionError(new AssertionError(message, cause));
        assertionError.setKnownIssue(issue);
        getAssertionCollection().addFailed(assertionError);

        eventBus.post(new AssertionFailedEvent(assertionError));

        if (assertionError.isFailTestSuiteFast())
        {
            failTestFastHandler.failTestSuiteFast();
        }
        if (failTestFastManager.isFailTestCaseFast() && !assertionError.isKnownIssue()
                                                     || assertionError.isFailTestCaseFast())
        {
            failTestFastHandler.failTestCaseFast();
        }
    }

    /**
     * Returns formatted description string
     * @param description Description string
     * @param assertionDescription Assertion description
     * @return Formatted description string
     */
    protected static String format(String description, String assertionDescription)
    {
        StringBuilder fullDescription = new StringBuilder();
        if (description != null)
        {
            fullDescription.append(description);
        }
        if (assertionDescription != null && !assertionDescription.isEmpty())
        {
            if (fullDescription.length() != 0)
            {
                fullDescription.append(" [").append(assertionDescription).append(']');
            }
            else
            {
                return assertionDescription;
            }
        }
        return fullDescription.toString();
    }

    private static String createAssertionDescription(final Object expected, final Object actual)
    {
        StringBuilder assertionDescription = new StringBuilder(EXPECTED);
        appendWrapped(assertionDescription, expected);
        assertionDescription.append(ACTUAL);
        appendWrapped(assertionDescription, actual);
        return assertionDescription.toString();
    }

    private static void appendWrapped(StringBuilder appendable, Object object)
    {
        appendable.append('<').append(object).append('>');
    }

    private AssertionCollection getAssertionCollection()
    {
        return testContext.get(AssertionCollection.class, AssertionCollection::new);
    }

    public void setFormatter(IAssertionFormatter formatter)
    {
        this.formatter = formatter;
    }

    public void setKnownIssueChecker(IKnownIssueChecker knownIssueChecker)
    {
        this.knownIssueChecker = knownIssueChecker;
    }

    public void setFailTestFastHandler(FailTestFastHandler failTestFastHandler)
    {
        this.failTestFastHandler = failTestFastHandler;
    }

    public void setTestContext(TestContext testContext)
    {
        this.testContext = testContext;
    }

    public void setEventBus(EventBus eventBus)
    {
        this.eventBus = eventBus;
    }

    public void setFailTestFastManager(FailTestFastManager failTestFastManager)
    {
        this.failTestFastManager = failTestFastManager;
    }
}
