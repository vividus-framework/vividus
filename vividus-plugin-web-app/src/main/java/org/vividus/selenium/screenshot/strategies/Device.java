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

import java.awt.Dimension;
import java.util.Arrays;
import java.util.stream.Stream;

import ru.yandex.qatools.ashot.shooting.DebuggingViewportPastingDecorator;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.shooting.cutter.CutStrategy;
import ru.yandex.qatools.ashot.shooting.cutter.VariableCutStrategy;

/**
 * @deprecated will be removed in favor of properties based configuration
 */
@SuppressWarnings("checkstyle:MagicNumber")
@Deprecated(forRemoval = true, since = "0.3.8")
public enum Device
{
    IPHONE("iPhone Simulator")
    {
        private static final int IPHONE6_HEADER_IOS_9_MAX = 43;
        private static final int IPHONE6_FOOTER_IOS_9_MAX = 44;

        private final Dimension resolution = new Dimension(750, 1334);

        @Override
        public CutStrategy getCutStrategy(boolean landscape)
        {
            CutStrategy cutStrategy = landscape
                    ? new VariableCutStrategy(HEADER_IOS_9_MIN, IPHONE6_HEADER_IOS_9_MAX,
                            (int) (resolution.width / getDpr() - IPHONE6_HEADER_IOS_9_MAX))
                    : new VariableCutStrategy(HEADER_IOS_9_MIN, HEADER_IOS_9_MAX, IPHONE6_FOOTER_IOS_9_MAX,
                            (int) (resolution.height / getDpr() - HEADER_IOS_9_MAX - IPHONE6_FOOTER_IOS_9_MAX));
            return new AdjustingCutStrategy(cutStrategy, 1);
        }

        @Override
        protected DebuggingViewportPastingDecorator decorateWithViewportPasting(ShootingStrategy shootingStrategy)
        {
            return new AdjustingViewportPastingDecorator(shootingStrategy, 1, 1);
        }

        @Override
        protected float getDpr()
        {
            return 2f;
        }
    },
    IPAD2("iPad Simulator")
    {
        private final Dimension resolution = new Dimension(768, 1024);

        @Override
        public CutStrategy getCutStrategy(boolean landscape)
        {
            int actualHeight = landscape ? resolution.width : resolution.height;
            CutStrategy cutStrategy = new VariableCutStrategy(HEADER_IOS_9_MIN, HEADER_IOS_9_MAX,
                    actualHeight - HEADER_IOS_9_MAX);
            return new AdjustingCutStrategy(cutStrategy, 1);
        }

        @Override
        protected DebuggingViewportPastingDecorator decorateWithViewportPasting(ShootingStrategy shootingStrategy)
        {
            return new AdjustingViewportPastingDecorator(shootingStrategy, 1, 0);
        }

        @Override
        protected float getDpr()
        {
            return 2f;
        }
    };

    private static final int HEADER_IOS_9_MIN = 41;
    private static final int HEADER_IOS_9_MAX = 64;

    private static final int SCROLL_TIMEOUT = 500;

    private final String[] deviceNames;

    Device(String... deviceNames)
    {
        this.deviceNames = Arrays.copyOf(deviceNames, deviceNames.length);
    }

    public ShootingStrategy decorate(boolean landscape)
    {
        CutStrategy cutStrategy = getCutStrategy(landscape);
        ShootingStrategy scalingDecorator = ShootingStrategies.scaling(getDpr());
        ShootingStrategy decoratedStrategy = decorateWithCutting(scalingDecorator, cutStrategy);
        return decorateWithViewportPasting(decoratedStrategy).withScrollTimeout(SCROLL_TIMEOUT);
    }

    protected abstract CutStrategy getCutStrategy(boolean landscape);

    protected abstract float getDpr();

    protected ShootingStrategy decorateWithCutting(ShootingStrategy shootingStrategy, CutStrategy cutStrategy)
    {
        return ShootingStrategies.cutting(shootingStrategy, cutStrategy);
    }

    protected DebuggingViewportPastingDecorator decorateWithViewportPasting(ShootingStrategy shootingStrategy)
    {
        return new DebuggingViewportPastingDecorator(shootingStrategy);
    }

    public static Device getByDeviceName(String deviceName)
    {
        return Stream.of(values())
                .filter(device -> device.hasDeviceName(deviceName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No device is found for name: " + deviceName));
    }

    private boolean hasDeviceName(String deviceName)
    {
        return Stream.of(deviceNames).anyMatch(name -> name.equalsIgnoreCase(deviceName));
    }
}
