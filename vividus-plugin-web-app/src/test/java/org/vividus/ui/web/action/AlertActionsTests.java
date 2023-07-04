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

package org.vividus.ui.web.action;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.action.WaitResult;
import org.vividus.ui.web.action.AlertActions.Action;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class AlertActionsTests
{
    private static final String ERROR_MESSAGE = "error message";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AlertActions.class);

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private IWebWaitActions waitActions;

    @Mock
    private WebDriver webDriver;

    @Mock
    private TargetLocator targetLocator;

    @Mock
    private Alert alert;

    @Mock
    private IWebDriverManager webDriverManager;

    @Mock
    private IWindowsActions windowsActions;

    @InjectMocks
    private AlertActions alertActions;

    @Test
    void testSwitchToAlert()
    {
        configureDefaultAlertMock();
        Optional<Alert> actualAlert = alertActions.switchToAlert();
        assertTrue(actualAlert.isPresent());
    }

    @Test
    void testSwitchToAlertNoAlert()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.switchTo()).thenReturn(targetLocator);
        when(targetLocator.alert()).thenReturn(null);
        Optional<Alert> actualAlert = alertActions.switchToAlert();
        assertFalse(actualAlert.isPresent());
    }

    private void configureDefaultAlertMock()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.switchTo()).thenReturn(targetLocator);
        when(targetLocator.alert()).thenReturn(alert);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testWaitForAlert()
    {
        Duration timeOut = Duration.ofMillis(1);
        alertActions.setWaitForAlertTimeout(timeOut);
        WaitResult<Alert> result = new WaitResult<>();
        result.setWaitPassed(true);
        result.setData(alert);
        when(waitActions.wait(eq(webDriver), eq(timeOut), any(ExpectedCondition.class), eq(false)))
                .thenReturn(result);
        alertActions.waitForAlert(webDriver);
        verify(waitActions).wait(eq(webDriver), eq(timeOut), any(ExpectedCondition.class), eq(false));
    }

    @Test
    void testIsAlertPresentMobile()
    {
        when(webDriverManager.isMobile()).thenReturn(true);
        boolean answer = alertActions.isAlertPresent(webDriver);
        assertFalse(answer);
        assertThat(logger.getLoggingEvents(), is(List.of(warn("Skipped alert interaction in mobile context"))));
    }

    @Test
    void testIsAlertPresentNoAlertPresentException()
    {
        when(webDriver.switchTo()).thenReturn(targetLocator);
        doThrow(NoAlertPresentException.class).when(targetLocator).alert();
        boolean answer = alertActions.isAlertPresent(webDriver);
        assertFalse(answer);
    }

    @Test
    void testIsAlertPresentNoSuchWindowException()
    {
        when(webDriver.switchTo()).thenReturn(targetLocator);
        doThrow(NoSuchWindowException.class).when(targetLocator).alert();
        boolean answer = alertActions.isAlertPresent(webDriver);
        assertFalse(answer);
        verify(windowsActions).switchToPreviousTab();
    }

    @Test
    void testProcessAlertWithAnyMessage()
    {
        configureDefaultAlertMock();
        when(webDriverManager.isMobile()).thenReturn(false);
        Action action = Action.ACCEPT;
        alertActions.processAlert(action);
        verify(alert).accept();
        verifyNoMoreInteractions(webDriverManager);
        assertEquals(List.of(), logger.getLoggingEvents());
    }

    @Test
    void testProcessAlertWithAnyMessageNoAlert()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.switchTo()).thenReturn(targetLocator);
        when(targetLocator.alert()).thenReturn(null);
        when(webDriverManager.isMobile()).thenReturn(false);
        Action action = Action.ACCEPT;
        alertActions.processAlert(action);
        verifyNoMoreInteractions(webDriverManager);
        verifyNoInteractions(alert);
        assertEquals(List.of(), logger.getLoggingEvents());
    }

    @Test
    void testProcessProvidedAlert()
    {
        Action action = Action.ACCEPT;
        alertActions.processAlert(action, alert);
        verify(alert).accept();
        verifyNoInteractions(webDriverManager);
    }

    @Test
    void testAcceptAction()
    {
        Action.ACCEPT.process(alert, webDriverManager);
        verify(alert).accept();
    }

    @Test
    void testWhenDeclineActionInAlertMessageNull()
    {
        Action.DISMISS.process(alert, webDriverManager);
        verify(alert).dismiss();
    }

    @Test
    void testProcessAlertAndroid()
    {
        doThrow(new WebDriverException(ERROR_MESSAGE)).when(alert).accept();
        when(webDriverManager.isAndroid()).thenReturn(Boolean.TRUE);
        Action.ACCEPT.process(alert, webDriverManager);
        verify(webDriverManager).performActionInNativeContext(argThat(f -> {
            f.accept(webDriver);
            return true;
        }));
    }
}
