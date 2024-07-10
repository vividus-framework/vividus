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

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PlaywrightLocatorConverterTests
{
    static Stream<Arguments> actionAttributeSource()
    {
        return Stream.of(
                Arguments.of(createExpectedLocator(Visibility.ALL),
                        "By.xpath(//div):all"),
                Arguments.of(createExpectedLocator(Visibility.VISIBLE),
                        "By.xpath(//div):v"),
                Arguments.of(createExpectedLocator(Visibility.VISIBLE),
                        "By.xpath(//div)"),
                Arguments.of(createExpectedLocator(Visibility.VISIBLE),
                        "xpath(//div)"));
    }

    @ParameterizedTest
    @MethodSource("actionAttributeSource")
    void shouldConvertToLocator(PlaywrightLocator expected, String testValue)
    {
        assertEquals(expected, PlaywrightLocatorConverter.convertToLocator(testValue));
    }

    @Test
    void testConvertToLocatorInvalidVisibilityType()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PlaywrightLocatorConverter.convertToLocator("By.css(div):invalid"));
        assertEquals("Illegal visibility type 'invalid'. Expected one of 'visible', 'all'",
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenInvalidLocatorFormat()
    {
        String locatorAsString = "InvalidLocatorFormat";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            PlaywrightLocatorConverter.convertToLocator(locatorAsString);
        });
        String expectedMessage = "Invalid locator format. Expected matches [(?:By\\.)?([a-zA-Z-]+)\\((.*)\\)(:(.*))?]"
                                 + " Actual: [" + locatorAsString + "]";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    private static PlaywrightLocator createExpectedLocator(Visibility visibility)
    {
        PlaywrightLocator locator = new PlaywrightLocator("xpath", "//div");
        locator.setVisibility(visibility);
        return locator;
    }
}
