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

package org.vividus.bdd.steps.ui.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.web.validation.IHighlightingSoftAssert;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.IWindowsActions;
import org.vividus.ui.web.action.SearchActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.action.search.Visibility;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.context.SearchContextSetter;
import org.vividus.ui.web.util.LocatorUtil;

@ExtendWith(MockitoExtension.class)
class SetContextStepsTests
{
    private static final String EQUALS_MATCHER = "\"new title\"";
    private static final String CONTAINING_MATCHER = "a string containing \"new title\"";
    private static final String NEW_TITLE = "new title";
    private static final String TITLE = "Title";
    private static final String MATCHER_STRING = "not \"{770e3411-5e19-4831-8f36-fc76e46a2807}\"";
    private static final String AN_ELEMENT_WITH_THE_NAME_NAME = "An element with the name 'name'";
    private static final String AN_ELEMENT_WITH_THE_ATTRIBUTE_ATTRIBUTE_TYPE_ATTRIBUTE_VALUE =
            "An element with the attribute 'attributeType'='attributeValue'";
    private static final String THE_FOUND_ELEMENT_IS = "The found element is ";
    private static final String ATTRIBUTE_TYPE = "attributeType";
    private static final String ATTRIBUTE_VALUE = "attributeValue";
    private static final String XPATH_ATTRIBUTE_VALUE = LocatorUtil.getXPath(".//*[@attributeType=%s]",
            ATTRIBUTE_VALUE);
    private static final String FRAME_XPATH = ".//*[(local-name()='frame' or local-name()='iframe')"
            + " and normalize-space(@attributeType)=\"attributeValue\"]";
    private static final String IS_FOUND = " is found";
    private static final String SIGN_EQUALITY = " = ";
    private static final String AND_NUMBER = " and number ";
    private static final String FRAME_WITH_ATTRIBUTE = "Frame with attribute ";
    private static final String NAME = "name";
    private static final String APOSTROPHE = "'";
    private static final String XPATH = "someXpath";
    private static final String CURRENT_WINDOW_HANDLE = "{770e3411-5e19-4831-8f36-fc76e46a2807}";
    private static final String OTHER_WINDOW_HANDLE = "{248427e8-e67d-47ba-923f-4051f349f813}";
    private static final String WINDOW_OR_TAB_WITH_NAME = "Window or tab name is ";
    private static final String NEW_WINDOW_IS_FOUND = "New window is found";
    private static final String NEW_WINDOW = "New window '";
    private static final String NEW_WINDOW_OR_TAB_IS_FOUND = "New window or browser tab name is ";
    private static final String TEXT = "text";
    private static final String TABLE_ROW_WITH_CELL_WITH_TEXT = "Table row containing cell with the text '%s'";
    private static final String ANCESTOR_TR = "./ancestor::tr[1]";

    @Mock
    private IBaseValidations mockedBaseValidations;

    @Mock
    private IWebUiContext webUiContext;

    @Mock
    private SearchActions mockedSearchActions;

    @Mock
    private WebElement mockedWebElement;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private WebDriver mockedWebDiver;

    @Mock
    private TargetLocator mockedTargetLocator;

    @Mock
    private IHighlightingSoftAssert softAssert;

    @Mock
    private IWindowsActions windowsActions;

    @InjectMocks
    private SetContextSteps setContextSteps;

    @Test
    void testChangeContextToPage()
    {
        setContextSteps.changeContextToPage();
        verify(webUiContext).reset();
    }

    private void verifyContextSetting(WebElement element)
    {
        verify(webUiContext).reset();
        verify(webUiContext).putSearchContext(eq(element), any(SearchContextSetter.class));
    }

    @Test
    void testChangeContextToElementWithAttribute()
    {
        when(mockedBaseValidations.assertIfElementExists(AN_ELEMENT_WITH_THE_ATTRIBUTE_ATTRIBUTE_TYPE_ATTRIBUTE_VALUE,
                new SearchAttributes(ActionAttributeType.XPATH, XPATH_ATTRIBUTE_VALUE))).thenReturn(mockedWebElement);
        setContextSteps.changeContextToElementWithAttribute(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verifyContextSetting(mockedWebElement);
    }

    @Test
    void testChangeContextToStateElementWithAttribute()
    {
        when(mockedBaseValidations.assertIfElementExists(AN_ELEMENT_WITH_THE_ATTRIBUTE_ATTRIBUTE_TYPE_ATTRIBUTE_VALUE,
                new SearchAttributes(ActionAttributeType.XPATH, XPATH_ATTRIBUTE_VALUE))).thenReturn(mockedWebElement);
        when(mockedBaseValidations.assertElementState(THE_FOUND_ELEMENT_IS + State.ENABLED, State.ENABLED,
                mockedWebElement)).thenReturn(true);
        setContextSteps.changeContextToStateElementWithAttribute(State.ENABLED, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verifyContextSetting(mockedWebElement);
    }

    @Test
    void testChangeContextToNotElementWithAttribute()
    {
        setContextSteps.changeContextToStateElementWithAttribute(State.ENABLED, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verifyContextSetting(null);
    }

    @Test
    void testChangeContextToElement()
    {
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPath(XPATH));
        when(mockedBaseValidations.assertIfElementExists(
                "Element to set context", searchAttributes)).thenReturn(mockedWebElement);
        setContextSteps.changeContextToElement(searchAttributes);
        verifyContextSetting(mockedWebElement);
    }

    @Test
    void testChangeContextToStateElementWithName()
    {
        when(mockedBaseValidations
                .assertIfElementExists(AN_ELEMENT_WITH_THE_NAME_NAME,
                        new SearchAttributes(ActionAttributeType.ELEMENT_NAME, NAME)
                                .addFilter(ActionAttributeType.STATE, State.ENABLED.toString())))
                .thenReturn(mockedWebElement);
        setContextSteps.changeContextToElementWithName(State.ENABLED, NAME);
        verifyContextSetting(mockedWebElement);
    }

    @Test
    void testChangeContextToNotStateElementWithName()
    {
        setContextSteps.changeContextToElementWithName(State.ENABLED, NAME);
        verifyContextSetting(null);
    }

    @Test
    void testSwitchingToWindow()
    {
        when(webDriverProvider.get()).thenReturn(mockedWebDiver);
        when(mockedWebDiver.getWindowHandle()).thenReturn(CURRENT_WINDOW_HANDLE);
        when(windowsActions.switchToNewWindow(CURRENT_WINDOW_HANDLE)).thenReturn(OTHER_WINDOW_HANDLE);
        when(softAssert.assertThat(eq(NEW_WINDOW + OTHER_WINDOW_HANDLE + APOSTROPHE + IS_FOUND),
                eq(NEW_WINDOW_IS_FOUND), eq(OTHER_WINDOW_HANDLE), argThat(matcher -> matcher.toString()
                        .contains(MATCHER_STRING)))).thenReturn(true);
        setContextSteps.switchingToWindow();

        verify(webUiContext).reset();
    }

    @Test
    void testSwitchingToWindowNoWindow()
    {
        when(webDriverProvider.get()).thenReturn(mockedWebDiver);
        when(mockedWebDiver.getWindowHandle()).thenReturn(CURRENT_WINDOW_HANDLE);
        when(windowsActions.switchToNewWindow(CURRENT_WINDOW_HANDLE)).thenReturn(CURRENT_WINDOW_HANDLE);
        setContextSteps.switchingToWindow();

        verify(softAssert).assertThat(eq(NEW_WINDOW + CURRENT_WINDOW_HANDLE + APOSTROPHE + IS_FOUND),
                eq(NEW_WINDOW_IS_FOUND), eq(CURRENT_WINDOW_HANDLE), argThat(matcher -> matcher.toString()
                        .contains(MATCHER_STRING)));
    }

    @Test
    void testSwitchingToWindowWithPartNameNoNewPage()
    {
        when(windowsActions.switchToWindowWithMatchingTitle(argThat(matcher -> matcher.toString()
                .contains(CONTAINING_MATCHER)))).thenReturn(TITLE);
        setContextSteps.switchingToWindowPartName(NEW_TITLE);
        verify(softAssert).assertThat(eq(NEW_WINDOW_OR_TAB_IS_FOUND), eq(WINDOW_OR_TAB_WITH_NAME),
                eq(TITLE), argThat(matcher -> matcher.toString().contains(CONTAINING_MATCHER)));
        verifyNoInteractions(webUiContext);
    }

    @Test
    void testSwitchingToWindowWithTheFullName()
    {
        when(windowsActions.switchToWindowWithMatchingTitle(argThat(matcher -> matcher.toString()
                .contains(CONTAINING_MATCHER)))).thenReturn(NEW_TITLE);
        when(softAssert.assertThat(eq(NEW_WINDOW_OR_TAB_IS_FOUND), eq(WINDOW_OR_TAB_WITH_NAME),
                eq(NEW_TITLE), argThat(matcher -> matcher.toString().contains(CONTAINING_MATCHER)))).thenReturn(true);

        setContextSteps.switchingToWindowPartName(NEW_TITLE);

        verify(webUiContext).reset();
    }

    @Test
    void testSwitchingToWindowWithNameNoNewPage()
    {
        when(windowsActions.switchToWindowWithMatchingTitle(argThat(matcher -> matcher.toString()
                .equals(EQUALS_MATCHER)))).thenReturn(TITLE);

        setContextSteps.switchingToWindow(NEW_TITLE);

        verify(softAssert).assertThat(eq(NEW_WINDOW_OR_TAB_IS_FOUND), eq(WINDOW_OR_TAB_WITH_NAME),
                eq(TITLE), argThat(matcher -> matcher.toString().equals(EQUALS_MATCHER)));
        verifyNoInteractions(webUiContext);
    }

    @Test
    void testSwitchingToWindowWithName()
    {
        when(windowsActions.switchToWindowWithMatchingTitle(argThat(matcher -> matcher.toString()
                .equals(EQUALS_MATCHER)))).thenReturn(NEW_TITLE);
        when(softAssert.assertThat(eq(NEW_WINDOW_OR_TAB_IS_FOUND), eq(WINDOW_OR_TAB_WITH_NAME),
                eq(NEW_TITLE), argThat(matcher -> matcher.toString().equals(EQUALS_MATCHER)))).thenReturn(true);

        setContextSteps.switchingToWindow(NEW_TITLE);

        verify(webUiContext).reset();
    }

    @Test
    void testSwitchingToFrame()
    {
        when(webDriverProvider.get()).thenReturn(mockedWebDiver);
        when(mockedWebDiver.switchTo()).thenReturn(mockedTargetLocator);
        String attributeType = "class";
        String attributeValue = "class1";
        SetContextSteps spy = Mockito.spy(setContextSteps);
        String xPath = LocatorUtil.getXPath(
                ".//*[(local-name()='frame' or local-name()='iframe') and @" + attributeType + "=%s]", attributeValue);
        when(mockedBaseValidations.assertIfElementExists(
                String.format("Frame with the attribute '%1$s'='%2$s'", attributeType, attributeValue),
                new SearchAttributes(ActionAttributeType.XPATH, xPath))).thenReturn(mockedWebElement);
        spy.switchingToFrame(attributeType, attributeValue);
        verify(spy).switchingToDefault();
        verify(mockedTargetLocator).frame(mockedWebElement);
        verify(webUiContext, times(2)).reset();
    }

    @Test
    void testSwitchingToFrameNull()
    {
        when(webDriverProvider.get()).thenReturn(mockedWebDiver);
        when(mockedWebDiver.switchTo()).thenReturn(mockedTargetLocator);
        setContextSteps.switchingToFrame(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verify(mockedTargetLocator).defaultContent();
        verify(mockedTargetLocator, never()).frame(mockedWebElement);
    }

    @Test
    void testSwitchingToFrameByXpathIfElementExist()
    {
        when(webDriverProvider.get()).thenReturn(mockedWebDiver);
        when(mockedWebDiver.switchTo()).thenReturn(mockedTargetLocator);
        when(mockedBaseValidations.assertIfElementExists("A frame", new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPath(XPATH)))).thenReturn(mockedWebElement);
        setContextSteps.switchingToFramebyXpath(XPATH);
        verify(mockedTargetLocator).frame(mockedWebElement);
        verify(webUiContext).reset();
    }

    @Test
    void testSwitchingToFrameByXpathIfElementNotExist()
    {
        when(mockedWebDiver.switchTo()).thenReturn(mockedTargetLocator);
        setContextSteps.switchingToFramebyXpath(XPATH);
        verify(mockedWebDiver.switchTo(), never()).frame(mockedWebElement);
    }

    @Test
    void testSwitchToFrameElementFoundCorrectNumber()
    {
        when(webUiContext.getSearchContext()).thenReturn(mockedWebElement);
        when(webDriverProvider.get()).thenReturn(mockedWebDiver);
        when(mockedWebDiver.switchTo()).thenReturn(mockedTargetLocator);
        int framesNumber = 2;
        List<WebElement> mockedWebElements = List.of(mockedWebElement, mockedWebElement);
        SearchParameters searchParameters = new SearchParameters(FRAME_XPATH, Visibility.ALL);
        SearchAttributes frameSearchAttributes = new SearchAttributes(ActionAttributeType.XPATH, searchParameters);
        when(mockedSearchActions.findElements(mockedWebElement, frameSearchAttributes)).thenReturn(mockedWebElements);
        when(softAssert
                .assertThat(
                        eq("Number of frames found"), eq(FRAME_WITH_ATTRIBUTE + ATTRIBUTE_TYPE + SIGN_EQUALITY
                                + ATTRIBUTE_VALUE + AND_NUMBER + framesNumber + IS_FOUND),
                        eq(mockedWebElements), any())).thenReturn(true);
        setContextSteps.switchToFrame(framesNumber, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verify(mockedTargetLocator).defaultContent();
        verify(mockedTargetLocator).frame(mockedWebElement);
        verify(webUiContext, times(2)).reset();
    }

    @Test
    void testSwitchToFrameElementNotFoundCorrectNumber()
    {
        when(webUiContext.getSearchContext()).thenReturn(mockedWebElement);
        when(webDriverProvider.get()).thenReturn(mockedWebDiver);
        when(mockedWebDiver.switchTo()).thenReturn(mockedTargetLocator);
        setContextSteps.switchToFrame(1, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verify(mockedTargetLocator, never()).frame(mockedWebElement);
    }

    @Test
    void testSwitchToFrameElementFoundIncorrectNumber()
    {
        when(webDriverProvider.get()).thenReturn(mockedWebDiver);
        when(mockedWebDiver.switchTo()).thenReturn(mockedTargetLocator);
        int framesNumber = 1;
        List<WebElement> mockedWebElements = List.of(mockedWebElement);
        Mockito.lenient().when(mockedSearchActions.findElements(mockedWebDiver,
                new SearchAttributes(ActionAttributeType.XPATH, FRAME_XPATH))).thenReturn(mockedWebElements);
        setContextSteps.switchToFrame(framesNumber, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verify(mockedTargetLocator, never()).frame(mockedWebElement);
    }
}
