/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.mobileapp.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.vividus.mobileapp.action.ApplicationActions;
import org.vividus.mobileapp.configuration.MobileEnvironment;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.selenium.mobileapp.MobileAppWebDriverManager;
import org.vividus.testcontext.ThreadedTestContext;
import org.vividus.variable.DynamicVariableCalculationResult;

import io.appium.java_client.clipboard.HasClipboard;

@ExtendWith(MockitoExtension.class)
class ClipboardTextDynamicVariableTests
{
    private static final String VALUE = "value";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private MobileAppWebDriverManager mobileAppWebDriverManager;
    @Spy private ThreadedTestContext testContext;
    @Mock private ApplicationActions applicationActions;
    private ClipboardTextDynamicVariable dynamicVariable;

    @Test
    void shouldReturnClipboardTextForSimulator()
    {
        init(false, null);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        mockGetClipboardText();

        assertEquals(DynamicVariableCalculationResult.withValue(VALUE), dynamicVariable.calculateValue());
    }

    @Test
    void shouldReturnClipboardTextForNonIos()
    {
        init(true, null);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(mobileAppWebDriverManager.isIOSNativeApp()).thenReturn(false);
        mockGetClipboardText();

        assertEquals(DynamicVariableCalculationResult.withValue(VALUE), dynamicVariable.calculateValue());
    }

    @Test
    void shouldReturnClipboardTextForIOSRealDevice()
    {
        var wdaBundleId = "wda-bundle-id";
        init(true, wdaBundleId);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(mobileAppWebDriverManager.isIOSNativeApp()).thenReturn(true);

        mockGetClipboardText();

        var appBundleId = "app-bundle-id";
        when(mobileAppWebDriverManager.getSessionDetail("bundleID")).thenReturn(appBundleId);

        assertEquals(DynamicVariableCalculationResult.withValue(VALUE), dynamicVariable.calculateValue());

        verify(applicationActions).activateApp(wdaBundleId);
        verify(applicationActions).activateApp(appBundleId);
    }

    @Test
    void shouldFailOnIOSReadDeviceIfWDABundleIsNotSet()
    {
        init(true, null);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(mobileAppWebDriverManager.isIOSNativeApp()).thenReturn(true);

        var thrown = assertThrows(IllegalArgumentException.class, dynamicVariable::calculateValue);

        assertEquals("WebDriverAgent bundle ID is not specified", thrown.getMessage());
    }

    @Test
    void shouldResetBundleId()
    {
        init(false, null);

        dynamicVariable.resetBundleId(new AfterWebDriverQuitEvent(VALUE));

        verify(testContext).remove(any(Class.class));
    }

    @Test
    void shouldReturnErrorInCaseOfNotStartedSession()
    {
        init(false, null);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(false);
        assertEquals(DynamicVariableCalculationResult.withError("application is not started"),
                dynamicVariable.calculateValue());
    }

    private void mockGetClipboardText()
    {
        var webDriverWithClipboard = mock(WebDriver.class, withSettings().extraInterfaces(HasClipboard.class));
        when(webDriverProvider.get()).thenReturn(webDriverWithClipboard);
        when(((HasClipboard) webDriverWithClipboard).getClipboardText()).thenReturn(VALUE);
    }

    private void init(boolean realDevice, String wdaBundle)
    {
        dynamicVariable = new ClipboardTextDynamicVariable(webDriverProvider, mobileAppWebDriverManager, testContext,
                applicationActions, new MobileEnvironment(realDevice, wdaBundle));
    }
}
