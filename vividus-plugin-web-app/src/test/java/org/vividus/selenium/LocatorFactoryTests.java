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

package org.vividus.selenium;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;

class LocatorFactoryTests
{
    private static final String XPATH_PARAM = "By.xpath(.//a)";
    private static final String XPATH_SET_PARAM = XPATH_PARAM + ", By.xpath(.//div)";
    private static final String XPATH_INCORRECT = "By.js(.//a)";
    private static final String XPATH = ".//a";
    private static final String XPATH_SECOND = ".//div";
    private static final String NO_SUCH_METHOD = "java.lang.NoSuchMethodException";
    private static final LocatorFactory.Locator FIRST_LOCATOR = new LocatorFactory.Locator("xpath", ".//");
    private static final LocatorFactory.Locator SECOND_LOCATOR = new LocatorFactory.Locator("cssSelector", "div");

    @ParameterizedTest
    @CsvSource({XPATH_PARAM, "xpath(.//a)"})
    void convertStringToLocatorTest(String locatorAsString)
    {
        By expected = By.xpath(XPATH);
        assertEquals(expected, LocatorFactory.convertStringToLocator(locatorAsString));
    }

    @Test
    void convertStringToLocatorExceptionTest()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> LocatorFactory.convertStringToLocator(XPATH_INCORRECT));
        assertThat(exception.getMessage(), containsString(NO_SUCH_METHOD));
    }

    @Test
    void convertStringToLocatorSetTest()
    {
        By first = By.xpath(XPATH);
        By second = By.xpath(XPATH_SECOND);
        Set<By> expected = new HashSet<>();
        expected.add(first);
        expected.add(second);
        assertEquals(expected, LocatorFactory.convertStringToLocatorSet(XPATH_SET_PARAM));
    }

    @Test
    void convertStringToLocatorSourceIsBlankTest()
    {
        assertNull(LocatorFactory.convertStringToLocator(StringUtils.EMPTY));
    }

    @Test
    void convertStringToLocatorSetExceptionTest()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> LocatorFactory.convertStringToLocatorSet(XPATH_INCORRECT));
        assertThat(exception.getMessage(), containsString(NO_SUCH_METHOD));
    }

    @Test
    void convertEmptyStringToLocatorSetTest()
    {
        assertEquals(Collections.emptySet(), LocatorFactory.convertStringToLocatorSet(StringUtils.EMPTY));
    }

    @Test
    void convertStringToLocatorsTest()
    {
        Set<LocatorFactory.Locator> expected = new HashSet<>();
        expected.add(FIRST_LOCATOR);
        expected.add(SECOND_LOCATOR);
        assertEquals(expected, LocatorFactory.convertStringToLocators("By.xpath(.//), By.cssSelector(div)"));
    }

    @Test
    void convertStringToLocatorsSourceIsBlankTest()
    {
        assertEquals(Collections.emptySet(), LocatorFactory.convertStringToLocators(""));
    }

    @Test
    void locatorEquals()
    {
        assertTrue(FIRST_LOCATOR.equals(FIRST_LOCATOR));
    }

    @Test
    void locatorEqualsToNull()
    {
        assertNotNull(SECOND_LOCATOR);
    }
}
