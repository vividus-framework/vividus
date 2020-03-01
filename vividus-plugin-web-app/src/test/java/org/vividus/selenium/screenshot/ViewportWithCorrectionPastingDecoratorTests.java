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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import ru.yandex.qatools.ashot.shooting.SimpleShootingStrategy;

@ExtendWith(MockitoExtension.class)
class ViewportWithCorrectionPastingDecoratorTests
{
    private static final int SCREENSHOT_CORRECTED_WIDTH = 10;
    private static final int SCREENSHOT_CORRECTED_HEIGHT = 20;
    private static final int PAGE_FULL_HEIGHT = 113;
    private static final int PAGE_FULL_WIDTH = 5;
    private static final int VIEWPORT_HEIGHT = 10;
    private static final float ACCURACY = 1.03f;
    private static final String SCRIPT = "var scrY = window.scrollY;if(scrY){return scrY;} else {return 0;}";

    @Mock(extraInterfaces = JavascriptExecutor.class)
    private WebDriver webDriver;

    @Mock
    private SimpleShootingStrategy simpleShootingStrategy;

    private ViewportWithCorrectionPastingDecorator pastingDecoratorSpy;

    private void mockPageSizes()
    {
        doReturn(PAGE_FULL_HEIGHT).when(pastingDecoratorSpy).getFullHeight(webDriver);
        doReturn(PAGE_FULL_WIDTH).when(pastingDecoratorSpy).getFullWidth(webDriver);
        doReturn(VIEWPORT_HEIGHT).when(pastingDecoratorSpy).getWindowHeight(webDriver);
    }

    @Test
    void testGetScreenshot()
    {
        pastingDecoratorSpy = Mockito.spy(new ViewportWithCorrectionPastingDecorator(simpleShootingStrategy));
        BufferedImage screenshot = new BufferedImage(SCREENSHOT_CORRECTED_WIDTH, SCREENSHOT_CORRECTED_HEIGHT,
                BufferedImage.TYPE_INT_RGB);
        pastingDecoratorSpy.withCorrectedHeight(SCREENSHOT_CORRECTED_HEIGHT);
        pastingDecoratorSpy.withCorrectedWidth(SCREENSHOT_CORRECTED_WIDTH);
        doReturn(screenshot).when(simpleShootingStrategy).getScreenshot(webDriver);
        mockPageSizes();
        Mockito.lenient().when(((JavascriptExecutor) webDriver).executeScript(SCRIPT)).thenReturn(VIEWPORT_HEIGHT);
        BufferedImage bufferedImageActual = pastingDecoratorSpy.getScreenshot(webDriver, null);
        int expectedHeight = Math.round((PAGE_FULL_HEIGHT * SCREENSHOT_CORRECTED_HEIGHT / VIEWPORT_HEIGHT) * ACCURACY);
        int expectedWidth = Math.round(PAGE_FULL_WIDTH * SCREENSHOT_CORRECTED_WIDTH / PAGE_FULL_WIDTH);
        assertEquals(expectedHeight, bufferedImageActual.getHeight());
        assertEquals(expectedWidth, bufferedImageActual.getWidth());
    }

    @Test
    void testGetScreenshotWithoutCorrectedHeight()
    {
        pastingDecoratorSpy = Mockito.spy(new ViewportWithCorrectionPastingDecorator(simpleShootingStrategy));
        pastingDecoratorSpy.withCorrectedWidth(SCREENSHOT_CORRECTED_WIDTH);
        mockPageSizes();
        assertThrows(IllegalArgumentException.class, () -> pastingDecoratorSpy.getScreenshot(webDriver));
    }

    @Test
    void testGetScreenshotWithoutCorrectedWidth()
    {
        pastingDecoratorSpy = Mockito.spy(new ViewportWithCorrectionPastingDecorator(simpleShootingStrategy));
        pastingDecoratorSpy.withCorrectedHeight(SCREENSHOT_CORRECTED_HEIGHT);
        mockPageSizes();
        assertThrows(IllegalArgumentException.class, () -> pastingDecoratorSpy.getScreenshot(webDriver));
    }
}
