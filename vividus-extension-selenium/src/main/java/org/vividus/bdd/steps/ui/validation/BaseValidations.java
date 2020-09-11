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

import static org.hamcrest.Matchers.greaterThan;
import static org.vividus.ui.validation.matcher.ElementNumberMatcher.elementNumber;

import java.util.List;
import java.util.Optional;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.IState;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.validation.matcher.ExistsMatcher;
import org.vividus.ui.validation.matcher.ExpectedConditionsMatcher;
import org.vividus.ui.validation.matcher.NotExistsMatcher;

@SuppressWarnings("MethodCount")
public class BaseValidations implements IBaseValidations
{
    private static final String AN_ELEMENT_WITH_ATTRIBUTES = "An element with attributes: '%s'";

    private final IWebDriverProvider webDriverProvider;
    private final IUiContext uiContext;
    private final ISearchActions searchActions;
    private final IDescriptiveSoftAssert softAssert;

    public BaseValidations(IWebDriverProvider webDriverProvider, IUiContext uiContext,
            ISearchActions searchActions, IDescriptiveSoftAssert softAssert)
    {
        this.webDriverProvider = webDriverProvider;
        this.uiContext = uiContext;
        this.searchActions = searchActions;
        this.softAssert = softAssert;
    }

    @Override
    public boolean assertElementState(String businessDescription, IState state, WebElement element)
    {
        return element != null && assertExpectedCondition(businessDescription, state.getExpectedCondition(element));
    }

    @Override
    public boolean assertElementState(String businessDescription, IState state, WrapsElement element)
    {
        return element != null && assertElementState(businessDescription, state, element.getWrappedElement());
    }

    @Override
    public WebElement assertIfElementExists(String businessDescription, List<WebElement> elements)
    {
        return assertIfElementExists(businessDescription, "Exactly one element within Search context", elements);
    }

    @Override
    public WebElement assertIfElementExists(String businessDescription, String systemDescription,
            List<WebElement> elements)
    {
        if (elements.size() > 1)
        {
            assertElementNumber(businessDescription, systemDescription, elements, 1);
        }
        else if (softAssert.assertThat(businessDescription, systemDescription, elements, ExistsMatcher.exists()))
        {
            return elements.get(0);
        }
        return null;
    }

    @Override
    public boolean assertElementNumber(String businessDescription, String systemDescription, List<WebElement> elements,
            int number)
    {
        return assertElementNumber(businessDescription, systemDescription, elements,
                elementNumber(Matchers.comparesEqualTo(number)));
    }

    @Override
    public boolean assertLeastElementNumber(String businessDescription, String systemDescription,
            List<WebElement> elements, int leastNumber)
    {
        return assertElementNumber(businessDescription, systemDescription, elements,
                elementNumber(Matchers.greaterThanOrEqualTo(leastNumber)));
    }

    private boolean assertElementNumber(String businessDescription, String systemDescription, List<WebElement> elements,
            Matcher<? super List<WebElement>> matcher)
    {
        return uiContext.withAssertingWebElements(elements,
            () -> softAssert.assertThat(businessDescription, systemDescription, elements, matcher));
    }

    @Override
    public boolean assertExpectedCondition(String businessDescription, ExpectedCondition<?> expectedCondition)
    {
        return softAssert.assertThat(businessDescription, expectedCondition.toString(), webDriverProvider.get(),
                ExpectedConditionsMatcher.expectedCondition(expectedCondition));
    }

    @Override
    public WebElement assertIfElementExists(String businessDescription, Locator locator)
    {
        return assertIfElementExists(businessDescription, uiContext.getSearchContext(), locator);
    }

    @Override
    public Optional<WebElement> assertElementExists(String description, Locator locator)
    {
        List<WebElement> elements = searchActions.findElements(uiContext.getSearchContext(), locator);
        if (elements.size() > 1)
        {
            String assertionFormat = "The number of elements found by the locator %s is %d, but expected 1";
            softAssert.recordFailedAssertion(String.format(assertionFormat, locator, elements.size()));
        }
        else if (doesOnlyOneElementExist(description, locator, elements))
        {
            return Optional.of(elements.get(0));
        }
        return Optional.empty();
    }

    private boolean doesOnlyOneElementExist(String description, Locator locator, List<WebElement> elements)
    {
        boolean passed = elements.size() == 1;
        StringBuilder assertionMessageBuilder = new StringBuilder(description).append(" is ");
        if (!passed)
        {
            assertionMessageBuilder.append("not ");
        }
        assertionMessageBuilder.append("found by the locator ").append(locator);
        return softAssert.recordAssertion(passed, assertionMessageBuilder.toString());
    }

    @Override
    public WebElement assertIfElementExists(String businessDescription, SearchContext searchContext, Locator locator)
    {
        List<WebElement> elements = searchActions.findElements(searchContext, locator);
        String systemDescription = String.format(AN_ELEMENT_WITH_ATTRIBUTES, locator);
        return assertIfElementExists(businessDescription, systemDescription, elements);
    }

    @Override
    public List<WebElement> assertIfElementsExist(String businessDescription, Locator locator)
    {
        return assertIfElementsExist(businessDescription, uiContext.getSearchContext(), locator);
    }

    @Override
    public List<WebElement> assertIfElementsExist(String businessDescription, SearchContext searchContext,
            Locator locator)
    {
        List<WebElement> elements = searchActions.findElements(searchContext, locator);
        String systemDescription = String.format("There is at least one element with attributes '%s'",
                locator);
        softAssert.assertThat(businessDescription, systemDescription, elements, elementNumber(greaterThan(0)));
        return elements;
    }

    @Override
    public boolean assertIfExactNumberOfElementsFound(String businessDescription, Locator locator, int number)
    {
        return assertIfExactNumberOfElementsFound(businessDescription, uiContext.getSearchContext(), locator, number);
    }

    @Override
    public boolean assertIfExactNumberOfElementsFound(String businessDescription, SearchContext searchContext,
            Locator locator, int number)
    {
        List<WebElement> elements = searchActions.findElements(searchContext, locator);
        String systemDescription = String.format("Number %d of elements found by '%s'", number, locator);
        return assertElementNumber(businessDescription, systemDescription, elements, number);
    }

    @Override
    public List<WebElement> assertIfAtLeastNumberOfElementsExist(String businessDescription, Locator locator,
            int leastNumber)
    {
        return assertIfAtLeastNumberOfElementsExist(businessDescription, uiContext.getSearchContext(), locator,
                leastNumber);
    }

    @Override
    public List<WebElement> assertIfAtLeastNumberOfElementsExist(String businessDescription,
            SearchContext searchContext, Locator locator, int leastNumber)
    {
        List<WebElement> elements = searchActions.findElements(searchContext, locator);
        String systemDescription = String.format("There are at least %d elements with attributes '%s'", leastNumber,
                locator);
        return assertLeastElementNumber(businessDescription, systemDescription, elements, leastNumber) ? elements
                : List.of();
    }

    @Override
    public WebElement assertIfAtLeastOneElementExists(String businessDescription, Locator locator)
    {
        return assertIfAtLeastOneElementExists(businessDescription, uiContext.getSearchContext(), locator);
    }

    @Override
    public WebElement assertIfAtLeastOneElementExists(String businessDescription, SearchContext searchContext,
            Locator locator)
    {
        List<WebElement> elements = assertIfElementsExist(businessDescription, searchContext, locator);
        return elements.isEmpty() ? null : elements.get(0);
    }

    @Override
    public Optional<WebElement> assertIfZeroOrOneElementFound(String businessDescription, Locator locator)
    {
        return assertIfZeroOrOneElementFound(businessDescription, uiContext.getSearchContext(), locator);
    }

    @Override
    public Optional<WebElement> assertIfZeroOrOneElementFound(String businessDescription, SearchContext searchContext,
            Locator locator)
    {
        List<WebElement> elements = searchActions.findElements(searchContext, locator);
        String systemDescription = String.format(AN_ELEMENT_WITH_ATTRIBUTES, locator);
        if (elements.isEmpty())
        {
            softAssert.recordPassedAssertion(systemDescription + " not found");
            return Optional.empty();
        }
        return Optional.ofNullable(assertIfElementExists(businessDescription, systemDescription, elements));
    }

    @Override
    public boolean assertIfElementDoesNotExist(String businessDescription, Locator locator,
            boolean recordAssertionIfFail)
    {
        return assertIfElementDoesNotExist(businessDescription, uiContext.getSearchContext(), locator,
                recordAssertionIfFail);
    }

    @Override
    public boolean assertIfElementDoesNotExist(String businessDescription, SearchContext searchContext, Locator locator,
            boolean recordAssertionIfFail)
    {
        String systemDescription = String.format("Element with attributes '%s'", locator);
        return assertIfElementDoesNotExist(businessDescription, systemDescription, searchContext, locator,
                recordAssertionIfFail);
    }

    @Override
    public boolean assertIfElementDoesNotExist(String businessDescription, Locator locator)
    {
        return assertIfElementDoesNotExist(businessDescription, uiContext.getSearchContext(), locator);
    }

    @Override
    public boolean assertIfElementDoesNotExist(String businessDescription, SearchContext searchContext, Locator locator)
    {
        return assertIfElementDoesNotExist(businessDescription, searchContext, locator, true);
    }

    @Override
    public boolean assertIfElementDoesNotExist(String businessDescription, String systemDescription, Locator locator)
    {
        return assertIfElementDoesNotExist(businessDescription, systemDescription, uiContext.getSearchContext(),
                locator);
    }

    @Override
    public boolean assertIfElementDoesNotExist(String businessDescription, String systemDescription,
            SearchContext searchContext, Locator locator)
    {
        return assertIfElementDoesNotExist(businessDescription, systemDescription, searchContext, locator,
                true);
    }

    @Override
    public boolean assertIfElementDoesNotExist(String businessDescription, String systemDescription, Locator locator,
            boolean recordAssertionIfFail)
    {
        return assertIfElementDoesNotExist(businessDescription, systemDescription, uiContext.getSearchContext(),
                locator, recordAssertionIfFail);
    }

    @Override
    public boolean assertIfElementDoesNotExist(String businessDescription, String systemDescription,
            SearchContext searchContext, Locator locator, boolean recordAssertionIfFail)
    {
        locator.getSearchParameters().setWaitForElement(false);
        List<WebElement> elements = searchActions.findElements(searchContext, locator);
        boolean noElementsExist = elements.isEmpty();
        if (noElementsExist || recordAssertionIfFail)
        {
            softAssert.assertThat(businessDescription, systemDescription, elements, NotExistsMatcher.notExists());
        }
        return noElementsExist;
    }

    @Override
    public List<WebElement> assertIfNumberOfElementsFound(String businessDescription, SearchContext searchContext,
            Locator locator, int number, ComparisonRule comparisonRule)
    {
        List<WebElement> elements = searchActions.findElements(searchContext, locator);
        String systemDescription = String.format("Number of elements found by '%s' is %s %d", locator,
                comparisonRule, number);
        return softAssert.assertThat(businessDescription, systemDescription, elements,
                elementNumber(comparisonRule.getComparisonRule(number))) ? elements : List.of();
    }

    @Override
    public List<WebElement> assertIfNumberOfElementsFound(String businessDescription, Locator locator,
            int number, ComparisonRule comparisonRule)
    {
        return assertIfNumberOfElementsFound(businessDescription, uiContext.getSearchContext(), locator,
                number, comparisonRule);
    }
}
