/*
 * Copyright 2019-2024 the original author or authors.
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.vividus.selenium.screenshot.DebuggingViewportPastingDecorator;

import pazone.ashot.util.InnerScript;

@ExtendWith(MockitoExtension.class)
class DebuggingViewportPastingDecoratorTests
{
    private static final int HEADER_ADJUSTMENT = 1;
    private static final int FOOTER_ADJUSTMENT = 2;

    private static final int WINDOW_HEIGHT = 500;
    private static final int WINDOW_WIDTH = 1000;

    private static final String PAGE_HEIGHT = "pageHeight";
    private static final String VIEWPORT_WIDTH = "viewportWidth";
    private static final String VIEWPORT_HEIGHT = "viewportHeight";

    private static final String SCROLL_Y_SCRIPT = "var scrY = window.pageYOffset;if(scrY){return scrY;} else {return "
            + "0;}";

    private final DebuggingViewportPastingDecorator decorator = new DebuggingViewportPastingDecorator(null,
            HEADER_ADJUSTMENT, FOOTER_ADJUSTMENT, 0);

    @Mock (extraInterfaces = JavascriptExecutor.class)
    private WebDriver webDriver;

    @Test
    void testOriginalWindowHeightIsAdjustedWithHeaderAndFooterCorrections()
    {
        try (var innerScriptMock = mockStatic(InnerScript.class))
        {
            innerScriptMock
                    .when(() -> InnerScript.execute(DebuggingViewportPastingDecorator.PAGE_DIMENSIONS_JS, webDriver))
                    .thenReturn(Map.of(PAGE_HEIGHT, 200, VIEWPORT_WIDTH, 150, VIEWPORT_HEIGHT, WINDOW_HEIGHT));
            var output = decorator.getPageDimensions(webDriver);
            assertEquals(WINDOW_HEIGHT - HEADER_ADJUSTMENT - FOOTER_ADJUSTMENT, output.getViewportHeight());
            assertEquals(200, output.getPageHeight());
            assertEquals(150, output.getViewportWidth());
        }
    }

    @Test
    void testNonAdjustedCurrentScrollYIsReturnedBeforeScrolling()
    {
        var jsExecutor = (JavascriptExecutor) webDriver;
        var currentScrollY = 0;
        when(jsExecutor.executeScript(SCROLL_Y_SCRIPT)).thenReturn(currentScrollY);
        assertEquals(currentScrollY, decorator.getCurrentScrollY(jsExecutor));
    }

    @Test
    void testAdjustedCurrentScrollYIsReturnedAfterSecondScrolling()
    {
        var jsExecutor = (JavascriptExecutor) webDriver;
        var currentScrollY = 100;
        when(jsExecutor.executeScript(SCROLL_Y_SCRIPT)).thenReturn(currentScrollY);
        try (var ignored = mockConstruction(CuttingDecorator.class))
        {
            decorator.getChunk(webDriver, 0, 2);
            assertEquals(currentScrollY, decorator.getCurrentScrollY(jsExecutor));
            decorator.getChunk(webDriver, 1, 3);
            assertEquals(currentScrollY + HEADER_ADJUSTMENT, decorator.getCurrentScrollY(jsExecutor));
        }
    }

    @CsvSource({
            "1000, 4, 3",
            "0,    5, 4",
            "1500, 5, 4",
            "2000, 5, 4"
    })
    @ParameterizedTest
    void testLimitScreenshotHeight(int screenshotMaxHeight, int numOfScriptInvocation, int numOfScreenshots)
    {
        ShootingStrategy shootingStrategy = mock();
        DebuggingViewportPastingDecorator decorator = new DebuggingViewportPastingDecorator(shootingStrategy,
                HEADER_ADJUSTMENT, FOOTER_ADJUSTMENT, screenshotMaxHeight);
        BufferedImage image = new BufferedImage(WINDOW_WIDTH, 497, BufferedImage.TYPE_INT_RGB);

        try (var innerScriptMock = mockStatic(InnerScript.class);)
        {
            innerScriptMock
                    .when(() -> InnerScript.execute(DebuggingViewportPastingDecorator.PAGE_DIMENSIONS_JS, webDriver))
                    .thenReturn(
                            Map.of(PAGE_HEIGHT, 1500, VIEWPORT_WIDTH, WINDOW_WIDTH, VIEWPORT_HEIGHT, WINDOW_HEIGHT));
            var jsExecutor = (JavascriptExecutor) webDriver;
            when(jsExecutor.executeScript(SCROLL_Y_SCRIPT)).thenReturn(0);
            when(shootingStrategy.getScreenshot(webDriver)).thenReturn(image);

            decorator.getScreenshot(webDriver, Set.of());

            verify(jsExecutor, times(numOfScriptInvocation)).executeScript(SCROLL_Y_SCRIPT);
            verify(shootingStrategy, times(numOfScreenshots)).getScreenshot(webDriver);
        }
    }
}
