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

import java.util.regex.Pattern;

import org.hamcrest.Matcher;
import org.vividus.softassert.exception.VerificationError;

public interface ISoftAssert
{
    /**
     * Asserts that a condition is true. If it isn't, an assertion error is added to the collection.
     * @param description The assertion description
     * @param condition Condition to be checked
     * @return <code>true</code> if assertion is passed, otherwise <code>false</code>
     */
    boolean assertTrue(String description, boolean condition);

    /**
     * Asserts that a condition is false. If it isn't an assertion error is added to the collection.
     * @param description The assertion description
     * @param condition Condition to be checked
     * @return <code>true</code> if assertion is passed, otherwise <code>false</code>
     */
    boolean assertFalse(String description, boolean condition);

    /**
     * Asserts that two objects are equal. If they are not, an assertion error is added to the collection.
     * If <code>expected</code> and <code>actual</code> are <code>null</code>, they are considered equal.
     * @param description The assertion description
     * @param expected Expected value
     * @param actual Actual value
     * @return <code>true</code> if assertion is passed, otherwise <code>false</code>
     */
    boolean assertEquals(String description, Object expected, Object actual);

    /**
     * Asserts that two objects are <b>not</b> equals. If they are, an assertion error is added to the collection.
     * If <code>expected</code> and <code>actual</code> are <code>null</code>, they are considered equal.
     * @param description The assertion description
     * @param expected Expected value
     * @param actual Actual value
     * @return <code>true</code> if assertion is passed, otherwise <code>false</code>
     */
    boolean assertNotEquals(String description, Object expected, Object actual);

    /**
     * Asserts that two doubles or floats are equal to within a positive delta. If they are not, an assertion error
     * is added to the collection. If the expected value is infinity then the delta value is ignored. NaNs are
     * considered equal: <code>assertEquals(Double.NaN, Double.NaN, *)</code> passes
     * @param description The assertion description
     * @param expected Expected value
     * @param actual The value to check against <code>expected</code>
     * @param delta The maximum delta between <code>expected</code> and <code>actual</code> for which both numbers are
     * still considered equal.
     * @return <code>true</code> if assertion is passed, otherwise <code>false</code>
     */
    boolean assertEquals(String description, double expected, double actual, double delta);

    /**
     * Asserts that two doubles are <b>not</b> equal to within a positive delta. If they are, an assertion error
     * is added to the collection. If the expected value is infinity then the delta value is ignored.
     * @param description The assertion description
     * @param expected Expected value
     * @param actual The value to check against <code>expected</code>
     * @param delta The maximum delta between <code>expected</code> and <code>actual</code> for which both numbers are
     * still considered <b>not</b> equal.
     * @return <code>true</code> if assertion is passed, otherwise <code>false</code>
     */
    boolean assertNotEquals(String description, double expected, double actual, double delta);

    /**
     * Asserts that two long values are equal. If they are not, an assertion error is added to the collection.
     * @param description The assertion description
     * @param expected Expected value
     * @param actual Actual value
     * @return <code>true</code> if assertion is passed, otherwise <code>false</code>
     */
    boolean assertEquals(String description, long expected, long actual);

    /**
     * Asserts that two long values are <b>not</b> equals. If they are, an assertion error is added to the collection.
     * @param description The assertion description
     * @param expected Expected value
     * @param actual Actual value
     * @return <code>true</code> if assertion is passed, otherwise <code>false</code>
     */
    boolean assertNotEquals(String description, long expected, long actual);

    /**
     * Asserts that two boolean values are equal. If they are not, an assertion error is added to the collection.
     * @param description The assertion description
     * @param expected Expected value
     * @param actual Actual value
     * @return <code>true</code> if assertion is passed, otherwise <code>false</code>
     */
    boolean assertEquals(String description, boolean expected, boolean actual);

    /**
     * Asserts that two boolean values are <b>not</b> equals. If they are,
     * an assertion error is added to the collection.
     * @param description The assertion description
     * @param expected Expected value
     * @param actual Actual value
     * @return <code>true</code> if assertion is passed, otherwise <code>false</code>
     */
    boolean assertNotEquals(String description, boolean expected, boolean actual);

    /**
     * Asserts that an object isn't null. If it is, an assertion error is added to the collection.
     * @param description The assertion description
     * @param object Object to check or <code>null</code>
     * @return <code>true</code> if assertion is passed, otherwise <code>false</code>
     */
    boolean assertNotNull(String description, Object object);

    /**
     * Asserts that an object is null. If it is not, an assertion error is added to the collection.
     * @param description The assertion description
     * @param object Object to check or <code>null</code>
     * @return <code>true</code> if assertion is passed, otherwise <code>false</code>
     */
    boolean assertNull(String description, Object object);

    /**
     * Asserts that <code>actual</code> satisfies the condition specified by <code>matcher</code>.
     * If it is not, an assertion error is added to the collection.
     * Example:
     *
     * <pre>
     *   assertThat(&quot;Help! Integers don't work&quot;, 0, is(1)); // fails:
     *     // failure description:
     *     // Help! Integers don't work
     *     // expected: is &lt;1&gt;
     *     // got value: &lt;0&gt;
     *   assertThat(&quot;Zero is one&quot;, 0, is(not(1))) // passes
     * </pre>
     *
     * <code>org.hamcrest.Matcher</code> does not currently document the meaning
     * of its type parameter <code>T</code>. This method assumes that a matcher
     * typed as <code>Matcher&lt;T&gt;</code> can be meaningfully applied only
     * to values that could be assigned to a variable of type <code>T</code>.
     * @param description The assertion description
     * @param <T> the static type accepted by the matcher (this can flag obvious
     * compile-time problems such as {@code assertThat(1, is("a"))}
     * @param actual the computed value being compared
     * @param matcher an expression, built of {@link Matcher}s, specifying allowed
     * values
     * @return <code>true</code> if assertion is passed, otherwise <code>false</code>
     * @see org.hamcrest.Matchers
     * @see org.hamcrest.MatcherAssert
     */
    <T> boolean assertThat(String description, T actual, Matcher<? super T> matcher);

    /**
     * Logs information about passed assertion and returns true.
     * @param description The assertion description
     * @return Always returns true value indicating passed assertion
     */
    boolean recordPassedAssertion(String description);

    /**
     * Adds assertion error to the collection and returns false.
     * @param description The assertion description
     * @return Always returns false value indicating failed assertion
     */
    boolean recordFailedAssertion(String description);

    /**
     * Adds assertion error to the collection and returns false.
     * @param exception Cause of the error
     * @return Always returns false value indicating failed assertion
     */
    boolean recordFailedAssertion(Throwable exception);

    /**
     * Adds assertion error to the collection and returns false.
     * No exception message logged as description by default.
     * @param description The assertion description
     * @param exception Cause of the error
     * @return Always returns false value indicating failed assertion
     */
    boolean recordFailedAssertion(String description, Throwable exception);

    /**
     * Verifies if there is any assertion error. Throws exception if assertion error exists
     * @throws VerificationError If there were any assertion errors collected
     */
    void verify() throws VerificationError;

    /**
     * Verifies if there is matching regex pattern assertion error. Throws exception if assertion error exists
     * @param pattern to match assertion messages
     * @throws VerificationError If there were any assertion errors collected
     */
    void verify(Pattern pattern) throws VerificationError;
}
