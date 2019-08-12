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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.IExpectedConditions;
import org.vividus.ui.web.action.IExpectedSearchContextCondition;
import org.vividus.ui.web.action.ISearchActions;
import org.vividus.ui.web.action.IWaitActions;
import org.vividus.ui.web.action.WaitResult;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.util.LocatorUtil;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class WaitStepsTests
{
    private static final String THERE_IS_NO_ELEMENT_PRESENT_WITH_THE_TAG = "There is no element present "
            + "with the tag '%s' and attribute '%s'='%s'";
    private static final String THERE_IS_NO_ELEMENT_PRESENT_WITH_THE_NAME =
            "There is no element present with the name '%s'";
    private static final String THERE_IS_NO_ELEMENT_PRESENT_BY_XPATH = "There is no element present by xpath '%s'";
    private static final String THERE_IS_NO_ELEMENT_PRESENT_WITH_THE_TEXT =
            "There is no element present with the text '%s'";
    private static final String ATTRIBUTE_VALUE = "attributeValue";
    private static final String ATTRIBUTE_TYPE = "attributeType";
    private static final String ELEMENT_TAG = "elementTag";
    private static final String NAME = "name";
    private static final String TEXT = "text";
    private static final String CONTAINS_STYLE_WIDTH_100 = "//*[contains(@style, 'width: 100%')]";
    private static final String XPATH = "xpath";
    private static final Duration TIMEOUT = Duration.ofSeconds(1L);
    private static final String ELEMENT_WITH_TAG = ".//elementTag[normalize-space(@attributeType)=\"attributeValue\"]";
    private static final String ALERT_TO_BE_PRESENT = "alert to be present";

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private IWaitActions waitActions;

    @Mock
    private IWebUiContext webUiContext;

    @Mock
    private WebElement webElement;

    @Mock
    private WebDriver webDriver;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private IExpectedConditions<By> expectedSearchContextConditions;

    @Mock
    private IExpectedConditions<SearchAttributes> expectedSearchActionsConditions;

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private ISearchActions searchActions;

    @InjectMocks
    private WaitSteps waitSteps;

    @Test
    void testElementByIdDisappears()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        waitSteps.elementByIdDisappears("id");
        By locator = By.xpath(".//*[normalize-space(@id)='id']");
        verify(waitActions).wait(webElement,
                State.NOT_VISIBLE.getExpectedCondition(expectedSearchContextConditions, locator));
    }

    @Test
    void testElementByNameDisappearsWithTimeout()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        long seconds = 10;
        WaitResult<Boolean> result = mock(WaitResult.class);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, NAME);
        when(waitActions.wait(webElement, Duration.ofSeconds(seconds),
                expectedSearchActionsConditions.invisibilityOfElement(attributes))).thenReturn(result);
        waitSteps.elementDisappears(NAME, (int) seconds);
        verify(waitActions).wait(webElement, Duration.ofSeconds(seconds),
                expectedSearchActionsConditions.invisibilityOfElement(attributes));
    }

    @Test
    void testWaitTillAlertAppears()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        waitSteps.waitTillAlertAppears();
        verify(waitActions).wait(eq(webDriver), argThat(e -> ALERT_TO_BE_PRESENT.equals(e.toString())));
    }

    @Test
    void testWaitTillAlertDisappears()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        waitSteps.waitTillAlertDisappears();
        verify(waitActions).wait(eq(webDriver),
                argThat(e -> "condition to not be valid: alert to be present".equals(e.toString())));
    }

    @Test
    void testWaitTillElementAppears()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, NAME);
        when(waitActions.wait(webElement, expectedSearchActionsConditions.visibilityOfElement(attributes)))
                .thenReturn(new WaitResult<>());
        waitSteps.waitTillElementAppears(NAME);
    }

    @Test
    void testWaitTillElementWithTagAndAttributeAppears()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        By locator = By.xpath(ELEMENT_WITH_TAG);
        when(waitActions.wait(webElement, expectedSearchContextConditions.visibilityOfAllElementsLocatedBy(locator)))
                .thenReturn(new WaitResult<>());
        waitSteps.waitTillElementAppears(ELEMENT_TAG, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
    }

    @Test
    void testWaitTillElementContainsText()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, NAME);
        waitSteps.waitTillElementContainsText(NAME, TEXT);
        verify(waitActions).wait(webElement,
                expectedSearchActionsConditions.textToBePresentInElementLocated(attributes, TEXT));
    }

    @Test
    void testWaitTillElementDisappears()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        when(webDriverProvider.get()).thenReturn(webDriver);
        List<WebElement> elements = List.of(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, NAME);
        when(searchActions.findElements(webElement, attributes)).thenReturn(elements);
        waitSteps.waitTillElementDisappears(NAME);
        verify(waitActions).wait(eq(webDriver), any(ExpectedCondition.class));
        verifyZeroInteractions(softAssert);
    }

    @Test
    void testWaitTillElementDisappearsElementIsNotPresent()
    {
        waitSteps.waitTillElementDisappears(NAME);
        verifyZeroInteractions(waitActions);
        verify(softAssert).recordPassedAssertion(
                String.format(THERE_IS_NO_ELEMENT_PRESENT_WITH_THE_NAME, NAME));
    }

    @Test
    void testWaitTillElementWithTagAndAttributeDisappears()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        when(webDriverProvider.get()).thenReturn(webDriver);
        List<WebElement> elements = List.of(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.XPATH, ELEMENT_WITH_TAG);
        when(searchActions.findElements(webElement, attributes))
                .thenReturn(elements);
        waitSteps.waitTillElementDisappears(ELEMENT_TAG, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verify(waitActions).wait(eq(webDriver), any(ExpectedCondition.class));
        verify(softAssert, never()).recordPassedAssertion(
                String.format(THERE_IS_NO_ELEMENT_PRESENT_WITH_THE_TAG, ELEMENT_TAG,
                        ATTRIBUTE_TYPE, ATTRIBUTE_VALUE));
    }

    @Test
    void testWaitTillElementWithTagAndAttributeDisappearsElementIsNotPresent()
    {
        waitSteps.waitTillElementDisappears(ELEMENT_TAG, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verify(waitActions, never()).wait(eq(webDriver), any(ExpectedCondition.class));
        verify(softAssert).recordPassedAssertion(
                String.format(THERE_IS_NO_ELEMENT_PRESENT_WITH_THE_TAG, ELEMENT_TAG,
                        ATTRIBUTE_TYPE, ATTRIBUTE_VALUE));
    }

    @Test
    void testWaitTillElementIsSelected()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, NAME);
        waitSteps.waitTillElementIsSelected(NAME, State.ENABLED);
        verify(waitActions).wait(webElement,
                State.ENABLED.getExpectedCondition(expectedSearchActionsConditions, attributes));
    }

    @Test
    void testWaitTillElementIsStale()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, NAME);
        when(baseValidations.assertIfElementExists("Element with the name: " + NAME, attributes))
                .thenReturn(webElement);
        waitSteps.waitTillElementIsStale(NAME);
        verify(waitActions).wait(eq(webDriver), any(ExpectedCondition.class));
    }

    @Test
    void testWaitTillElementsAreVisible()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, NAME);
        when(waitActions.wait(webElement, expectedSearchActionsConditions.visibilityOfElement(attributes)))
                .thenReturn(new WaitResult<>());
        waitSteps.waitTillElementsAreVisible(NAME);
    }

    @Test
    void testWaitTillElementWithTextAppears()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.CASE_SENSITIVE_TEXT, TEXT);
        when(waitActions.wait(webElement, expectedSearchActionsConditions.visibilityOfElement(attributes)))
                .thenReturn(new WaitResult<>());
        waitSteps.waitTillElementWithTextAppears(TEXT);
    }

    @Test
    void testWaitTillElementWithTextDisappears()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        when(webDriverProvider.get()).thenReturn(webDriver);
        List<WebElement> elements = List.of(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.CASE_SENSITIVE_TEXT, TEXT);
        when(searchActions.findElements(webElement, attributes)).thenReturn(elements);
        waitSteps.waitTillElementWithTextDisappears(TEXT);
        verify(waitActions).wait(eq(webDriver), any(ExpectedCondition.class));
        verifyZeroInteractions(softAssert);
    }

    @Test
    void testWaitTillElementWithTextDisappearsElementIsNotPresent()
    {
        waitSteps.waitTillElementWithTextDisappears(TEXT);
        verifyZeroInteractions(waitActions);
        verify(softAssert).recordPassedAssertion(String.format(THERE_IS_NO_ELEMENT_PRESENT_WITH_THE_TEXT, TEXT));
    }

    @Test
    void testWaitTillElementWithXpathAppears()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        By by = By.xpath(".//xpath");
        when(waitActions.wait(webElement, expectedSearchContextConditions.visibilityOfAllElementsLocatedBy(by)))
                .thenReturn(new WaitResult<>());
        waitSteps.waitTillElementWithXpathAppears(XPATH);
    }

    @Test
    void testWaitTillElementWithXpathAppears2()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        By by = By.xpath("//*[contains(normalize-space(@style), 'width: 100%')]");
        when(waitActions.wait(webElement, expectedSearchContextConditions.visibilityOfAllElementsLocatedBy(by)))
                .thenReturn(new WaitResult<>());
        waitSteps.waitTillElementWithXpathAppears(CONTAINS_STYLE_WIDTH_100);
    }

    @Test
    void testWaitTillElementWithXpathDisappears()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        when(webDriverProvider.get()).thenReturn(webDriver);
        List<WebElement> elements = List.of(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH);
        when(searchActions.findElements(webElement, attributes)).thenReturn(elements);
        waitSteps.waitTillElementWithXpathDisappeares(XPATH);
        verify(waitActions).wait(eq(webDriver), any(ExpectedCondition.class));
        verifyZeroInteractions(softAssert);
    }

    @Test
    void testWaitTillElementWithXpathDisappearsElementIsNotPresent()
    {
        waitSteps.waitTillElementWithXpathDisappeares(XPATH);
        verifyZeroInteractions(waitActions);
        verify(softAssert).recordPassedAssertion(String.format(THERE_IS_NO_ELEMENT_PRESENT_BY_XPATH, XPATH));
    }

    @Test
    void testWaitTillElementWithXpathDisappears2()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        when(webDriverProvider.get()).thenReturn(webDriver);
        List<WebElement> elements = List.of(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.XPATH, CONTAINS_STYLE_WIDTH_100);
        when(searchActions.findElements(webElement, attributes)).thenReturn(elements);
        waitSteps.waitTillElementWithXpathDisappeares(CONTAINS_STYLE_WIDTH_100);
        verify(waitActions).wait(eq(webDriver), any(ExpectedCondition.class));
        verifyZeroInteractions(softAssert);
    }

    @Test
    void testWaitDurationWithPollingDurationTillElementAppears()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH);
        when(waitActions.wait(webElement, TIMEOUT, TIMEOUT, expectedSearchActionsConditions
                .visibilityOfAllElementsLocatedBy(attributes))).thenReturn(new WaitResult<>());
        waitSteps.waitDurationWithPollingDurationTillElementAppears(TIMEOUT, TIMEOUT, attributes);
    }

    @Test
    void testWaitTillFrameAppears()
    {
        when(webUiContext.getSearchContext(WebDriver.class)).thenReturn(webDriver);
        IExpectedSearchContextCondition<List<WebElement>> condition = mock(IExpectedSearchContextCondition.class);
        By locator = LocatorUtil.getXPathLocator("*[(local-name()='frame' or local-name()='iframe') and @*=\"name\"]");
        when(expectedSearchContextConditions.visibilityOfAllElementsLocatedBy(locator)).thenReturn(condition);
        when(waitActions.wait(webDriver, condition)).thenReturn(new WaitResult<>());
        waitSteps.waitTillFrameAppears(NAME);
    }

    @Test
    void testWaitTillPageContainsTitle()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        waitSteps.waitTillPageContainsTitle(TEXT);
        verify(waitActions).wait(eq(webDriver),
                argThat(e -> "title to contain \"text\". Current title: \"\"".equals(e.toString())));
    }

    @Test
    void testWaitTillPageHasTitle()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        waitSteps.waitTillPageHasTitle(TEXT);
        verify(waitActions).wait(eq(webDriver),
                argThat(e -> "title to be \"text\". Current title: \"\"".equals(e.toString())));
    }

    @Test
    void testWaitTillElementWithTextAppearsPageRefresh()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        WaitResult<?> waitResult = new WaitResult<>();
        waitResult.setWaitPassed(true);
        when(waitActions.wait(eq(webDriver), eq(TIMEOUT), any(Function.class))).thenReturn(waitResult);
        assertTrue(waitSteps.waitTillElementWithTextAppearsPageRefresh(TEXT, TIMEOUT));
    }

    @Test
    void testWaitTillElementWithTextDisappearsPageRefresh()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(waitActions.wait(eq(webDriver), eq(TIMEOUT), any(Function.class)))
                .thenReturn(new WaitResult<>());
        waitSteps.waitTillElementWithTextDisappearsPageRefresh(TEXT, TIMEOUT);
    }

    @Test
    void testWaitForElementPresence()
    {
        By by = By.xpath("//div");
        when(waitActions.wait(webElement, expectedSearchContextConditions.presenceOfAllElementsLocatedBy(by)))
                .thenReturn(new WaitResult<>());
        waitSteps.waitForElementPresence(webElement, by);
    }

    @Test
    void testDoesElementExistsForTimePeriod()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        By xpath = LocatorUtil.getXPathLocator(XPATH);
        IExpectedSearchContextCondition<List<WebElement>> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedSearchContextConditions.presenceOfAllElementsLocatedBy(xpath)).thenReturn(condition);
        IExpectedSearchContextCondition<Boolean> notCondition = mock(IExpectedSearchContextCondition.class);
        when(expectedSearchContextConditions.not(condition)).thenReturn(notCondition);
        WaitResult<Boolean> waitResult = new WaitResult<>();
        waitResult.setWaitPassed(true);
        when(waitActions.wait(webElement, TIMEOUT, notCondition, false)).thenReturn(waitResult);
        waitSteps.doesElementExistsForTimePeriod(XPATH, TIMEOUT.getSeconds());
        verify(softAssert).assertFalse(String.format("Element with xpath '%s' has existed during '%d' seconds",
                XPATH, TIMEOUT.getSeconds()), true);
    }

    @Test
    void testWaitAlertDoesNotAppear()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        Duration timeout = Duration.ofSeconds(10);
        WaitResult<Alert> waitResult = new WaitResult<>();
        waitResult.setWaitPassed(false);
        when(waitActions.wait(eq(webDriver), eq(timeout), eq(Duration.ofSeconds(1)),
                (Function<WebDriver, Alert>) argThat(condition -> ALERT_TO_BE_PRESENT.equals(condition.toString())),
                eq(false))).thenReturn(waitResult);
        waitSteps.waitAlertDoesNotAppear(timeout);
        verify(softAssert).assertFalse("Alert does not appear", waitResult.isWaitPassed());
    }
}
