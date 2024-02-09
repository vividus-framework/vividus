/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.listener;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.vividus.ui.web.event.PageLoadEndEvent;
import org.vividus.ui.web.listener.AlertHandlingListener.AlertHandlingOption;
import org.vividus.ui.web.listener.AlertHandlingListener.Factory;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AlertHandlingListenerTests
{
    private static final String ALERT_SCRIPT = "confirm = function(message){return arguments[0];};"
            + "alert = function(message){return arguments[0];};prompt = function(message){return arguments[0];};";

    @Mock(extraInterfaces = JavascriptExecutor.class)
    private WebDriver webDriver;

    @Mock private EventBus eventBus;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AlertHandlingListener.class);

    private AlertHandlingListener createListener(AlertHandlingOption option)
    {
        var listener = new Factory(option, eventBus).createListener(webDriver);
        verify(eventBus).register(listener);
        return listener;
    }

    @Test
    void shouldUnregisterListenerBeforeWebDriverQuit()
    {
        var listener = createListener(AlertHandlingOption.ACCEPT);
        listener.beforeQuit(null);
        verify(eventBus).unregister(listener);
    }

    @Test
    void shouldAcceptAlertAfterAnyNavigationIfItExists()
    {
        createListener(AlertHandlingOption.ACCEPT).afterAnyNavigationCall(mock(Navigation.class), null, null, null);
        verify((JavascriptExecutor) webDriver).executeScript(ALERT_SCRIPT, true);
    }

    @Test
    void shouldDismissAlertAfterClickIfItExists()
    {
        createListener(AlertHandlingOption.DISMISS).afterClick(null);
        verify((JavascriptExecutor) webDriver).executeScript(ALERT_SCRIPT, false);
    }

    @Test
    void shouldDoNotingIfOptionIsSet()
    {
        var event = new PageLoadEndEvent(true, null);
        createListener(AlertHandlingOption.DO_NOTHING).onPageLoadFinish(event);
        verifyNoInteractions(webDriver);
    }

    @Test
    void shouldHandleNoSuchFrameExceptionQuietly()
    {
        var exception = new NoSuchFrameException("reason");
        when(((JavascriptExecutor) webDriver).executeScript(ALERT_SCRIPT, true)).thenThrow(exception);
        createListener(AlertHandlingOption.ACCEPT).afterClick(null);
        assertThat(logger.getLoggingEvents(),
                is(List.of(warn(exception, "Swallowing exception quietly (browser frame may have been closed)"))));
    }
}
