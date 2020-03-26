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

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;

import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.driver.TextFormattingWebDriver;
import org.vividus.selenium.manager.WebDriverManager;
import org.vividus.util.json.IJsonUtils;
import org.vividus.util.property.IPropertyParser;

public class WebDriverFactory implements IWebDriverFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverFactory.class);

    private static final String COMMAND_LINE_ARGUMENTS = "command-line-arguments";

    private static final String SELENIUM_GRID_PROPERTY_PREFIX = "selenium.grid.capabilities.";
    private static final String LOCAL_DRIVER_PROPERTY_PREFIX = "selenium.capabilities.";

    @Inject private IRemoteWebDriverFactory remoteWebDriverFactory;
    @Inject private ITimeoutConfigurer timeoutConfigurer;
    @Inject private IPropertyParser propertyParser;
    private WebDriverType webDriverType;
    private URL remoteDriverUrl;

    private IJsonUtils jsonUtils;

    private final Supplier<DesiredCapabilities> seleniumGridDesiredCapabilities = Suppliers.memoize(
        () -> getCapabilitiesByPrefix(SELENIUM_GRID_PROPERTY_PREFIX));

    private final Supplier<DesiredCapabilities> localDriverDesiredCapabilities = Suppliers.memoize(
        () -> getCapabilitiesByPrefix(LOCAL_DRIVER_PROPERTY_PREFIX));

    private final LoadingCache<Boolean, DesiredCapabilities> webDriverCapabilities = CacheBuilder.newBuilder()
            .build(new CacheLoader<Boolean, DesiredCapabilities>()
            {
                public DesiredCapabilities load(Boolean local)
                {
                    DesiredCapabilities localCapabilities = localDriverDesiredCapabilities.get();
                    if (Boolean.TRUE.equals(local))
                    {
                        return localCapabilities;
                    }
                    return merge(localCapabilities, seleniumGridDesiredCapabilities.get());
                }
            });

    private final Map<WebDriverType, WebDriverConfiguration> configurations = new ConcurrentHashMap<>();

    private DesiredCapabilities getCapabilitiesByPrefix(String prefix)
    {
        return propertyParser.getPropertyValuesTreeByPrefix(prefix)
            .entrySet()
            .stream()
            .map(e -> isBoolean(e.getValue())
                ? Map.entry(e.getKey(), Boolean.parseBoolean((String) e.getValue()))
                : e)
            .collect(Collectors.collectingAndThen(toMap(Entry::getKey, Entry::getValue), DesiredCapabilities::new));
    }

    private boolean isBoolean(Object value)
    {
        return value instanceof String && equalsAnyIgnoreCase((String) value, "true", "false");
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
        return createWebDriver(driverType.getWebDriver(getWebDriverCapabilities(localRun, desiredCapabilities),
                configuration));
    }

    @Override
    public WebDriver getRemoteWebDriver(DesiredCapabilities desiredCapabilities)
    {
        DesiredCapabilities mergedDesiredCapabilities = getWebDriverCapabilities(false, desiredCapabilities);
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

        LOGGER.atInfo()
              .addArgument(() -> jsonUtils
                      .toPrettyJson(WebDriverUtil.unwrap(driver, HasCapabilities.class).getCapabilities().asMap()))
              .log("Session capabilities:\n{}");
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

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(String capabilityName, boolean localRun)
    {
        return (T) getWebDriverCapabilities(localRun).getCapability(capabilityName);
    }

    private DesiredCapabilities getWebDriverCapabilities(boolean localRun)
    {
        return webDriverCapabilities.getUnchecked(localRun);
    }

    private DesiredCapabilities getWebDriverCapabilities(boolean localRun, DesiredCapabilities toMerge)
    {
        return merge(getWebDriverCapabilities(localRun), toMerge);
    }

    private DesiredCapabilities merge(DesiredCapabilities base, DesiredCapabilities toMerge)
    {
        return new DesiredCapabilities(base).merge(toMerge);
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
