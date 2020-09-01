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

package org.vividus.bdd.steps.ui.web;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.State;
import org.vividus.ui.action.IExpectedConditions;
import org.vividus.ui.action.IExpectedSearchContextCondition;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.WaitResult;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.IJavascriptActions;
import org.vividus.ui.web.action.IWebWaitActions;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.ui.web.util.LocatorUtil;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class WaitStepsTests
{
    private static final String INVISIBILITY_OF = "invisibility of ";
    private static final String THERE_IS_NO_ELEMENT_PRESENT_WITH_THE_TAG = "There is no element present "
            + "with the tag '%s' and attribute '%s'='%s'";
    private static final String ATTRIBUTE_VALUE = "attributeValue";
    private static final String ATTRIBUTE_TYPE = "attributeType";
    private static final String ELEMENT_TAG = "elementTag";
    private static final String NAME = "name";
    private static final String TEXT = "text";
    private static final String XPATH = "xpath";
    private static final Duration TIMEOUT = Duration.ofSeconds(1L);
    private static final String ELEMENT_WITH_TAG = ".//elementTag[normalize-space(@attributeType)=\"attributeValue\"]";
    private static final String ALERT_TO_BE_PRESENT = "alert to be present";

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private IWebWaitActions waitActions;

    @Mock
    private IUiContext uiContext;

    @Mock
    private WebElement webElement;

    @Mock
    private WebDriver webDriver;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private IExpectedConditions<By> expectedSearchContextConditions;

    @Mock
    private IExpectedConditions<Locator> expectedSearchActionsConditions;

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private ISearchActions searchActions;

    @Mock
    private IJavascriptActions javascriptActions;

    @InjectMocks
    private WaitSteps waitSteps;

    @Test
    void testElementByIdDisappears()
    {
        By locator = By.xpath(".//*[normalize-space(@id)=\"id\"]");
        when(uiContext.getSearchContext()).thenReturn(webElement);
        IExpectedSearchContextCondition<Boolean> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedSearchContextConditions.invisibilityOfElement(locator)).thenReturn(condition);
        waitSteps.elementByIdDisappears("id");
        verify(waitActions).wait(webElement, condition);
    }

    @Test
    void testElementByNameDisappearsWithTimeout()
    {
        when(uiContext.getSearchContext()).thenReturn(webElement);
        WaitResult<Boolean> waitResult = mock(WaitResult.class);
        Locator locator = new Locator(WebLocatorType.ELEMENT_NAME, NAME);
        IExpectedSearchContextCondition<Boolean> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedSearchActionsConditions.invisibilityOfElement(locator)).thenReturn(condition);
        when(waitActions.wait(webElement, TIMEOUT, condition)).thenReturn(waitResult);
        waitSteps.waitForElementDisappearance(locator, TIMEOUT);
        verify(waitResult).isWaitPassed();
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
        when(uiContext.getSearchContext()).thenReturn(webElement);
        WaitResult<WebElement> waitResult = mock(WaitResult.class);
        Locator locator = new Locator(WebLocatorType.ELEMENT_NAME, NAME);
        IExpectedSearchContextCondition<WebElement> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedSearchActionsConditions.visibilityOfElement(locator)).thenReturn(condition);
        when(waitActions.wait(webElement, condition)).thenReturn(waitResult);
        waitSteps.waitForElementAppearance(locator);
        verify(waitResult).isWaitPassed();
    }

    @Test
    void testWaitTillElementWithTagAndAttributeAppears()
    {
        when(uiContext.getSearchContext()).thenReturn(webElement);
        By locator = By.xpath(ELEMENT_WITH_TAG);
        WaitResult<List<WebElement>> waitResult = mock(WaitResult.class);
        IExpectedSearchContextCondition<List<WebElement>> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedSearchContextConditions.visibilityOfAllElementsLocatedBy(locator)).thenReturn(condition);
        when(waitActions.wait(webElement, condition)).thenReturn(waitResult);
        waitSteps.waitTillElementAppears(ELEMENT_TAG, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verify(waitResult).isWaitPassed();
    }

    @Test
    void testWaitTillElementContainsText()
    {
        when(uiContext.getSearchContext()).thenReturn(webElement);
        Locator locator = new Locator(WebLocatorType.ELEMENT_NAME, NAME);
        IExpectedSearchContextCondition<Boolean> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedSearchActionsConditions.textToBePresentInElementLocated(locator, TEXT)).thenReturn(condition);
        waitSteps.waitTillElementContainsText(locator, TEXT);
        verify(waitActions).wait(webElement, condition);
    }

    @Test
    void testWaitTillElementDisappears()
    {
        when(uiContext.getSearchContext()).thenReturn(webElement);
        WaitResult<Boolean> waitResult = mock(WaitResult.class);
        Locator locator = new Locator(WebLocatorType.ELEMENT_NAME, NAME);
        IExpectedSearchContextCondition<Boolean> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedSearchActionsConditions.invisibilityOfElement(locator)).thenReturn(condition);
        when(waitActions.wait(webElement, condition)).thenReturn(waitResult);
        waitSteps.waitForElementDisappearance(locator);
        verify(waitResult).isWaitPassed();
    }

    @Test
    void testWaitTillElementWithTagAndAttributeDisappears()
    {
        when(uiContext.getSearchContext()).thenReturn(webElement);
        when(webDriverProvider.get()).thenReturn(webDriver);
        List<WebElement> elements = List.of(webElement);
        Locator locator = new Locator(WebLocatorType.XPATH, ELEMENT_WITH_TAG);
        when(searchActions.findElements(webElement, locator)).thenReturn(elements);
        waitSteps.waitTillElementDisappears(ELEMENT_TAG, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verify(waitActions).wait(eq(webDriver),
                argThat(condition -> condition.toString().equals(INVISIBILITY_OF + webElement)));
        verifyNoInteractions(softAssert);
    }

    @Test
    void testWaitTillElementWithTagAndAttributeDisappearsElementIsNotPresent()
    {
        waitSteps.waitTillElementDisappears(ELEMENT_TAG, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verifyNoInteractions(waitActions);
        verify(softAssert).recordPassedAssertion(String.format(THERE_IS_NO_ELEMENT_PRESENT_WITH_THE_TAG, ELEMENT_TAG,
                        ATTRIBUTE_TYPE, ATTRIBUTE_VALUE));
    }

    @Test
    void testWaitTillElementIsSelected()
    {
        when(uiContext.getSearchContext()).thenReturn(webElement);
        Locator locator = new Locator(WebLocatorType.ELEMENT_NAME, NAME);
        IExpectedSearchContextCondition<WebElement> expectedCondition = mock(IExpectedSearchContextCondition.class);
        when(expectedSearchActionsConditions.elementToBeClickable(locator)).thenReturn(expectedCondition);
        waitSteps.waitTillElementIsSelected(locator, State.ENABLED);
        verify(waitActions).wait(webElement, expectedCondition);
    }

    @Test
    void testWaitTillElementIsStale()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        Locator locator = new Locator(WebLocatorType.ELEMENT_NAME, NAME);
        when(baseValidations.assertIfElementExists("Required element", locator))
                .thenReturn(webElement);
        waitSteps.waitTillElementIsStale(locator);
        verify(waitActions).wait(eq(webDriver), argThat(condition ->
                        condition.toString().equals(String.format("element (%s) to become stale", webElement))));
    }

    @Test
    void testWaitTillElementsAreVisible()
    {
        when(uiContext.getSearchContext()).thenReturn(webElement);
        Locator locator = new Locator(WebLocatorType.ELEMENT_NAME, NAME);
        WaitResult<WebElement> waitResult = mock(WaitResult.class);
        IExpectedSearchContextCondition<WebElement> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedSearchActionsConditions.visibilityOfElement(locator)).thenReturn(condition);
        when(waitActions.wait(webElement, condition)).thenReturn(waitResult);
        waitSteps.waitTillElementsAreVisible(NAME);
        verify(waitResult).isWaitPassed();
    }

    @Test
    void testWaitDurationWithPollingDurationTillElementDisappears()
    {
        when(uiContext.getSearchContext()).thenReturn(webElement);
        WaitResult<Boolean> waitResult = new WaitResult<>();
        waitResult.setWaitPassed(true);
        Locator locator = new Locator(WebLocatorType.XPATH, XPATH);
        IExpectedSearchContextCondition<Boolean> condition = mock(IExpectedSearchContextCondition.class);
        when(waitActions.wait(webElement, TIMEOUT, TIMEOUT, condition)).thenReturn(waitResult);
        when(expectedSearchActionsConditions.invisibilityOfElement(locator)).thenReturn(condition);
        assertTrue(waitSteps.waitDurationWithPollingDurationTillElementState(TIMEOUT, TIMEOUT, locator,
                State.NOT_VISIBLE));
    }

    @Test
    void testWaitTillFrameAppears()
    {
        when(uiContext.getSearchContext(WebDriver.class)).thenReturn(webDriver);
        WaitResult<List<WebElement>> waitResult = mock(WaitResult.class);
        IExpectedSearchContextCondition<List<WebElement>> condition = mock(IExpectedSearchContextCondition.class);
        By locator = LocatorUtil
                .getXPathLocator("*[(local-name()='frame' or local-name()='iframe') and @*=\"name\"]");
        when(expectedSearchContextConditions.visibilityOfAllElementsLocatedBy(locator)).thenReturn(condition);
        when(waitActions.wait(webDriver, condition)).thenReturn(waitResult);
        waitSteps.waitTillFrameAppears(NAME);
        verify(waitResult).isWaitPassed();
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
        WaitResult<Boolean> waitResult = new WaitResult<>();
        waitResult.setWaitPassed(true);
        when(waitActions.wait(eq(webDriver), eq(TIMEOUT),
                (Function<WebDriver, Boolean>) argThat(condition ->
                    condition.toString().equals("Waiting presence of any element with text: " + TEXT))))
            .thenReturn(waitResult);
        assertTrue(waitSteps.waitTillElementWithTextAppearsPageRefresh(TEXT, TIMEOUT));
    }

    @Test
    void testWaitTillElementWithTextDisappearsPageRefresh()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        WaitResult<Boolean> waitResult = new WaitResult<>();
        waitResult.setWaitPassed(true);
        when(waitActions.wait(eq(webDriver), eq(TIMEOUT),
                (Function<WebDriver, Boolean>) argThat(condition ->
                    condition.toString().equals("Waiting disappearance of all elements with text: " + TEXT))))
            .thenReturn(waitResult);
        assertTrue(waitSteps.waitTillElementWithTextDisappearsPageRefresh(TEXT, TIMEOUT));
    }

    @Test
    void testWaitForElementPresence()
    {
        By by = By.xpath(XPATH);
        IExpectedSearchContextCondition<List<WebElement>> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedSearchContextConditions.presenceOfAllElementsLocatedBy(by)).thenReturn(condition);
        WaitResult<List<WebElement>> result = new WaitResult<>();
        when(waitActions.wait(webElement, condition)).thenReturn(result);
        result.setWaitPassed(true);
        assertTrue(waitSteps.waitForElementPresence(webElement, by));
    }

    @Test
    void testDoesElementExistsForTimePeriod()
    {
        when(uiContext.getSearchContext()).thenReturn(webElement);
        By xpath =  By.xpath(XPATH);
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

    @Test
    void testWaitForElementDisappearanceBy()
    {
        By by = By.xpath(XPATH);
        IExpectedSearchContextCondition<Boolean> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedSearchContextConditions.invisibilityOfElement(by)).thenReturn(condition);
        WaitResult<Boolean> waitResult = new WaitResult<>();
        when(waitActions.wait(webElement, condition)).thenReturn(waitResult);
        waitResult.setWaitPassed(true);
        assertTrue(waitSteps.waitForElementDisappearance(webElement, by));
    }

    @Test
    void testWaitForElementDisappearanceByWithDuration()
    {
        By by = By.xpath(XPATH);
        IExpectedSearchContextCondition<Boolean> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedSearchContextConditions.invisibilityOfElement(by)).thenReturn(condition);
        WaitResult<Boolean> waitResult = new WaitResult<>();
        when(waitActions.wait(webElement, TIMEOUT, condition)).thenReturn(waitResult);
        waitResult.setWaitPassed(true);
        assertTrue(waitSteps.waitForElementDisappearance(webElement, by, TIMEOUT));
    }

    @Test
    void testWaitTillFrameAppearsAndSwitchToIt()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        waitSteps.waitTillFrameAppearsAndSwitchToIt(NAME);
        verify(waitActions).wait(eq(webDriver),
                argThat(condition -> condition.toString().equals("frame to be available: " + NAME)));
    }

    @Test
    void shouldWaitForAScroll()
    {
        waitSteps.waitForScroll();
        verify(javascriptActions).waitUntilScrollFinished();
    }
}
