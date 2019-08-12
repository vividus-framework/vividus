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

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.powermock.reflect.Whitebox;
import org.vividus.ui.web.action.IJavascriptActions;

import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

@ExtendWith(MockitoExtension.class)
class ScrollableElementAwareViewportPastingDecoratorTests
{
    private static final int THE_ANSWER = 42;

    @Mock
    private IJavascriptActions javascriptActions = mock(IJavascriptActions.class, withSettings().lenient());
    @Mock
    private WebElement scrollableElement;
    @Mock
    private ShootingStrategy shootingStrategy;
    @Mock
    private ScreenshotConfiguration screenshotConfiguration;
    @Mock
    private ScreenshotDebugger screenshotDebugger;

    private ScrollableElementAwareViewportPastingDecorator strategy;

    @BeforeEach
    void setUp()
    {
        when(screenshotConfiguration.getScrollTimeout()).thenReturn(Duration.ofSeconds(1));
        strategy = new ScrollableElementAwareViewportPastingDecorator(shootingStrategy, scrollableElement,
                javascriptActions, screenshotConfiguration);
        strategy.withDebugger(screenshotDebugger);
    }

    @Test
    void shouldUseScrollableElementToGetHeight()
    {
        mockGetHeight(javascriptActions);
        assertEquals(42, strategy.getFullHeight(mock(WebDriver.class)));
    }

    private void mockGetHeight(IJavascriptActions javascriptActions)
    {
        when(javascriptActions.executeScript(
                          "return Math.max(document.body.scrollHeight,"
                        + "document.body.offsetHeight,"
                        + "document.documentElement.clientHeight,"
                        + "document.documentElement.scrollHeight,"
                        + "document.documentElement.offsetHeight,"
                        + "arguments[0].scrollHeight);", scrollableElement)).thenReturn(THE_ANSWER);
    }

    @Test
    void shouldUseScrollableElementToScrollVertically()
    {
        strategy.scrollVertically(mock(JavascriptExecutor.class), THE_ANSWER);
        verifyScrollTo(THE_ANSWER);
    }

    private void verifyScrollTo(int scrollTo)
    {
        verifyScrollTo(scrollTo, 1);
    }

    private void verifyScrollTo(int scrollTo, int times)
    {
        verify(javascriptActions, Mockito.times(times)).executeScript(
                "arguments[0].scrollTo(0, arguments[1]); return [];", scrollableElement, scrollTo);
    }

    @Test
    void shouldUseScrollableElementToGetCurrentYScroll()
    {
        mockCurrentScrollY(javascriptActions);
        assertEquals(THE_ANSWER, strategy.getCurrentScrollY(mock(JavascriptExecutor.class)));
    }

    private void mockCurrentScrollY(IJavascriptActions javascriptActions)
    {
        mockCurrentScrollY(javascriptActions, THE_ANSWER);
    }

    private OngoingStubbing<Object> mockCurrentScrollY(IJavascriptActions javascriptActions, int scrollY)
    {
        return when(javascriptActions.executeScript("var scrollTop = arguments[0].scrollTop;"
                + "if(scrollTop){return scrollTop;} else {return 0;}", scrollableElement)).thenReturn(scrollY);
    }

    @Test
    void shouldTakeScreenshot()
    {
        mockGetHeight(javascriptActions);
        mockCurrentScrollY(javascriptActions);
        WebDriver webDriver = mock(WebDriver.class, withSettings().extraInterfaces(JavascriptExecutor.class));
        mockViewportSize(webDriver);
        ScreenshotDebugger debugger = mock(ScreenshotDebugger.class);
        strategy.withDebugger(debugger);

        assertNotNull(strategy.getScreenshot(webDriver, Set.of()));
        InOrder ordered = Mockito.inOrder(debugger);
        String suffix = "current_scroll_42";
        ordered.verify(debugger).debug(strategy.getClass(), suffix + "_part_0", null);
        ordered.verify(debugger).debug(eq(strategy.getClass()), eq(suffix), any(BufferedImage.class));
        assertEquals(Duration.ofMillis(1000), Whitebox.getInternalState(strategy, "scrollTimeout"));
    }

    private void mockViewportSize(WebDriver webDriver)
    {
        when(((JavascriptExecutor) webDriver).executeScript("return window.innerWidth || "
               + "document.documentElement.clientWidth || "
               + "document.getElementsByTagName('body')[0].clientWidth;")).thenReturn(THE_ANSWER);
        when(((JavascriptExecutor) webDriver).executeScript("return window.innerHeight ||"
               + " document.documentElement.clientHeight || document.getElementsByTagName('body')[0].clientHeight;"))
               .thenReturn(THE_ANSWER);
    }

    @Test
    void shouldReturnToAStartingPositionAfterScreenshotShooting()
    {
        mockCurrentScrollY(javascriptActions, 0).thenReturn(THE_ANSWER);
        mockGetHeight(javascriptActions);
        WebDriver webDriver = mock(WebDriver.class, withSettings().extraInterfaces(JavascriptExecutor.class));
        when(((JavascriptExecutor) webDriver).executeScript(anyString(), any())).thenReturn(THE_ANSWER);
        assertNotNull(strategy.getScreenshot(webDriver, Set.of()));
        verifyScrollTo(0, 2);
    }
}
