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

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
class SeleniumNavigateActionsTests
{
    private static final String SCRIPT_WINDOW_STOP = "window.stop()";
    private static final String URL = "https://somewhere.com/page";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(SeleniumNavigateActions.class);

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private WebDriver webDriver;
    @Mock private Navigation navigation;
    @Mock private IWebWaitActions waitActions;
    @Mock private ISoftAssert softAssert;
    @Mock private WebJavascriptActions javascriptActions;
    @InjectMocks private SeleniumNavigateActions seleniumNavigateActions;

    @Test
    void testNavigateTo()
    {
        when(webDriver.navigate()).thenReturn(navigation);
        when(webDriverProvider.get()).thenReturn(webDriver);
        seleniumNavigateActions.navigateTo(URL);
        verify(navigation).to(URL);
    }

    @Test
    void testNavigateToTimeoutException()
    {
        when(webDriver.navigate()).thenReturn(navigation);
        when(webDriverProvider.get()).thenReturn(webDriver);
        TimeoutException exception = mock();
        doThrow(exception).when(navigation).to(URL);
        seleniumNavigateActions.navigateTo(URL);
        verify(softAssert).recordFailedAssertion(exception);
        verify(javascriptActions).executeScript(SCRIPT_WINDOW_STOP);
    }

    @Test
    void testNavigateWithTimeoutExceptionAndExceptionAtResourceLoading()
    {
        when(webDriver.navigate()).thenReturn(navigation);
        when(webDriverProvider.get()).thenReturn(webDriver);
        TimeoutException exception = mock();
        doThrow(exception).when(navigation).to(URL);
        var webDriverException = new WebDriverException();
        when(javascriptActions.executeScript(SCRIPT_WINDOW_STOP)).thenThrow(webDriverException);
        seleniumNavigateActions.navigateTo(URL);
        verify(softAssert).recordFailedAssertion(exception);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
                info("Loading: {}", URL),
                error(webDriverException, "Unable to stop resource loading"))));
    }

    @Test
    void shouldReturnCurrentPageUrl()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(URL);
        var actualUrl = seleniumNavigateActions.getCurrentUrl();
        assertEquals(URL, actualUrl);
    }

    @Test
    void testRefresh()
    {
        when(webDriver.navigate()).thenReturn(navigation);
        when(webDriverProvider.get()).thenReturn(webDriver);
        seleniumNavigateActions.refresh();
        verify(navigation).refresh();
        verify(waitActions).waitForPageLoad();
    }

    @Test
    void testRefreshTimeoutException()
    {
        when(webDriver.navigate()).thenReturn(navigation);
        when(webDriverProvider.get()).thenReturn(webDriver);
        TimeoutException exception = mock();
        doThrow(exception).when(navigation).refresh();
        seleniumNavigateActions.refresh();
        verify(softAssert).recordFailedAssertion(exception);
        verify(javascriptActions).executeScript(SCRIPT_WINDOW_STOP);
    }
}
