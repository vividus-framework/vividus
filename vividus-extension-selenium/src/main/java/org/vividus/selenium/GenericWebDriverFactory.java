/*
 * Copyright 2019-2025 the original author or authors.
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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.driver.TextFormattingWebDriver;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.IPropertyParser;

public class GenericWebDriverFactory implements IGenericWebDriverFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericWebDriverFactory.class);

    private static final String SELENIUM_GRID_PROPERTY_PREFIX = "selenium.grid.capabilities.";
    private static final String LOCAL_DRIVER_PROPERTY_PREFIX = "selenium.capabilities.";

    private final IRemoteWebDriverFactory remoteWebDriverFactory;
    private final IPropertyParser propertyParser;
    private final JsonUtils jsonUtils;
    private final Optional<Set<DesiredCapabilitiesAdjuster>> desiredCapabilitiesAdjusters;
    private final Optional<SeleniumConfigurationValidator> seleniumConfigurationValidator;

    private final Supplier<DesiredCapabilities> seleniumGridDesiredCapabilities = Suppliers.memoize(
        () -> getCapabilitiesByPrefix(SELENIUM_GRID_PROPERTY_PREFIX));

    private final Supplier<DesiredCapabilities> localDriverDesiredCapabilities = Suppliers.memoize(
        () -> getCapabilitiesByPrefix(LOCAL_DRIVER_PROPERTY_PREFIX));

    private final LoadingCache<Boolean, DesiredCapabilities> webDriverCapabilities = CacheBuilder.newBuilder()
            .build(new CacheLoader<>()
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

    public GenericWebDriverFactory(IRemoteWebDriverFactory remoteWebDriverFactory, IPropertyParser propertyParser,
            JsonUtils jsonUtils, Optional<Set<DesiredCapabilitiesAdjuster>> desiredCapabilitiesAdjusters,
            Optional<SeleniumConfigurationValidator> seleniumConfigurationValidator)
    {
        this.remoteWebDriverFactory = remoteWebDriverFactory;
        this.propertyParser = propertyParser;
        this.jsonUtils = jsonUtils;
        this.desiredCapabilitiesAdjusters = desiredCapabilitiesAdjusters;
        this.seleniumConfigurationValidator = seleniumConfigurationValidator;
    }

    private DesiredCapabilities getCapabilitiesByPrefix(String prefix)
    {
        return propertyParser.getPropertyValuesTreeByPrefix(prefix)
                             .entrySet()
                             .stream()
                             .map(e -> e.getValue() instanceof String valueAsString
                                 ? Map.entry(e.getKey(), adjustType(valueAsString))
                                 : e)
                             .collect(Collectors.collectingAndThen(toMap(Entry::getKey, Entry::getValue),
                                 DesiredCapabilities::new));
    }

    @Override
    public WebDriver createWebDriver(DesiredCapabilities desiredCapabilities)
    {
        validateConfigurationProperties();
        DesiredCapabilities mergedDesiredCapabilities = getWebDriverCapabilities(false, desiredCapabilities);
        DesiredCapabilities updatedDesiredCapabilities = updateDesiredCapabilities(mergedDesiredCapabilities);
        return createWebDriver(() -> remoteWebDriverFactory.getRemoteWebDriver(updatedDesiredCapabilities),
                updatedDesiredCapabilities);
    }

    protected WebDriver createWebDriver(Supplier<WebDriver> webDriver, DesiredCapabilities sessionRequestCapabilities)
    {
        logCapabilities(sessionRequestCapabilities, "Requested capabilities:\n{}");
        TextFormattingWebDriver driver = new TextFormattingWebDriver(webDriver.get());
        logCapabilities(driver.getCapabilities(), "Session capabilities:\n{}");
        return driver;
    }

    private void logCapabilities(Capabilities capabilities, String message)
    {
        LOGGER.atInfo()
              .addArgument(() -> jsonUtils.toPrettyJson(capabilities.asMap()))
              .log(message);
    }

    protected DesiredCapabilities updateDesiredCapabilities(DesiredCapabilities desiredCapabilities)
    {
        desiredCapabilitiesAdjusters.ifPresent(
                adjusters -> adjusters.forEach(adjuster -> adjuster.adjust(desiredCapabilities)));
        return desiredCapabilities;
    }

    protected void validateConfigurationProperties()
    {
        seleniumConfigurationValidator.ifPresent(SeleniumConfigurationValidator::validate);
    }

    protected DesiredCapabilities getWebDriverCapabilities(boolean localRun, DesiredCapabilities toMerge)
    {
        return merge(webDriverCapabilities.getUnchecked(localRun), toMerge);
    }

    protected IPropertyParser getPropertyParser()
    {
        return propertyParser;
    }

    protected JsonUtils getJsonUtils()
    {
        return jsonUtils;
    }
}
