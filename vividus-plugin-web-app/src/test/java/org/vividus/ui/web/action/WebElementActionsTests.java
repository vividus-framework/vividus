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

package org.vividus.ui.web.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class WebElementActionsTests
{
    private static final String PROPERTY_NAME = "someName";
    private static final String SCRIPT_PROPERTY_VALUE_CONTENT_AFTER =
            "return window.getComputedStyle(arguments[0],':after').getPropertyValue('content')";
    private static final String SCRIPT_PROPERTY_VALUE_CONTENT_BEFORE =
            "return window.getComputedStyle(arguments[0],':before').getPropertyValue('content')";
    private static final String CONTENT_FOUND_CORRECT = "Content found correct";
    private static final String QUOTE = "'";
    private static final String TEXT = "text";

    @Mock private WebJavascriptActions javascriptActions;
    @Mock private WebElement webElement;
    @InjectMocks private WebElementActions webElementActions;

    @Test
    void testGetBeforePseudoElementContentFromElement()
    {
        when(javascriptActions.executeScript(SCRIPT_PROPERTY_VALUE_CONTENT_BEFORE, webElement)).thenReturn(
                QUOTE + TEXT + QUOTE);
        var contentActual = webElementActions.getPseudoElementContent(webElement);
        assertEquals(TEXT, contentActual, CONTENT_FOUND_CORRECT);
    }

    @Test
    void testGetAfterPseudoElementContentFromElement()
    {
        when(javascriptActions.executeScript(SCRIPT_PROPERTY_VALUE_CONTENT_BEFORE, webElement)).thenReturn(null);
        when(javascriptActions.executeScript(SCRIPT_PROPERTY_VALUE_CONTENT_AFTER, webElement)).thenReturn(
                QUOTE + TEXT + QUOTE);
        var contentActual = webElementActions.getPseudoElementContent(webElement);
        assertEquals(TEXT, contentActual, CONTENT_FOUND_CORRECT);
    }

    @Test
    void testGetNullPseudoElementContentFromElement()
    {
        when(javascriptActions.executeScript(SCRIPT_PROPERTY_VALUE_CONTENT_BEFORE, webElement)).thenReturn(null);
        when(javascriptActions.executeScript(SCRIPT_PROPERTY_VALUE_CONTENT_AFTER, webElement)).thenReturn(null);
        var contentActual = webElementActions.getPseudoElementContent(webElement);
        assertEquals("", contentActual, CONTENT_FOUND_CORRECT);
    }

    @ParameterizedTest
    @CsvSource({"''", "none"})
    void testGetEmptyOrNonePseudoElementContentFromElement(String contentValue)
    {
        when(javascriptActions.executeScript(SCRIPT_PROPERTY_VALUE_CONTENT_BEFORE, webElement))
                .thenReturn(contentValue);
        var contentActual = webElementActions.getPseudoElementContent(webElement);
        assertEquals("", contentActual, CONTENT_FOUND_CORRECT);
    }

    @Test
    void testGetCssValue()
    {
        when(webElement.getCssValue(PROPERTY_NAME)).thenReturn("some 'value' with \"quotes\"");
        assertEquals("some value with quotes", webElementActions.getCssValue(webElement, PROPERTY_NAME));
    }

    @Test
    void testGetAllPseudoElementsContent()
    {
        var script = "var nodeList = document.querySelectorAll('*');var i;var contentList = [];"
                + "for (i = 0; i < nodeList.length; i++){"
                + "var valueBefore = window.getComputedStyle(nodeList[i],':before').getPropertyValue('content');"
                + "var valueAfter = window.getComputedStyle(nodeList[i],':after').getPropertyValue('content');"
                + "if (valueBefore != '' && valueBefore != undefined)"
                + "{contentList[contentList.length] = valueBefore;}"
                + "if (valueAfter != '' && valueAfter != undefined)" + "{contentList[contentList.length] = valueAfter;}"
                + "}return contentList;";
        webElementActions.getAllPseudoElementsContent();
        verify(javascriptActions).executeScript(script);
    }

    @Test
    void testGetElementText()
    {
        when(javascriptActions.getElementText(webElement)).thenReturn(TEXT);
        assertEquals(TEXT, webElementActions.getElementText(webElement));
    }

    @Test
    void testGetElementTextSelenium()
    {
        when(webElement.getText()).thenReturn(TEXT);
        assertEquals(TEXT, webElementActions.getElementText(webElement));
    }

    @Test
    void testGetElementTextJS()
    {
        when(webElement.getText()).thenReturn("");
        when(javascriptActions.getElementText(webElement)).thenReturn(TEXT);
        assertEquals(TEXT, webElementActions.getElementText(webElement));
    }

    @Test
    void testGetNullElementText()
    {
        webElementActions.getElementText(null);
        verify(javascriptActions, never()).getElementText(webElement);
    }

    @Test
    void testGetElementNullText()
    {
        webElementActions.getElementText(webElement);
        verify(javascriptActions).getElementText(webElement);
    }

    @Test
    void testGetPageText()
    {
        when(javascriptActions.getPageText()).thenReturn(TEXT);
        assertEquals(TEXT, webElementActions.getPageText());
    }

    @Test
    void shouldCheckIsElementVisible()
    {
        when(webElement.isDisplayed()).thenReturn(true);

        assertTrue(webElementActions.isElementVisible(webElement));

        verifyNoInteractions(javascriptActions);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldCheckIsElementVisibleWithScrolling(boolean visibilityAfterScroll)
    {
        when(webElement.isDisplayed()).thenReturn(false).thenReturn(visibilityAfterScroll);

        assertEquals(visibilityAfterScroll, webElementActions.isElementVisible(webElement));

        verify(javascriptActions).scrollIntoView(webElement, true);
        verifyNoMoreInteractions(javascriptActions);
    }
}
