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

package org.vividus.ui.web.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.util.FormatUtil;

@ExtendWith(MockitoExtension.class)
class WebElementActionsTests
{
    private static final By RICH_TEXT_EDITOR_LOCATOR = By
            .xpath("//preceding-sibling::head[descendant::title[contains(text(),'Rich Text')]]");
    private static final String TRUE = "true";
    private static final String PROPERTY_NAME = "someName";
    private static final String CONTENTEDITABLE = "contenteditable";
    private static final String SCRIPT_PROPERTY_VALUE_CONTENT_AFTER =
            "return window.getComputedStyle(arguments[0],':after').getPropertyValue('content')";
    private static final String SCRIPT_PROPERTY_VALUE_CONTENT_BEFORE =
            "return window.getComputedStyle(arguments[0],':before').getPropertyValue('content')";
    private static final int ELEMENT_Y_LOCATION = 100;
    private static final String SCRIPT_WINDOW_SCROLL = "return ((window.scrollY <= " + ELEMENT_Y_LOCATION
            + ") && (" + ELEMENT_Y_LOCATION + "<= (window.scrollY + window.innerHeight)))";
    private static final String CONTENT_FOUND_CORRECT = "Content found correct";
    private static final String QUOTE = "'";
    private static final String TEXT = "text";
    private static final String GET_ELEMENT_VALUE_JS = "return arguments[0].value;";

    @Mock
    private IJavascriptActions javascriptActions;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private WebElement webElement;

    @Mock
    private RemoteWebDriver webDriver;

    @Mock
    private Point point;

    @Mock
    private IWebDriverManager webDriverManager;

    @InjectMocks
    private WebElementActions webElementActions;

    @Test
    void testGetBeforePseudoElementContentFromElement()
    {
        when(javascriptActions.executeScript(SCRIPT_PROPERTY_VALUE_CONTENT_BEFORE, webElement)).thenReturn(
                QUOTE + TEXT + QUOTE);
        String contentActual = webElementActions.getPseudoElementContent(webElement);
        assertEquals(TEXT, contentActual, CONTENT_FOUND_CORRECT);
    }

    @Test
    void testGetAfterPseudoElementContentFromElement()
    {
        when(javascriptActions.executeScript(SCRIPT_PROPERTY_VALUE_CONTENT_BEFORE, webElement)).thenReturn(null);
        when(javascriptActions.executeScript(SCRIPT_PROPERTY_VALUE_CONTENT_AFTER, webElement)).thenReturn(
                QUOTE + TEXT + QUOTE);
        String contentActual = webElementActions.getPseudoElementContent(webElement);
        assertEquals(TEXT, contentActual, CONTENT_FOUND_CORRECT);
    }

    @Test
    void testGetNullPseudoElementContentFromElement()
    {
        when(javascriptActions.executeScript(SCRIPT_PROPERTY_VALUE_CONTENT_BEFORE, webElement)).thenReturn(null);
        when(javascriptActions.executeScript(SCRIPT_PROPERTY_VALUE_CONTENT_AFTER, webElement)).thenReturn(null);
        String contentActual = webElementActions.getPseudoElementContent(webElement);
        assertEquals("", contentActual, CONTENT_FOUND_CORRECT);
    }

    @ParameterizedTest
    @CsvSource({"''", "none"})
    void testGetEmptyOrNonePseudoElementContentFromElement(String contentValue)
    {
        when(javascriptActions.executeScript(SCRIPT_PROPERTY_VALUE_CONTENT_BEFORE, webElement))
                .thenReturn(contentValue);
        String contentActual = webElementActions.getPseudoElementContent(webElement);
        assertEquals("", contentActual, CONTENT_FOUND_CORRECT);
    }

    @Test
    void testGetCssValue()
    {
        when(webElement.getCssValue(PROPERTY_NAME)).thenReturn("some 'value' with \"quotes\"");
        assertEquals("some value with quotes", webElementActions.getCssValue(webElement, PROPERTY_NAME));
    }

    @Test
    void testGetCssValueOfNullElement()
    {
        assertNull(webElementActions.getCssValue(null, PROPERTY_NAME));
    }

    @Test
    void testTypeTextNotSafari()
    {
        when(webDriverManager.isTypeAnyOf(WebDriverType.SAFARI)).thenReturn(false);
        webElementActions.typeText(webElement, TEXT);
        verify(webElement).clear();
        verify(webElement).sendKeys(TEXT);
    }

    @Test
    void testTypeTextIExploreRequireWindowFocusFalse()
    {
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.IEXPLORE)).thenReturn(true);
        mockRequireWindowFocusOption(false);
        webElementActions.typeText(webElement, TEXT);
        InOrder inOrder = inOrder(webElement);
        inOrder.verify(webElement).clear();
        inOrder.verify(webElement).sendKeys(TEXT);
    }

    @Test
    void testTypeTextIExploreRequireWindowFocusTrueWithoutReentering()
    {
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.IEXPLORE)).thenReturn(true);
        mockRequireWindowFocusOption(true);
        when(javascriptActions.executeScript(GET_ELEMENT_VALUE_JS, webElement)).thenReturn(TEXT);
        webElementActions.typeText(webElement, TEXT);
        InOrder inOrder = inOrder(webElement);
        inOrder.verify(webElement).clear();
        inOrder.verify(webElement).sendKeys(TEXT);
    }

    @Test
    void testTypeTextIExploreRequireWindowFocusTrueWithReentering()
    {
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.IEXPLORE)).thenReturn(true);
        mockRequireWindowFocusOption(true);
        when(javascriptActions.executeScript(GET_ELEMENT_VALUE_JS, webElement)).thenReturn(StringUtils.EMPTY, TEXT);
        webElementActions.typeText(webElement, TEXT);
        verify(webElement, times(2)).clear();
        verify(webElement, times(2)).sendKeys(TEXT);
    }

    @Test
    void testTypeTextIExploreRequireWindowFocusTrueFieldNotFilledCorrectly()
    {
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.IEXPLORE)).thenReturn(true);
        mockRequireWindowFocusOption(true);
        when(javascriptActions.executeScript(GET_ELEMENT_VALUE_JS, webElement)).thenReturn(StringUtils.EMPTY);
        webElementActions.typeText(webElement, TEXT);
        verify(webElement, times(6)).clear();
        verify(webElement, times(6)).sendKeys(TEXT);
        verify(softAssert).recordFailedAssertion("The element is not filled correctly after 6 typing attempt(s)");
    }

    @Test
    void testTypeTextIExploreRequireWindowFocusTrueFieldIsFilledCorrectlyAfter5Attempts()
    {
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.IEXPLORE)).thenReturn(true);
        mockRequireWindowFocusOption(true);
        when(javascriptActions.executeScript(GET_ELEMENT_VALUE_JS, webElement)).thenReturn(StringUtils.EMPTY,
                StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, TEXT);
        webElementActions.typeText(webElement, TEXT);
        verify(webElement, times(6)).clear();
        verify(webElement, times(6)).sendKeys(TEXT);
        verifyZeroInteractions(softAssert);
    }

    @Test
    void testTypeTextSafariContentEditableFrame()
    {
        when(webElement.getAttribute(CONTENTEDITABLE)).thenReturn(TRUE);
        when(webDriverManager.isTypeAnyOf(WebDriverType.SAFARI)).thenReturn(true);
        webElementActions.typeText(webElement, TEXT);
        verify(webElement).clear();
        verify(javascriptActions).executeScript("var element = arguments[0];element.innerHTML = arguments[1];",
                webElement, TEXT);
        verify(webElement, never()).sendKeys(TEXT);
    }

    @Test
    void testTypeTextSafariSimpleFrame()
    {
        when(webElement.getAttribute(CONTENTEDITABLE)).thenReturn(null);
        when(webDriverManager.isTypeAnyOf(WebDriverType.SAFARI)).thenReturn(true);
        webElementActions.typeText(webElement, TEXT);
        verify(webElement).clear();
        verify(webElement).sendKeys(TEXT);
    }

    @Test
    void testTypeTextInNullElement()
    {
        webElementActions.typeText(null, TEXT);
    }

    @Test
    void testGetAllPseudoElementsContent()
    {
        String script = "var nodeList = document.querySelectorAll('*');var i;var contentList = [];"
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
    void testIsPageVisibleAreaScrolledToElement()
    {
        when(webElement.getLocation()).thenReturn(point);
        when(point.getY()).thenReturn(ELEMENT_Y_LOCATION);
        when(javascriptActions.executeScript(SCRIPT_WINDOW_SCROLL)).thenReturn(true);
        assertTrue(webElementActions.isPageVisibleAreaScrolledToElement(webElement));
    }

    @Test
    void testIsPageVisibleAreaScrolledToElementNull()
    {
        assertFalse(webElementActions.isPageVisibleAreaScrolledToElement(null));
    }

    @Test
    void testIsPageVisibleAreaScrolledToElementFalseCondition()
    {
        when(webElement.getLocation()).thenReturn(point);
        when(point.getY()).thenReturn(ELEMENT_Y_LOCATION);
        when(javascriptActions.executeScript(SCRIPT_WINDOW_SCROLL)).thenReturn(false);
        assertFalse(webElementActions.isPageVisibleAreaScrolledToElement(webElement));
    }

    @Test
    void testAddTextSafariOrIExploreContenteditableRichText()
    {
        when(webDriverManager.isTypeAnyOf(WebDriverType.SAFARI, WebDriverType.IEXPLORE)).thenReturn(true);
        when(webElement.getAttribute(CONTENTEDITABLE)).thenReturn(TRUE);
        when(webElement.findElements(RICH_TEXT_EDITOR_LOCATOR)).thenReturn(List.of(webElement));
        webElementActions.addText(webElement, TEXT);
        verifyRichTextNotEditable();
    }

    @Test
    void testAddTextSafariOrIExploreNotContextEditableNotRichText()
    {
        when(webDriverManager.isTypeAnyOf(WebDriverType.SAFARI, WebDriverType.IEXPLORE)).thenReturn(true);
        webElementActions.addText(webElement, TEXT);
        InOrder inOrder = verifyWebElementInOrderInvocation();
        inOrder.verify(webElement).sendKeys(TEXT);
        verifyZeroInteractions(javascriptActions);
    }

    @Test
    void testAddTextSafariOrIExploreRichTextNotEditable()
    {
        when(webDriverManager.isTypeAnyOf(WebDriverType.SAFARI, WebDriverType.IEXPLORE)).thenReturn(true);
        when(webElement.findElements(RICH_TEXT_EDITOR_LOCATOR)).thenReturn(List.of(webElement));
        webElementActions.addText(webElement, TEXT);
        InOrder inOrder = verifyWebElementInOrderInvocation();
        inOrder.verify(webElement).sendKeys(TEXT);
        verifyZeroInteractions(javascriptActions);
    }

    @Test
    void testAddTextNotSafari()
    {
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.SAFARI)).thenReturn(false);
        webElementActions.addText(webElement, TEXT);
        verifyZeroInteractions(javascriptActions);
        verify(webElement).sendKeys(TEXT);
        verifyNoMoreInteractions(webElement);
    }

    @Test
    void testAddTextElementIsNull()
    {
        String normalizedText = FormatUtil.normalizeLineEndings(TEXT);
        webElementActions.addText(null, TEXT);
        verify(webElement, never()).sendKeys(normalizedText);
    }

    private void verifyRichTextNotEditable()
    {
        verify(javascriptActions).executeScript(
                "var text=arguments[0].innerHTML;arguments[0].innerHTML = text+arguments[1];", webElement, TEXT);
        InOrder inOrder = verifyWebElementInOrderInvocation();
        inOrder.verify(webElement).getAttribute(CONTENTEDITABLE);
        verifyNoMoreInteractions(webElement);
    }

    private InOrder verifyWebElementInOrderInvocation()
    {
        InOrder inOrder = inOrder(webElement);
        inOrder.verify(webElement).findElements(RICH_TEXT_EDITOR_LOCATOR);
        return inOrder;
    }

    private void mockRequireWindowFocusOption(boolean requireWindowFocus)
    {
        Map<String, Object> options = Map.of("requireWindowFocus", requireWindowFocus);
        Capabilities capabilities = mock(Capabilities.class);
        when(capabilities.getCapability("se:ieOptions")).thenReturn(options);
        when(webDriverManager.getCapabilities()).thenReturn(capabilities);
    }
}
