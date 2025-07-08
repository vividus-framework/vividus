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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlaywrightLocatorConverterTests
{
    @ParameterizedTest
    @CsvSource({
            "By.xpath(//div):all, ALL",
            "By.xpath(//div):v, VISIBLE",
            "By.xpath(//div), VISIBLE",
            "xpath(//div), VISIBLE"
    })
    void shouldConvertToLocator(String testValue, Visibility expectedVisibility)
    {
        var expectedLocator = new PlaywrightLocator("xpath", "//div");
        expectedLocator.setVisibility(expectedVisibility);
        assertEquals(expectedLocator, PlaywrightLocatorConverter.convertToLocator(testValue));
    }

    @Test
    void shouldConvertTagNameLocator()
    {
        var expectedLocator = new PlaywrightLocator("css", "div");
        assertEquals(expectedLocator, PlaywrightLocatorConverter.convertToLocator("tagName(div)"));
    }

    @Test
    void testConvertToLocatorInvalidVisibilityType()
    {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> PlaywrightLocatorConverter.convertToLocator("By.css(div):invalid"));
        assertEquals("Illegal visibility type 'invalid'. Expected one of 'visible', 'all'", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenInvalidLocatorFormat()
    {
        var locatorAsString = "InvalidLocatorFormat";
        var exception = assertThrows(IllegalArgumentException.class,
                () -> PlaywrightLocatorConverter.convertToLocator(locatorAsString));
        var expectedMessage =
                "Invalid locator format. Expected matches [(?:By\\.)?([a-zA-Z-]+)\\((.*)\\)(:(.*))?] Actual: ["
                + locatorAsString + "]";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUnsupportedLocatorType()
    {
        var locatorAsString = "invalidType(locatorValue)";
        var exception = assertThrows(IllegalArgumentException.class,
                () -> PlaywrightLocatorConverter.convertToLocator(locatorAsString));
        assertEquals("Unsupported locator type: invalidType", exception.getMessage());
    }
}
