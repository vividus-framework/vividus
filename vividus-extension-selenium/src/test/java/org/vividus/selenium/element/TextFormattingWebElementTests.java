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

package org.vividus.selenium.element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class TextFormattingWebElementTests
{
    private static final String ELEMENT_INSTANCE_SHOULD_BE_OF_TYPE_TEXT_FORMATTING_WEB_ELEMENT = "Element instance "
            + "should be of type TextFormattingWebElement";
    private static final String XPATH = "xpath";
    private static final String NAME = "name";
    private static final String TEXT = "text";
    private static final By LOCATOR = By.xpath(XPATH);

    @Mock
    private WebElement webElement;

    @InjectMocks
    private TextFormattingWebElement textFormattingWebElement;

    @Test
    void testGetText()
    {
        when(webElement.getText()).thenReturn(TEXT);
        assertEquals(TEXT, textFormattingWebElement.getText());
    }

    @Test
    void testGetTextNull()
    {
        when(webElement.getText()).thenReturn(null);
        assertNull(textFormattingWebElement.getText());
    }

    @SuppressWarnings("checkstyle:avoidescapedunicodecharacters")
    @Test
    void testGetAttribute()
    {
        when(webElement.getAttribute(NAME)).thenReturn("att\u00A0");
        assertEquals("att", textFormattingWebElement.getAttribute(NAME));
    }

    @Test
    void testGetAttributeNull()
    {
        when(webElement.getAttribute(NAME)).thenReturn(null);
        assertNull(textFormattingWebElement.getAttribute(NAME));
    }

    @Test
    void testFindElements()
    {
        when(webElement.getText()).thenReturn(TEXT);
        List<WebElement> list = List.of(webElement);
        when(webElement.findElements(LOCATOR)).thenReturn(list);
        List<WebElement> actualList = textFormattingWebElement.findElements(LOCATOR);
        TextFormattingWebElement element = (TextFormattingWebElement) actualList.get(0);
        verify(webElement).findElements(LOCATOR);
        assertEquals(TEXT, element.getText());
    }

    @Test
    void testFindElement()
    {
        assertTrue(textFormattingWebElement.findElement(LOCATOR) instanceof TextFormattingWebElement,
                ELEMENT_INSTANCE_SHOULD_BE_OF_TYPE_TEXT_FORMATTING_WEB_ELEMENT);
        verify(webElement).findElement(LOCATOR);
    }
}
