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

package org.vividus.selenium.mobileapp.screenshot;

import java.util.Optional;

import org.vividus.selenium.mobileapp.MobileAppWebDriverManager;
import org.vividus.selenium.mobileapp.screenshot.util.CoordsUtil;
import org.vividus.selenium.screenshot.AbstractAshotFactory;
import org.vividus.selenium.screenshot.ScreenshotConfiguration;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.coordinates.CoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

public class MobileAppAshotFactory extends AbstractAshotFactory<ScreenshotConfiguration>
{
    private final MobileAppWebDriverManager mobileAppWebDriverManager;
    private final CoordsProvider coordsProvider;
    private boolean downscale;

    public MobileAppAshotFactory(MobileAppWebDriverManager genericWebDriverManager, CoordsProvider coordsProvider)
    {
        this.mobileAppWebDriverManager = genericWebDriverManager;
        this.coordsProvider = coordsProvider;
    }

    @Override
    public AShot create(Optional<ScreenshotConfiguration> screenshotConfiguration)
    {
        ScreenshotConfiguration ashotConfig = getAshotConfiguration(screenshotConfiguration, (c, b) -> {
            if (c.getNativeFooterToCut() == 0)
            {
                c.setNativeFooterToCut(b.getNativeFooterToCut());
            }
            if (c.getShootingStrategy().isEmpty())
            {
                c.setShootingStrategy(b.getShootingStrategy());
            }
            return c;
        }).get();
        ShootingStrategy strategy = getStrategyBy(ashotConfig.getShootingStrategy().get())
            .getDecoratedShootingStrategy(getBaseShootingStrategy());
        int statusBarSize = mobileAppWebDriverManager.getStatusBarSize();
        if (!downscale)
        {
            statusBarSize = CoordsUtil.multiply(statusBarSize, getDpr());
        }
        strategy = configureNativePartialsToCut(statusBarSize, ashotConfig, strategy);
        return new AShot().shootingStrategy(strategy).coordsProvider(coordsProvider);
    }

    @Override
    protected ShootingStrategy getBaseShootingStrategy()
    {
        return downscale ? super.getBaseShootingStrategy() : ShootingStrategies.simple();
    }

    @Override
    protected double getDpr()
    {
        return mobileAppWebDriverManager.getDpr();
    }

    public void setDownscale(boolean downscale)
    {
        this.downscale = downscale;
    }
}
