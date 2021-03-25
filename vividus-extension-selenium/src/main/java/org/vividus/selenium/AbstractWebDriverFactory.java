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

package org.vividus.selenium;

import static java.util.stream.Collectors.toMap;
import static org.vividus.selenium.DesiredCapabilitiesMerger.merge;
import static org.vividus.selenium.type.CapabilitiesValueTypeAdjuster.adjustType;

import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.driver.TextFormattingWebDriver;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.IPropertyParser;

public abstract class AbstractWebDriverFactory implements IGenericWebDriverFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWebDriverFactory.class);

    private static final String SELENIUM_GRID_PROPERTY_PREFIX = "selenium.grid.capabilities.";
    private static final String LOCAL_DRIVER_PROPERTY_PREFIX = "selenium.capabilities.";

    private final IRemoteWebDriverFactory remoteWebDriverFactory;
    private final IPropertyParser propertyParser;
    private final JsonUtils jsonUtils;

    private URL remoteDriverUrl;

    private final Supplier<DesiredCapabilities> seleniumGridDesiredCapabilities = Suppliers.memoize(
        () -> getCapabilitiesByPrefix(SELENIUM_GRID_PROPERTY_PREFIX));

    private final Supplier<DesiredCapabilities> localDriverDesiredCapabilities = Suppliers.memoize(
        () -> getCapabilitiesByPrefix(LOCAL_DRIVER_PROPERTY_PREFIX));

    private final LoadingCache<Boolean, DesiredCapabilities> webDriverCapabilities = CacheBuilder.newBuilder()
            .build(new CacheLoader<Boolean, DesiredCapabilities>()
            {
                @Override
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

    public AbstractWebDriverFactory(IRemoteWebDriverFactory remoteWebDriverFactory, IPropertyParser propertyParser,
            JsonUtils jsonUtils)
    {
        this.remoteWebDriverFactory = remoteWebDriverFactory;
        this.propertyParser = propertyParser;
        this.jsonUtils = jsonUtils;
    }

    private DesiredCapabilities getCapabilitiesByPrefix(String prefix)
    {
        return propertyParser.getPropertyValuesTreeByPrefix(prefix)
                             .entrySet()
                             .stream()
                             .map(e -> e.getValue() instanceof String
                                 ? Map.entry(e.getKey(), adjustType((String) e.getValue()))
                                 : e)
                             .collect(Collectors.collectingAndThen(toMap(Entry::getKey, Entry::getValue),
                                 DesiredCapabilities::new));
    }

    @Override
    public WebDriver getRemoteWebDriver(DesiredCapabilities desiredCapabilities)
    {
        DesiredCapabilities mergedDesiredCapabilities = getWebDriverCapabilities(false, desiredCapabilities);
        DesiredCapabilities updatedDesiredCapabilities = updateDesiredCapabilities(mergedDesiredCapabilities);
        return createWebDriver(() -> remoteWebDriverFactory.getRemoteWebDriver(remoteDriverUrl,
                updatedDesiredCapabilities), updatedDesiredCapabilities);
    }

    protected WebDriver createWebDriver(Supplier<WebDriver> webDriver, DesiredCapabilities sessionRequestCapabilities)
    {
        logCapabilities(sessionRequestCapabilities, "Requested capabilities:\n{}");
        WebDriver driver = new TextFormattingWebDriver(webDriver.get());
        configureWebDriver(driver);

        logCapabilities(WebDriverUtil.unwrap(driver, HasCapabilities.class).getCapabilities(),
                "Session capabilities:\n{}");
        return driver;
    }

    private void logCapabilities(Capabilities capabilities, String message)
    {
        LOGGER.atInfo()
              .addArgument(() -> jsonUtils.toPrettyJson(capabilities.asMap()))
              .log(message);
    }

    protected abstract DesiredCapabilities updateDesiredCapabilities(DesiredCapabilities desiredCapabilities);

    protected abstract void configureWebDriver(WebDriver webDriver);

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

    protected DesiredCapabilities getWebDriverCapabilities(boolean localRun, DesiredCapabilities toMerge)
    {
        return merge(getWebDriverCapabilities(localRun), toMerge);
    }

    protected IPropertyParser getPropertyParser()
    {
        return propertyParser;
    }

    protected JsonUtils getJsonUtils()
    {
        return jsonUtils;
    }

    public void setRemoteDriverUrl(URL remoteDriverUrl)
    {
        this.remoteDriverUrl = remoteDriverUrl;
    }
}
