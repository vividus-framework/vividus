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

package org.vividus.selenium.manager;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ContextAware;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverUtil;

import io.appium.java_client.remote.MobilePlatform;
import io.appium.java_client.remote.SupportsContextSwitching;

public class GenericWebDriverManager implements IGenericWebDriverManager
{
    private final IWebDriverProvider webDriverProvider;
    private final IWebDriverManagerContext webDriverManagerContext;

    private boolean mobileApp;

    public GenericWebDriverManager(IWebDriverProvider webDriverProvider,
            IWebDriverManagerContext webDriverManagerContext)
    {
        this.webDriverProvider = webDriverProvider;
        this.webDriverManagerContext = webDriverManagerContext;
    }

    @Override
    public Dimension getSize()
    {
        if (!isMobile())
        {
            return getSize(webDriverProvider.get());
        }
        Dimension screenSize = webDriverManagerContext.getParameter(WebDriverManagerParameter.SCREEN_SIZE);
        if (screenSize == null)
        {
            screenSize = runInNativeContext(this::getSize);
            webDriverManagerContext.putParameter(WebDriverManagerParameter.SCREEN_SIZE, screenSize);
        }
        return screenSize;
    }

    @Override
    public void performActionInNativeContext(Consumer<WebDriver> consumer)
    {
        runInNativeContext(webDriver -> {
            consumer.accept(webDriver);
            return webDriver;
        });
    }

    private Dimension getSize(WebDriver webDriver)
    {
        return webDriver.manage().window().getSize();
    }

    private <R> R runInNativeContext(Function<WebDriver, R> function)
    {
        if (isMobile())
        {
            SupportsContextSwitching contextSwitchingDriver = getUnwrappedDriver(SupportsContextSwitching.class);
            String originalContext = contextSwitchingDriver.getContext();
            if (!NATIVE_APP_CONTEXT.equals(originalContext))
            {
                switchToContext(contextSwitchingDriver, NATIVE_APP_CONTEXT);
                try
                {
                    return function.apply(contextSwitchingDriver);
                }
                finally
                {
                    switchToContext(contextSwitchingDriver, originalContext);
                }
            }
            return function.apply(contextSwitchingDriver);
        }
        return function.apply(webDriverProvider.get());
    }

    private void switchToContext(ContextAware contextAwareDriver, String contextName)
    {
        if (contextAwareDriver.getContextHandles().contains(contextName))
        {
            contextAwareDriver.context(contextName);
        }
        else
        {
            throw new IllegalStateException("MobileDriver doesn't have context: " + contextName);
        }
    }

    @Override
    public boolean isMobile()
    {
        Capabilities capabilities = getCapabilities();
        return isIOS(capabilities) || isAndroid(capabilities);
    }

    @Override
    public boolean isIOS()
    {
        return isIOS(getCapabilities());
    }

    public static boolean isIOS(Capabilities capabilities)
    {
        return isPlatformName(capabilities, MobilePlatform.IOS);
    }

    @Override
    public boolean isTvOS()
    {
        return isTvOS(getCapabilities());
    }

    public static boolean isTvOS(Capabilities capabilities)
    {
        return isPlatformName(capabilities, MobilePlatform.TVOS);
    }

    @Override
    public boolean isAndroid()
    {
        return isAndroid(getCapabilities());
    }

    public static boolean isAndroid(Capabilities capabilities)
    {
        return isPlatformName(capabilities, MobilePlatform.ANDROID);
    }

    private static boolean isPlatformName(Capabilities capabilities, String platformName)
    {
        return checkCapabilities(capabilities, () ->
        {
            Object platformNameFromCaps = capabilities.getCapability(CapabilityType.PLATFORM_NAME);
            if (platformNameFromCaps instanceof String)
            {
                return ((String) platformNameFromCaps).equalsIgnoreCase(platformName);
            }
            // https://github.com/SeleniumHQ/selenium/issues/9127
            if (platformNameFromCaps instanceof Platform && !MobilePlatform.TVOS.equals(platformName))
            {
                return platformNameFromCaps == Platform.fromString(platformName);
            }
            return false;
        });
    }

    @Override
    public boolean isIOSNativeApp()
    {
        return mobileApp && isIOS();
    }

    @Override
    public boolean isAndroidNativeApp()
    {
        return mobileApp && isAndroid();
    }

    @Override
    public Capabilities getCapabilities()
    {
        return getCapabilities(webDriverProvider.get());
    }

    protected static Capabilities getCapabilities(WebDriver webDriver)
    {
        return WebDriverUtil.unwrap(webDriver, HasCapabilities.class).getCapabilities();
    }

    protected static boolean checkCapabilities(Capabilities capabilities, BooleanSupplier supplier)
    {
        return capabilities != null && supplier.getAsBoolean();
    }

    protected <T> T getUnwrappedDriver(Class<T> clazz)
    {
        return webDriverProvider.getUnwrapped(clazz);
    }

    protected IWebDriverProvider getWebDriverProvider()
    {
        return webDriverProvider;
    }

    public void setMobileApp(boolean mobileApp)
    {
        this.mobileApp = mobileApp;
    }
}
