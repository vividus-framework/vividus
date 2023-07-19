/*
 * Copyright 2019-2023 the original author or authors.
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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.proxy.IProxy;
import org.vividus.selenium.manager.WebDriverManager;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.IPropertyParser;

public class WebDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory
{
    private static final String COMMAND_LINE_ARGUMENTS = "command-line-arguments";

    private final TimeoutConfigurer timeoutConfigurer;
    private final IProxy proxy;
    private final WebDriverStartContext webDriverStartContext;
    private WebDriverType webDriverType;

    private final Map<WebDriverType, WebDriverConfiguration> configurations = new ConcurrentHashMap<>();

    public WebDriverFactory(IRemoteWebDriverFactory remoteWebDriverFactory, IPropertyParser propertyParser,
            JsonUtils jsonUtils, TimeoutConfigurer timeoutConfigurer, IProxy proxy,
            WebDriverStartContext webDriverStartContext,
            Optional<Set<DesiredCapabilitiesAdjuster>> desiredCapabilitiesAdjusters)
    {
        super(remoteWebDriverFactory, propertyParser, jsonUtils, desiredCapabilitiesAdjusters);
        this.timeoutConfigurer = timeoutConfigurer;
        this.proxy = proxy;
        this.webDriverStartContext = webDriverStartContext;
    }

    @Override
    public WebDriver getWebDriver(DesiredCapabilities desiredCapabilities)
    {
        WebDriverType driverType =
            Optional.ofNullable(desiredCapabilities.getCapability(CapabilityType.BROWSER_NAME))
                    .map(String.class::cast)
                    .map(WebDriverType::valueOf)
                    .orElse(this.webDriverType);
        boolean localRun = true;
        WebDriverConfiguration configuration = getWebDriverConfiguration(driverType, localRun);
        configureProxy(driverType, desiredCapabilities);
        DesiredCapabilities webDriverCapabilities = getWebDriverCapabilities(localRun, desiredCapabilities);
        return createWebDriver(() -> driverType.getWebDriver(webDriverCapabilities, configuration),
                webDriverCapabilities);
    }

    @Override
    protected DesiredCapabilities updateDesiredCapabilities(DesiredCapabilities desiredCapabilities)
    {
        DesiredCapabilities toUpdate = super.updateDesiredCapabilities(desiredCapabilities);
        Capabilities capabilities = Stream.of(WebDriverType.values())
                .filter(type -> WebDriverManager.isBrowser(toUpdate, type.getBrowser()))
                .findFirst()
                .map(type ->
                {
                    type.prepareCapabilities(toUpdate);
                    configureProxy(type, toUpdate);
                    if (type == WebDriverType.CHROME)
                    {
                        WebDriverConfiguration configuration = getWebDriverConfiguration(type, false);
                        ChromeOptions chromeOptions = new ChromeOptions();
                        chromeOptions.addArguments(configuration.getCommandLineArguments());
                        configuration.getExperimentalOptions().forEach(chromeOptions::setExperimentalOption);
                        return chromeOptions.merge(toUpdate);
                    }
                    return toUpdate;
                })
                .orElse(desiredCapabilities);
        return new DesiredCapabilities(capabilities);
    }

    @Override
    protected void configureWebDriver(WebDriver webDriver)
    {
        timeoutConfigurer.configure(webDriver.manage().timeouts());
    }

    private void configureProxy(WebDriverType webDriverType, DesiredCapabilities desiredCapabilities)
    {
        if (proxy.isStarted() && supportsAcceptInsecureCerts(webDriverType))
        {
            desiredCapabilities.setAcceptInsecureCerts(true);
        }
    }

    private boolean supportsAcceptInsecureCerts(WebDriverType webDriverType)
    {
        return webDriverType != WebDriverType.SAFARI && webDriverType != WebDriverType.IEXPLORE;
    }

    private WebDriverConfiguration getWebDriverConfiguration(WebDriverType webDriverType, boolean localRun)
    {
        WebDriverConfiguration defaultConfiguration = configurations.computeIfAbsent(webDriverType, type ->
        {
            WebDriverConfiguration configuration = createWebDriverConfiguration(type);
            if (localRun)
            {
                webDriverType.setDriverExecutablePath(configuration.getDriverExecutablePath());
            }
            return configuration;
        });
        String overrideCommandLineArguments = webDriverStartContext.get(
                WebDriverStartParameters.COMMAND_LINE_ARGUMENTS);
        if (overrideCommandLineArguments != null)
        {
            checkCommandLineArgumentsSupported(webDriverType);
            WebDriverConfiguration configuration = new WebDriverConfiguration();
            configuration.setBinaryPath(defaultConfiguration.getBinaryPath());
            configuration.setDriverExecutablePath(defaultConfiguration.getDriverExecutablePath());
            configuration.setExperimentalOptions(defaultConfiguration.getExperimentalOptions());
            configuration.setCommandLineArguments(splitCliArguments(Optional.of(overrideCommandLineArguments)));
            return configuration;
        }
        return defaultConfiguration;
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
        if (commandLineArguments.isPresent())
        {
            checkCommandLineArgumentsSupported(webDriverType);
        }

        WebDriverConfiguration configuration = new WebDriverConfiguration();
        configuration.setDriverExecutablePath(getPropertyValue("driver-executable-path", webDriverType));
        configuration.setBinaryPath(binaryPath);
        configuration.setCommandLineArguments(splitCliArguments(commandLineArguments));
        getPropertyValue("experimental-options", webDriverType)
                .map(options -> getJsonUtils().toObject(options, Map.class))
                .ifPresent(configuration::setExperimentalOptions);
        return configuration;
    }

    private static String[] splitCliArguments(Optional<String> commandLineArguments)
    {
        return commandLineArguments.map(args -> StringUtils.split(args, ' '))
                .orElseGet(() -> new String[0]);
    }

    private void checkCommandLineArgumentsSupported(WebDriverType webDriverType)
    {
        if (!webDriverType.isCommandLineArgumentsSupported())
        {
            throw new UnsupportedOperationException(
                    "Configuring of command-line-arguments is not supported for " + webDriverType);
        }
    }

    private Optional<String> getPropertyValue(String propertyKey, WebDriverType webDriverType)
    {
        return Optional.ofNullable(
                getPropertyParser().getPropertyValue("web.driver.%s.%s", webDriverType.toString().toLowerCase(),
                        propertyKey));
    }

    public void setWebDriverType(WebDriverType webDriverType)
    {
        this.webDriverType = webDriverType;
    }
}
