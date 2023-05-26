/*
 * Copyright 2019-2023 the original author or authors.
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
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private static final String SCROLL_Y_SCRIPT = "var scrY = window.pageYOffset;if(scrY){return scrY;} else {return "
            + "0;}";

    private final DebuggingViewportPastingDecorator decorator = new DebuggingViewportPastingDecorator(null,
            HEADER_ADJUSTMENT, FOOTER_ADJUSTMENT);

    @Mock (extraInterfaces = JavascriptExecutor.class)
    private WebDriver webDriver;

    @Test
    void testOriginalWindowHeightIsAdjustedWithHeaderAndFooterCorrections()
    {
        try (var innerScriptMock = mockStatic(InnerScript.class))
        {
            innerScriptMock
                    .when(() -> InnerScript.execute(DebuggingViewportPastingDecorator.PAGE_DIMENSIONS_JS, webDriver))
                    .thenReturn(Map.of("pageHeight", 200, "viewportWidth", 150, "viewportHeight", WINDOW_HEIGHT));
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
}
