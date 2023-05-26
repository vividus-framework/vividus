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

package org.vividus.selenium.screenshot.strategies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.screenshot.DebuggingViewportPastingDecorator;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.screenshot.WebCutOptions;

import pazone.ashot.PageDimensions;
import pazone.ashot.ShootingStrategy;
import pazone.ashot.util.InnerScript;

@ExtendWith(MockitoExtension.class)
class AdjustingScrollableElementAwareViewportPastingDecoratorTests
{
    private static final int THE_ANSWER = 42;

    @Mock
    private WebElement scrollableElement;

    @Mock
    private WebJavascriptActions javascriptActions;

    @Mock
    private WebCutOptions webCutOptions;

    @Mock
    private ShootingStrategy shootingStrategy;

    private TestAdjustingScrollableElementAwareViewportPastingDecorator strategy;

    @BeforeEach
    void beforeEach()
    {
        when(webCutOptions.getWebHeaderToCut()).thenReturn(THE_ANSWER);
        when(webCutOptions.getWebFooterToCut()).thenReturn(THE_ANSWER);
        strategy = new TestAdjustingScrollableElementAwareViewportPastingDecorator(shootingStrategy, scrollableElement,
                javascriptActions, webCutOptions);
    }

    @Test
    void shouldAdjustViewportAndPageHeight()
    {
        try (MockedStatic<InnerScript> innerScriptMock = mockStatic(InnerScript.class))
        {
            WebDriver webDriver = mock(WebDriver.class);
            when(javascriptActions.executeScript(
                    "return Math.max(document.body.scrollHeight,"
                  + "document.body.offsetHeight,"
                  + "document.documentElement.clientHeight,"
                  + "document.documentElement.scrollHeight,"
                  + "document.documentElement.offsetHeight,"
                  + "arguments[0].scrollHeight);", scrollableElement)).thenReturn(100);
            innerScriptMock
                    .when(() -> InnerScript.execute(DebuggingViewportPastingDecorator.PAGE_DIMENSIONS_JS, webDriver))
                    .thenReturn(Map.of("pageHeight", 200, "viewportWidth", 150, "viewportHeight", 184));
            PageDimensions output = strategy.getPageDimensions(webDriver);
            assertEquals(100, output.getViewportHeight());
            assertEquals(184, output.getPageHeight());
            assertEquals(150, output.getViewportWidth());
        }
    }

    @Test
    void shouldIncreaseScrollYByHeaderSizeAfterFirstTwoIteration()
    {
        WebDriver webDriver = mock(WebDriver.class, withSettings().extraInterfaces(JavascriptExecutor.class));
        when(javascriptActions.executeScript("var scrollTop = arguments[0].scrollTop;"
                + "if(scrollTop){return scrollTop;} else {return 0;}", scrollableElement)).thenReturn(100);
        assertEquals(100, strategy.getScrollY((JavascriptExecutor) webDriver, -1));
        assertEquals(100, strategy.getScrollY((JavascriptExecutor) webDriver, 0));
        assertEquals(142, strategy.getScrollY((JavascriptExecutor) webDriver, 1));
    }

    @Test
    void shouldUseScrollableElementToScrollVertically()
    {
        strategy.scrollVertically(mock(JavascriptExecutor.class), THE_ANSWER);
        verify(javascriptActions, Mockito.times(1)).executeScript(
                  "if ('scrollBehavior' in document.documentElement.style) {"
                + "    arguments[1].scrollTo({"
                + "        \"top\": arguments[0],"
                + "        \"left\": 0,"
                + "        \"behavior\": \"instant\""
                + "    });"
                + "} else {"
                + "    arguments[1].scrollTo(0, arguments[0]);"
                + "}"
                + "return [];",
                THE_ANSWER, scrollableElement);
    }

    private static final class TestAdjustingScrollableElementAwareViewportPastingDecorator
            extends AdjustingScrollableElementAwareViewportPastingDecorator
    {
        private TestAdjustingScrollableElementAwareViewportPastingDecorator(ShootingStrategy strategy,
                WebElement scrollableElement, WebJavascriptActions javascriptActions, WebCutOptions webCutOptions)
        {
            super(strategy, scrollableElement, javascriptActions, webCutOptions);
        }

        @Override
        protected PageDimensions getPageDimensions(WebDriver driver)
        {
            return super.getPageDimensions(driver);
        }

        @Override
        protected void scrollVertically(JavascriptExecutor js, int scrollY)
        {
            super.scrollVertically(js, scrollY);
        }

        @Override
        protected int getCurrentScrollY(JavascriptExecutor js)
        {
            return super.getCurrentScrollY(js);
        }
    }
}
