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

package org.vividus.selenium.mobileapp.screenshot;

import java.util.Optional;

import org.vividus.selenium.mobileapp.MobileAppWebDriverManager;
import org.vividus.selenium.mobileapp.screenshot.strategies.MobileViewportShootingStrategy;
import org.vividus.selenium.screenshot.AbstractAshotFactory;
import org.vividus.selenium.screenshot.ScreenshotCropper;
import org.vividus.ui.screenshot.ScreenshotParameters;

import pazone.ashot.AShot;
import pazone.ashot.ShootingStrategies;
import pazone.ashot.ShootingStrategy;
import pazone.ashot.coordinates.CoordsProvider;

public class MobileAppAshotFactory extends AbstractAshotFactory<ScreenshotParameters>
{
    private final MobileAppWebDriverManager mobileAppWebDriverManager;
    private final CoordsProvider coordsProvider;
    private boolean appendBottomNavigationBarOnAndroid;

    public MobileAppAshotFactory(ScreenshotCropper screenshotCropper, MobileAppWebDriverManager genericWebDriverManager,
            CoordsProvider coordsProvider)
    {
        super(screenshotCropper);
        this.mobileAppWebDriverManager = genericWebDriverManager;
        this.coordsProvider = coordsProvider;
    }

    @SuppressWarnings("checkstyle:Indentation")
    @Override
    public AShot create(Optional<ScreenshotParameters> screenshotParameters)
    {
        String strategyName = screenshotParameters.flatMap(ScreenshotParameters::getShootingStrategy)
                .orElseGet(this::getScreenshotShootingStrategy);

        ShootingStrategy strategy = switch (strategyName)
        {
            case "VIEWPORT" -> getViewportStrategy();
            case "FULL_SCREEN" -> ShootingStrategies.simple();
            default -> throw new IllegalArgumentException(
                    String.format("Unknown shooting strategy with the name: %s", strategyName));
        };

        if (screenshotParameters.isPresent())
        {
            strategy = decorateWithCropping(strategy, screenshotParameters.get());
        }

        return new AShot().shootingStrategy(strategy).coordsProvider(coordsProvider);
    }

    private ShootingStrategy getViewportStrategy()
    {
        if (appendBottomNavigationBarOnAndroid && mobileAppWebDriverManager.isAndroid())
        {
            int statusBarSize = mobileAppWebDriverManager.getStatusBarSize();
            return decorateWithFixedCutStrategy(ShootingStrategies.simple(), statusBarSize, 0);
        }
        return new MobileViewportShootingStrategy();
    }

    public void setAppendBottomNavigationBarOnAndroid(boolean appendBottomNavigationBarOnAndroid)
    {
        this.appendBottomNavigationBarOnAndroid = appendBottomNavigationBarOnAndroid;
    }
}
