/*
 * Copyright 2019-2020 the original author or authors.
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
import java.util.Set;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

public class ViewportShootingStrategy implements ShootingStrategy
{
    private static final long serialVersionUID = -4641502273023034104L;

    private final ShootingStrategy shootingStrategy;

    public ViewportShootingStrategy(ShootingStrategy shootingStrategy)
    {
        this.shootingStrategy = shootingStrategy;
    }

    @Override
    public BufferedImage getScreenshot(WebDriver wd)
    {
        JavascriptExecutor executor = (JavascriptExecutor) wd;
        int y = Math.toIntExact((Long) executor.executeScript("return window.pageYOffset;"));
        int h = Math.toIntExact((Long) executor.executeScript("return window.innerHeight;"));
        BufferedImage image = shootingStrategy.getScreenshot(wd);
        return image.getSubimage(0, y, image.getWidth(), h);
    }

    @Override
    public BufferedImage getScreenshot(WebDriver wd, Set<Coords> coords)
    {
        return getScreenshot(wd);
    }

    @Override
    public Set<Coords> prepareCoords(Set<Coords> coordsSet)
    {
        return coordsSet;
    }
}
