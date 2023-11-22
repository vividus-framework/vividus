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

package org.vividus.ui.locator;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public final class LocatorParser
{
    private static final String PREFIX = "By.";
    private static final Pattern DELIMITER = Pattern.compile(",\\s*By\\.");

    private LocatorParser()
    {
    }

    public static <T> Set<T> parseLocatorsCollection(String locatorsAsString,
            BiFunction<String, String, T> locatorFactory)
    {
        return Stream.of(DELIMITER.split(locatorsAsString))
                .map(String::trim)
                .map(l -> LocatorParser.parseSingleLocator(l, locatorFactory))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static <T> T parseSingleLocator(String locatorAsString, BiFunction<String, String, T> locatorFactory)
    {
        if (StringUtils.isBlank(locatorAsString))
        {
            return null;
        }
        int startIndex = locatorAsString.startsWith(PREFIX) ? PREFIX.length() : 0;
        int firstBracketIndex = locatorAsString.indexOf('(');
        String locatorType = locatorAsString.substring(startIndex, firstBracketIndex);
        String locatorValue = locatorAsString.substring(firstBracketIndex + 1, locatorAsString.length() - 1);
        return locatorFactory.apply(locatorType, locatorValue);
    }
}
