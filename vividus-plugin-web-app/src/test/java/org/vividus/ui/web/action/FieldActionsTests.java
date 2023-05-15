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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static java.util.Map.entry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.support.ui.Select;
import org.vividus.selenium.KeysManager;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.util.FormatUtils;

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
    void testClearFieldUsingKeyboardNull()
    {
        fieldActions.clearFieldUsingKeyboard(null);
    }

    @Test
    void testAddTextSafariOrIExploreContenteditableRichText()
    {
        when(webDriverManager.isBrowserAnyOf(Browser.SAFARI, Browser.IE)).thenReturn(true);
        when(webElement.getAttribute(CONTENTEDITABLE)).thenReturn(TRUE);
        when(webElement.findElements(RICH_TEXT_EDITOR_LOCATOR)).thenReturn(List.of(webElement));
        fieldActions.addText(webElement, TEXT);
        verifyRichTextNotEditable();
    }

    @Test
    void testAddTextSafariOrIExploreNotContextEditableNotRichText()
    {
        when(webDriverManager.isBrowserAnyOf(Browser.SAFARI, Browser.IE)).thenReturn(true);
        fieldActions.addText(webElement, TEXT);
        var inOrder = verifyWebElementInOrderInvocation();
        inOrder.verify(webElement).sendKeys(TEXT);
        verifyNoInteractions(javascriptActions);
    }

    @Test
    void testAddTextSafariOrIExploreRichTextNotEditable()
    {
        when(webDriverManager.isBrowserAnyOf(Browser.SAFARI, Browser.IE)).thenReturn(true);
        when(webElement.findElements(RICH_TEXT_EDITOR_LOCATOR)).thenReturn(List.of(webElement));
        fieldActions.addText(webElement, TEXT);
        var inOrder = verifyWebElementInOrderInvocation();
        inOrder.verify(webElement).sendKeys(TEXT);
        verifyNoInteractions(javascriptActions);
    }

    @Test
    void testAddTextNotSafari()
    {
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.SAFARI)).thenReturn(false);
        fieldActions.addText(webElement, TEXT);
        verifyNoInteractions(javascriptActions);
        verify(webElement).sendKeys(TEXT);
        verifyNoMoreInteractions(webElement);
    }

    @Test
    void testAddTextElementIsNull()
    {
        var normalizedText = FormatUtils.normalizeLineEndings(TEXT);
        fieldActions.addText(null, TEXT);
        verify(webElement, never()).sendKeys(normalizedText);
    }

    @ParameterizedTest
    @ValueSource(strings = { TRUE, "false" })
    void testIsElementContenteditable(String result)
    {
        when(webElement.getAttribute(CONTENTEDITABLE)).thenReturn(result);
        assertEquals(Boolean.valueOf(result), fieldActions.isElementContenteditable(webElement));
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

    private void verifyRichTextNotEditable()
    {
        verify(javascriptActions).executeScript(
                "var text=arguments[0].innerHTML;arguments[0].innerHTML = text+arguments[1];", webElement, TEXT);
        var ordered = verifyWebElementInOrderInvocation();
        ordered.verify(webElement).getAttribute(CONTENTEDITABLE);
        verifyNoMoreInteractions(webElement);
    }

    private InOrder verifyWebElementInOrderInvocation()
    {
        var ordered = inOrder(webElement);
        ordered.verify(webElement).findElements(RICH_TEXT_EDITOR_LOCATOR);
        return ordered;
    }
}
