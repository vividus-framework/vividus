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

package org.vividus.selenium.manager;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ContextAware;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Rotatable;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.selenium.BrowserWindowSize;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.SauceLabsCapabilityType;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.WebDriverUtil;
import org.vividus.util.Sleeper;

import io.appium.java_client.MobileDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.remote.MobilePlatform;

public class WebDriverManager implements IWebDriverManager
{
    private static final int GET_WINDOW_HANDLE_RETRIES = 5;
    private static final Duration GET_WINDOW_HANDLE_SLEEP_DURATION = Duration.ofMillis(500);

    private static final String DESIREDCAPABILITIES_KEY = "desired";

    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IWebDriverManagerContext webDriverManagerContext;
    private boolean nativeApp;

    @Override
    public void resize(BrowserWindowSize browserWindowSize)
    {
        resize(getWebDriver(), browserWindowSize);
        // Chrome-only workaround for situations when custom browser viewport size was set before 'window.maximize();'
        // and it prevents following window-resizing actions
        // Reported issue: https://bugs.chromium.org/p/chromedriver/issues/detail?id=1638
        if (isBrowserAnyOf(BrowserType.CHROME) && !isAndroid())
        {
            Window window = getWebDriver().manage().window();
            Dimension size = window.getSize();
            window.setSize(size);
        }
    }

    public static void resize(WebDriver webDriver, BrowserWindowSize browserWindowSize)
    {
        if (!isMobile(WebDriverUtil.unwrap(webDriver, HasCapabilities.class).getCapabilities()))
        {
            Window window = webDriver.manage().window();
            if (browserWindowSize == null)
            {
                window.maximize();
            }
            else
            {
                window.setSize(browserWindowSize.toDimension());
            }
        }
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

    private static boolean isMobile(Capabilities capabilities)
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
            Object desiredCapabilities = capabilitiesToCheck.getCapability(DESIREDCAPABILITIES_KEY);
            if (desiredCapabilities instanceof Map<?, ?>)
            {
                capabilitiesToCheck = new DesiredCapabilities((Map<String, ?>) desiredCapabilities);
            }
            return isPlatformName(capabilities, MobilePlatform.ANDROID)
                    || isBrowserAnyOf(capabilitiesToCheck, BrowserType.ANDROID);
        });
    }

    @Override
    public boolean isTypeAnyOf(WebDriverType... webDriverTypes)
    {
        return isTypeAnyOf(getWebDriver(), webDriverTypes);
    }

    public static boolean isTypeAnyOf(WebDriver webDriver, WebDriverType... webDriverTypes)
    {
        Capabilities capabilities = getCapabilities(webDriver);
        return Stream.of(webDriverTypes).anyMatch(type -> isBrowserAnyOf(capabilities, type.getBrowserNames()));
    }

    @Override
    public WebDriverType detectType()
    {
        return detectType(getCapabilities());
    }

    public static WebDriverType detectType(Capabilities capabilities)
    {
        return Stream.of(WebDriverType.values()).filter(type -> isBrowserAnyOf(capabilities, type.getBrowserNames()))
                .findFirst().orElse(null);
    }

    @Override
    public boolean isBrowserAnyOf(String... browserTypes)
    {
        return isBrowserAnyOf(getCapabilities(), browserTypes);
    }

    private static boolean isBrowserAnyOf(Capabilities capabilities, String... browserNames)
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
                return platformNameFromCaps.equals(platformName);
            }
            if (platformNameFromCaps instanceof Platform)
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
        return nativeApp && isIOS();
    }

    @Override
    public boolean isAndroidNativeApp()
    {
        return nativeApp && isAndroid();
    }

    @Override
    public Capabilities getCapabilities()
    {
        return getCapabilities(getWebDriver());
    }

    private static Capabilities getCapabilities(WebDriver webDriver)
    {
        return WebDriverUtil.unwrap(webDriver, HasCapabilities.class).getCapabilities();
    }

    @Override
    public Set<String> getWindowHandles()
    {
        WebDriver driver = getWebDriver();
        if (isIOS())
        {
            // Workaround for issue https://github.com/appium/appium/issues/6825
            for (int counter = 0; counter < GET_WINDOW_HANDLE_RETRIES; counter++)
            {
                try
                {
                    return driver.getWindowHandles();
                }
                catch (WebDriverException exception)
                {
                    if (exception.getMessage().contains("Could not connect to a valid app after 20 tries."))
                    {
                        Sleeper.sleep(GET_WINDOW_HANDLE_SLEEP_DURATION);
                    }
                }
            }
        }
        return driver.getWindowHandles();
    }

    @Override
    public boolean isOrientation(ScreenOrientation orientation)
    {
        if (!isMobile())
        {
            return false;
        }
        ScreenOrientation screenOrientation =
                webDriverManagerContext.getParameter(WebDriverManagerParameter.ORIENTATION);
        if (screenOrientation == null)
        {
            screenOrientation = webDriverProvider.getUnwrapped(Rotatable.class).getOrientation();
            webDriverManagerContext.putParameter(WebDriverManagerParameter.ORIENTATION, screenOrientation);
        }
        return orientation == screenOrientation;
    }

    private static boolean checkCapabilities(Capabilities capabilities, BooleanSupplier supplier)
    {
        return capabilities != null && supplier.getAsBoolean();
    }

    private WebDriver getWebDriver()
    {
        return webDriverProvider.get();
    }

    public void setNativeApp(boolean nativeApp)
    {
        this.nativeApp = nativeApp;
    }
}
