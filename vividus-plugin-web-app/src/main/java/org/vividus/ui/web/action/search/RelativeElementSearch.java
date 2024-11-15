/*
 * Copyright 2019-2024 the original author or authors.
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

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

public class RelativeElementSearch extends AbstractWebElementSearchAction implements IElementSearchAction
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RelativeElementSearch.class);

    private static final String LOCATOR_ARGUMENTS_PATTERN =
            "(?::([a-zA-Z]+))?(?:->filter\\.((?:[a-zA-Z.]+\\([^\\)]+\\))+))?)";
    private static final Pattern ROOT_ELEMENTS_LOCATOR_PATTERN =
            Pattern.compile("^(\\w+\\([^)]*\\)" + LOCATOR_ARGUMENTS_PATTERN);
    private static final Pattern RELATIVE_LOCATOR_PATTERN = Pattern.compile(">>(\\w+)\\((\\w+\\([^)]*\\)"
            + LOCATOR_ARGUMENTS_PATTERN + "\\)");
    private static final Pattern NEAR_XPX_PATTERN = Pattern.compile("^near(?:(\\d+)(?:px|PX|Px))?$");

    private final StringToLocatorConverter converter;
    private final IBaseValidations baseValidations;

    public RelativeElementSearch(StringToLocatorConverter converter, IBaseValidations baseValidations)
    {
        super(WebLocatorType.RELATIVE);
        this.converter = converter;
        this.baseValidations = baseValidations;
    }

    @Override
    public List<WebElement> search(SearchContext searchContext, SearchParameters parameters)
    {
        String searchParams = parameters.getValue();
        Matcher rootElementsMatcher = ROOT_ELEMENTS_LOCATOR_PATTERN.matcher(searchParams);
        if (rootElementsMatcher.find())
        {
            String rootElementsLocatorString = rootElementsMatcher.group(0);
            return searchRelativeElements(rootElementsLocatorString, searchContext, parameters);
        }
        throw new IllegalArgumentException("Incorrect relative locator format - unable to parse root element locator");
    }

    private List<WebElement> searchRelativeElements(String rootElementsLocatorString, SearchContext searchContext,
                                                    SearchParameters parameters)
    {
        String searchParams = parameters.getValue();

        Optional<By> rootElementByOpt = createRootElementBy(rootElementsLocatorString);
        if (rootElementByOpt.isEmpty())
        {
            return List.of();
        }
        By rootElementBy = rootElementByOpt.get();
        String relativePart = searchParams.substring(rootElementsLocatorString.length());
        Optional<By> relativeLocatorOpt = applyRelativeFiltersToLocator(rootElementBy, relativePart, searchContext);

        if (relativeLocatorOpt.isPresent())
        {
            List<WebElement> elements = findElements(searchContext, relativeLocatorOpt.get(), parameters);
            if (elements.isEmpty())
            {
                LOGGER.atInfo()
                        .addArgument(rootElementBy)
                        .log("Element located {} was found. But it doesn't place in proper position"
                                + " relative to other element in locator");
            }
            return elements;
        }
        return List.of();
    }

    private Optional<By> createRootElementBy(String itemToFindLocatorString)
    {
        Locator rootElementLocator = converter.convert(itemToFindLocatorString);
        List<WebElement> rootElements = baseValidations
                .assertIfElementsExist("root elements", rootElementLocator);
        if (!rootElements.isEmpty())
        {
            return Optional.of(rootElementLocator.getLocatorType()
                    .buildBy(rootElementLocator.getSearchParameters().getValue()));
        }
        return Optional.empty();
    }

    private Optional<By> applyRelativeFiltersToLocator(By rootElementBy, String relativePart,
                                                       SearchContext searchContext)
    {
        RelativeLocator.RelativeBy relativeBy = RelativeLocator.with(rootElementBy);

        for (Map.Entry<String, Map.Entry<String, String>> relativeLocator : getRelativeLocators(relativePart)
                .entrySet())
        {
            Map.Entry<String, String> relativeLocatorEntry = relativeLocator.getValue();

            String relativePositionString = relativeLocatorEntry.getValue();
            RelativeElementPosition relativeElementPosition = findRelativePosition(relativePositionString);

            Locator relativeElementLocator = converter.convert(relativeLocatorEntry.getKey());
            Optional<WebElement> relativeElementOpt = baseValidations
                    .assertElementExists("relative element", searchContext, relativeElementLocator);
            if (relativeElementOpt.isEmpty())
            {
                return Optional.empty();
            }
            WebElement relativeWebElement = unwrapElement(relativeElementOpt.get());

            relativeBy = RelativeElementPosition.NEAR == relativeElementPosition
                    ? applyRelativeNearPosition(relativeBy, relativeWebElement, relativeElementPosition,
                    relativePositionString)
                    : relativeElementPosition.apply(relativeBy, relativeWebElement);
        }
        return Optional.of(relativeBy);
    }

    private Map<String, Map.Entry<String, String>> getRelativeLocators(String relativePart)
    {
        Map<String, Map.Entry<String, String>> relativeLocators = new LinkedHashMap<>();
        Matcher relativeMatcher = RELATIVE_LOCATOR_PATTERN.matcher(relativePart);
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
        String elementRelativePositionInLowerCase = relativePosition.toLowerCase();
        return Stream.of(RelativeElementPosition.values())
                .filter(t -> {
                    String relativePositionKey = StringUtils.replace(t.name().toLowerCase(), "_", "");
                    if (RelativeElementPosition.NEAR == t)
                    {
                        return relativePosition.startsWith(relativePositionKey);
                    }
                    return relativePositionKey.equals(elementRelativePositionInLowerCase);
                })
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Unsupported relative element position: "
                        + relativePosition));
    }

    private WebElement unwrapElement(WebElement webElement)
    {
        if (webElement instanceof WrapsElement)
        {
            return unwrapElement(((WrapsElement) webElement).getWrappedElement());
        }
        return webElement;
    }

    private RelativeLocator.RelativeBy applyRelativeNearPosition(RelativeLocator.RelativeBy relativeBy,
                                                                 WebElement webElement,
                                                                 RelativeElementPosition relativeElementPosition,
                                                                 String relativePositionString)
    {
        Matcher nearMatcher = NEAR_XPX_PATTERN.matcher(relativePositionString);
        if (nearMatcher.matches())
        {
            String distanceInPixelsString = nearMatcher.group(1);
            if (null != distanceInPixelsString)
            {
                int atMostDistanceInPixels = Integer.parseInt(distanceInPixelsString);
                return relativeElementPosition.apply(relativeBy, webElement, atMostDistanceInPixels);
            }
            return relativeElementPosition.apply(relativeBy, webElement);
        }
        throw new IllegalArgumentException(String.format("Invalid near position format."
                + " Expected matches [%s]. Actual [%s]", NEAR_XPX_PATTERN, relativePositionString));
    }
}
