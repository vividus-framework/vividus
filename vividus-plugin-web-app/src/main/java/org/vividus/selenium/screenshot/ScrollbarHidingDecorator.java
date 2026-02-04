/*
 * Copyright 2019-2026 the original author or authors.
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

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.openqa.selenium.WebElement;

import pazone.ashot.ShootingStrategy;
import pazone.ashot.coordinates.Coords;

public class ScrollbarHidingDecorator extends ScreenshotShootingDecorator
{
    private static final long serialVersionUID = -6146195031592698438L;

    private final transient Optional<WebElement> scrollableElement;
    private final transient IScrollbarHandler scrollbarHandler;

    public ScrollbarHidingDecorator(ShootingStrategy shootingStrategy, Optional<WebElement> scrollableElement,
            IScrollbarHandler scrollbarHandler)
    {
        super(shootingStrategy);
        this.scrollableElement = scrollableElement;
        this.scrollbarHandler = scrollbarHandler;
    }

    @Override
    public Set<Coords> prepareCoords(Set<Coords> coordsSet)
    {
        return getShootingStrategy().prepareCoords(coordsSet);
    }

    @Override
    public BufferedImage perform(Supplier<BufferedImage> bufferedImageSupplier)
    {
        return scrollableElement.map(e -> scrollbarHandler.performActionWithHiddenScrollbars(bufferedImageSupplier, e))
                .orElseGet(() -> scrollbarHandler.performActionWithHiddenScrollbars(bufferedImageSupplier));
    }
}
