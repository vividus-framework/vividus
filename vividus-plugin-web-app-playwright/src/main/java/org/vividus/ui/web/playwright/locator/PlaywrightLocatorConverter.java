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

package org.vividus.ui.web.playwright.locator;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.text.CaseUtils;
import org.vividus.ui.locator.LocatorParser;

public final class PlaywrightLocatorConverter
{
    private static final int ATTRIBUTE_TYPE_GROUP = 1;
    private static final int SEARCH_VALUE_GROUP = 2;
    private static final int ELEMENT_TYPE_GROUP = 3;
    private static final int VISIBILITY_GROUP = 4;
    private static final char CLOSING_BRACKET = ']';
    private static final String LOCATOR_FORMAT = "(?:By\\.)?([a-zA-Z-]+)\\((.*)\\)(:(.*))?";
    private static final Pattern LOCATOR_PATTERN = Pattern.compile(LOCATOR_FORMAT);

    private PlaywrightLocatorConverter()
    {
    }

    public static Set<PlaywrightLocator> convertToLocatorSet(String locatorsAsString)
    {
        return LocatorParser.parseLocatorsCollection(locatorsAsString, PlaywrightLocatorConverter::convertToLocator);
    }

    public static PlaywrightLocator convertToLocator(String locatorAsString)
    {
        Matcher matcher = LOCATOR_PATTERN.matcher(locatorAsString);
        if (matcher.matches())
        {
            String elementActionType = matcher.group(ATTRIBUTE_TYPE_GROUP);
            String searchValue = matcher.group(SEARCH_VALUE_GROUP);
            String elementType = matcher.group(ELEMENT_TYPE_GROUP);
            PlaywrightLocator convertedLocator = convertToLocator(elementActionType, searchValue);
            if (elementType != null)
            {
                String visibilityType = matcher.group(VISIBILITY_GROUP);
                convertedLocator.setVisibility(Visibility.getElementType(visibilityType));
            }
            return convertedLocator;
        }
        throw new IllegalArgumentException("Invalid locator format. Expected matches [" + LOCATOR_FORMAT
                                           + CLOSING_BRACKET + " Actual: [" + locatorAsString + CLOSING_BRACKET);
    }

    private static PlaywrightLocator convertToLocator(String type, String value)
    {
        return getLocatorType(type).createLocator(value);
    }

    private static PlaywrightLocatorType getLocatorType(String locatorType)
    {
        return Stream.of(PlaywrightLocatorType.values())
                .filter(type -> CaseUtils.toCamelCase(type.name(), false, '_').equals(locatorType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported locator type: " + locatorType));
    }
}
