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

import static org.apache.commons.lang3.Validate.isTrue;

import com.google.common.eventbus.Subscribe;

import org.openqa.selenium.WebDriver;
import org.vividus.mobileapp.action.ApplicationActions;
import org.vividus.mobileapp.configuration.MobileEnvironment;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverUtil;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.selenium.mobileapp.MobileAppWebDriverManager;
import org.vividus.testcontext.TestContext;
import org.vividus.ui.variable.AbstractWebDriverDynamicVariable;

import io.appium.java_client.clipboard.HasClipboard;

public class ClipboardTextDynamicVariable extends AbstractWebDriverDynamicVariable
{
    private static final Object KEY = BundleIdKey.class;

    private final TestContext testContext;

    public ClipboardTextDynamicVariable(IWebDriverProvider webDriverProvider,
            MobileAppWebDriverManager mobileAppWebDriverManager, TestContext testContext,
            ApplicationActions applicationActions, MobileEnvironment mobileEnvironment)
    {
        super(webDriverProvider, webDriver -> {
            if (mobileEnvironment.isRealDevice() && mobileAppWebDriverManager.isIOSNativeApp())
            {
                String webDriverAgentBundleId = mobileEnvironment.getWebDriverAgentBundleId();
                isTrue(webDriverAgentBundleId != null, "WebDriverAgent bundle ID is not specified");
                String appBundleId = testContext.get(KEY, () -> mobileAppWebDriverManager.getSessionDetail("bundleID"));
                try
                {
                    applicationActions.activateApp(webDriverAgentBundleId);
                    return getClipboardText(webDriver);
                }
                finally
                {
                    applicationActions.activateApp(appBundleId);
                }
            }
            return getClipboardText(webDriver);
        });
        this.testContext = testContext;
    }

    private static String getClipboardText(WebDriver webDriver)
    {
        return WebDriverUtil.unwrap(webDriver, HasClipboard.class).getClipboardText();
    }

    @Subscribe
    public void resetBundleId(@SuppressWarnings("unused") AfterWebDriverQuitEvent event)
    {
        testContext.remove(KEY);
    }

    private static final class BundleIdKey
    {
    }
}
