/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.selenium.screenshot;

import static org.vividus.selenium.screenshot.ScreenshotConfiguration.SCROLL_TIMEOUT;
import static ru.yandex.qatools.ashot.shooting.ShootingStrategies.viewportPasting;

import java.awt.Rectangle;

import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

public enum ScreenshotShootingStrategy
{
    SIMPLE
    {
        @Override
        public ShootingStrategy getDecoratedShootingStrategy(ShootingStrategy shootingStrategy,
                boolean viewportScreenshot, boolean landscape, String deviceName)
        {
            return viewportScreenshot ? new ViewportShootingStrategy(shootingStrategy) : shootingStrategy;
        }
    },
    VIEWPORT_PASTING
    {
        @Override
        public ShootingStrategy getDecoratedShootingStrategy(ShootingStrategy shootingStrategy,
                boolean viewportScreenshot, boolean landscape, String deviceName)
        {
            return viewportScreenshot ? shootingStrategy : viewportPasting(shootingStrategy, SCROLL_TIMEOUT);
        }
    },
    DEVICE_DEPENDENT
    {
        @Override
        public ShootingStrategy getDecoratedShootingStrategy(ShootingStrategy shootingStrategy,
                boolean viewportScreenshot, boolean landscape, String deviceName)
        {
            return Device.getByDeviceName(deviceName).decorate(landscape, viewportScreenshot);
        }
    },
    PHONE_ANDROID_PORTRAIT
    {
        private static final int WINDOW_X = 0;
        private static final int WINDOW_Y = 116;
        private static final int WINDOW_WIDTH = 480;
        private static final int WINDOW_HEIGHT = 800;

        @Override
        public ShootingStrategy getDecoratedShootingStrategy(ShootingStrategy shootingStrategy,
                boolean viewportScreenshot, boolean landscape, String deviceName)
        {
            if (landscape)
            {
                return ScreenshotShootingStrategy.PHONE_ANDROID_LANDSCAPE.getDecoratedShootingStrategy(
                        shootingStrategy, viewportScreenshot, true, deviceName);
            }
            return getDecoratedAndroidShootingStrategy(shootingStrategy, WINDOW_X, WINDOW_Y, WINDOW_WIDTH,
                    WINDOW_HEIGHT, viewportScreenshot);
        }
    },
    PHONE_ANDROID_LANDSCAPE
    {
        private static final int WINDOW_X = 0;
        private static final int WINDOW_Y = 106;
        private static final int WINDOW_WIDTH = 800;
        private static final int WINDOW_HEIGHT = 480;

        @Override
        public ShootingStrategy getDecoratedShootingStrategy(ShootingStrategy shootingStrategy,
                boolean viewportScreenshot, boolean landscape, String deviceName)
        {
            if (!landscape)
            {
                return PHONE_ANDROID_PORTRAIT.getDecoratedShootingStrategy(
                        shootingStrategy, viewportScreenshot, false, deviceName);
            }
            return getDecoratedAndroidShootingStrategy(shootingStrategy, WINDOW_X, WINDOW_Y, WINDOW_WIDTH,
                    WINDOW_HEIGHT, viewportScreenshot);
        }
    };

    private static ShootingStrategy getDecoratedAndroidShootingStrategy(ShootingStrategy shootingStrategy, int x,
            int y, int width, int height, boolean viewportScreenshot)
    {
        Rectangle targetArea = new Rectangle(x, y, width - x, height - y);
        ShootingTargetAreaStrategy shootingTargetAreaStrategy = new ShootingTargetAreaStrategy(shootingStrategy,
                targetArea);
        if (viewportScreenshot)
        {
            return shootingTargetAreaStrategy;
        }
        return new ViewportWithCorrectionPastingDecorator(shootingTargetAreaStrategy).withCorrectedWidth(width - x)
                .withCorrectedHeight(height - y).withScrollTimeout(SCROLL_TIMEOUT);
    }

    public abstract ShootingStrategy getDecoratedShootingStrategy(ShootingStrategy shootingStrategy,
            boolean viewportScreenshot, boolean landscape, String deviceName);
}
