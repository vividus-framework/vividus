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

package pazone.ashot;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openqa.selenium.WebDriver;
import org.vividus.selenium.screenshot.IgnoreStrategy;
import org.vividus.selenium.screenshot.ScreenshotCropper;
import org.vividus.ui.action.search.Locator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import pazone.ashot.coordinates.Coords;

public class ElementCroppingDecorator extends ShootingDecorator
{
    private static final long serialVersionUID = 1088965678314008274L;

    private final transient ScreenshotCropper screenshotCropper;
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private final transient Map<IgnoreStrategy, Set<Locator>> ignoreStrategies;

    public ElementCroppingDecorator(ShootingStrategy shootingStrategy, ScreenshotCropper screenshotCropper,
            Map<IgnoreStrategy, Set<Locator>> ignoreStrategies)
    {
        super(shootingStrategy);
        this.screenshotCropper = screenshotCropper;
        this.ignoreStrategies = ignoreStrategies;
    }

    @Override
    public BufferedImage getScreenshot(WebDriver webDriver)
    {
        BufferedImage image = getShootingStrategy().getScreenshot(webDriver);
        return screenshotCropper.crop(image, Optional.empty(), ignoreStrategies, 0);
    }

    @Override
    public BufferedImage getScreenshot(WebDriver webDriver, Set<Coords> coords)
    {
        Coords originalCoords = coords.iterator().next();
        Coords contextCoords = new Coords(originalCoords);
        int contextY = contextCoords.y;
        BufferedImage image = getShootingStrategy().getScreenshot(webDriver, coords);
        Set<Coords> preparedCoords = prepareCoords(Set.of(contextCoords));
        return screenshotCropper.crop(image, Optional.of(originalCoords), ignoreStrategies,
                contextY - preparedCoords.iterator().next().y);
    }

    @Override
    public Set<Coords> prepareCoords(Set<Coords> coordsSet)
    {
        return getShootingStrategy().prepareCoords(coordsSet);
    }
}
