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

package org.vividus.selenium.driver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.element.TextFormattingWebElement;

@ExtendWith(MockitoExtension.class)
class TextFormattingWebDriverTests
{
    @Mock
    private WebDriver wrappedDriver;

    @InjectMocks
    private TextFormattingWebDriver textFormattingWebDriver;

    @Test
    void testFindElementsFound()
    {
        By locator = By.id("//input");
        WebElement webElement = mock(WebElement.class);
        List<WebElement> webElements = List.of(webElement);
        when(wrappedDriver.findElements(locator)).thenReturn(webElements);
        assertEquals(webElements, textFormattingWebDriver.getWrappedDriver().findElements(locator));
    }

    @Test
    void testFindElementFound()
    {
        By locator = By.id("//div[1]");
        WebElement webElement = mock(WebElement.class);
        when(wrappedDriver.findElement(locator)).thenReturn(webElement);
        assertEquals(webElement, textFormattingWebDriver.getWrappedDriver().findElement(locator));
    }

    @Test
    @SuppressWarnings("checkstyle:avoidescapedunicodecharacters")
    void testGetTitle()
    {
        when(wrappedDriver.getTitle()).thenReturn("Vividus\u2019S\u00AE\u00A0data");
        assertEquals("Vividus’S® data", textFormattingWebDriver.getTitle());
    }

    @Test
    void testFindElement()
    {
        By locator = By.id("//a");
        TextFormattingWebElement textFormattingWebElement = mock(TextFormattingWebElement.class);
        when(wrappedDriver.findElement(locator)).thenReturn(textFormattingWebElement);
        assertTrue(textFormattingWebDriver.findElement(locator) instanceof TextFormattingWebElement);
    }

    @Test
    void testFindElements()
    {
        By locator = By.id("//div/a");
        WebElement webElement = mock(WebElement.class);
        when(wrappedDriver.findElements(locator)).thenReturn(List.of(webElement));
        assertEquals(webElement,
                ((TextFormattingWebElement) textFormattingWebDriver.findElements(locator).get(0)).getWrappedElement());
    }
}
