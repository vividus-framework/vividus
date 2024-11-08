/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.ui.web.action.search;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.support.locators.RelativeLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.locator.Locator;
import org.vividus.selenium.locator.RelativeElementPosition;
import org.vividus.spring.StringToLocatorConverter;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.IElementSearchAction;
import org.vividus.ui.action.search.SearchParameters;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class RelativeElementSearch extends AbstractWebElementSearchAction implements IElementSearchAction
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RelativeElementSearch.class);

    private static final String LOCATOR_ARGUMENTS_PATTERN = "(?::([a-zA-Z]+))?(?:->filter\\.((?:[a-zA-Z.]+\\([^\\)]+\\))+))?)";
    //    private static final Pattern ITEM_TO_FIND_LOCATOR_PATTERN = Pattern.compile("^(\\w+\\([^)]+\\))");
    private static final Pattern ITEM_TO_FIND_LOCATOR_PATTERN = Pattern.compile("^(\\w+\\([^)]*\\)" + LOCATOR_ARGUMENTS_PATTERN);
//    private static final Pattern RELATIVE_LOCATORS_PATTERN = Pattern.compile("\\.(\\w+)\\((\\w+\\([^)]+\\))\\)");
    private static final Pattern RELATIVE_LOCATORS_PATTERN = Pattern.compile(">>(\\w+)\\((\\w+\\([^)]*\\)" + LOCATOR_ARGUMENTS_PATTERN + "\\)");
    private static final Pattern NEAR_XPX_PATTERN = Pattern.compile("^near(\\d+)px$");

    private StringToLocatorConverter converter;
    private IBaseValidations baseValidations;

    public RelativeElementSearch()
    {
        super(WebLocatorType.RELATIVE);
    }

    @Override
    public List<WebElement> search(SearchContext searchContext, SearchParameters parameters)
    {
        return convertRelativeStringToBy(searchContext, parameters);
    }

    private List<WebElement> convertRelativeStringToBy(SearchContext searchContext, SearchParameters parameters)
    {
        String searchParams = parameters.getValue();
        Matcher itemToFindMatcher = ITEM_TO_FIND_LOCATOR_PATTERN.matcher(searchParams);
        if (itemToFindMatcher.find())
        {
            final String itemToFindLocatorString = itemToFindMatcher.group(0);
            String relativePart = searchParams.substring(itemToFindLocatorString.length());

            Locator itemToFindLocator = converter.convert(itemToFindLocatorString);
            List<WebElement> webElements = baseValidations.assertIfElementsExist("main element", itemToFindLocator);
            if (!webElements.isEmpty())
            {
                By itemToFindBy = itemToFindLocator.getLocatorType().buildBy(itemToFindLocator.getSearchParameters().getValue());
                RelativeLocator.RelativeBy relativeBy = RelativeLocator.with(itemToFindBy);

                for (Map.Entry<String, Map.Entry<String, String>> relativeLocator : getRelativeLocators(relativePart).entrySet())
                {
                    Map.Entry<String, String> relativeLocatorEntry = relativeLocator.getValue();
                    Locator locator = converter.convert(relativeLocatorEntry.getKey());
                    Optional<WebElement> element = baseValidations.assertElementExists("relative element", searchContext, locator);
                    if (element.isEmpty())
                    {
                        return List.of();
                    }
                    WebElement webElement = unwrapElement(element.get());
                    String relativePositionString = relativeLocatorEntry.getValue();
                    RelativeElementPosition relativeElementPosition = findRelativePosition(relativePositionString);
                    if (RelativeElementPosition.NEAR_XPX == relativeElementPosition)
                    {
                        Matcher nearMatcher = NEAR_XPX_PATTERN.matcher(relativePositionString);
                        if (nearMatcher.matches())
                        {
                            int atMostDistanceInPixels = Integer.parseInt(nearMatcher.group(1));
                            relativeBy = relativeElementPosition.apply(relativeBy, webElement, atMostDistanceInPixels);
                        }
                    }
                    else
                    {
                        relativeBy = relativeElementPosition.apply(relativeBy, webElement);
                    }
                }
                List<WebElement> elements = findElements(searchContext, relativeBy, parameters);
                if (elements.isEmpty())
                {
                    LOGGER.atInfo()
                            .addArgument(itemToFindBy.toString())
                            .log("Element located {} was found. But it doesn't place in proper position"
                                    + " relative to other element in locator");
                }
                return elements;
            }
            return List.of();
        }
        throw new IllegalArgumentException("Invalid relative locator format");
    }

    private WebElement unwrapElement(WebElement webElement)
    {
        if (webElement instanceof WrapsElement)
        {
            return unwrapElement(((WrapsElement) webElement).getWrappedElement());
        }
        return webElement;
    }

    private Map<String, Map.Entry<String, String>> getRelativeLocators(String relativePart)
    {
        Matcher relativeMatcher = RELATIVE_LOCATORS_PATTERN.matcher(relativePart);
        Map<String, Map.Entry<String, String>> relativeLocators = new LinkedHashMap<>();
        while (relativeMatcher.find())
        {
            String action = relativeMatcher.group(1);
            String locator = relativeMatcher.group(2);
            relativeLocators.put(action + locator, new AbstractMap.SimpleEntry<>(locator, action));
        }
        return relativeLocators;
    }

    private RelativeElementPosition findRelativePosition(String relativePosition)
    {
        String typeInLowerCase = relativePosition.toLowerCase();
        return Stream.of(RelativeElementPosition.values())
                .filter(t -> {
                    String relativePositionKey = StringUtils.replace(t.name().toLowerCase(), "_", "");
                    if ("nearxpx".equals(relativePositionKey))
                    {

                        return NEAR_XPX_PATTERN.matcher(typeInLowerCase).matches();
                    }
                    return relativePositionKey.equals(typeInLowerCase);
                })
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        String.format("Unsupported relative element position: %s", relativePosition)));
    }

    public void setConverter(StringToLocatorConverter converter)
    {
        this.converter = converter;
    }

    public void setBaseValidations(IBaseValidations baseValidations)
    {
        this.baseValidations = baseValidations;
    }
}
