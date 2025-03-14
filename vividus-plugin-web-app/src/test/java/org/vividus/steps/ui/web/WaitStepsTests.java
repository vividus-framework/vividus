/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.steps.ui.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.TimeoutConfigurer;
import org.vividus.selenium.locator.Locator;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.State;
import org.vividus.ui.action.IExpectedConditions;
import org.vividus.ui.action.IExpectedSearchContextCondition;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.WaitResult;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.IWebWaitActions;
import org.vividus.ui.web.action.ScrollActions;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.action.search.WebLocatorType;

@SuppressWarnings({ "unchecked", "PMD.CouplingBetweenObjects" })
@ExtendWith(MockitoExtension.class)
class WaitStepsTests
{
    private static final String NAME = "name";
    private static final String TEXT = "text";
    private static final Duration TIMEOUT = Duration.ofSeconds(1L);
    private static final String ALERT_TO_BE_PRESENT = "alert to be present";
    private static final String ALERT_TO_BE_NOT_PRESENT = "alert to be not present";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IWebWaitActions waitActions;
    @Mock private IUiContext uiContext;
    @Mock private WebElement webElement;
    @Mock private WebDriver webDriver;
    @Mock private ISoftAssert softAssert;
    @Mock private IExpectedConditions<Locator> expectedSearchActionsConditions;
    @Mock private IBaseValidations baseValidations;
    @Mock private WebJavascriptActions javascriptActions;
    @Mock private TimeoutConfigurer timeoutConfigurer;
    @Mock private ScrollActions<WebElement> scrollActions;
    @Mock private ISearchActions searchActions;
    @InjectMocks private WaitSteps waitSteps;

    @Test
    void shouldElementByNameAppearsWithTimeout()
    {
        when(uiContext.getSearchContext()).thenReturn(webElement);
        Locator locator = new Locator(WebLocatorType.ELEMENT_NAME, NAME);
        IExpectedSearchContextCondition<WebElement> condition = mock();
        when(expectedSearchActionsConditions.visibilityOfElement(locator)).thenReturn(condition);
        waitSteps.waitForElementAppearance(locator, TIMEOUT);
        verify(waitActions).wait(webElement, TIMEOUT, condition);
    }

    @Test
    void shouldThrowAnExceptionInCaseOfIncorrectVisibilityUsedForAppearanceWait()
    {
        Locator locator = new Locator(WebLocatorType.ELEMENT_NAME, new SearchParameters(NAME, Visibility.ALL));
        var iae = assertThrows(IllegalArgumentException.class,
                () -> waitSteps.waitForElementAppearance(locator, TIMEOUT));
        assertEquals("The step supports locators with VISIBLE visibility settings only, but the locator is "
                + "`element name 'name' (visible or invisible)`", iae.getMessage());
        verifyNoInteractions(expectedSearchActionsConditions, waitActions);
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
    void testWaitTillAlertDisappearsSuccess()
    {
        var targetLocation = mock(TargetLocator.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.switchTo()).thenReturn(targetLocation);
        when(targetLocation.alert()).thenThrow(new NoAlertPresentException());
        var capture = ArgumentCaptor.forClass(ExpectedCondition.class);
        waitSteps.waitTillAlertDisappears();
        verify(waitActions).wait(eq(webDriver), capture.capture());
        ExpectedCondition expectedCondition = capture.getValue();
        assertTrue((boolean) expectedCondition.apply(webDriver));
        assertEquals(ALERT_TO_BE_NOT_PRESENT, expectedCondition.toString());
    }

    @Test
    void testWaitTillAlertDisappearsFail()
    {
        var targetLocation = mock(TargetLocator.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.switchTo()).thenReturn(targetLocation);
        when(targetLocation.alert()).thenReturn(mock(Alert.class));
        var capture = ArgumentCaptor.forClass(ExpectedCondition.class);
        waitSteps.waitTillAlertDisappears();
        verify(waitActions).wait(eq(webDriver), capture.capture());
        ExpectedCondition expectedCondition = capture.getValue();
        assertFalse((boolean) expectedCondition.apply(webDriver));
        assertEquals(ALERT_TO_BE_NOT_PRESENT, expectedCondition.toString());
    }

    @Test
    void testWaitTillAlertDisappearsDeprecated()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        waitSteps.waitTillAlertDisappearsDeprecated();
        verify(waitActions).wait(eq(webDriver),
                argThat(e -> "condition to not be valid: alert to be present".equals(e.toString())));
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
    void testWaitUntilPageTitleIs()
    {
        String exampleTitle = "Example Title";
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getTitle()).thenReturn(exampleTitle);
        var capture = ArgumentCaptor.forClass(ExpectedCondition.class);
        waitSteps.waitUntilPageTitleIs(StringComparisonRule.IS_EQUAL_TO, exampleTitle);
        verify(waitActions).wait(eq(webDriver), capture.capture());
        ExpectedCondition expectedCondition = capture.getValue();
        assertTrue((boolean) expectedCondition.apply(webDriver));
        assertEquals("current title is equal to \"Example Title\". Current title: \"Example Title\"",
                expectedCondition.toString());
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

    @Test
    void testConfigurePageLoadTimeout()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        Options options = mock(Options.class);
        when(webDriver.manage()).thenReturn(options);
        Timeouts timeouts = mock(Timeouts.class);
        when(options.timeouts()).thenReturn(timeouts);
        waitSteps.configurePageLoadTimeout(TIMEOUT);
        verify(timeoutConfigurer).configurePageLoadTimeout(TIMEOUT, timeouts);
    }

    @Test
    void shouldWaitForElementAppearanceInViewportElementIsInViewport()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        var locator = new Locator(WebLocatorType.BUTTON_NAME, NAME);
        var conditionCaptor = ArgumentCaptor.forClass(IExpectedSearchContextCondition.class);

        when(searchActions.findElement(locator)).thenReturn(Optional.of(webElement));
        when(scrollActions.isElementInViewport(webElement)).thenReturn(true);

        waitSteps.waitForElementAppearanceInViewport(locator);

        verify(waitActions).wait(eq(webDriver), conditionCaptor.capture());
        IExpectedSearchContextCondition<WebElement> condition = conditionCaptor.getValue();
        WebElement elementInViewport = condition.apply(null);
        assertEquals(webElement, elementInViewport);
    }

    @Test
    void shouldWaitForElementAppearanceInViewportElementInNotInViewport()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        var locator = new Locator(WebLocatorType.BUTTON_NAME, NAME);
        var conditionCaptor = ArgumentCaptor.forClass(IExpectedSearchContextCondition.class);

        StaleElementReferenceException sere = mock();
        when(searchActions.findElement(locator)).thenThrow(sere).thenReturn(Optional.empty());

        waitSteps.waitForElementAppearanceInViewport(locator);

        verify(waitActions).wait(eq(webDriver), conditionCaptor.capture());
        IExpectedSearchContextCondition<WebElement> condition = conditionCaptor.getValue();
        assertNull(condition.apply(null));
        assertNull(condition.apply(null));
        assertEquals(String.format("element located by %s to be visible in viewport", locator), condition.toString());
        verifyNoInteractions(scrollActions);
    }
}
