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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.shooting.cutter.CutStrategy;

class AdjustingRotatingDecoratorTests
{
    @Test
    void testGetScreenshot()
    {
        BufferedImage image = new BufferedImage(100, 50, BufferedImage.TYPE_4BYTE_ABGR);
        WebDriver webDriver = mock(WebDriver.class);
        ShootingStrategy shootingStrategy = mock(ShootingStrategy.class);
        when(shootingStrategy.getScreenshot(webDriver)).thenReturn(image);
        CutStrategy cutStrategy = mock(CutStrategy.class);
        AdjustingRotatingDecorator adjustingRotatingDecorator = new AdjustingRotatingDecorator(cutStrategy,
                shootingStrategy, 10);
        BufferedImage screenshot = adjustingRotatingDecorator.getScreenshot(webDriver);
        assertEquals(image.getWidth(), screenshot.getHeight());
        assertEquals(image.getHeight(), screenshot.getWidth());
    }
}
