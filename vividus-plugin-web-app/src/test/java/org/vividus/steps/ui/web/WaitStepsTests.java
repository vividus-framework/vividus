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

package org.vividus.steps.ui.web;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.TimeoutConfigurer;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.State;
import org.vividus.ui.action.IExpectedConditions;
import org.vividus.ui.action.IExpectedSearchContextCondition;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.WaitResult;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.util.XpathLocatorUtils;
import org.vividus.ui.web.action.IWebWaitActions;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.action.search.WebLocatorType;

@SuppressWarnings("unchecked")
@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class WaitStepsTests
{
    private static final String ATTRIBUTE_VALUE = "attributeValue";
    private static final String ATTRIBUTE_TYPE = "attributeType";
    private static final String ELEMENT_TAG = "elementTag";
    private static final String NAME = "name";
    private static final String TEXT = "text";
    private static final Duration TIMEOUT = Duration.ofSeconds(1L);
    private static final String ELEMENT_WITH_TAG = ".//elementTag[normalize-space(@attributeType)=\"attributeValue\"]";
    private static final String ALERT_TO_BE_PRESENT = "alert to be present";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IWebWaitActions waitActions;
    @Mock private IUiContext uiContext;
    @Mock private WebElement webElement;
    @Mock private WebDriver webDriver;
    @Mock private ISoftAssert softAssert;
    @Mock private IExpectedConditions<By> expectedSearchContextConditions;
    @Mock private IExpectedConditions<Locator> expectedSearchActionsConditions;
    @Mock private IBaseValidations baseValidations;
    @Mock private ISearchActions searchActions;
    @Mock private WebJavascriptActions javascriptActions;
    @Mock private TimeoutConfigurer timeoutConfigurer;
    @InjectMocks private WaitSteps waitSteps;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(WaitSteps.class);

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
        assertThat(logger.getLoggingEvents(), is(List.of(warn("The step: \"When I wait until an element with the tag "
                + "'$elementTag' and attribute '$attributeType'='$attributeValue' appears\" is deprecated and will be "
                + "removed in VIVIDUS 0.6.0. Use step: \"When I wait until element located by `$locator` appears\""))));
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
    void testWaitTillElementWithTagAndAttributeDisappears()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        List<WebElement> elements = List.of(webElement);
        Locator locator = new Locator(WebLocatorType.XPATH, ELEMENT_WITH_TAG);
        locator.getSearchParameters().setWaitForElement(false);
        when(searchActions.findElements(locator)).thenReturn(elements);
        waitSteps.waitTillElementDisappears(ELEMENT_TAG, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verify(waitActions).wait(eq(webDriver),
                argThat(condition -> condition.toString().equals("invisibility of " + webElement)));
        verifyNoInteractions(softAssert);
        assertThat(logger.getLoggingEvents(), is(List.of(
                warn("The step: \"When I wait until an element with the tag '$elementTag' and attribute "
                        + "'$attributeType'='$attributeValue' disappears\" is deprecated and will be removed in "
                        + "VIVIDUS 0.6.0. Use step: \"When I wait until element located by `$locator` disappears\""))));
    }

    @Test
    void testWaitTillElementWithTagAndAttributeDisappearsElementIsNotPresent()
    {
        Locator locator = new Locator(WebLocatorType.XPATH, ELEMENT_WITH_TAG);
        when(searchActions.findElements(locator)).thenReturn(List.of());
        locator.getSearchParameters().setWaitForElement(false);
        waitSteps.waitTillElementDisappears(ELEMENT_TAG, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verifyNoInteractions(waitActions);
        verify(softAssert).recordPassedAssertion(
                "There is no element present with the tag 'elementTag' and attribute 'attributeType'='attributeValue'");
        assertThat(logger.getLoggingEvents(), is(List.of(warn("The step: \"When I wait until an element with"
                + " the tag '$elementTag' and attribute '$attributeType'='$attributeValue' disappears\" is deprecated"
                + " and will be removed in VIVIDUS 0.6.0. Use step: \"When I wait until element located by `$locator`"
                + " disappears\""))));
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
    void testWaitTillFrameAppears()
    {
        when(uiContext.getSearchContext(WebDriver.class)).thenReturn(Optional.of(webDriver));
        WaitResult<List<WebElement>> waitResult = mock(WaitResult.class);
        IExpectedSearchContextCondition<List<WebElement>> condition = mock(IExpectedSearchContextCondition.class);
        By locator = XpathLocatorUtils
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
}
