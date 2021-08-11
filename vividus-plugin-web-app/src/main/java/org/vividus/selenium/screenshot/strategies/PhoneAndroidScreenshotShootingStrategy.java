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

package org.vividus.selenium.screenshot.strategies;

import static org.vividus.selenium.screenshot.WebScreenshotConfiguration.SCROLL_TIMEOUT;

import java.awt.Rectangle;

import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

/**
 * @deprecated will be removed in favor of properties based configuration
 */
@Deprecated(forRemoval = true, since = "0.3.8")
public class PhoneAndroidScreenshotShootingStrategy implements ScreenshotShootingStrategy
{
    private static final int X = 0;
    private static final int Y_PORTRAIT = 116;
    private static final int Y_LANDSCAPE = 106;
    private static final int WIDTH_PORTRAIT = 480;
    private static final int HEIGHT_PORTRAIT = 800;

    @Override
    public ShootingStrategy getDecoratedShootingStrategy(ShootingStrategy shootingStrategy,
            boolean landscape, String deviceName)
    {
        if (landscape)
        {
            getDecoratedAndroidShootingStrategy(shootingStrategy, X, Y_LANDSCAPE, HEIGHT_PORTRAIT,
                    WIDTH_PORTRAIT);
        }
        return getDecoratedAndroidShootingStrategy(shootingStrategy, X, Y_PORTRAIT,
                WIDTH_PORTRAIT, HEIGHT_PORTRAIT);
    }

    private static ShootingStrategy getDecoratedAndroidShootingStrategy(ShootingStrategy shootingStrategy, int x,
            int y, int width, int height)
    {
        Rectangle targetArea = new Rectangle(x, y, width - x, height - y);
        ShootingTargetAreaStrategy shootingTargetAreaStrategy = new ShootingTargetAreaStrategy(shootingStrategy,
                targetArea);
        return new ViewportWithCorrectionPastingDecorator(shootingTargetAreaStrategy).withCorrectedWidth(width - x)
                .withCorrectedHeight(height - y).withScrollTimeout(SCROLL_TIMEOUT);
    }
}
