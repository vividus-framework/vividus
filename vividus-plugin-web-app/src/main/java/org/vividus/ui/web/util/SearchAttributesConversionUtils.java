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

package org.vividus.ui.web.util;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.vividus.selenium.LocatorFactory;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.Visibility;

public final class SearchAttributesConversionUtils
{
    private static final String EMPTY = "";
    private static final int ELEMENT_TYPE_GROUP = 3;
    private static final char CLOSING_BRACKET = ']';
    private static final String LOCATOR_FORMAT = "By\\.([a-zA-Z]+)\\((.+?)\\):?([a-zA-Z]*)?";
    private static final Pattern SEARCH_ATTRIBUTE_PATTERN = Pattern.compile(LOCATOR_FORMAT);
    private static final Pattern FILTER_PATTERN = Pattern.compile("([a-zA-Z]+)(?:\\()([^()]*)(?:\\))");

    private SearchAttributesConversionUtils()
    {
    }

    public static Set<SearchAttributes> convertToSearchAttributesSet(String locatorsAsString)
    {
        return LocatorFactory.convertStringToLocators(locatorsAsString).stream()
                .map(locator -> convertToSearchAttributes(locator.getType(), locator.getValue()))
                .collect(Collectors.toSet());
    }

    public static SearchAttributes convertToSearchAttributes(String locatorAsString)
    {
        String[] locatorParts = locatorAsString.split("->filter\\.");
        String locator = locatorParts.length == 0 ? locatorAsString : locatorParts[0];
        String filters = locatorParts.length == 2 ? locatorParts[1] : EMPTY;
        Matcher matcher = SEARCH_ATTRIBUTE_PATTERN.matcher(locator);
        if (matcher.matches())
        {
            String actionAttributeType = matcher.group(1).toLowerCase();
            String searchValue = matcher.group(2);
            String elementType = matcher.group(ELEMENT_TYPE_GROUP);
            SearchAttributes searchAttributes = convertToSearchAttributes(actionAttributeType, searchValue);
            if (!elementType.isEmpty())
            {
                searchAttributes.getSearchParameters().setVisibility(Visibility.getElementType(elementType));
            }
            if (!filters.isEmpty())
            {
                Matcher filterMatcher = FILTER_PATTERN.matcher(filters);
                while (filterMatcher.find())
                {
                    applyFilter(searchAttributes, filterMatcher.group(1).toLowerCase(), filterMatcher.group(2));
                }
            }
            return searchAttributes;
        }
        throw new IllegalArgumentException("Invalid locator format. Expected matches ["
                + LOCATOR_FORMAT + CLOSING_BRACKET + " Actual: [" + locatorAsString + CLOSING_BRACKET);
    }

    private static void applyFilter(SearchAttributes searchAttributes, String filterType, String filterValue)
    {
        findActionAttributeType(ActionAttributeType.getFilterTypes(), filterType).ifPresent(
            type -> searchAttributes.addFilter(type, filterValue));
    }

    private static SearchAttributes convertToSearchAttributes(String searchType, String searchValue)
    {
        switch (searchType.toLowerCase())
        {
            case "name":
                return new SearchAttributes(ActionAttributeType.ELEMENT_NAME, searchValue);
            case "partiallinktext":
                return new SearchAttributes(ActionAttributeType.TAG_NAME, "a")
                    .addFilter(ActionAttributeType.TEXT_PART, searchValue);
            case "xpath":
                return new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(searchValue));
            default:
                return findActionAttributeType(ActionAttributeType.getSearchTypes(), searchType)
                        .map(type -> new SearchAttributes(type, searchValue))
                        .orElseThrow(() -> new IllegalArgumentException("Unsupported locator type: " + searchType));
        }
    }

    private static Optional<ActionAttributeType> findActionAttributeType(Set<ActionAttributeType> types, String type)
    {
        String typeInLowerCase = type.toLowerCase();
        return types.stream().filter(t -> StringUtils.replace(t.name().toLowerCase(), "_", "").equals(typeInLowerCase))
                .findFirst();
    }
}
