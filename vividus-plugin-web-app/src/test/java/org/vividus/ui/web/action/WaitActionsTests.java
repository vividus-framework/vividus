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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;

@ExtendWith(MockitoExtension.class)
class WaitActionsTests
{
    private static final String VALUE_222 = "222";
    private static final String OLD_URL = "oldUrl";
    private static final String NEW_URL = "newUrl";
    private static final String COMPLETE = "complete";
    private static final String INTERACTIVE = "interactive";
    private static final String SCRIPT_READY_STATE = "return document.readyState";
    private static final long TIMEOUT_VALUE = 1;
    private static final Duration TIMEOUT_SECONDS = Duration.ofSeconds(TIMEOUT_VALUE);
    private static final Duration TIMEOUT_MILLIS = Duration.ofMillis(1);
    private static final String EXCEPTION_TIMEOUT_MESSAGE = "mocked timeout exception message";
    private static final String MOCKED_WAIT_DESCRIPTION = "mocked wait description";

    private WaitActions spy;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private IWaitFactory waitFactory;

    @Mock
    private IJavascriptActions javascriptActions;

    @Mock
    private IAlertActions alertActions;

    @Mock
    private IWebDriverManager webDriverManager;

    @Mock
    private Wait<SearchContext> wait;

    @Mock
    private Function<SearchContext, ?> isTrue;

    @Mock
    private SearchContext searchContext;

    @Mock(extraInterfaces = HasCapabilities.class)
    private WebDriver webDriver;

    @Mock
    private TargetLocator targetLocator;

    @InjectMocks
    private WaitActions waitActions;

    @Test
    void testWaitWith4Parameters()
    {
        when(wait.toString()).thenReturn(MOCKED_WAIT_DESCRIPTION);
        when(waitFactory.createWait(searchContext, TIMEOUT_SECONDS)).thenReturn(wait);
        waitActions.wait(searchContext, TIMEOUT_SECONDS, isTrue);
        verifySuccess();
    }

    @Test
    void testWaitWith3Parameters()
    {
        when(wait.toString()).thenReturn(MOCKED_WAIT_DESCRIPTION);
        when(waitFactory.createWait(searchContext)).thenReturn(wait);
        waitActions.wait(searchContext, isTrue, true);
        verifySuccess();
    }

    @Test
    void testWaitWith2Parameters()
    {
        when(wait.toString()).thenReturn(MOCKED_WAIT_DESCRIPTION);
        when(waitFactory.createWait(searchContext)).thenReturn(wait);
        waitActions.wait(searchContext, isTrue);
        verifySuccess();
    }

    @Test
    void testWaitWithNullInput()
    {
        SearchContext searchContext = null;
        waitActions.wait(searchContext, isTrue);
        verifyZeroInteractions(waitFactory);
        verify(softAssert).assertNotNull("The input value to pass to the wait condition", searchContext);
    }

    @Test
    void testWaitWith4ParametersFailWithTimeoutException()
    {
        when(wait.toString()).thenReturn(MOCKED_WAIT_DESCRIPTION);
        when(waitFactory.createWait(searchContext, TIMEOUT_SECONDS)).thenReturn(wait);
        mockException(TimeoutException.class);
        waitActions.wait(searchContext, TIMEOUT_SECONDS, isTrue);
        verifyFailureRecording();
    }

    @Test
    void testWaitWith4ParametersFailWithTimeoutExceptionAndDoNotRecordAssertionFailure()
    {
        when(waitFactory.createWait(searchContext, TIMEOUT_SECONDS)).thenReturn(wait);
        mockException(TimeoutException.class);
        waitActions.wait(searchContext, TIMEOUT_SECONDS, isTrue, false);
        verifyZeroInteractions(softAssert);
    }

    @Test
    void testWaitWith5ParametersFailWithTimeoutExceptionAndDoNotRecordAssertionFailure()
    {
        when(waitFactory.createWait(searchContext, TIMEOUT_SECONDS, TIMEOUT_MILLIS)).thenReturn(wait);
        mockException(TimeoutException.class);
        waitActions.wait(searchContext, TIMEOUT_SECONDS, TIMEOUT_MILLIS, isTrue, false);
        verifyZeroInteractions(softAssert);
    }

    @Test
    void testWaitWith4ParametersFailWithNoSuchElementException()
    {
        when(wait.toString()).thenReturn(MOCKED_WAIT_DESCRIPTION);
        when(waitFactory.createWait(searchContext, TIMEOUT_SECONDS)).thenReturn(wait);
        mockException(NoSuchElementException.class);
        waitActions.wait(searchContext, TIMEOUT_SECONDS, isTrue);
        verifyFailureRecording();
    }

    @Test
    void testWaitForPageLoadWithAlert()
    {
        when(alertActions.isAlertPresent(webDriver)).thenReturn(Boolean.TRUE);
        waitActions.waitForPageLoad(webDriver);
        verifyZeroInteractions(javascriptActions);
    }

    @Test
    void testWaitForPageLoadWithoutAlert()
    {
        mockDescriptiveWait(ChronoUnit.DAYS);
        when(javascriptActions.executeScript(SCRIPT_READY_STATE)).thenReturn(COMPLETE);
        waitActions.waitForPageLoad(webDriver);
    }

    @Test
    void shouldReturnDuringWaitForPageLoadWhenAlertAppearsBeforePageReadyCheck()
    {
        when(alertActions.isAlertPresent(webDriver)).thenReturn(false).thenReturn(true);
        mockDescriptiveWait(ChronoUnit.DAYS);
        waitActions.waitForPageLoad(webDriver);
        verifyZeroInteractions(javascriptActions);
    }

    @Test
    void testWaitForNewWindowOpen()
    {
        mockDescriptiveWait(ChronoUnit.MILLIS);
        Set<String> windowHandles = new HashSet<>();
        windowHandles.add("111");
        windowHandles.add(VALUE_222);
        when(webDriverManager.getWindowHandles()).thenReturn(windowHandles);
        waitActions.waitForNewWindowOpen(webDriver, 1, TIMEOUT_MILLIS, true);
        verify(webDriverManager).getWindowHandles();
    }

    @Test
    void testWaitForNewWindowOpenRecordNotSpecified()
    {
        spy = Mockito.spy(waitActions);
        mockDescriptiveWait(ChronoUnit.MILLIS);
        spy.waitForNewWindowOpen(webDriver, 1, TIMEOUT_MILLIS);
        verify(spy).waitForNewWindowOpen(webDriver, 1, TIMEOUT_MILLIS, true);
    }

    @Test
    void testWaitForNewWindowOpenWithDefaultTimeout()
    {
        configureWaitActions();
        spy = Mockito.spy(waitActions);
        mockDescriptiveWait(ChronoUnit.MILLIS);
        spy.waitForNewWindowOpen(webDriver, 1, true);
        verify(spy).waitForNewWindowOpen(webDriver, 1, TIMEOUT_MILLIS, true);
    }

    @Test
    void testWaitForNewWindowOpenWithDefaultTimeoutRecordNotSpecified()
    {
        configureWaitActions();
        spy = Mockito.spy(waitActions);
        mockDescriptiveWait(ChronoUnit.MILLIS);
        spy.waitForNewWindowOpen(webDriver, 1);
        verify(spy).waitForNewWindowOpen(webDriver, 1, TIMEOUT_MILLIS, true);
    }

    @Test
    void testWaitForNewWindowOpenNotRecordAssertion()
    {
        configureWaitActions();
        spy = Mockito.spy(waitActions);
        mockDescriptiveWait(ChronoUnit.MILLIS);
        spy.waitForNewWindowOpen(webDriver, 1, false);
        verify(spy).waitForNewWindowOpen(webDriver, 1, TIMEOUT_MILLIS, false);
    }

    @Test
    void testWaitForNewPageOpen()
    {
        mockDescriptiveWait(ChronoUnit.MILLIS);
        when(webDriver.getCurrentUrl()).thenReturn(NEW_URL);
        waitActions.waitForPageOpen(webDriver, OLD_URL, TIMEOUT_MILLIS);
        verify(webDriver).getCurrentUrl();
    }

    @Test
    void testWaitForNewPageOpenWithDefaultTimeout()
    {
        configureWaitActions();
        spy = Mockito.spy(waitActions);
        mockDescriptiveWait(ChronoUnit.MILLIS);
        when(webDriver.getCurrentUrl()).thenReturn(OLD_URL);
        spy.waitForPageOpen(webDriver, OLD_URL);
        verify(spy).waitForPageOpen(webDriver, OLD_URL, TIMEOUT_MILLIS);
    }

    @Test
    void testWaitForPageLoad()
    {
        mockDescriptiveWait(ChronoUnit.DAYS);
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.CHROME)).thenReturn(false);
        waitActions.waitForPageLoad();
    }

    @Test
    void testWaitForPageLoadWithWebDriverException()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.switchTo()).thenReturn(targetLocator);
        mockDescriptiveWait(ChronoUnit.DAYS);
        when(javascriptActions.executeScript(SCRIPT_READY_STATE)).thenThrow(new WebDriverException())
                .thenReturn(COMPLETE);
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.CHROME)).thenReturn(false);
        waitActions.waitForPageLoad();
        verify(targetLocator).defaultContent();
    }

    @Test
    void testWaitForWindowToClose()
    {
        spy = Mockito.spy(waitActions);
        mockDescriptiveWait(ChronoUnit.MILLIS);
        spy.waitForWindowToClose(webDriver, VALUE_222);
        verify(spy).wait(eq(webDriver), any());
    }

    @Test
    void testWaitForPageLoadChrome()
    {
        configureWaitActions();
        spy = Mockito.spy(waitActions);
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.CHROME)).thenReturn(true);
        testWaitForPageLoadSleepForTimeout();
    }

    @Test
    void testWaitForPageLoadIOS()
    {
        configureWaitActions();
        spy = Mockito.spy(waitActions);
        when(webDriverManager.isIOS()).thenReturn(true);
        testWaitForPageLoadSleepForTimeout();
    }

    private void testWaitForPageLoadSleepForTimeout()
    {
        Mockito.lenient().when(targetLocator.alert()).thenThrow(new NoAlertPresentException());
        mockDescriptiveWait(ChronoUnit.DAYS);
        when(javascriptActions.executeScript(SCRIPT_READY_STATE)).thenReturn(COMPLETE);
        spy.waitForPageLoad();
        verify(spy).sleepForTimeout(TIMEOUT_MILLIS);
    }

    @Test
    void testWaitForPageLoadIExplore()
    {
        spy = Mockito.spy(waitActions);
        mockDescriptiveWait(ChronoUnit.DAYS);
        when(alertActions.isAlertPresent(webDriver)).thenReturn(false);
        when(webDriverManager.isTypeAnyOf(WebDriverType.IEXPLORE)).thenReturn(true);
        when(javascriptActions.executeScript(SCRIPT_READY_STATE)).thenReturn(INTERACTIVE);
        spy.waitForPageLoad(webDriver);
        verify(spy).wait(eq(webDriver), any());
    }

    @Test
    void testWaitForPageLoadIExploreEmptyDocumentReadyState()
    {
        spy = Mockito.spy(waitActions);
        mockDescriptiveWait(ChronoUnit.MILLIS);
        when(alertActions.isAlertPresent(webDriver)).thenReturn(false);
        when(webDriverManager.isTypeAnyOf(WebDriverType.IEXPLORE)).thenReturn(true);
        when(javascriptActions.executeScript(SCRIPT_READY_STATE)).thenReturn("");
        spy.waitForPageLoad(webDriver);
        verify(spy).wait(eq(webDriver), any());
    }

    @Test
    void testWaitForPageLoadChromeNoSleep()
    {
        spy = Mockito.spy(waitActions);
        mockDescriptiveWait(ChronoUnit.DAYS);
        when(javascriptActions.executeScript(SCRIPT_READY_STATE)).thenReturn("").thenReturn(COMPLETE);
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.CHROME)).thenReturn(Boolean.TRUE);
        spy.waitForPageLoad();
        verify(spy, never()).sleepForTimeout(Duration.ofDays(TIMEOUT_VALUE));
    }

    private void mockException(Class<? extends Exception> clazz)
    {
        Exception exception = mock(clazz);
        Mockito.lenient().when(exception.getMessage()).thenReturn(EXCEPTION_TIMEOUT_MESSAGE);
        when(wait.until(isTrue)).thenThrow(exception);
    }

    private void mockDescriptiveWait(TemporalUnit timeunit)
    {
        FluentWait<WebDriver> fluentWait = new FluentWait<>(webDriver);
        DescriptiveWait<WebDriver> descriptiveWait = new DescriptiveWait<>(fluentWait);
        descriptiveWait.setTimeout(Duration.of(TIMEOUT_VALUE, timeunit));
        Mockito.lenient().when(waitFactory.createWait(webDriver)).thenReturn(descriptiveWait);
        Mockito.lenient().when(waitFactory.createWait(webDriver, Duration.of(TIMEOUT_VALUE, timeunit)))
                .thenReturn(descriptiveWait);
    }

    private void verifyFailureRecording()
    {
        verify(softAssert, never()).recordPassedAssertion(MOCKED_WAIT_DESCRIPTION);
        verify(softAssert).recordFailedAssertion(MOCKED_WAIT_DESCRIPTION + ". Error: " + EXCEPTION_TIMEOUT_MESSAGE);
    }

    private void verifySuccess()
    {
        verify(wait).until(isTrue);
    }

    private void configureWaitActions()
    {
        waitActions.setWindowOpenTimeout(TIMEOUT_MILLIS);
        waitActions.setPageOpenTimeout(TIMEOUT_MILLIS);
        waitActions.setPageStartsToLoadTimeout(TIMEOUT_MILLIS);
    }
}
