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

package org.vividus.bdd.steps.ui.web.generic.steps;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.hamcrest.Matcher;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.steps.ui.web.validation.ElementValidations;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.web.action.ISearchActions;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.IElementFilterAction;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.action.search.Visibility;
import org.vividus.ui.web.util.ElementUtil;
import org.vividus.ui.web.util.LocatorUtil;

public class ParameterizedSearchActions implements IParameterizedSearchActions
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterizedSearchActions.class);

    @Inject private ISearchActions searchActions;
    @Inject private IWebElementActions webElementActions;
    @Inject private IWebDriverProvider webDriverProvider;
    private IElementFilterAction linkFilterAction;

    @Override
    public List<WebElement> findElements(SearchContext searchContext, SearchInputData searchInputData)
    {
        SearchAttributes attributes = searchInputData.getSearchAttributes();
        return attributes != null ? searchActions.findElements(searchContext, attributes) : new ArrayList<>();
    }

    @Override
    public List<WebElement> findElementsWithAttributes(SearchContext searchContext,
            SearchInputData searchInputData, List<WebElement> elements)
    {
        WebElementAttribute attribute = searchInputData.getAttribute();
        List<WebElement> elementsWithAttribute;
        String attributeName = attribute.getName();
        String attributeValue = attribute.getValue();
        if (attributeName != null)
        {
            if (!elements.isEmpty())
            {
                elementsWithAttribute = filterExistingListByWebElementAttribute(elements, attribute);
            }
            else
            {
                String xPath = attributeValue != null
                        ? LocatorUtil.getXPathByAttribute(attributeName, attributeValue)
                        : LocatorUtil.getXPathByAttribute(attributeName);
                SearchParameters searchParameters = new SearchParameters(xPath)
                        .setDisplayedOnly(searchInputData.isVisible());
                elementsWithAttribute = searchActions.findElements(searchContext,
                        new SearchAttributes(ActionAttributeType.XPATH, searchParameters));
            }
            elementsWithAttribute = filterElementWithAdditionalAttributes(elementsWithAttribute, searchInputData);
            if (!elementsWithAttribute.isEmpty())
            {
                LOGGER.debug("The number of found elements with the specified '{}' attribute: {}",
                        attributeName, elementsWithAttribute.size());
            }
            return elementsWithAttribute;
        }
        else if (attributeValue != null)
        {
            throw new UnsupportedOperationException("Attribute value cannot be checked. No attribute name was set");
        }
        return elements;
    }

    @Override
    public List<WebElement> filterElementsWithCssProperties(SearchInputData searchInputData, List<WebElement> elements)
    {
        List<WebElement> foundElements = new ArrayList<>();
        Integer absWidth = searchInputData.getAbsWidth();
        String cssProperty = searchInputData.getCssProperty();
        String cssValue = searchInputData.getCssValue();
        String cssValuePart = searchInputData.getCssValuePart();
        for (WebElement element : elements)
        {
            if (!hasAbsWidth(element, absWidth))
            {
                break;
            }
            if (cssProperty != null)
            {
                String actualCssValue = webElementActions.getCssValue(element, cssProperty);
                if (null == actualCssValue || ((!actualCssValue.equals(cssValue))
                        && (null == cssValuePart || !actualCssValue.contains(cssValuePart))))
                {
                    break;
                }
            }
            else if (cssValue != null || cssValuePart != null)
            {
                throw new UnsupportedOperationException(
                        "CSS property value cannot be checked. No property name was set");
            }
            foundElements.add(element);
        }
        return foundElements;
    }

    private List<WebElement> filterExistingListByWebElementAttribute(List<WebElement> elements,
            WebElementAttribute expectedAttribute)
    {
        String expectedAttributeName = expectedAttribute.getName();
        if ("href".equals(expectedAttributeName))
        {
            return linkFilterAction.filter(elements, expectedAttribute.getValue());
        }
        String value = expectedAttribute.getValue();
        Matcher<Object> matcher = value == null ? notNullValue() : equalTo(value);
        return elements.stream().filter(e -> matcher.matches(e.getAttribute(expectedAttributeName)))
                .collect(Collectors.toList());
    }

    private boolean hasAbsWidth(WebElement element, Integer width)
    {
        if (width != null)
        {
            SearchParameters parentSearchParameters = new SearchParameters("//body").setVisibility(Visibility.ALL);
            List<WebElement> parentElements = searchActions.findElements(webDriverProvider.get(),
                    new SearchAttributes(ActionAttributeType.XPATH, parentSearchParameters));
            if (parentElements.size() == 1)
            {
                WebElement parentElement = parentElements.get(0);
                return Math.abs(width - ElementUtil.getElementWidthInPerc(parentElement, element))
                        <= ElementValidations.ACCURACY;
            }
        }
        return true;
    }

    private List<WebElement> filterElementWithAdditionalAttributes(List<WebElement> elementsWithAttribute,
            SearchInputData searchInputData)
    {
        List<WebElement> filterElements = Collections.unmodifiableList(elementsWithAttribute);
        for (WebElementAttribute attribute : searchInputData.getAdditionalAttributes())
        {
            filterElements = filterExistingListByWebElementAttribute(filterElements, attribute);
        }
        return filterElements;
    }

    public void setLinkFilterAction(IElementFilterAction linkFilterAction)
    {
        this.linkFilterAction = linkFilterAction;
    }
}
