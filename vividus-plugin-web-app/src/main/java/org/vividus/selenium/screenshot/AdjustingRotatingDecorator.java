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

import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.openqa.selenium.WebDriver;

import ru.yandex.qatools.ashot.shooting.RotatingDecorator;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.shooting.cutter.CutStrategy;

/**
 * Modified copy of ru.yandex.qatools.ashot.shooting.RotatingDecorator
 */
class AdjustingRotatingDecorator extends RotatingDecorator
{
    private static final long serialVersionUID = -4663745274781571424L;

    private final ShootingStrategy shootingStrategy;
    private final CutStrategy cutStrategy;
    private final int adjustingIndentation;

    AdjustingRotatingDecorator(CutStrategy cutStrategy, ShootingStrategy shootingStrategy, int adjustingIndentation)
    {
        super(cutStrategy, shootingStrategy);
        this.cutStrategy = cutStrategy;
        this.shootingStrategy = shootingStrategy;
        this.adjustingIndentation = adjustingIndentation;
    }

    @Override
    public BufferedImage getScreenshot(WebDriver wd)
    {
        return rotate(shootingStrategy.getScreenshot(wd), wd);
    }

    private BufferedImage rotate(BufferedImage baseImage, WebDriver wd)
    {
        BufferedImage rotated = new BufferedImage(baseImage.getHeight(), baseImage.getWidth(), TYPE_4BYTE_ABGR);
        Graphics2D graphics = rotated.createGraphics();
        double theta = Math.PI / 2;
        int origin = baseImage.getWidth();
        graphics.rotate(theta, origin, origin);
        graphics.drawImage(baseImage, null, 0, adjustingIndentation);
        int rotatedHeight = rotated.getHeight();
        int rotatedWidth = rotated.getWidth();
        int headerToCut = cutStrategy.getHeaderHeight(wd);
        return rotated.getSubimage(0, headerToCut, rotatedWidth, rotatedHeight - headerToCut);
    }
}
