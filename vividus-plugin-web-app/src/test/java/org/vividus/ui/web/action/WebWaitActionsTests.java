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

package org.vividus.ui.web.action;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.support.ui.FluentWait;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.DescriptiveWait;
import org.vividus.ui.action.IWaitFactory;
import org.vividus.util.Sleeper;

@ExtendWith(MockitoExtension.class)
class WebWaitActionsTests
{
    private static final String COMPLETE = "complete";
    private static final String INTERACTIVE = "interactive";
    private static final String SCRIPT_READY_STATE = "return document.readyState";
    private static final long TIMEOUT_VALUE = 1;

    @Mock(extraInterfaces = HasCapabilities.class)
    private WebDriver webDriver;

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IWaitFactory waitFactory;
    @Mock private WebJavascriptActions javascriptActions;
    @Mock private IAlertActions alertActions;
    @Mock private ISoftAssert softAssert;
    @Mock private IWebDriverManager webDriverManager;
    @InjectMocks private WebWaitActions waitActions;

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
        var spy = spy(waitActions);
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
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.CHROME)).thenReturn(false);
        waitActions.waitForPageLoad();
        verify(softAssert).assertNotNull("The input value to pass to the wait condition", null);
    }

    @Test
    void testWaitForPageLoadWithWebDriverException()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        TargetLocator targetLocator = mock();
        when(webDriver.switchTo()).thenReturn(targetLocator);
        mockDescriptiveWait(ChronoUnit.DAYS);
        when(javascriptActions.executeScript(SCRIPT_READY_STATE)).thenThrow(new WebDriverException())
                .thenReturn(COMPLETE);
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.CHROME)).thenReturn(false);
        waitActions.waitForPageLoad();
        verify(targetLocator).defaultContent();
    }

    @Test
    void testWaitForPageLoadChrome()
    {
        Duration pageStartsToLoadTimeout = Duration.ofMillis(1);
        waitActions.setPageStartsToLoadTimeout(pageStartsToLoadTimeout);
        var spy = spy(waitActions);
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.CHROME)).thenReturn(true);
        mockDescriptiveWait(ChronoUnit.DAYS);
        when(javascriptActions.executeScript(SCRIPT_READY_STATE)).thenReturn(COMPLETE);
        try (MockedStatic<Sleeper> sleeper = mockStatic(Sleeper.class))
        {
            spy.waitForPageLoad();
            sleeper.verify(() -> Sleeper.sleep(pageStartsToLoadTimeout));
        }
    }

    @Test
    void testWaitForPageLoadIExplore()
    {
        var spy = spy(waitActions);
        mockDescriptiveWait(ChronoUnit.DAYS);
        when(alertActions.isAlertPresent(webDriver)).thenReturn(false);
        when(webDriverManager.isBrowserAnyOf(Browser.IE)).thenReturn(true);
        when(javascriptActions.executeScript(SCRIPT_READY_STATE)).thenReturn(INTERACTIVE);
        spy.waitForPageLoad(webDriver);
        verify(spy).wait(eq(webDriver), any());
    }

    @Test
    void testWaitForPageLoadIExploreEmptyDocumentReadyState()
    {
        var spy = spy(waitActions);
        mockDescriptiveWait(ChronoUnit.MILLIS);
        when(alertActions.isAlertPresent(webDriver)).thenReturn(false);
        when(webDriverManager.isBrowserAnyOf(Browser.IE)).thenReturn(true);
        when(javascriptActions.executeScript(SCRIPT_READY_STATE)).thenReturn("");
        spy.waitForPageLoad(webDriver);
        verify(spy).wait(eq(webDriver), any());
    }

    @Test
    void testWaitForPageLoadChromeNoSleep()
    {
        var spy = spy(waitActions);
        mockDescriptiveWait(ChronoUnit.DAYS);
        when(javascriptActions.executeScript(SCRIPT_READY_STATE)).thenReturn("").thenReturn(COMPLETE);
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.CHROME)).thenReturn(Boolean.TRUE);
        try (MockedStatic<Sleeper> sleeper = mockStatic(Sleeper.class))
        {
            spy.waitForPageLoad();
            sleeper.verifyNoInteractions();
        }
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
}
