/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.ui.util;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.vividus.selenium.LocatorFactory;
import org.vividus.ui.action.search.ElementActionService;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.LocatorPattern;
import org.vividus.ui.action.search.LocatorType;
import org.vividus.ui.action.search.Visibility;
import org.vividus.util.property.PropertyMappedCollection;

public class LocatorConversionUtils
{
    private static final int SEARCH_VALUE_GROUP = 2;
    private static final int ATTRIBUTE_TYPE_GROUP = 1;
    private static final String EMPTY = "";
    private static final int ELEMENT_TYPE_GROUP = 3;
    private static final int VISIBILITY_GROUP = 4;
    private static final char CLOSING_BRACKET = ']';
    private static final String LOCATOR_FORMAT = "(?:By\\.)?([a-zA-Z-]+)\\((.*)\\)(:(.*))?";
    private static final String ESCAPED_COMMA = "\\,";
    private static final Pattern LOCATOR_PATTERN = Pattern.compile(LOCATOR_FORMAT);
    private static final Pattern FILTER_PATTERN = Pattern.compile("([a-zA-Z]+)(?:\\()([^()]*)(?:\\))");
    private static final Pattern PARAMS_SEPARATOR = Pattern.compile("(?<!\\\\),\\s*");

    @Inject private ElementActionService elementActionService;
    private PropertyMappedCollection<LocatorPattern> dynamicLocators;

    public Set<Locator> convertToLocatorSet(String locatorsAsString)
    {
        return LocatorFactory.convertStringToLocators(locatorsAsString).stream()
                .map(locator -> convertToLocator(locator.getType(), locator.getValue()))
                .collect(Collectors.toSet());
    }

    public Locator convertToLocator(String locatorAsString)
    {
        String[] locatorParts = locatorAsString.split("->filter\\.");
        String locator = locatorParts.length == 0 ? locatorAsString : locatorParts[0];
        String filters = locatorParts.length == 2 ? locatorParts[1] : EMPTY;
        Matcher matcher = LOCATOR_PATTERN.matcher(locator);
        if (matcher.matches())
        {
            String elementActionType = matcher.group(ATTRIBUTE_TYPE_GROUP).toLowerCase();
            String searchValue = matcher.group(SEARCH_VALUE_GROUP);
            String elementType = matcher.group(ELEMENT_TYPE_GROUP);
            Locator convertedLocator = convertToLocator(elementActionType, searchValue);
            if (elementType != null)
            {
                String visibilityType = matcher.group(VISIBILITY_GROUP);
                convertedLocator.getSearchParameters().setVisibility(Visibility.getElementType(visibilityType));
            }
            if (!filters.isEmpty())
            {
                Matcher filterMatcher = FILTER_PATTERN.matcher(filters);
                while (filterMatcher.find())
                {
                    applyFilter(convertedLocator, filterMatcher.group(1), filterMatcher.group(2));
                }
            }
            return convertedLocator;
        }
        throw new IllegalArgumentException("Invalid locator format. Expected matches ["
                + LOCATOR_FORMAT + CLOSING_BRACKET + " Actual: [" + locatorAsString + CLOSING_BRACKET);
    }

    private void applyFilter(Locator locator, String filterType, String filterValue)
    {
        createLocator(elementActionService.getFilterLocatorTypes(), locator::addFilter, "filter", filterType,
                filterValue);
    }

    private Locator convertToLocator(String searchType, String searchValue)
    {
        return createLocator(elementActionService.getSearchLocatorTypes(), Locator::new, "locator", searchType,
            searchValue);
    }

    private Locator createLocator(Set<LocatorType> locatorTypes, BiFunction<LocatorType, String, Locator> mapper,
            String operatorType, String searchType, String searchValue)
    {
        return findLocatorType(locatorTypes, searchType)
                .map(t -> mapper.apply(t, searchValue))
                .or(() -> dynamicLocators.getNullable(searchType)
                                         .map(l -> createLocator(locatorTypes, mapper, operatorType, l.getLocatorType(),
                                                 buildLocator(searchValue, l))))
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Unsupported %s type: %s", operatorType, searchType)));
    }

    private String buildLocator(String searchValue, LocatorPattern locatorPattern)
    {
        String[] parameters;
        int parametersQuantity = locatorPattern.getParametersQuantity();
        if (parametersQuantity == 1)
        {
            parameters = new String[] { searchValue };
        }
        else
        {
            parameters = PARAMS_SEPARATOR.split(searchValue);
            parameters = searchValue.contains(ESCAPED_COMMA) ? Stream.of(parameters)
                    .map(p -> p.replace(ESCAPED_COMMA, ","))
                    .toArray(String[]::new)
                    : parameters;
        }
        String pattern = locatorPattern.getPattern();
        if (parametersQuantity > parameters.length)
        {
            throw new IllegalArgumentException(
                    String.format("The pattern `%s` expecting `%d` parameters, but got `%d`", pattern,
                            parametersQuantity, parameters.length));
        }
        return "xpath".equalsIgnoreCase(locatorPattern.getLocatorType())
            ? XpathLocatorUtils.getXPath(pattern, parameters)
            : String.format(pattern, parameters);
    }

    private static Optional<LocatorType> findLocatorType(Set<LocatorType> locatorTypes, String type)
    {
        String typeInLowerCase = type.toLowerCase();
        return locatorTypes.stream()
                       .filter(t -> StringUtils.replace(t.getKey().toLowerCase(), "_", "")
                           .equals(typeInLowerCase))
                       .findFirst();
    }

    public void setDynamicLocators(PropertyMappedCollection<LocatorPattern> dynamicLocators)
    {
        this.dynamicLocators = dynamicLocators;
    }
}
