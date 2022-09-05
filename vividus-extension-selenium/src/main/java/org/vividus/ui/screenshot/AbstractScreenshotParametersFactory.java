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

package org.vividus.ui.screenshot;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.screenshot.IgnoreStrategy;
import org.vividus.ui.action.search.Locator;
import org.vividus.util.property.PropertyMappedCollection;

public abstract class AbstractScreenshotParametersFactory<C extends ScreenshotConfiguration,
        P extends ScreenshotParameters> implements ScreenshotParametersFactory<C>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractScreenshotParametersFactory.class);

    private PropertyMappedCollection<C> screenshotConfigurations;
    private String shootingStrategy;
    private Map<IgnoreStrategy, Set<Locator>> ignoreStrategies;

    @Override
    public Optional<ScreenshotParameters> create()
    {
        return getDefaultConfiguration().map(this::createParameters);
    }

    @Override
    public P create(Optional<C> screenshotConfiguration, String sourceKey, Map<IgnoreStrategy, Set<Locator>> ignores)
    {
        Optional<C> defaultConfiguration = getDefaultConfiguration();

        return screenshotConfiguration.map(currentConfig ->
        {
            patchIgnores(sourceKey, currentConfig, ignores);

            C configuration = defaultConfiguration
                    .map(defaultConfig -> getConfigurationMerger().apply(currentConfig, defaultConfig))
                    .orElse(currentConfig);
            return createParameters(configuration);
        }).orElseGet(() -> {
            C configuration = defaultConfiguration.orElseGet(this::createScreenshotConfiguration);
            return createParameters(configuration, ignores);
        });
    }

    protected abstract C createScreenshotConfiguration();

    protected abstract P createScreenshotParameters();

    protected abstract BinaryOperator<C> getConfigurationMerger();

    protected abstract void configure(C config, P parameters);

    private void patchIgnores(String sourceKey, ScreenshotConfiguration screenshotConfiguration,
            Map<IgnoreStrategy, Set<Locator>> ignores)
    {
        Set<Locator> elementsToIgnore = getIgnoresFromOneOf(screenshotConfiguration.getElementsToIgnore(), sourceKey,
                ignores.get(IgnoreStrategy.ELEMENT));
        screenshotConfiguration.setElementsToIgnore(elementsToIgnore);

        Set<Locator> areasToIgnore = getIgnoresFromOneOf(screenshotConfiguration.getAreasToIgnore(), sourceKey,
                ignores.get(IgnoreStrategy.AREA));
        screenshotConfiguration.setAreasToIgnore(areasToIgnore);
    }

    private Set<Locator> getIgnoresFromOneOf(Set<Locator> configIgnores, String sourceKey, Set<Locator> source)
    {
        if (!source.isEmpty())
        {
            Validate.isTrue(configIgnores.isEmpty(), "The elements and areas to ignore must be passed "
                    + "either through screenshot configuration or %s", sourceKey);
            LOGGER.atWarn().addArgument(sourceKey).log("The passing of elements and areas to ignore through {}"
                    + " is deprecated, please use screenshot configuration instead");
            return source;
        }
        return configIgnores;
    }

    private Optional<C> getDefaultConfiguration()
    {
        return screenshotConfigurations.getNullable(shootingStrategy);
    }

    private P createParameters(C configuration)
    {
        Map<IgnoreStrategy, Set<Locator>> stepIgnores = Map.of(
            IgnoreStrategy.ELEMENT, configuration.getElementsToIgnore(),
            IgnoreStrategy.AREA, configuration.getAreasToIgnore()
        );

        return createParameters(configuration, stepIgnores);
    }

    private P createParameters(C configuration, Map<IgnoreStrategy, Set<Locator>> stepIgnores)
    {
        P parameters = createScreenshotParameters();
        parameters.setShootingStrategy(configuration.getShootingStrategy());
        parameters.setCutTop(configuration.getCutTop());
        parameters.setCutBottom(configuration.getCutBottom());
        parameters.setCutLeft(configuration.getCutLeft());
        parameters.setCutRight(configuration.getCutRight());
        Map<IgnoreStrategy, Set<Locator>> allIgnores = new EnumMap<>(IgnoreStrategy.class);

        for (Map.Entry<IgnoreStrategy, Set<Locator>> globalIgnores : ignoreStrategies.entrySet())
        {
            IgnoreStrategy ignoreStrategy = globalIgnores.getKey();
            Set<Locator> ignore = Stream.concat(
                    getLocatorsStream(globalIgnores.getValue()),
                    getLocatorsStream(stepIgnores.get(ignoreStrategy)))
                    .collect(Collectors.toSet());
            allIgnores.put(ignoreStrategy, ignore);
        }
        parameters.setIgnoreStrategies(allIgnores);

        configure(configuration, parameters);

        return parameters;
    }

    protected int ensureValidCutSize(int value, String key)
    {
        Validate.isTrue(value >= 0, "The %s to cut must be greater than or equal to zero", key);
        return value;
    }

    private Stream<Locator> getLocatorsStream(Set<Locator> locatorsSet)
    {
        return Optional.ofNullable(locatorsSet).stream().flatMap(Collection::stream);
    }

    public void setShootingStrategy(String shootingStrategy)
    {
        this.shootingStrategy = shootingStrategy;
    }

    public void setScreenshotConfigurations(PropertyMappedCollection<C> screenshotConfigurations)
    {
        this.screenshotConfigurations = screenshotConfigurations;
    }

    public void setIgnoreStrategies(Map<IgnoreStrategy, Set<Locator>> ignoreStrategies)
    {
        this.ignoreStrategies = ignoreStrategies;
    }
}
