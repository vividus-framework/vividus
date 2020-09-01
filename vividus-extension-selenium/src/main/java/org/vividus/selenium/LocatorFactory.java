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

package org.vividus.selenium;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

public final class LocatorFactory
{
    private static final String LOCATOR_PREFIX = "By.";
    private static final String DELIMITER = ", " + LOCATOR_PREFIX;

    private LocatorFactory()
    {
    }

    public static By convertStringToLocator(String source)
    {
        Locator locator = parseLocator(source);
        if (locator == null)
        {
            return null;
        }
        try
        {
            Method method = By.class.getMethod(locator.getType(), String.class);
            return (By) method.invoke(null, locator.getValue());
        }
        catch (ReflectiveOperationException | SecurityException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    public static Set<By> convertStringToLocatorSet(String source)
    {
        return convertToLocators(source, LocatorFactory::convertStringToLocator);
    }

    public static Set<Locator> convertStringToLocators(String source)
    {
        return convertToLocators(source, LocatorFactory::parseLocator);
    }

    private static <T> Set<T> convertToLocators(String source, Function<String, T> elementConverter)
    {
        String[] locators = StringUtils.splitByWholeSeparator(source, DELIMITER);
        return Stream.of(locators)
            .map(String::trim)
            .map(elementConverter)
            .collect(Collectors.toSet());
    }

    private static Locator parseLocator(String source)
    {
        if (StringUtils.isBlank(source))
        {
            return null;
        }
        String processedLocator = source.replace(LOCATOR_PREFIX, "");
        int firstBracketIndex = processedLocator.indexOf('(');
        String locatorType = processedLocator.substring(0, firstBracketIndex);
        String locatorValue = processedLocator.substring(firstBracketIndex + 1, processedLocator.length() - 1);
        return new Locator(locatorType, locatorValue);
    }

    public static final class Locator
    {
        private final String type;
        private final String value;

        Locator(String type, String value)
        {
            this.type = type;
            this.value = value;
        }

        public String getType()
        {
            return type;
        }

        public String getValue()
        {
            return value;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            Locator that = (Locator) o;
            return Objects.equals(type, that.type) && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(type, value);
        }
    }
}
