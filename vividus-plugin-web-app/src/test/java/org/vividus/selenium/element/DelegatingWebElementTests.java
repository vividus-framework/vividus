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

package org.vividus.selenium.element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Coordinates;
import org.openqa.selenium.interactions.Locatable;

@ExtendWith(MockitoExtension.class)
class DelegatingWebElementTests
{
    @Mock
    private WebElement webElement;

    @InjectMocks
    private DelegatingWebElement delegatingWebElement;

    @Test
    void testClick()
    {
        delegatingWebElement.click();
        verify(webElement).click();
    }

    @Test
    void testSubmit()
    {
        delegatingWebElement.submit();
        verify(webElement).submit();
    }

    @Test
    void testSendKeys()
    {
        CharSequence keysToSend = "keysToSend";
        delegatingWebElement.sendKeys(keysToSend);
        verify(webElement).sendKeys(keysToSend);
    }

    @Test
    void testClear()
    {
        delegatingWebElement.clear();
        verify(webElement).clear();
    }

    @Test
    void testGetTagName()
    {
        String tagName = "tagName";
        when(webElement.getTagName()).thenReturn(tagName);
        assertEquals(tagName, delegatingWebElement.getTagName());
    }

    @Test
    void testGetAttribute()
    {
        String attribute = "attribute";
        String attributeValue = "attributeValue";
        when(webElement.getAttribute(attribute)).thenReturn(attributeValue);
        assertEquals(attributeValue, delegatingWebElement.getAttribute(attribute));
    }

    @Test
    void testIsSelected()
    {
        when(webElement.isSelected()).thenReturn(true);
        assertTrue(delegatingWebElement.isSelected());
    }

    @Test
    void testIsEnabled()
    {
        when(webElement.isEnabled()).thenReturn(true);
        assertTrue(delegatingWebElement.isEnabled());
    }

    @Test
    void testGetText()
    {
        String text = "text";
        when(webElement.getText()).thenReturn(text);
        assertEquals(text, delegatingWebElement.getText());
    }

    @Test
    void testFindElements()
    {
        By by = Mockito.mock(By.class);
        List<WebElement> list = new ArrayList<>();
        WebElement element = Mockito.mock(WebElement.class);
        list.add(element);
        when(webElement.findElements(by)).thenReturn(list);
        assertEquals(list, delegatingWebElement.findElements(by));
    }

    @Test
    void testFindElement()
    {
        By by = Mockito.mock(By.class);
        WebElement element = Mockito.mock(WebElement.class);
        when(webElement.findElement(by)).thenReturn(element);
        assertEquals(element, delegatingWebElement.findElement(by));
    }

    @Test
    void testIsDisplayed()
    {
        when(webElement.isDisplayed()).thenReturn(true);
        assertTrue(delegatingWebElement.isDisplayed());
    }

    @Test
    void testGetLocation()
    {
        Point point = Mockito.mock(Point.class);
        when(webElement.getLocation()).thenReturn(point);
        assertEquals(point, delegatingWebElement.getLocation());
    }

    @Test
    void testGetSize()
    {
        Dimension dimension = Mockito.mock(Dimension.class);
        when(webElement.getSize()).thenReturn(dimension);
        assertEquals(dimension, delegatingWebElement.getSize());
    }

    @Test
    void testGetCssValue()
    {
        String propertyName = "propertyName";
        String cssValue = "cssValue";
        when(webElement.getCssValue(propertyName)).thenReturn(cssValue);
        assertEquals(cssValue, delegatingWebElement.getCssValue(propertyName));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetScreenshotAs()
    {
        OutputType<String> target = Mockito.mock(OutputType.class);
        String output = "output";
        when(webElement.getScreenshotAs(target)).thenReturn(output);
        assertEquals(output, delegatingWebElement.getScreenshotAs(target));
    }

    @Test
    void testGetRect()
    {
        Rectangle rect = Mockito.mock(Rectangle.class);
        when(webElement.getRect()).thenReturn(rect);
        assertEquals(rect, delegatingWebElement.getRect());
    }

    @Test
    void testGetCoordinates()
    {
        WebElement locatableWebElement = Mockito.mock(WebElement.class,
                withSettings().extraInterfaces(Locatable.class));
        Coordinates coordinates = Mockito.mock(Coordinates.class);
        when(((Locatable) locatableWebElement).getCoordinates()).thenReturn(coordinates);
        DelegatingWebElement locatableDelegatingWebElement = new DelegatingWebElement(locatableWebElement);
        assertEquals(coordinates, locatableDelegatingWebElement.getCoordinates());
    }

    @Test
    void testToString()
    {
        String toStringValue = "webElementString";
        when(webElement.toString()).thenReturn(toStringValue);
        assertEquals(toStringValue, delegatingWebElement.toString());
    }

    @Test
    void testToStringOfNullWrappedElement()
    {
        assertEquals("null", new DelegatingWebElement(null).toString());
    }

    @Test
    void testEqualToWrappedElement()
    {
        assertEquals(delegatingWebElement, webElement);
    }

    @Test
    void testEqualToSelf()
    {
        assertEquals(delegatingWebElement, delegatingWebElement);
    }

    @Test
    void testNotEqualToAnyObject()
    {
        assertNotEquals(delegatingWebElement, new Object());
    }

    @Test
    void testHashCode()
    {
        assertEquals(webElement.hashCode(), delegatingWebElement.hashCode());
    }

    @Test
    void testHashCodeOfNullWrappedElement()
    {
        assertEquals(0, new DelegatingWebElement(null).hashCode());
    }
}
