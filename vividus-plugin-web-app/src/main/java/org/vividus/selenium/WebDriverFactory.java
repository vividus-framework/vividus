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

package org.vividus.selenium;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.common.base.Suppliers;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.selenium.driver.TextFormattingWebDriver;
import org.vividus.selenium.manager.WebDriverManager;
import org.vividus.util.json.IJsonUtils;
import org.vividus.util.property.IPropertyParser;

public class WebDriverFactory implements IWebDriverFactory
{
    private static final Set<String> GENERIC_CAPABILITIES = Stream.of(CapabilityType.class.getFields())
            .filter(f -> f.getType().equals(String.class))
            .map(f ->
            {
                try
                {
                    return f.get(null);
                }
                catch (IllegalAccessException e)
                {
                    throw new IllegalStateException(e);
                }
            })
            .map(String.class::cast)
            .collect(Collectors.toSet());

    private static final String COMMAND_LINE_ARGUMENTS = "command-line-arguments";

    private static final String SELENIUM_GRID_PROPERTY_PREFIX = "selenium.grid.capabilities.";

    @Inject private IRemoteWebDriverFactory remoteWebDriverFactory;
    @Inject private ITimeoutConfigurer timeoutConfigurer;
    @Inject private IPropertyParser propertyParser;
    private WebDriverType webDriverType;
    private URL remoteDriverUrl;

    private IJsonUtils jsonUtils;

    private final Supplier<DesiredCapabilities> seleniumGridDesiredCapabilities = Suppliers.memoize(
        () -> new DesiredCapabilities(propertyParser.getPropertyValuesByPrefix(SELENIUM_GRID_PROPERTY_PREFIX)));

    private final Map<WebDriverType, WebDriverConfiguration> configurations = new ConcurrentHashMap<>();

    @Override
    public WebDriver getWebDriver(DesiredCapabilities desiredCapabilities)
    {
        return Optional.ofNullable(desiredCapabilities.getCapability(CapabilityType.BROWSER_NAME))
                       .map(String.class::cast)
                       .map(browserName -> getWebDriver(WebDriverType.valueOf(browserName), desiredCapabilities))
                       .orElseGet(() -> getWebDriver(this.webDriverType, desiredCapabilities));
    }

    private WebDriver getWebDriver(WebDriverType webDriverType, DesiredCapabilities desiredCapabilities)
    {
        WebDriverConfiguration configuration = getWebDriverConfiguration(webDriverType, true);
        return createWebDriver(webDriverType.getWebDriver(desiredCapabilities, configuration));
    }

    @Override
    public WebDriver getRemoteWebDriver(DesiredCapabilities desiredCapabilities)
    {
        DesiredCapabilities mergedDesiredCapabilities = new DesiredCapabilities(getSeleniumGridDesiredCapabilities())
                .merge(desiredCapabilities);
        WebDriverType webDriverType = WebDriverManager.detectType(mergedDesiredCapabilities);

        Capabilities capabilities = mergedDesiredCapabilities;
        if (webDriverType != null)
        {
            webDriverType.prepareCapabilities(mergedDesiredCapabilities);
            if (webDriverType == WebDriverType.CHROME)
            {
                WebDriverConfiguration configuration = getWebDriverConfiguration(webDriverType, false);
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments(configuration.getCommandLineArguments());
                configuration.getExperimentalOptions().forEach(chromeOptions::setExperimentalOption);
                capabilities = chromeOptions.merge(mergedDesiredCapabilities);
            }
        }
        return createWebDriver(remoteWebDriverFactory.getRemoteWebDriver(remoteDriverUrl, capabilities));
    }

    private WebDriver createWebDriver(WebDriver webDriver)
    {
        WebDriver driver = new TextFormattingWebDriver(webDriver);
        timeoutConfigurer.configure(driver.manage().timeouts());
        return driver;
    }

    private WebDriverConfiguration getWebDriverConfiguration(WebDriverType webDriverType, boolean localRun)
    {
        return configurations.computeIfAbsent(webDriverType, type ->
        {
            WebDriverConfiguration configuration = createWebDriverConfiguration(type);
            if (localRun)
            {
                webDriverType.setDriverExecutablePath(configuration.getDriverExecutablePath());
            }
            return configuration;
        });
    }

    @SuppressWarnings("unchecked")
    private WebDriverConfiguration createWebDriverConfiguration(WebDriverType webDriverType)
    {
        Optional<String> binaryPath = getPropertyValue("binary-path", webDriverType);
        if (binaryPath.isPresent() && !webDriverType.isBinaryPathSupported())
        {
            throw new UnsupportedOperationException("Configuring of binary-path is not supported for " + webDriverType);
        }

        Optional<String> commandLineArguments = getPropertyValue(COMMAND_LINE_ARGUMENTS, webDriverType);
        if (commandLineArguments.isPresent() && !webDriverType.isCommandLineArgumentsSupported())
        {
            throw new UnsupportedOperationException(
                    "Configuring of command-line-arguments is not supported for " + webDriverType);
        }

        WebDriverConfiguration configuration = new WebDriverConfiguration();
        configuration.setDriverExecutablePath(getPropertyValue("driver-executable-path", webDriverType));
        configuration.setBinaryPath(binaryPath);
        configuration.setCommandLineArguments(
                commandLineArguments.map(args -> StringUtils.split(args, ' ')).orElseGet(() -> new String[0]));
        getPropertyValue("experimental-options", webDriverType)
                .map(options -> jsonUtils.toObject(options, Map.class))
                .ifPresent(configuration::setExperimentalOptions);
        return configuration;
    }

    private Optional<String> getPropertyValue(String propertyKey, WebDriverType webDriverType)
    {
        return Optional.ofNullable(propertyParser.getPropertyValue("web.driver." + webDriverType + "." + propertyKey));
    }

    @Override
    public DesiredCapabilities getSeleniumGridDesiredCapabilities()
    {
        return seleniumGridDesiredCapabilities.get();
    }

    public void setWebDriverType(WebDriverType webDriverType)
    {
        this.webDriverType = webDriverType;
    }

    public void setRemoteDriverUrl(URL remoteDriverUrl)
    {
        this.remoteDriverUrl = remoteDriverUrl;
    }

    public void setJsonUtils(IJsonUtils jsonUtils)
    {
        this.jsonUtils = jsonUtils;
    }
}
