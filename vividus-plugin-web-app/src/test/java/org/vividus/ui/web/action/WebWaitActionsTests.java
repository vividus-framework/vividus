/*
 * Copyright 2019-2021 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.FluentWait;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.DescriptiveWait;
import org.vividus.ui.action.IWaitFactory;

@ExtendWith(MockitoExtension.class)
class WebWaitActionsTests
{
    private static final String COMPLETE = "complete";
    private static final String INTERACTIVE = "interactive";
    private static final String SCRIPT_READY_STATE = "return document.readyState";
    private static final long TIMEOUT_VALUE = 1;
    private static final Duration TIMEOUT_MILLIS = Duration.ofMillis(1);

    private WebWaitActions spy;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private IWaitFactory waitFactory;

    @Mock
    private WebJavascriptActions javascriptActions;

    @Mock
    private IAlertActions alertActions;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private IWebDriverManager webDriverManager;

    @Mock(extraInterfaces = HasCapabilities.class)
    private WebDriver webDriver;

    @Mock
    private TargetLocator targetLocator;

    @InjectMocks
    private WebWaitActions waitActions;

    @Test
    void testWaitForPageLoadWithAlert()
    {
        when(alertActions.isAlertPresent(webDriver)).thenReturn(Boolean.TRUE);
        waitActions.waitForPageLoad(webDriver);
        verifyNoInteractions(javascriptActions);
    }

    @Test
    void shouldNoWaitForElectronAppsWindowsLoad()
    {
        when(webDriverManager.isElectronApp()).thenReturn(true);
        waitActions.waitForPageLoad(webDriver);
        verifyNoInteractions(javascriptActions);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testWaitForPageLoadWithoutAlert()
    {
        WebWaitActions spy = spy(waitActions);
        mockDescriptiveWait(ChronoUnit.DAYS);
        when(javascriptActions.executeScript(SCRIPT_READY_STATE)).thenReturn(COMPLETE);
        doAnswer(a ->
        {
            Function<WebDriver, Boolean> func = (Function<WebDriver, Boolean>) a.getArguments()[1];
            assertTrue(func.apply(webDriver));
            return null;
        }).when(spy).wait(eq(webDriver), any());
        spy.waitForPageLoad(webDriver);
    }

    @Test
    void shouldReturnDuringWaitForPageLoadWhenAlertAppearsBeforePageReadyCheck()
    {
        when(alertActions.isAlertPresent(webDriver)).thenReturn(false).thenReturn(true);
        mockDescriptiveWait(ChronoUnit.DAYS);
        waitActions.waitForPageLoad(webDriver);
        verifyNoInteractions(javascriptActions);
    }

    @Test
    void testWaitForPageLoad()
    {
        mockDescriptiveWait(ChronoUnit.DAYS);
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.CHROME)).thenReturn(false);
        waitActions.waitForPageLoad();
        verify(softAssert).assertNotNull("The input value to pass to the wait condition", null);
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
    void testWaitForPageLoadChrome()
    {
        configureWaitActions();
        spy = spy(waitActions);
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.CHROME)).thenReturn(true);
        testWaitForPageLoadSleepForTimeout();
    }

    @Test
    void testWaitForPageLoadIOS()
    {
        configureWaitActions();
        spy = spy(waitActions);
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
        spy = spy(waitActions);
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
        spy = spy(waitActions);
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
        spy = spy(waitActions);
        mockDescriptiveWait(ChronoUnit.DAYS);
        when(javascriptActions.executeScript(SCRIPT_READY_STATE)).thenReturn("").thenReturn(COMPLETE);
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.CHROME)).thenReturn(Boolean.TRUE);
        spy.waitForPageLoad();
        verify(spy, never()).sleepForTimeout(Duration.ofDays(TIMEOUT_VALUE));
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

    private void configureWaitActions()
    {
        waitActions.setPageStartsToLoadTimeout(TIMEOUT_MILLIS);
    }
}
