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

import java.util.function.BinaryOperator;

import org.vividus.ui.screenshot.AbstractScreenshotParametersFactory;
import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.ui.screenshot.ScreenshotParameters;

public class MobileAppScreenshotParametersFactory
        extends AbstractScreenshotParametersFactory<ScreenshotConfiguration, ScreenshotParameters>
{
    @Override
    protected ScreenshotConfiguration createScreenshotConfiguration()
    {
        return new ScreenshotConfiguration();
    }

    @Override
    protected ScreenshotParameters createScreenshotParameters()
    {
        return new ScreenshotParameters();
    }

    @Override
    protected BinaryOperator<ScreenshotConfiguration> getConfigurationMerger()
    {
        return (currentConfig, defaultConfig) ->
        {
            if (currentConfig.getShootingStrategy().isEmpty())
            {
                currentConfig.setShootingStrategy(defaultConfig.getShootingStrategy());
            }
            return currentConfig;
        };
    }

    @Override
    protected void configure(ScreenshotConfiguration config, ScreenshotParameters parameters)
    {
         // No changes are needed in ScreenshotParameters
    }
}
