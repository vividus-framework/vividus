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

    WebElement assertIfElementExists(String businessDescription, String systemDescription, List<WebElement> elements);

    boolean assertElementNumber(String businessDescription, String systemDescription, List<WebElement> elements,
            int number);

    boolean assertLeastElementNumber(String businessDescription, String systemDescription, List<WebElement> elements,
            int leastNumber);

    boolean assertExpectedCondition(String businessDescription, ExpectedCondition<?> expectedCondition);

    WebElement assertIfElementExists(String businessDescription, Locator locator);

    WebElement assertIfElementExists(String businessDescription, SearchContext searchContext, Locator locator);

    List<WebElement> assertIfElementsExist(String businessDescription, Locator locator);

    List<WebElement> assertIfElementsExist(String businessDescription, SearchContext searchContext, Locator locator);

    List<WebElement> assertIfNumberOfElementsFound(String businessDescription, Locator locator, int number,
            ComparisonRule comparisonRule);

    List<WebElement> assertIfNumberOfElementsFound(String businessDescription, SearchContext searchContext,
            Locator locator, int number, ComparisonRule comparisonRule);

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

    boolean assertIfElementDoesNotExist(String businessDescription, String systemDescription, Locator locator);

    boolean assertIfElementDoesNotExist(String businessDescription, String systemDescription,
            SearchContext searchContext, Locator locator);

    boolean assertIfElementDoesNotExist(String businessDescription, String systemDescription,
            Locator locator, boolean recordAssertionIfFail);

    boolean assertIfElementDoesNotExist(String businessDescription, String systemDescription,
            SearchContext searchContext, Locator locator, boolean recordAssertionIfFail);
}
