/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.ui.web.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Browser;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.TestContext;
import org.vividus.ui.web.event.DeviceMetricsOverrideEvent;
import org.vividus.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class WebJavascriptActionsTests
{
    private static final String SCROLL_TO_END_OF_PAGE = "scroll-to-end-of-page.js";
    private static final String TEXT = "text";
    private static final String BODY_INNER_TEXT = "return document.body.innerText";
    private static final String ELEMENT_INNER_TEXT = "return arguments[0].innerText";
    private static final String SCRIPT_GET_ELEMENT_ATTRIBUTES = "var attributes = arguments[0].attributes;"
            + " var map = new Object(); for(i=0; i< attributes.length; i++)"
            + "{ map[attributes[i].name] = attributes[i].value; } return map;";
    private static final String SCRIPT_GET_DEVICE_PIXEL_RATIO = "return window.devicePixelRatio;";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IWebDriverManager webDriverManager;
    @Spy private TestContext testContext = new SimpleTestContext();

    @Mock(extraInterfaces = { JavascriptExecutor.class, HasCapabilities.class })
    private WebDriver webDriver;

    @InjectMocks private WebJavascriptActions javascriptActions;

    @BeforeEach
    void beforeEach()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
    }

    @Test
    void testExecuteScriptFromResource()
    {
        javascriptActions.executeScriptFromResource(WebJavascriptActions.class, SCROLL_TO_END_OF_PAGE);
        verify((JavascriptExecutor) webDriver)
                .executeScript(ResourceUtils.loadResource(WebJavascriptActions.class, SCROLL_TO_END_OF_PAGE));
    }

    @Test
    void testExecuteAsyncScriptFromResource()
    {
        javascriptActions.executeAsyncScriptFromResource(WebJavascriptActions.class, SCROLL_TO_END_OF_PAGE);
        verify((JavascriptExecutor) webDriver)
                .executeAsyncScript(ResourceUtils.loadResource(WebJavascriptActions.class, SCROLL_TO_END_OF_PAGE));
    }

    @Test
    void testExecuteAsyncScript()
    {
        var script = "asyncScript";
        var arg1 = "asyncScriptArg1";
        var arg2 = "asyncScriptArg2";
        javascriptActions.executeAsyncScript(script, arg1, arg2);
        verify((JavascriptExecutor) webDriver).executeAsyncScript(script, arg1, arg2);
    }

    @Test
    void testScrollIntoView()
    {
        var webElement = mock(WebElement.class);
        javascriptActions.scrollIntoView(webElement, true);
        verify((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView(arguments[1])", webElement,
                true);
        verifyWaitingForScrollFinish();
        verifyNoMoreInteractions(webDriver);
    }

    @Test
    void testScrollElementIntoViewportCenter()
    {
        var webElement = mock(WebElement.class);
        var stickyHeaderSize = 25;
        javascriptActions.setStickyHeaderSizePercentage(stickyHeaderSize);
        javascriptActions.scrollElementIntoViewportCenter(webElement);
        verify((JavascriptExecutor) webDriver).executeAsyncScript(
                ResourceUtils.loadResource(WebJavascriptActionsTests.class, "scroll-element-into-viewport-center.js"),
                webElement, stickyHeaderSize);
    }

    @Test
    void testScrollToEndOfPage()
    {
        javascriptActions.scrollToEndOfPage();
        verify((JavascriptExecutor) webDriver).executeAsyncScript(
                ResourceUtils.loadResource(WebJavascriptActionsTests.class, SCROLL_TO_END_OF_PAGE));
    }

    @Test
    void testScrollToStartOfPage()
    {
        javascriptActions.scrollToStartOfPage();
        verify((JavascriptExecutor) webDriver).executeScript(
                "if (window.PageYOffset || document.documentElement.scrollTop > 0){ window.scrollTo(0,0);}");
    }

    @Test
    void testScrollToEndOf()
    {
        var webElement = mock(WebElement.class);
        javascriptActions.scrollToEndOf(webElement);
        verify((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight",
                webElement);
    }

    @Test
    void testScrollToStartOf()
    {
        var webElement = mock(WebElement.class);
        javascriptActions.scrollToStartOf(webElement);
        verify((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollTop = 0", webElement);
    }

    @Test
    void testOpenNewWindow()
    {
        javascriptActions.openNewTab();
        verify((JavascriptExecutor) webDriver).executeScript("window.open()");
    }

    @Test
    void testCloseCurrentWindow()
    {
        javascriptActions.closeCurrentTab();
        verify((JavascriptExecutor) webDriver).executeScript("window.close()");
    }

    @Test
    void testTriggerEvents()
    {
        var webElement = mock(WebElement.class);
        var script = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');"
                + "evObj.initEvent('click', true, false); arguments[0].dispatchEvent(evObj);}"
                + " else if(document.createEventObject) { arguments[0].fireEvent('onclick');}";
        javascriptActions.triggerMouseEvents(webElement, "click");
        verify((JavascriptExecutor) webDriver).executeScript(script, webElement);
    }

    @Test
    void testClick()
    {
        var webElement = mock(WebElement.class);
        javascriptActions.click(webElement);
        verify((JavascriptExecutor) webDriver).executeScript("arguments[0].click()", webElement);
    }

    @Test
    void testGetPageTextFirefox()
    {
        mockIsFirefox(true);
        mockScriptExecution("return document.body.textContent", TEXT);
        javascriptActions.getPageText();
        assertEquals(TEXT, javascriptActions.getPageText());
    }

    @Test
    void testGetPageTextNotFirefox()
    {
        mockIsFirefox(false);
        mockScriptExecution(BODY_INNER_TEXT, TEXT);
        assertEquals(TEXT, javascriptActions.getPageText());
    }

    @Test
    void testGetElementTextNotFirefox()
    {
        var webElement = mock(WebElement.class);
        mockIsFirefox(false);
        when(((JavascriptExecutor) webDriver).executeScript(ELEMENT_INNER_TEXT, webElement)).thenReturn(TEXT);
        assertEquals(TEXT, javascriptActions.getElementText(webElement));
    }

    @Test
    void testGetElementValue()
    {
        var webElement = mock(WebElement.class);
        when(((JavascriptExecutor) webDriver).executeScript("return arguments[0].value", webElement))
                .thenReturn(TEXT);
        assertEquals(TEXT, javascriptActions.getElementValue(webElement));
    }

    @Test
    void shouldReturnDevicePixelRatio()
    {
        double dpr = 1.5;
        Number devicePixelRatioValue = dpr;
        mockScriptExecution(SCRIPT_GET_DEVICE_PIXEL_RATIO, devicePixelRatioValue);
        assertEquals(dpr, javascriptActions.getDevicePixelRatio());
        assertEquals(dpr, javascriptActions.getDevicePixelRatio());
        verify((JavascriptExecutor) webDriver).executeScript(SCRIPT_GET_DEVICE_PIXEL_RATIO);
    }

    @Test
    void shouldResetDevicePixelRatioOnDeviceMetricsOverrideEvent()
    {
        double dpr1 = 1.5;
        double dpr2 = 2.5;
        mockScriptExecution(SCRIPT_GET_DEVICE_PIXEL_RATIO, dpr1, dpr2);
        assertEquals(dpr1, javascriptActions.getDevicePixelRatio());
        javascriptActions.onDeviceMetricsOverride(new DeviceMetricsOverrideEvent());
        assertEquals(dpr2, javascriptActions.getDevicePixelRatio());
        assertEquals(dpr2, javascriptActions.getDevicePixelRatio());
        verify((JavascriptExecutor) webDriver, times(2)).executeScript(SCRIPT_GET_DEVICE_PIXEL_RATIO);
    }

    @Test
    void shouldResetDevicePixelRatioOnAfterWebDriverQuitEvent()
    {
        double dpr1 = 1.5;
        double dpr2 = 2.5;
        mockScriptExecution(SCRIPT_GET_DEVICE_PIXEL_RATIO, dpr1, dpr2);
        assertEquals(dpr1, javascriptActions.getDevicePixelRatio());
        javascriptActions.onAfterWebDriverQuitEvent(new AfterWebDriverQuitEvent(""));
        assertEquals(dpr2, javascriptActions.getDevicePixelRatio());
        assertEquals(dpr2, javascriptActions.getDevicePixelRatio());
        verify((JavascriptExecutor) webDriver, times(2)).executeScript(SCRIPT_GET_DEVICE_PIXEL_RATIO);
    }

    @Test
    void testGetElementAttributesValues()
    {
        var webElement = mock(WebElement.class);
        var attributes = Map.of(TEXT, TEXT);
        when(((JavascriptExecutor) webDriver).executeScript(SCRIPT_GET_ELEMENT_ATTRIBUTES, webElement))
                .thenReturn(attributes);
        assertEquals(javascriptActions.getElementAttributes(webElement), attributes);
    }

    @Test
    void testGetViewportSize()
    {
        Map<String, Long> map = new HashMap<>();
        Long width = 375L;
        Long height = 600L;
        map.put("width", width);
        map.put("height", height);
        var viewportSize = "return {width: Math.max(document.documentElement.clientWidth, window.innerWidth || 0),"
                + "height: Math.max(document.documentElement.clientHeight, window.innerHeight || 0)}";
        when(((JavascriptExecutor) webDriver).executeScript(viewportSize)).thenReturn(map);
        var size = javascriptActions.getViewportSize();
        assertEquals(new Dimension(width.intValue(), height.intValue()), size);
    }

    @Test
    void shouldScrollToTheStartOfWebElement()
    {
        var webElement = mock(WebElement.class);
        javascriptActions.scrollToLeftOf(webElement);
        verify((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollLeft=0;", webElement);
    }

    @Test
    void shouldScrollToTheEndOfWebElement()
    {
        var webElement = mock(WebElement.class);
        javascriptActions.scrollToRightOf(webElement);
        verify((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollLeft=arguments[0].scrollWidth;",
                webElement);
    }

    @Test
    void shouldExecuteScriptWaitingForScrollFinish()
    {
        javascriptActions.waitUntilScrollFinished();
        verifyWaitingForScrollFinish();
        verifyNoMoreInteractions(webDriver);
    }

    @Test
    void shouldStopPageLoadingAndReturnResult()
    {
        var script =   "let before = document.readyState;"
                        + "window.stop();"
                        + "return {before: before, after: document.readyState};";
        var result = Map.of("before", "interactive", "after", "complete");
        when(((JavascriptExecutor) webDriver).executeScript(script)).thenReturn(result);
        assertEquals(result, javascriptActions.stopPageLoading());
    }

    private void mockScriptExecution(String script, Object... results)
    {
        OngoingStubbing<Object> stubbing = when(((JavascriptExecutor) webDriver).executeScript(script));
        for (Object result : results)
        {
            stubbing = stubbing.thenReturn(result);
        }
    }

    private void mockIsFirefox(boolean firefox)
    {
        when(webDriverManager.isBrowserAnyOf(Browser.FIREFOX)).thenReturn(firefox);
    }

    private void verifyWaitingForScrollFinish()
    {
        verify((JavascriptExecutor) webDriver).executeAsyncScript(
                ResourceUtils.loadResource(WebJavascriptActions.class, "wait-for-scroll.js"));
    }
}
