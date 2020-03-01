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

package org.vividus.ui.web.listener;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.event.PageLoadEndEvent;

@ExtendWith(MockitoExtension.class)
class AlertHandlingPageLoadListenerTests
{
    private static final String URL = "url";
    private static final String ALERT_SCRIPT = "confirm = function(message){return arguments[0];};"
            + "alert = function(message){return arguments[0];};prompt = function(message){return arguments[0];};";

    @Mock(extraInterfaces = JavascriptExecutor.class)
    private WebDriver webDriver;

    @Mock
    private WebElement webElement;

    @InjectMocks
    private AlertHandlingPageLoadListener pageLoadListenerForAlertHanding;

    @Test
    void testAfterNavigateToAcceptAlert()
    {
        pageLoadListenerForAlertHanding.setAlertHandlingOptions(AlertHandlingOptions.ACCEPT);
        pageLoadListenerForAlertHanding.afterNavigateTo(URL, webDriver);
        verify((JavascriptExecutor) webDriver).executeScript(ALERT_SCRIPT, true);
    }

    @Test
    void testAfterNavigateToDismissAlert()
    {
        pageLoadListenerForAlertHanding.setAlertHandlingOptions(AlertHandlingOptions.DISMISS);
        pageLoadListenerForAlertHanding.afterNavigateTo(URL, webDriver);
        verify((JavascriptExecutor) webDriver).executeScript(ALERT_SCRIPT, false);
    }

    @Test
    void testAfterNavigateToOffWorkaround()
    {
        pageLoadListenerForAlertHanding.setAlertHandlingOptions(AlertHandlingOptions.DO_NOTHING);
        pageLoadListenerForAlertHanding.afterNavigateTo(URL, webDriver);
        verifyNoInteractions(webDriver);
    }

    @Test
    void testAfterNavigateBackAcceptAlert()
    {
        pageLoadListenerForAlertHanding.setAlertHandlingOptions(AlertHandlingOptions.ACCEPT);
        pageLoadListenerForAlertHanding.afterNavigateBack(webDriver);
        verify((JavascriptExecutor) webDriver).executeScript(ALERT_SCRIPT, true);
    }

    @Test
    void testAfterNavigateForwardAcceptAlert()
    {
        pageLoadListenerForAlertHanding.setAlertHandlingOptions(AlertHandlingOptions.ACCEPT);
        pageLoadListenerForAlertHanding.afterNavigateForward(webDriver);
        verify((JavascriptExecutor) webDriver).executeScript(ALERT_SCRIPT, true);
    }

    @Test
    void testAfterClickOnAcceptAlert()
    {
        pageLoadListenerForAlertHanding.setAlertHandlingOptions(AlertHandlingOptions.ACCEPT);
        pageLoadListenerForAlertHanding.afterClickOn(webElement, webDriver);
        verify((JavascriptExecutor) webDriver).executeScript(ALERT_SCRIPT, true);
    }

    @Test
    void testAfterClickOnWebDriverException()
    {
        pageLoadListenerForAlertHanding.setAlertHandlingOptions(AlertHandlingOptions.ACCEPT);
        doThrow(NoSuchFrameException.class).when((JavascriptExecutor) webDriver).executeScript(ALERT_SCRIPT, true);
        pageLoadListenerForAlertHanding.afterClickOn(webElement, webDriver);
    }

    @Test
    void testOnPageLoadFinish()
    {
        PageLoadEndEvent event = new PageLoadEndEvent(true, webDriver);
        pageLoadListenerForAlertHanding.setAlertHandlingOptions(AlertHandlingOptions.ACCEPT);
        pageLoadListenerForAlertHanding.onPageLoadFinish(event);
        verify((JavascriptExecutor) webDriver).executeScript(ALERT_SCRIPT, true);
    }
}
