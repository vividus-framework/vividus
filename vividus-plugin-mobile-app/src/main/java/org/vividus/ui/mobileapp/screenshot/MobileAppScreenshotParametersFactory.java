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

package org.vividus.ui.mobileapp.screenshot;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;

import org.vividus.selenium.screenshot.IgnoreStrategy;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.screenshot.AbstractScreenshotParametersFactory;
import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.ui.screenshot.ScreenshotParameters;

public class MobileAppScreenshotParametersFactory
        extends AbstractScreenshotParametersFactory<ScreenshotConfiguration, ScreenshotParameters>
{
    @Override
    public Optional<ScreenshotParameters> create(Optional<ScreenshotConfiguration> screenshotConfiguration)
    {
        return getScreenshotConfiguration(screenshotConfiguration, getConfigurationMerger())
                .map(this::createWithBaseConfiguration);
    }

    @Override
    public Optional<ScreenshotParameters> create(Map<IgnoreStrategy, Set<Locator>> ignores)
    {
        ScreenshotConfiguration configuration = getDefaultConfiguration().orElseGet(ScreenshotConfiguration::new);
        return Optional.of(createWithBaseConfiguration(configuration, ignores));
    }

    @Override
    protected ScreenshotParameters createScreenshotParameters()
    {
        return new ScreenshotParameters();
    }

    private BinaryOperator<ScreenshotConfiguration> getConfigurationMerger()
    {
        return (p, b) ->
        {
            if (p.getNativeFooterToCut() == 0)
            {
                p.setNativeFooterToCut(b.getNativeFooterToCut());
            }
            if (p.getShootingStrategy().isEmpty())
            {
                p.setShootingStrategy(b.getShootingStrategy());
            }
            return p;
        };
    }
}
