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

package org.vividus.ui.web.action;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static java.util.Map.entry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

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
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.support.ui.Select;
import org.vividus.selenium.KeysManager;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;

@SuppressWarnings("PMD.UnnecessaryBooleanAssertion")
@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class FieldActionsTests
{
    private static final String INDEX = "index";
    private static final String ITEMS_WITH_THE_TEXT_TEXT_ARE_SELECTED_FROM_A_DROP_DOWN =
            "Items with the text 'text' are selected from a drop down";
    private static final String ITEM_WITH_THE_TEXT_TEXT_IS_SELECTED_FROM_A_DROP_DOWN =
            "Item with the text 'text' is selected from a drop down";
    private static final String TEXT = "text";
    private static final String SELECT = "select";
    private static final String MULTIPLE = "multiple";
    private static final By RICH_TEXT_EDITOR_LOCATOR = By
            .xpath("//preceding-sibling::head[descendant::title[contains(text(),'Rich Text')]]");
    private static final String TRUE = "true";
    private static final String CONTENTEDITABLE = "contenteditable";
    private static final String CHECK_IF_CKE_JS = "return arguments[0].ckeditorInstance !== undefined";
    private static final String GET_ELEMENT_VALUE_JS = "return arguments[0].value;";

    @Mock private IWebDriverManager webDriverManager;
    @Mock private KeysManager keysManager;
    @Mock private WebJavascriptActions javascriptActions;
    @Mock private WebElementActions webElementActions;
    @Mock private WebElement webElement;
    @Mock private ISoftAssert softAssert;
    @Mock private IWebWaitActions waitActions;
    @InjectMocks private FieldActions fieldActions;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(FieldActions.class);

    @Test
    void testSelectItemInDDLSelectPresentOptionExist()
    {
        var select = findDropDownListWithParameters(false);
        addOptionsToSelect(select, TEXT);
        fieldActions.selectItemInDropDownList(select, TEXT, false);
        verify(webElementActions).getElementText(webElement);
        verify(waitActions).waitForPageLoad();
        verify(softAssert).assertTrue(ITEM_WITH_THE_TEXT_TEXT_IS_SELECTED_FROM_A_DROP_DOWN, true);
    }

    @Test
    void testSelectItemInDDLMultiSelectNotAdditable()
    {
        var selectedElement = mock(WebElement.class);
        when(selectedElement.isSelected()).thenReturn(true);
        when(selectedElement.getAttribute(INDEX)).thenReturn(Integer.toString(1));
        var select = findDropDownListWithParameters(true);

        var options = List.of(webElement, selectedElement);
        when(select.getOptions()).thenReturn(options);
        when(webElementActions.getElementText(webElement)).thenReturn(TEXT);
        when(webElementActions.getElementText(selectedElement)).thenReturn("not" + TEXT);
        fieldActions.selectItemInDropDownList(select, TEXT, false);
        verify(webElementActions).getElementText(webElement);
        verify(waitActions).waitForPageLoad();
        verify(softAssert).assertTrue(ITEMS_WITH_THE_TEXT_TEXT_ARE_SELECTED_FROM_A_DROP_DOWN,
                true);
    }

    @Test
    void testSelectItemInDDLSelectWithWhiteSpacesBefore()
    {
        var select = findDropDownListWithParameters(false);
        addOptionsToSelect(select, " text");
        fieldActions.selectItemInDropDownList(select, TEXT, false);
        verify(webElementActions).getElementText(webElement);
        verify(waitActions).waitForPageLoad();
        verify(softAssert).assertTrue(ITEM_WITH_THE_TEXT_TEXT_IS_SELECTED_FROM_A_DROP_DOWN, true);
    }

    @Test
    void testSelectItemInDDLSelectWithWhiteSpacesBeforeAfter()
    {
        var select = findDropDownListWithParameters(false);
        addOptionsToSelect(select, " text ");
        fieldActions.selectItemInDropDownList(select, TEXT, false);
        verify(webElementActions).getElementText(webElement);
        verify(waitActions).waitForPageLoad();
        verify(softAssert).assertTrue(ITEM_WITH_THE_TEXT_TEXT_IS_SELECTED_FROM_A_DROP_DOWN, true);
    }

    @Test
    void testSelectItemInDDLSelectMultiple()
    {
        var select = findDropDownListWithParameters(true);
        addOptionsToSelect(select, TEXT);
        fieldActions.selectItemInDropDownList(select, TEXT, false);
        verify(webElementActions).getElementText(webElement);
        verify(waitActions).waitForPageLoad();
        verify(softAssert).assertTrue(ITEMS_WITH_THE_TEXT_TEXT_ARE_SELECTED_FROM_A_DROP_DOWN,
                true);
    }

    @Test
    void testSelectItemInDDLSelectPresentOptionDoesntExist()
    {
        when(webElement.getTagName()).thenReturn(SELECT);
        when(webElement.getDomAttribute(MULTIPLE)).thenReturn(Boolean.toString(false));
        var select = new Select(webElement);
        addOptionsToSelect(select, "anotherOne");
        fieldActions.selectItemInDropDownList(select, TEXT, false);
        verify(webElementActions).getElementText(webElement);
        verify(waitActions).waitForPageLoad();
        verify(softAssert).assertTrue(ITEM_WITH_THE_TEXT_TEXT_IS_SELECTED_FROM_A_DROP_DOWN,
                false);
    }

    @Test
    void testSelectItemInDDLSelectNull()
    {
        fieldActions.selectItemInDropDownList(null, TEXT, false);
        verify(webElementActions, never()).getElementText(webElement);
        verify(waitActions, never()).waitForPageLoad();
    }

    @Test
    void testSelectItemInDDLSingleSelectAdditable()
    {
        when(webElement.getTagName()).thenReturn(SELECT);
        when(webElement.getDomAttribute(MULTIPLE)).thenReturn(Boolean.toString(false));
        var select = new Select(webElement);
        fieldActions.selectItemInDropDownList(select, TEXT, true);
        verifyNoMoreInteractions(waitActions);
        verify(softAssert)
                .recordFailedAssertion("Multiple selecting is not available to single select drop down");
    }

    @ParameterizedTest
    @CsvSource({
            "CONTROL, Ctrl",
            "COMMAND, Cmd"
    })
    void testClearFieldUsingKeyboard(Keys controllingKey, String controllingKeyName)
    {
        when(keysManager.getOsIndependentControlKey()).thenReturn(entry(controllingKey, controllingKeyName));
        fieldActions.clearFieldUsingKeyboard(webElement);
        verify(webElement).sendKeys(Keys.chord(controllingKey, "a") + Keys.BACK_SPACE);
        assertThat(logger.getLoggingEvents(), is(List.of(
                info("Attempting to clear field with [{} + A, Backspace] keys sequence", controllingKeyName))));
    }

    @Test
    void testAddTextSafariOrIExploreContenteditableRichText()
    {
        when(webElement.getAttribute(CONTENTEDITABLE)).thenReturn(TRUE);
        when(webDriverManager.isBrowserAnyOf(Browser.SAFARI, Browser.IE)).thenReturn(true);
        when(webElement.findElements(RICH_TEXT_EDITOR_LOCATOR)).thenReturn(List.of(webElement));
        when(javascriptActions.executeScript(CHECK_IF_CKE_JS, webElement)).thenReturn(false);
        fieldActions.addText(webElement, TEXT);
        verify(javascriptActions).executeScript(
                "var text=arguments[0].innerHTML;arguments[0].innerHTML = text+arguments[1];", webElement, TEXT);
        verifyWebElementInOrderInvocation();
        verifyNoMoreInteractions(webElement);
    }

    @Test
    void testNotContextEditable()
    {
        fieldActions.addText(webElement, TEXT);
        verify(webElement).sendKeys(TEXT);
        verifyNoInteractions(javascriptActions);
    }

    @Test
    void testAddTextContenteditableNotSafariNotCKE()
    {
        when(webElement.getAttribute(CONTENTEDITABLE)).thenReturn(TRUE);
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.SAFARI)).thenReturn(false);
        when(javascriptActions.executeScript(CHECK_IF_CKE_JS, webElement)).thenReturn(false);
        fieldActions.addText(webElement, TEXT);
        verify(webElement).sendKeys(TEXT);
        verifyNoMoreInteractions(webElement);
    }

    @Test
    void testAddTextContenteditableCKE()
    {
        when(webElement.getAttribute(CONTENTEDITABLE)).thenReturn(TRUE);
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.SAFARI)).thenReturn(false);
        when(javascriptActions.executeScript(CHECK_IF_CKE_JS, webElement))
                .thenReturn(true);
        fieldActions.addText(webElement, TEXT);
        verify(javascriptActions).executeScript(
                "const editor = arguments[0].ckeditorInstance;"
                        + "const originalText = editor.getData();"
                        + "const lastPCloseTagIndex = originalText.lastIndexOf('</p>');"
                        + "if (lastPCloseTagIndex !== -1) {"
                        + "editor.setData( originalText.substring(0, lastPCloseTagIndex)"
                        + " + arguments[1] + originalText.substring(lastPCloseTagIndex) );"
                        + "} else {"
                        + "editor.setData(originalText + arguments[1])"
                        + "}", webElement, TEXT);
        verifyNoMoreInteractions(webElement);
    }

    @Test
    void shouldTypeTextInInteractableElement()
    {
        fieldActions.typeText(webElement, TEXT);
        verify(webElement).sendKeys(TEXT);
    }

    @Test
    void shouldRecordFailedAssertionOnAttemptToTypeTextInNotInteractableElement()
    {
        var exception = new ElementNotInteractableException("element not interactable");
        doThrow(exception).when(webElement).sendKeys(TEXT);
        fieldActions.typeText(webElement, TEXT);
        verify(softAssert).recordFailedAssertion(exception);
    }

    private Select findDropDownListWithParameters(boolean isMultiple)
    {
        when(webElement.getTagName()).thenReturn(SELECT);
        when(webElement.isEnabled()).thenReturn(true);
        when(webElement.getAttribute(INDEX)).thenReturn("0");
        when(webElement.getDomAttribute(MULTIPLE)).thenReturn(Boolean.toString(isMultiple));
        return new Select(webElement);
    }

    private void addOptionsToSelect(Select select, String selectText)
    {
        var options = List.of(webElement);
        when(select.getOptions()).thenReturn(options);
        when(webElementActions.getElementText(webElement)).thenReturn(selectText);
    }

    private InOrder verifyWebElementInOrderInvocation()
    {
        var ordered = inOrder(webElement);
        ordered.verify(webElement).findElements(RICH_TEXT_EDITOR_LOCATOR);
        return ordered;
    }

    @Test
    void testEnterTextInFieldIExploreRequireWindowFocusFalse()
    {
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.IE)).thenReturn(true);
        mockRequireWindowFocusOption(false);
        fieldActions.typeText(webElement, TEXT);
        var inOrder = inOrder(webElement);
        inOrder.verify(webElement).clear();
        inOrder.verify(webElement).sendKeys(TEXT);
    }

    @Test
    void testEnterTextInFieldIExploreRequireWindowFocusTrueWithoutReentering()
    {
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.IE)).thenReturn(true);
        mockRequireWindowFocusOption(true);
        when(javascriptActions.executeScript(GET_ELEMENT_VALUE_JS, webElement)).thenReturn(TEXT);
        fieldActions.typeText(webElement, TEXT);
        var inOrder = inOrder(webElement);
        inOrder.verify(webElement).clear();
        inOrder.verify(webElement).sendKeys(TEXT);
    }

    @Test
    void testEnterTextInFieldIExploreRequireWindowFocusTrueWithReentering()
    {
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.IE)).thenReturn(true);
        mockRequireWindowFocusOption(true);
        when(javascriptActions.executeScript(GET_ELEMENT_VALUE_JS, webElement)).thenReturn(StringUtils.EMPTY, TEXT);
        fieldActions.typeText(webElement, TEXT);
        verify(webElement, times(2)).clear();
        verify(webElement, times(2)).sendKeys(TEXT);
    }

    @Test
    void testEnterTextInFieldIExploreRequireWindowFocusTrueFieldNotFilledCorrectly()
    {
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.IE)).thenReturn(true);
        mockRequireWindowFocusOption(true);
        when(javascriptActions.executeScript(GET_ELEMENT_VALUE_JS, webElement)).thenReturn(StringUtils.EMPTY);
        fieldActions.typeText(webElement, TEXT);
        verify(webElement, times(6)).clear();
        verify(webElement, times(6)).sendKeys(TEXT);
        verify(softAssert).recordFailedAssertion("The element is not filled correctly after 6 typing attempt(s)");
    }

    @Test
    void testEnterTextInFieldIExploreRequireWindowFocusTrueFieldIsFilledCorrectlyAfter5Attempts()
    {
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.IE)).thenReturn(true);
        mockRequireWindowFocusOption(true);
        when(javascriptActions.executeScript(GET_ELEMENT_VALUE_JS, webElement)).thenReturn(StringUtils.EMPTY,
                StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, TEXT);
        fieldActions.typeText(webElement, TEXT);
        verify(webElement, times(6)).clear();
        verify(webElement, times(6)).sendKeys(TEXT);
        verifyNoInteractions(softAssert);
    }

    @Test
    void testEnterTextInContentEditableElement()
    {
        when(webElement.getAttribute(CONTENTEDITABLE)).thenReturn(TRUE);
        fieldActions.typeText(webElement, TEXT);
        verify(webElement).clear();
        verify(javascriptActions).executeScript("arguments[0].ckeditorInstance ?"
                + " arguments[0].ckeditorInstance.setData(arguments[1])"
                + " : arguments[0].innerHTML = arguments[1]", webElement, TEXT);
        verify(webElement, never()).sendKeys(TEXT);
    }

    @Test
    void shouldEnterTextInFieldWhichBecameStaleOnce()
    {
        doThrow(StaleElementReferenceException.class).doNothing().when(webElement).sendKeys(TEXT);
        fieldActions.typeText(webElement, TEXT);
        verify(webElement, times(2)).clear();
        verify(webElement, times(2)).sendKeys(TEXT);
    }

    @Test
    void shouldFailToEnterTextInFieldWhichIsAlwaysStale()
    {
        var exception1 = new StaleElementReferenceException("one");
        var exception2 = new StaleElementReferenceException("two");
        doThrow(exception1, exception2).when(webElement).sendKeys(TEXT);
        var actual = assertThrows(StaleElementReferenceException.class,
                () -> fieldActions.typeText(webElement, TEXT));
        assertEquals(exception2, actual);
        verify(webElement, times(2)).clear();
        verify(webElement, times(2)).sendKeys(TEXT);
    }

    private void mockRequireWindowFocusOption(boolean requireWindowFocus)
    {
        Map<String, Object> options = Map.of("requireWindowFocus", requireWindowFocus);
        var capabilities = mock(Capabilities.class);
        when(capabilities.getCapability("se:ieOptions")).thenReturn(options);
        when(webDriverManager.getCapabilities()).thenReturn(capabilities);
    }
}
