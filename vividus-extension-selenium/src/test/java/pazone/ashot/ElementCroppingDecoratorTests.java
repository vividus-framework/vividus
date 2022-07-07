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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.vividus.selenium.screenshot.ScreenshotCropper;

import pazone.ashot.coordinates.Coords;

@ExtendWith(MockitoExtension.class)
class ElementCroppingDecoratorTests
{
    @Mock private ShootingStrategy shootingStrategy;
    @Mock private BufferedImage image;
    @Mock private WebDriver webDriver;
    @Mock private ScreenshotCropper screenshotCropper;

    private ElementCroppingDecorator decorator;

    @BeforeEach
    void init()
    {
        decorator = new ElementCroppingDecorator(shootingStrategy, screenshotCropper, Map.of());
    }

    @Test
    void shouldGetScreenshot()
    {
        when(shootingStrategy.getScreenshot(webDriver)).thenReturn(image);
        when(screenshotCropper.crop(image, Optional.empty(), Map.of(), 0)).thenReturn(image);

        BufferedImage result = decorator.getScreenshot(webDriver);
        assertEquals(image, result);
    }

    @Test
    void shouldPrepareCoords()
    {
        Coords coords = new Coords(1, 1, 1, 1);
        when(shootingStrategy.prepareCoords(Set.of(coords))).thenReturn(Set.of(coords));
        assertEquals(Set.of(coords), decorator.prepareCoords(Set.of(coords)));
    }

    @Test
    void shouldReturnImageOfContextElement()
    {
        Coords contextCoords = new Coords(16, 2331, 1888, 515);
        Coords preparedCoords = new Coords(16, 206, 1888, 515);

        when(shootingStrategy.getScreenshot(webDriver, Set.of(contextCoords))).thenReturn(image);
        when(shootingStrategy.prepareCoords(Set.of(contextCoords))).thenReturn(Set.of(preparedCoords));
        when(screenshotCropper.crop(image, Optional.of(contextCoords), Map.of(), 2125)).thenReturn(image);

        decorator = new ElementCroppingDecorator(shootingStrategy, screenshotCropper, Map.of());
        BufferedImage screenshot = decorator.getScreenshot(webDriver, Set.of(contextCoords));
        assertEquals(image, screenshot);
    }
}
