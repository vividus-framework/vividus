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

package org.vividus.selenium;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariDriverService;
import org.openqa.selenium.safari.SafariOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

public enum WebDriverType
{
    FIREFOX(true, true, Browser.FIREFOX, GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY, WebDriverManager::firefoxdriver)
    {
        @Override
        public void prepareCapabilities(MutableCapabilities desiredCapabilities)
        {
            FirefoxOptions options = new FirefoxOptions();
            options.addPreference("startup.homepage_welcome_url.additional", "about:blank");
            desiredCapabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options);
        }

        @Override
        public WebDriver getWebDriver(DesiredCapabilities desiredCapabilities, WebDriverConfiguration configuration)
        {
            prepareCapabilities(desiredCapabilities);
            FirefoxOptions options = new FirefoxOptions(desiredCapabilities);
            configuration.getBinaryPath().ifPresent(options::setBinary);
            options.addArguments(configuration.getCommandLineArguments());
            return new FirefoxDriver(options);
        }
    },
    @Deprecated(since = "0.6.8", forRemoval = true)
    IEXPLORE(false, true, Browser.IE, InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY, WebDriverManager::iedriver)
    {
        @SuppressWarnings("unchecked")
        @Override
        public void prepareCapabilities(MutableCapabilities desiredCapabilities)
        {
            Object options = desiredCapabilities.getCapability(InternetExplorerOptions.IE_OPTIONS);
            Map<String, Object> ieOptions;
            if (options == null)
            {
                ieOptions = new HashMap<>();
                desiredCapabilities.setCapability(InternetExplorerOptions.IE_OPTIONS, ieOptions);
            }
            else
            {
                ieOptions = (Map<String, Object>) options;
            }

            // Workaround for IExplore:
            // https://stackoverflow.com/questions/50287435/actions-movetoelement-not-working-on-ie-11
            ieOptions.put(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true);
        }

        @Override
        public WebDriver getWebDriver(DesiredCapabilities desiredCapabilities, WebDriverConfiguration configuration)
        {
            InternetExplorerOptions options = new InternetExplorerOptions(desiredCapabilities);
            prepareCapabilities(options);

            String[] switches = configuration.getCommandLineArguments();
            if (switches.length > 0)
            {
                options.addCommandSwitches(switches);
                options.useCreateProcessApiToLaunchIe();
            }
            return new InternetExplorerDriver(options);
        }
    },
    CHROME(true, true, Browser.CHROME, ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, WebDriverManager::chromedriver)
    {
        @Override
        public WebDriver getWebDriver(DesiredCapabilities desiredCapabilities, WebDriverConfiguration configuration)
        {
            ChromeOptions options = new ChromeOptions().merge(desiredCapabilities);
            fillOptions(options, configuration);
            return new ChromeDriver(options);
        }
    },
    SAFARI(false, false, Browser.SAFARI, SafariDriverService.SAFARI_DRIVER_EXE_PROPERTY, WebDriverManager::safaridriver)
    {
        @Override
        public WebDriver getWebDriver(DesiredCapabilities desiredCapabilities, WebDriverConfiguration configuration)
        {
            return new SafariDriver(SafariOptions.fromCapabilities(desiredCapabilities));
        }
    },
    OPERA(true, true, Browser.OPERA, ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, WebDriverManager::operadriver)
    {
        @Override
        public WebDriver getWebDriver(DesiredCapabilities desiredCapabilities, WebDriverConfiguration configuration)
        {
            ChromeOptions options = new ChromeOptions().merge(desiredCapabilities);
            fillOptions(options, configuration);
            return new ChromeDriver(options);
        }
    },
    EDGE(true, true, Browser.EDGE, EdgeDriverService.EDGE_DRIVER_EXE_PROPERTY, WebDriverManager::edgedriver)
    {
        @Override
        public WebDriver getWebDriver(DesiredCapabilities desiredCapabilities, WebDriverConfiguration configuration)
        {
            EdgeOptions options = new EdgeOptions().merge(desiredCapabilities);
            fillOptions(options, configuration);
            return new EdgeDriver(options);
        }
    };

    private final boolean binaryPathSupported;
    private final boolean commandLineArgumentsSupported;
    private final Browser browser;
    private final String driverExePropertyName;
    private final Supplier<WebDriverManager> webDriverManagerSupplier;

    WebDriverType(boolean binaryPathSupported, boolean commandLineArgumentsSupported, Browser browser,
            String driverExePropertyName, Supplier<WebDriverManager> webDriverManagerSupplier)
    {
        this.binaryPathSupported = binaryPathSupported;
        this.commandLineArgumentsSupported = commandLineArgumentsSupported;
        this.browser = browser;
        this.driverExePropertyName = driverExePropertyName;
        this.webDriverManagerSupplier = webDriverManagerSupplier;
    }

    public void prepareCapabilities(@SuppressWarnings("unused") MutableCapabilities desiredCapabilities)
    {
        // Nothing to do by default
    }

    public abstract WebDriver getWebDriver(DesiredCapabilities desiredCapabilities,
            WebDriverConfiguration configuration);

    void setDriverExecutablePath(Optional<String> driverExecutablePath)
    {
        driverExecutablePath.ifPresentOrElse(
            path -> System.setProperty(driverExePropertyName, path),
            () -> webDriverManagerSupplier.get().setup()
        );
    }

    private static <T extends ChromiumOptions<T>> void fillOptions(ChromiumOptions<T> options,
            WebDriverConfiguration configuration)
    {
        configuration.getBinaryPath().ifPresent(options::setBinary);
        options.addArguments(configuration.getCommandLineArguments());
        configuration.getExperimentalOptions().forEach(options::setExperimentalOption);
    }

    public boolean isBinaryPathSupported()
    {
        return binaryPathSupported;
    }

    public boolean isCommandLineArgumentsSupported()
    {
        return commandLineArgumentsSupported;
    }

    public Browser getBrowser()
    {
        return browser;
    }
}
