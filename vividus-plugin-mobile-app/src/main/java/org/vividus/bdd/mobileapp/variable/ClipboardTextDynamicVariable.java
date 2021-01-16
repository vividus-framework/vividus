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

package org.vividus.bdd.mobileapp.variable;

import static org.apache.commons.lang3.Validate.isTrue;

import javax.inject.Named;

import com.google.common.eventbus.Subscribe;

import org.vividus.bdd.mobileapp.configuration.MobileEnvironment;
import org.vividus.bdd.variable.DynamicVariable;
import org.vividus.mobileapp.action.ApplicationActions;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.selenium.manager.IGenericWebDriverManager;
import org.vividus.testcontext.TestContext;

import io.appium.java_client.HasSessionDetails;
import io.appium.java_client.clipboard.HasClipboard;

@Named("clipboard-text")
public class ClipboardTextDynamicVariable implements DynamicVariable
{
    private static final Object KEY = BundleIdKey.class;

    private final IWebDriverProvider webDriverProvider;
    private final IGenericWebDriverManager genericWebDriverManager;
    private final TestContext testContext;
    private final ApplicationActions applicationActions;
    private final MobileEnvironment mobileEnvironment;

    public ClipboardTextDynamicVariable(IWebDriverProvider webDriverProvider,
            IGenericWebDriverManager genericWebDriverManager, TestContext testContext,
            ApplicationActions applicationActions, MobileEnvironment mobileEnvironment)
    {
        this.webDriverProvider = webDriverProvider;
        this.genericWebDriverManager = genericWebDriverManager;
        this.testContext = testContext;
        this.applicationActions = applicationActions;
        this.mobileEnvironment = mobileEnvironment;
    }

    @Override
    public String getValue()
    {
        if (mobileEnvironment.isRealDevice() && genericWebDriverManager.isIOSNativeApp())
        {
            String webDriverAgentBundleId = mobileEnvironment.getWebDriverAgentBundleId();
            isTrue(webDriverAgentBundleId != null, "WebDriverAgent bundle ID is not specified");
            String appBundleId = testContext.get(KEY, () -> (String) webDriverProvider
                    .getUnwrapped(HasSessionDetails.class).getSessionDetail("bundleID"));
            try
            {
                applicationActions.activateApp(webDriverAgentBundleId);
                return getClipboardText();
            }
            finally
            {
                applicationActions.activateApp(appBundleId);
            }
        }
        return getClipboardText();
    }

    private String getClipboardText()
    {
        return webDriverProvider.getUnwrapped(HasClipboard.class).getClipboardText();
    }

    @Subscribe
    public void resetBundleId(AfterWebDriverQuitEvent event)
    {
        testContext.remove(KEY);
    }

    private static final class BundleIdKey
    {
    }
}
