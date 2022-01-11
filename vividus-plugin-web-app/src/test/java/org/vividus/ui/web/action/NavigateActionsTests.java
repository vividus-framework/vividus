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

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriverException;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class })
class NavigateActionsTests
{
    private static final String SCRIPT_WINDOW_STOP = "window.stop()";
    private static final String URL_STR = "http://somewhere.com/page";
    private static final URI URL = URI.create(URL_STR);

    private final TestLogger logger = TestLoggerFactory.getTestLogger(NavigateActions.class);

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private WebDriver webDriver;

    @Mock
    private Navigation navigation;

    @Mock
    private IWebWaitActions waitActions;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private WebJavascriptActions javascriptActions;

    @InjectMocks
    private NavigateActions navigateActions;

    @Test
    void testNavigateTo()
    {
        when(webDriver.navigate()).thenReturn(navigation);
        when(webDriverProvider.get()).thenReturn(webDriver);
        navigateActions.navigateTo(URL_STR);
        verify(navigation).to(URL_STR);
    }

    @Test
    void testNavigateToByUri()
    {
        when(webDriver.navigate()).thenReturn(navigation);
        when(webDriverProvider.get()).thenReturn(webDriver);
        navigateActions.navigateTo(URL);
        verify(navigation).to(URL_STR);
    }

    @Test
    void testNavigateToTimeoutException()
    {
        when(webDriver.navigate()).thenReturn(navigation);
        when(webDriverProvider.get()).thenReturn(webDriver);
        TimeoutException exception = mock(TimeoutException.class);
        doThrow(exception).when(navigation).to(URL_STR);
        navigateActions.navigateTo(URL_STR);
        verify(softAssert).recordFailedAssertion(exception);
        verify(javascriptActions).executeScript(SCRIPT_WINDOW_STOP);
    }

    @Test
    void testNavigateWithTimeoutExceptionAndExceptionAtResourceLoading()
    {
        when(webDriver.navigate()).thenReturn(navigation);
        when(webDriverProvider.get()).thenReturn(webDriver);
        TimeoutException exception = mock(TimeoutException.class);
        doThrow(exception).when(navigation).to(URL_STR);
        WebDriverException webDriverException = new WebDriverException();
        when(javascriptActions.executeScript(SCRIPT_WINDOW_STOP)).thenThrow(webDriverException);
        navigateActions.navigateTo(URL_STR);
        verify(softAssert).recordFailedAssertion(exception);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
                info("Loading: {}", URL_STR),
                error(webDriverException, "Unable to stop resource loading"))));
    }

    @Test
    void testRefresh()
    {
        when(webDriver.navigate()).thenReturn(navigation);
        when(webDriverProvider.get()).thenReturn(webDriver);
        navigateActions.refresh();
        verify(navigation).refresh();
        verify(waitActions).waitForPageLoad();
    }

    @Test
    void testRefreshTimeoutException()
    {
        when(webDriver.navigate()).thenReturn(navigation);
        when(webDriverProvider.get()).thenReturn(webDriver);
        TimeoutException exception = mock(TimeoutException.class);
        doThrow(exception).when(navigation).refresh();
        navigateActions.refresh();
        verify(softAssert).recordFailedAssertion(exception);
        verify(javascriptActions).executeScript(SCRIPT_WINDOW_STOP);
    }

    @Test
    void testRefreshWithWebDriver()
    {
        when(webDriver.navigate()).thenReturn(navigation);
        navigateActions.refresh(webDriver);
        verify(webDriverProvider, never()).get();
        verify(navigation).refresh();
        verify(waitActions).waitForPageLoad();
    }

    @Test
    void testBack()
    {
        when(webDriver.navigate()).thenReturn(navigation);
        when(webDriverProvider.get()).thenReturn(webDriver);
        navigateActions.back();
        verify(navigation).back();
        verify(waitActions).waitForPageLoad();
    }

    @Test
    void testBackWithPreviousUrl()
    {
        when(webDriver.navigate()).thenReturn(navigation);
        String defaultUrl = URL_STR + "/previous";
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(URL_STR).thenReturn(URL_STR);
        navigateActions.back(defaultUrl);
        verify(navigation).to(defaultUrl);
    }

    @Test
    void testBackWithPreviousUrlEqualToCurrent()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(URL_STR);
        navigateActions.back(URL_STR);
        verify(navigation, never()).to(URL_STR);
    }
}
