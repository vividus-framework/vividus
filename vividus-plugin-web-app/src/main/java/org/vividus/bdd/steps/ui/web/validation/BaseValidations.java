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

import static org.hamcrest.Matchers.greaterThan;
import static org.vividus.ui.validation.matcher.WebElementMatchers.elementNumber;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.hamcrest.Matchers;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.validation.matcher.ExistsMatcher;
import org.vividus.ui.validation.matcher.ExpectedConditionsMatcher;
import org.vividus.ui.validation.matcher.NotExistsMatcher;
import org.vividus.ui.web.IState;
import org.vividus.ui.web.action.ISearchActions;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.util.UriUtils;

@SuppressWarnings("MethodCount")
public class BaseValidations implements IBaseValidations
{
    private static final String AN_ELEMENT_WITH_ATTRIBUTES = "An element with attributes: '%s'";

    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IWebUiContext webUiContext;
    @Inject private ISearchActions searchActions;
    @Inject private IElementValidations elementValidations;
    @Inject private IHighlightingSoftAssert softAssert;

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
        return elementValidations.assertElementNumber(businessDescription, systemDescription, elements,
                elementNumber(Matchers.comparesEqualTo(number)));
    }

    @Override
    public boolean assertLeastElementNumber(String businessDescription, String systemDescription,
            List<WebElement> elements, int leastNumber)
    {
        return elementValidations.assertElementNumber(businessDescription, systemDescription, elements,
                elementNumber(Matchers.greaterThanOrEqualTo(leastNumber)));
    }

    @Override
    public boolean assertExpectedCondition(String businessDescription, ExpectedCondition<?> expectedCondition)
    {
        return softAssert.assertThat(businessDescription, expectedCondition.toString(), getWebDriver(),
                ExpectedConditionsMatcher.expectedCondition(expectedCondition));
    }

    @Override
    public WebElement assertIfElementExists(String businessDescription, SearchAttributes searchAttributes)
    {
        return assertIfElementExists(businessDescription, webUiContext.getSearchContext(), searchAttributes);
    }

    @Override
    public WebElement assertIfElementExists(String businessDescription, SearchContext searchContext,
            SearchAttributes searchAttributes)
    {
        List<WebElement> elements = searchActions.findElements(searchContext, searchAttributes);
        String systemDescription = String.format(AN_ELEMENT_WITH_ATTRIBUTES, searchAttributes);
        return assertIfElementExists(businessDescription, systemDescription, elements);
    }

    @Override
    public List<WebElement> assertIfElementsExist(String businessDescription, SearchAttributes searchAttributes)
    {
        return assertIfElementsExist(businessDescription, webUiContext.getSearchContext(), searchAttributes);
    }

    @Override
    public List<WebElement> assertIfElementsExist(String businessDescription, SearchContext searchContext,
            SearchAttributes searchAttributes)
    {
        List<WebElement> elements = searchActions.findElements(searchContext, searchAttributes);
        String systemDescription = String.format("There is at least one element with attributes '%s'",
                searchAttributes);
        softAssert.assertThat(businessDescription, systemDescription, elements, elementNumber(greaterThan(0)));
        return elements;
    }

    @Override
    public boolean assertIfExactNumberOfElementsFound(String businessDescription, SearchAttributes searchAttributes,
            int number)
    {
        return assertIfExactNumberOfElementsFound(businessDescription, webUiContext.getSearchContext(),
                searchAttributes, number);
    }

    @Override
    public boolean assertIfExactNumberOfElementsFound(String businessDescription, SearchContext searchContext,
            SearchAttributes searchAttributes, int number)
    {
        List<WebElement> elements = searchActions.findElements(searchContext, searchAttributes);
        String systemDescription = String.format("Number %d of elements found by '%s'", number, searchAttributes);
        return assertElementNumber(businessDescription, systemDescription, elements, number);
    }

    @Override
    public List<WebElement> assertIfAtLeastNumberOfElementsExist(String businessDescription,
            SearchAttributes searchAttributes, int leastNumber)
    {
        return assertIfAtLeastNumberOfElementsExist(businessDescription, webUiContext.getSearchContext(),
                searchAttributes, leastNumber);
    }

    @Override
    public List<WebElement> assertIfAtLeastNumberOfElementsExist(String businessDescription,
            SearchContext searchContext, SearchAttributes searchAttributes, int leastNumber)
    {
        List<WebElement> elements = searchActions.findElements(searchContext, searchAttributes);
        String systemDescription = String.format("There are at least %d elements with attributes '%s'", leastNumber,
                searchAttributes);
        return assertLeastElementNumber(businessDescription, systemDescription, elements, leastNumber) ? elements
                : List.of();
    }

    @Override
    public WebElement assertIfAtLeastOneElementExists(String businessDescription, SearchAttributes searchAttributes)
    {
        return assertIfAtLeastOneElementExists(businessDescription, webUiContext.getSearchContext(), searchAttributes);
    }

    @Override
    public WebElement assertIfAtLeastOneElementExists(String businessDescription, SearchContext searchContext,
            SearchAttributes searchAttributes)
    {
        List<WebElement> elements = assertIfElementsExist(businessDescription, searchContext, searchAttributes);
        return elements.isEmpty() ? null : elements.get(0);
    }

    @Override
    public Optional<WebElement> assertIfZeroOrOneElementFound(String businessDescription,
            SearchAttributes searchAttributes)
    {
        return assertIfZeroOrOneElementFound(businessDescription, webUiContext.getSearchContext(), searchAttributes);
    }

    @Override
    public Optional<WebElement> assertIfZeroOrOneElementFound(String businessDescription, SearchContext searchContext,
            SearchAttributes searchAttributes)
    {
        List<WebElement> elements = searchActions.findElements(searchContext, searchAttributes);
        String systemDescription = String.format(AN_ELEMENT_WITH_ATTRIBUTES, searchAttributes);
        if (elements.isEmpty())
        {
            softAssert.recordPassedAssertion(systemDescription + " not found");
            return Optional.empty();
        }
        return Optional.ofNullable(assertIfElementExists(businessDescription, systemDescription, elements));
    }

    @Override
    public boolean assertIfElementDoesNotExist(String businessDescription, SearchAttributes searchAttributes,
            boolean recordAssertionIfFail)
    {
        return assertIfElementDoesNotExist(businessDescription, webUiContext.getSearchContext(), searchAttributes,
                recordAssertionIfFail);
    }

    @Override
    public boolean assertIfElementDoesNotExist(String businessDescription, SearchContext searchContext,
            SearchAttributes searchAttributes, boolean recordAssertionIfFail)
    {
        String systemDescription = String.format("Element with attributes '%s'", searchAttributes);
        return assertIfElementDoesNotExist(businessDescription, systemDescription, searchContext, searchAttributes,
                recordAssertionIfFail);
    }

    @Override
    public boolean assertIfElementDoesNotExist(String businessDescription, SearchAttributes searchAttributes)
    {
        return assertIfElementDoesNotExist(businessDescription, webUiContext.getSearchContext(), searchAttributes);
    }

    @Override
    public boolean assertIfElementDoesNotExist(String businessDescription, SearchContext searchContext,
            SearchAttributes searchAttributes)
    {
        return assertIfElementDoesNotExist(businessDescription, searchContext, searchAttributes, true);
    }

    @Override
    public boolean assertIfElementDoesNotExist(String businessDescription, String systemDescription,
            SearchAttributes searchAttributes)
    {
        return assertIfElementDoesNotExist(businessDescription, systemDescription, webUiContext.getSearchContext(),
                searchAttributes);
    }

    @Override
    public boolean assertIfElementDoesNotExist(String businessDescription, String systemDescription,
            SearchContext searchContext, SearchAttributes searchAttributes)
    {
        return assertIfElementDoesNotExist(businessDescription, systemDescription, searchContext, searchAttributes,
                true);
    }

    @Override
    public boolean assertIfElementDoesNotExist(String businessDescription, String systemDescription,
            SearchAttributes searchAttributes, boolean recordAssertionIfFail)
    {
        return assertIfElementDoesNotExist(businessDescription, systemDescription, webUiContext.getSearchContext(),
                searchAttributes, recordAssertionIfFail);
    }

    @Override
    public boolean assertIfElementDoesNotExist(String businessDescription, String systemDescription,
            SearchContext searchContext, SearchAttributes searchAttributes, boolean recordAssertionIfFail)
    {
        searchAttributes.getSearchParameters().setWaitForElement(false);
        List<WebElement> elements = searchActions.findElements(searchContext, searchAttributes);
        boolean noElementsExist = elements.isEmpty();
        if (noElementsExist || recordAssertionIfFail)
        {
            softAssert.assertThat(businessDescription, systemDescription, elements, NotExistsMatcher.notExists());
        }
        return noElementsExist;
    }

    @Override
    public boolean assertPageWithURLPartIsLoaded(String urlPart)
    {
        URI actualUrl = UriUtils.createUri(getWebDriver().getCurrentUrl());
        String actualDecodedUrl = actualUrl.toString();
        return softAssert.assertThat(String.format("Page with the URLpart '%s' is loaded", urlPart),
                String.format("Page url '%1$s' contains part '%2$s'", actualDecodedUrl, urlPart), actualDecodedUrl,
                Matchers.containsString(urlPart));
    }

    @Override
    public List<WebElement> assertIfNumberOfElementsFound(String businessDescription, SearchContext searchContext,
            SearchAttributes searchAttributes, int number, ComparisonRule comparisonRule)
    {
        String comparisonDescription = comparisonRule.toString().toLowerCase().replace('_', ' ');
        List<WebElement> elements = searchActions.findElements(searchContext, searchAttributes);
        String systemDescription = String.format("Number of elements found by '%s' is %s %d", searchAttributes,
                comparisonDescription, number);
        return softAssert.assertThat(businessDescription, systemDescription, elements,
                elementNumber(comparisonRule.getComparisonRule(number))) ? elements : List.of();
    }

    @Override
    public List<WebElement> assertIfNumberOfElementsFound(String businessDescription, SearchAttributes searchAttributes,
            int number, ComparisonRule comparisonRule)
    {
        return assertIfNumberOfElementsFound(businessDescription, webUiContext.getSearchContext(), searchAttributes,
                number, comparisonRule);
    }

    private WebDriver getWebDriver()
    {
        return webDriverProvider.get();
    }
}
