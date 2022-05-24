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

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;
import org.vividus.util.property.PropertyMappedCollection;

public abstract class AbstractScreenshotParametersFactory<C extends ScreenshotConfiguration>
        implements ScreenshotParametersFactory<C>
{
    private PropertyMappedCollection<C> screenshotConfigurations;
    private String shootingStrategy;

    protected Optional<C> getScreenshotConfiguration(Optional<C> screenshotConfiguration, BinaryOperator<C> merger)
    {
        Optional<C> defaultConfiguration = screenshotConfigurations.getNullable(shootingStrategy);
        if (screenshotConfiguration.isEmpty())
        {
            return defaultConfiguration;
        }
        else if (defaultConfiguration.isPresent())
        {
            return Optional.of(merger.apply(screenshotConfiguration.get(), defaultConfiguration.get()));
        }
        return screenshotConfiguration;
    }

    protected int ensureValidCutSize(int value, String key)
    {
        Validate.isTrue(value >= 0, "The %s to cut must be greater than or equal to zero", key);
        return value;
    }

    protected <R extends ScreenshotParameters> R createWithBaseConfiguration(ScreenshotConfiguration configuration,
            Supplier<R> parametersFactory)
    {
        R parameters = parametersFactory.get();
        parameters.setShootingStrategy(configuration.getShootingStrategy());
        parameters.setNativeFooterToCut(ensureValidCutSize(configuration.getNativeFooterToCut(), "native footer"));
        return parameters;
    }

    public void setShootingStrategy(String shootingStrategy)
    {
        this.shootingStrategy = shootingStrategy;
    }

    public void setScreenshotConfigurations(PropertyMappedCollection<C> screenshotConfigurations)
    {
        this.screenshotConfigurations = screenshotConfigurations;
    }
}
