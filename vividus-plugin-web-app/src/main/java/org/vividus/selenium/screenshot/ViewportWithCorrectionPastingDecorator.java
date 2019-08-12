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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.Set;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.vividus.util.Sleeper;

import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.shooting.ViewportPastingDecorator;

/**
 * Modified copy of ru.yandex.qatools.ashot.shooting.ViewportPastingDecorator
 */
public class ViewportWithCorrectionPastingDecorator extends ViewportPastingDecorator
{
    private static final long serialVersionUID = 1294938703067038698L;

    private static final float ACCURACY = 1.03f;
    private int correctedHeight;
    private int correctedWidth;

    public ViewportWithCorrectionPastingDecorator(ShootingStrategy strategy)
    {
        super(strategy);
    }

    public ViewportWithCorrectionPastingDecorator withCorrectedHeight(int correctedHeight)
    {
        this.correctedHeight = correctedHeight;
        return this;
    }

    public ViewportWithCorrectionPastingDecorator withCorrectedWidth(int correctedWidth)
    {
        this.correctedWidth = correctedWidth;
        return this;
    }

    @Override
    public BufferedImage getScreenshot(WebDriver wd, Set<Coords> coordsSet)
    {
        JavascriptExecutor js = (JavascriptExecutor) wd;
        int pageHeight = getFullHeight(wd);
        int pageWidth = getFullWidth(wd);
        int viewportHeight = getWindowHeight(wd);
        Coords shootingArea = getShootingCoords(coordsSet, pageWidth, pageHeight, viewportHeight);

        double heightRation = (double) correctedHeight / viewportHeight;
        double widthRation = (double) correctedWidth / pageWidth;

        BufferedImage finalImage = new BufferedImage(Math.toIntExact(Math.round(pageWidth * widthRation)),
                Math.toIntExact(Math.round(shootingArea.height * heightRation * ACCURACY)), BufferedImage
                .TYPE_3BYTE_BGR);
        Graphics2D graphics = finalImage.createGraphics();

        int scrollTimes = (int) Math.ceil(shootingArea.getHeight() / viewportHeight);
        for (int n = 0; n < scrollTimes; n++)
        {
            scrollVertically(js, shootingArea.y + viewportHeight * n);
            Sleeper.sleep(Duration.ofMillis(scrollTimeout));
            BufferedImage part = getShootingStrategy().getScreenshot(wd);
            graphics.drawImage(part, 0, Math.toIntExact(Math.round((getCurrentScrollY(js) - shootingArea.y)
                    * heightRation)), null);
        }

        graphics.dispose();
        return finalImage;
    }

    private Coords getShootingCoords(Set<Coords> coords, int pageWidth, int pageHeight, int viewPortHeight)
    {
        if (coords == null || coords.isEmpty())
        {
            return new Coords(0, 0, pageWidth, pageHeight);
        }
        return extendShootingArea(Coords.unity(coords), viewPortHeight, pageHeight);
    }

    private Coords extendShootingArea(Coords shootingCoords, int viewportHeight, int pageHeight)
    {
        int halfViewport = viewportHeight / 2;
        shootingCoords.y = Math.max(shootingCoords.y - halfViewport / 2, 0);
        shootingCoords.height = Math.min(shootingCoords.height + halfViewport, pageHeight);
        return shootingCoords;
    }
}
