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

package org.vividus.selenium.screenshot;

import static pazone.ashot.ShootingStrategies.cutting;

import org.vividus.ui.screenshot.ScreenshotParameters;

import pazone.ashot.ElementCroppingDecorator;
import pazone.ashot.ShootingStrategy;
import pazone.ashot.cutter.FixedCutStrategy;

public abstract class AbstractAshotFactory<T extends ScreenshotParameters> implements AshotFactory<T>
{
    private String screenshotShootingStrategy;

    private final ScreenshotCropper screenshotCropper;

    protected AbstractAshotFactory(ScreenshotCropper screenshotCropper)
    {
        this.screenshotCropper = screenshotCropper;
    }

    protected ShootingStrategy decorateWithFixedCutStrategy(ShootingStrategy original, int headerToCut, int footerToCut)
    {
        return footerToCut > 0 || headerToCut > 0
                ? cutting(original, new FixedCutStrategy(headerToCut, footerToCut))
                : original;
    }

    protected ShootingStrategy decorateWithCropping(ShootingStrategy strategy,
            ScreenshotParameters screenshotParameters)
    {
        return new ElementCroppingDecorator(strategy, screenshotCropper, screenshotParameters.getIgnoreStrategies());
    }

    protected String getScreenshotShootingStrategy()
    {
        return screenshotShootingStrategy;
    }

    public void setScreenshotShootingStrategy(String screenshotShootingStrategy)
    {
        this.screenshotShootingStrategy = screenshotShootingStrategy;
    }
}
