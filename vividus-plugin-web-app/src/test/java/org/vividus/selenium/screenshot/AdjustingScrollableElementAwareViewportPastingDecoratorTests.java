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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.action.IJavascriptActions;

import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

@ExtendWith(MockitoExtension.class)
class AdjustingScrollableElementAwareViewportPastingDecoratorTests
{
    private static final int THE_ANSWER = 42;

    @Mock
    private WebElement scrollableElement;

    @Mock
    private IJavascriptActions javascriptActions;

    @Mock
    private ScreenshotConfiguration screenshotConfiguration;

    @Mock
    private ShootingStrategy shootingStrategy;

    private AdjustingScrollableElementAwareViewportPastingDecorator strategy;

    @BeforeEach
    void beforeEach()
    {
        when(screenshotConfiguration.getWebHeaderToCut()).thenReturn(THE_ANSWER);
        when(screenshotConfiguration.getWebFooterToCut()).thenReturn(THE_ANSWER);
        strategy = new AdjustingScrollableElementAwareViewportPastingDecorator(shootingStrategy, scrollableElement,
                javascriptActions, screenshotConfiguration);
    }

    @Test
    void shouldDecreaseViewportHeightByFooterAndHeaderSize()
    {
        WebDriver webDriver = mock(WebDriver.class, withSettings().extraInterfaces(JavascriptExecutor.class));
        when(((JavascriptExecutor) webDriver).executeScript("return window.innerHeight ||"
                + " document.documentElement.clientHeight || document.getElementsByTagName('body')[0].clientHeight;"))
                .thenReturn(184);
        assertEquals(100, strategy.getWindowHeight(webDriver));
    }

    @Test
    void shouldIncreasePageSizeByHeaderAndFooterSizes()
    {
        WebDriver webDriver = mock(WebDriver.class);
        when(javascriptActions.executeScript(
                "return Math.max(document.body.scrollHeight,"
              + "document.body.offsetHeight,"
              + "document.documentElement.clientHeight,"
              + "document.documentElement.scrollHeight,"
              + "document.documentElement.offsetHeight,"
              + "arguments[0].scrollHeight);", scrollableElement)).thenReturn(100);
        assertEquals(184, strategy.getFullHeight(webDriver));
    }

    @Test
    void shouldIncreaseScrollYByHeaderSizeAfterFirstTwoIteration()
    {
        WebDriver webDriver = mock(WebDriver.class, withSettings().extraInterfaces(JavascriptExecutor.class));
        when(javascriptActions.executeScript("var scrollTop = arguments[0].scrollTop;"
                + "if(scrollTop){return scrollTop;} else {return 0;}", scrollableElement)).thenReturn(100);
        assertEquals(100, strategy.getCurrentScrollY((JavascriptExecutor) webDriver));
        assertEquals(100, strategy.getCurrentScrollY((JavascriptExecutor) webDriver));
        assertEquals(142, strategy.getCurrentScrollY((JavascriptExecutor) webDriver));
    }
}
