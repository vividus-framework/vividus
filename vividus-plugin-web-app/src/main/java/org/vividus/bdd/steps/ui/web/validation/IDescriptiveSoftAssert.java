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

import org.hamcrest.Matcher;
import org.vividus.softassert.ISoftAssert;

public interface IDescriptiveSoftAssert extends ISoftAssert
{
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
     * @param businessDescription The assertion description readable for user excluding technical information such as
     * locators and so on
     * @param systemDescription The assertion description for debug
     * @param <T> the static type accepted by the matcher (this can flag obvious
     * compile-time problems such as {@code assertThat(1, is("a"))}
     * @param actual the computed value being compared
     * @param matcher an expression, built of {@link Matcher}s, specifying allowed
     * values
     * @return <code>true</code> if assertion is passed, otherwise <code>false</code>
     * @see org.hamcrest.CoreMatchers
     * @see org.hamcrest.MatcherAssert
     */
    <T> boolean assertThat(String businessDescription, String systemDescription, T actual,
            Matcher<? super T> matcher);
}
