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

package org.vividus.bdd.steps.ui.validation;

import java.util.List;
import java.util.Optional;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.ui.IState;
import org.vividus.ui.action.search.Locator;

public interface IBaseValidations
{
    boolean assertElementState(String businessDescription, IState state, WebElement element);

    boolean assertElementState(String businessDescription, IState state, WrapsElement element);

    WebElement assertIfElementExists(String businessDescription, List<WebElement> elements);

    @Deprecated(since = "0.2.8", forRemoval = true)
    WebElement assertIfElementExists(String businessDescription, String systemDescription, List<WebElement> elements);

    @Deprecated(since = "0.2.8", forRemoval = true)
    boolean assertElementNumber(String businessDescription, String systemDescription, List<WebElement> elements,
            int number);

    /**
     * Assert that the number of elements in <b>elements</b> collection is <b>comparisonRule</b> <b>number</b>
     * @param description description of elements in the <b>elements</b> collection
     * @param elements collection being verified
     * @param comparisonRule The rule to compare values
     * (&lt;i&gt;Possible values:&lt;b&gt; LESS_THAN, LESS_THAN_OR_EQUAL_TO, GREATER_THAN, GREATER_THAN_OR_EQUAL_TO,
     * EQUAL_TO&lt;/b&gt;&lt;/i&gt;)
     * @param number expected number of elements in the <b>elements</b> collection
     * @return whether the number of elements in <b>elements</b> is equal to <b>number</b>
     */
    boolean assertElementsNumber(String description, List<WebElement> elements, ComparisonRule comparisonRule,
            int number);

    @Deprecated(since = "0.2.8", forRemoval = true)
    boolean assertLeastElementNumber(String businessDescription, String systemDescription, List<WebElement> elements,
            int leastNumber);

    boolean assertExpectedCondition(String businessDescription, ExpectedCondition<?> expectedCondition);

    /**
     * Asserts that an element located by the <b>locator</b> exists
     * @param businessDescription business description of the element being validated, must not be {@code null}
     * @param locator locator to find element, must not be {@code null}
     * @return the element being validated
     * @deprecated Use {@link #assertElementExists(String, Locator)}
     */
    @Deprecated(since = "0.2.8", forRemoval = true)
    WebElement assertIfElementExists(String businessDescription, Locator locator);

    /**
     * Asserts that an element located by the <b>locator</b> exists
     * @param description description of the element being validated, must not be {@code null}
     * @param locator locator to find element, must not be {@code null}
     * @return the element being validated wrapped in an {@link Optional}
     */
    Optional<WebElement> assertElementExists(String description, Locator locator);

    WebElement assertIfElementExists(String businessDescription, SearchContext searchContext, Locator locator);

    List<WebElement> assertIfElementsExist(String businessDescription, Locator locator);

    List<WebElement> assertIfElementsExist(String businessDescription, SearchContext searchContext, Locator locator);

    /**
     * Assert that the number of elements found by <b>locator</b> is <b>comparisonRule</b> <b>number</b>
     * @param businessDescription business description of the elements being found, must not be {@code null}
     * @param locator locator to find elements, must not be {@code null}
     * @param number expected number of found elements
     * @param comparisonRule The rule to compare values
     * (&lt;i&gt;Possible values:&lt;b&gt; LESS_THAN, LESS_THAN_OR_EQUAL_TO, GREATER_THAN, GREATER_THAN_OR_EQUAL_TO,
     * EQUAL_TO&lt;/b&gt;&lt;/i&gt;)
     * @return collection of found elements
     * @deprecated Use {@link #assertNumberOfElementsFound(String, Locator, int, ComparisonRule)}
     */
    @Deprecated(since = "0.2.9", forRemoval = true)
    List<WebElement> assertIfNumberOfElementsFound(String businessDescription, Locator locator, int number,
            ComparisonRule comparisonRule);

    List<WebElement> assertIfNumberOfElementsFound(String businessDescription, SearchContext searchContext,
            Locator locator, int number, ComparisonRule comparisonRule);

    /**
     * Assert that the number of elements found by <b>locator</b> is <b>comparisonRule</b> <b>number</b>
     * @param description business description of the elements being found, must not be {@code null}
     * @param locator locator to find elements, must not be {@code null}
     * @param number expected number of found elements
     * @param comparisonRule The rule to compare values
     * (&lt;i&gt;Possible values:&lt;b&gt; LESS_THAN, LESS_THAN_OR_EQUAL_TO, GREATER_THAN, GREATER_THAN_OR_EQUAL_TO,
     * EQUAL_TO&lt;/b&gt;&lt;/i&gt;)
     * @return collection of found elements
     */
    List<WebElement> assertNumberOfElementsFound(String description, Locator locator, int number,
            ComparisonRule comparisonRule);

    boolean assertIfExactNumberOfElementsFound(String businessDescription, Locator locator, int number);

    boolean assertIfExactNumberOfElementsFound(String businessDescription, SearchContext searchContext, Locator locator,
            int number);

    List<WebElement> assertIfAtLeastNumberOfElementsExist(String businessDescription, Locator locator,
            int leastNumber);

    List<WebElement> assertIfAtLeastNumberOfElementsExist(String businessDescription, SearchContext searchContext,
            Locator locator, int leastNumber);

    WebElement assertIfAtLeastOneElementExists(String businessDescription, Locator locator);

    WebElement assertIfAtLeastOneElementExists(String businessDescription, SearchContext searchContext,
            Locator locator);

    Optional<WebElement> assertIfZeroOrOneElementFound(String businessDescription, Locator locator);

    Optional<WebElement> assertIfZeroOrOneElementFound(String businessDescription, SearchContext searchContext,
            Locator locator);

    boolean assertIfElementDoesNotExist(String businessDescription, Locator locator);

    boolean assertIfElementDoesNotExist(String businessDescription, SearchContext searchContext, Locator locator);

    boolean assertIfElementDoesNotExist(String businessDescription, Locator locator, boolean recordAssertionIfFail);

    boolean assertIfElementDoesNotExist(String businessDescription, SearchContext searchContext, Locator locator,
            boolean recordAssertionIfFail);

    @Deprecated(since = "0.2.8", forRemoval = true)
    boolean assertIfElementDoesNotExist(String businessDescription, String systemDescription, Locator locator);

    @Deprecated(since = "0.2.8", forRemoval = true)
    boolean assertIfElementDoesNotExist(String businessDescription, String systemDescription,
            SearchContext searchContext, Locator locator);

    @Deprecated(since = "0.2.8", forRemoval = true)
    boolean assertIfElementDoesNotExist(String businessDescription, String systemDescription,
            Locator locator, boolean recordAssertionIfFail);

    @Deprecated(since = "0.2.8", forRemoval = true)
    boolean assertIfElementDoesNotExist(String businessDescription, String systemDescription,
            SearchContext searchContext, Locator locator, boolean recordAssertionIfFail);
}
