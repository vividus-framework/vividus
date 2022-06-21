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
import org.vividus.selenium.screenshot.IgnoreStrategy;
import org.vividus.ui.action.search.Locator;
import org.vividus.util.property.PropertyMappedCollection;

public abstract class AbstractScreenshotParametersFactory<C extends ScreenshotConfiguration,
        P extends ScreenshotParameters> implements ScreenshotParametersFactory<C>
{
    private PropertyMappedCollection<C> screenshotConfigurations;
    private String shootingStrategy;
    private Map<IgnoreStrategy, Set<Locator>> ignoreStrategies;

    protected Optional<C> getDefaultConfiguration()
    {
        return screenshotConfigurations.getNullable(shootingStrategy);
    }

    protected Optional<C> getScreenshotConfiguration(Optional<C> screenshotConfiguration, BinaryOperator<C> merger)
    {
        Optional<C> defaultConfiguration = getDefaultConfiguration();
        if (screenshotConfiguration.isEmpty())
        {
            return defaultConfiguration;
        }
        return defaultConfiguration.map(c -> merger.apply(screenshotConfiguration.get(), c))
                .or(() -> screenshotConfiguration);
    }

    protected int ensureValidCutSize(int value, String key)
    {
        Validate.isTrue(value >= 0, "The %s to cut must be greater than or equal to zero", key);
        return value;
    }

    protected P createWithBaseConfiguration(ScreenshotConfiguration configuration)
    {
        Map<IgnoreStrategy, Set<Locator>> stepIgnores = Map.of(
            IgnoreStrategy.ELEMENT, configuration.getElementsToIgnore(),
            IgnoreStrategy.AREA, configuration.getAreasToIgnore()
        );

        return createWithBaseConfiguration(configuration, stepIgnores);
    }

    protected P createWithBaseConfiguration(ScreenshotConfiguration configuration,
            Map<IgnoreStrategy, Set<Locator>> stepIgnores)
    {
        P parameters = createScreenshotParameters();
        parameters.setShootingStrategy(configuration.getShootingStrategy());
        parameters.setNativeFooterToCut(ensureValidCutSize(configuration.getNativeFooterToCut(), "native footer"));

        Map<IgnoreStrategy, Set<Locator>> ignores = new EnumMap<>(IgnoreStrategy.class);

        for (Map.Entry<IgnoreStrategy, Set<Locator>> ignoreStrategy : ignoreStrategies.entrySet())
        {
            IgnoreStrategy cropStrategy = ignoreStrategy.getKey();
            Set<Locator> ignore = Stream.concat(
                    getLocatorsStream(ignoreStrategy.getValue()),
                    getLocatorsStream(stepIgnores.get(cropStrategy)))
                    .collect(Collectors.toSet());
            ignores.put(cropStrategy, ignore);
        }
        parameters.setIgnoreStrategies(ignores);

        return parameters;
    }

    protected abstract P createScreenshotParameters();

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
