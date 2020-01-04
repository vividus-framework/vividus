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
import static org.mockito.Mockito.when;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;

import ru.yandex.qatools.ashot.shooting.SimpleShootingStrategy;

@ExtendWith(MockitoExtension.class)
class ShootingTargetAreaStrategyTest
{
    private static final int SCREENSHOT_WIDTH = 100;
    private static final int SCREENSHOT_HEIGHT = 500;
    private static final int TARGET_AREA_WIDTH = 50;
    private static final int TARGET_AREA_HEIGHT = 400;
    private static final int TARGET_AREA_X = 10;
    private static final int TARGET_AREA_Y = 20;

    @Mock
    private WebDriver webDriver;

    @Mock
    private SimpleShootingStrategy simpleShootingStrategy;

    @Test
    void testGetScreenshot()
    {
        BufferedImage screenshot = new BufferedImage(SCREENSHOT_WIDTH, SCREENSHOT_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Rectangle targetArea = new Rectangle(TARGET_AREA_X, TARGET_AREA_Y, TARGET_AREA_WIDTH, TARGET_AREA_HEIGHT);
        BufferedImage expectedScreenshot = new BufferedImage(TARGET_AREA_WIDTH, TARGET_AREA_HEIGHT,
                BufferedImage.TYPE_INT_RGB);
        ShootingTargetAreaStrategy shootingTargetAreaStrategy = new ShootingTargetAreaStrategy(simpleShootingStrategy,
                targetArea);
        when(simpleShootingStrategy.getScreenshot(webDriver)).thenReturn(screenshot);
        BufferedImage bufferedImageActual = shootingTargetAreaStrategy.getScreenshot(webDriver);
        assertEquals(expectedScreenshot.getWidth(), bufferedImageActual.getWidth());
        assertEquals(expectedScreenshot.getHeight(), bufferedImageActual.getHeight());
    }
}
