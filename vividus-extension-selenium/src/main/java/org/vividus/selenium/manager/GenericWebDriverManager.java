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

package org.vividus.selenium.manager;

import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ContextAware;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Rotatable;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.SauceLabsCapabilityType;
import org.vividus.selenium.WebDriverUtil;

import io.appium.java_client.MobileDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.remote.MobilePlatform;

public class GenericWebDriverManager implements IGenericWebDriverManager
{
    private static final String DESIRED_CAPABILITIES_KEY = "desired";

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
        if (isMobile())
        {
            Dimension dimension =
                    webDriverManagerContext.getParameter(WebDriverManagerParameter.SCREEN_SIZE);
            if (dimension == null)
            {
                dimension = runInNativeContext(this::getSize);
                webDriverManagerContext.putParameter(WebDriverManagerParameter.SCREEN_SIZE, dimension);
            }
            return isOrientation(ScreenOrientation.LANDSCAPE) ? new Dimension(dimension.height, dimension.width)
                    : dimension;
        }
        return getSize(getWebDriver());
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
            @SuppressWarnings("unchecked")
            MobileDriver<MobileElement> mobileDriver = webDriverProvider.getUnwrapped(MobileDriver.class);
            String originalContext = mobileDriver.getContext();
            if (!NATIVE_APP_CONTEXT.equals(originalContext))
            {
                switchToContext(mobileDriver, NATIVE_APP_CONTEXT);
                try
                {
                    return function.apply(mobileDriver);
                }
                finally
                {
                    switchToContext(mobileDriver, originalContext);
                }
            }
            return function.apply(mobileDriver);
        }
        return function.apply(getWebDriver());
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
        return isMobile(getCapabilities());
    }

    protected static boolean isMobile(Capabilities capabilities)
    {
        return isIOS(capabilities) || isAndroid(capabilities);
    }

    @Override
    public boolean isIOS()
    {
        return isIOS(getCapabilities());
    }

    public static boolean isIOS(Capabilities capabilities)
    {
        return isPlatformName(capabilities, MobilePlatform.IOS)
                || isBrowserAnyOf(capabilities, BrowserType.IPHONE, BrowserType.IPAD)
                || isDeviceAnyOf(capabilities, BrowserType.IPHONE, BrowserType.IPAD);
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

    @SuppressWarnings("unchecked")
    public static boolean isAndroid(Capabilities capabilities)
    {
        return checkCapabilities(capabilities, () ->
        {
            Capabilities capabilitiesToCheck = capabilities;
            Object desiredCapabilities = capabilitiesToCheck.getCapability(DESIRED_CAPABILITIES_KEY);
            if (desiredCapabilities instanceof Map<?, ?>)
            {
                capabilitiesToCheck = new DesiredCapabilities((Map<String, ?>) desiredCapabilities);
            }
            return isPlatformName(capabilities, MobilePlatform.ANDROID)
                    || isBrowserAnyOf(capabilitiesToCheck, BrowserType.ANDROID);
        });
    }

    @Override
    public boolean isBrowserAnyOf(String... browserTypes)
    {
        return isBrowserAnyOf(getCapabilities(), browserTypes);
    }

    protected static boolean isBrowserAnyOf(Capabilities capabilities, String... browserNames)
    {
        return checkCapabilities(capabilities, () ->
        {
            String capabilitiesBrowserName = capabilities.getBrowserName();
            return Stream.of(browserNames).anyMatch(name -> name.equalsIgnoreCase(capabilitiesBrowserName));
        });
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

    private static boolean isDeviceAnyOf(Capabilities capabilities, String... devices)
    {
        return checkCapabilities(capabilities, () ->
        {
            String deviceCapability = (String) capabilities.getCapability(SauceLabsCapabilityType.DEVICE);
            if (deviceCapability == null)
            {
                Object innerCapabilities = capabilities.getCapability(SauceLabsCapabilityType.CAPABILITIES);
                if (innerCapabilities instanceof Map<?, ?>)
                {
                    deviceCapability = (String) ((Map<?, ?>) innerCapabilities).get(SauceLabsCapabilityType.DEVICE);
                }
            }
            for (String device : devices)
            {
                if (device.equalsIgnoreCase(deviceCapability))
                {
                    return true;
                }
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
        return getCapabilities(getWebDriver());
    }

    protected static Capabilities getCapabilities(WebDriver webDriver)
    {
        return WebDriverUtil.unwrap(webDriver, HasCapabilities.class).getCapabilities();
    }

    @Override
    public Set<String> getWindowHandles()
    {
        return getWebDriver().getWindowHandles();
    }

    @Override
    public boolean isOrientation(ScreenOrientation orientation)
    {
        if (isMobile())
        {
            ScreenOrientation screenOrientation =
                    webDriverManagerContext.getParameter(WebDriverManagerParameter.ORIENTATION);
            if (screenOrientation == null)
            {
                screenOrientation = webDriverProvider.getUnwrapped(Rotatable.class).getOrientation();
                webDriverManagerContext.putParameter(WebDriverManagerParameter.ORIENTATION, screenOrientation);
            }
            return orientation == screenOrientation;
        }
        return false;
    }

    private static boolean checkCapabilities(Capabilities capabilities, BooleanSupplier supplier)
    {
        return capabilities != null && supplier.getAsBoolean();
    }

    protected WebDriver getWebDriver()
    {
        return webDriverProvider.get();
    }

    public void setMobileApp(boolean mobileApp)
    {
        this.mobileApp = mobileApp;
    }
}
