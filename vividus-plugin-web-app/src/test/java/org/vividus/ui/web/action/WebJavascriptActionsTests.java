/*
 * Copyright 2019-2021 the original author or authors.
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
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Browser;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
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
    private static final String SCRIPT_SET_TOP_POSITION = "var originTop = arguments[0].getBoundingClientRect().top;"
            + " arguments[0].style.top = \"%dpx\"; return Math.round(originTop);";

    private static final String GET_BROWSER_CONFIG_JS = "return {userAgent: navigator.userAgent,"
            + "devicePixelRatio: window.devicePixelRatio}";
    private static final String USER_AGENT_VALUE = "Mozilla";
    private static final Double DEVICE_PIXEL_RATIO_VALUE = 1.5;
    private static final Map<String, ?> BROWSER_CONFIG = Map.of("userAgent", USER_AGENT_VALUE, "devicePixelRatio",
            DEVICE_PIXEL_RATIO_VALUE);

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private IWebDriverManager webDriverManager;

    @Mock(extraInterfaces = { JavascriptExecutor.class, HasCapabilities.class })
    private WebDriver webDriver;

    @InjectMocks
    private WebJavascriptActions javascriptActions;

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
    void testExecuteAsyncSript()
    {
        String script = "asyncScript";
        String arg1 = "asyncScriptArg1";
        String arg2 = "asyncScriptArg2";
        javascriptActions.executeAsyncScript(script, arg1, arg2);
        verify((JavascriptExecutor) webDriver).executeAsyncScript(script, arg1, arg2);
    }

    @Test
    void testScrollIntoView()
    {
        WebElement webElement = mock(WebElement.class);
        javascriptActions.scrollIntoView(webElement, true);
        verify((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView(arguments[1])", webElement,
                true);
    }

    @Test
    void testScrollElementIntoViewportCenter()
    {
        WebElement webElement = mock(WebElement.class);
        javascriptActions.scrollElementIntoViewportCenter(webElement);
        verify((JavascriptExecutor) webDriver).executeAsyncScript(
                ResourceUtils.loadResource(WebJavascriptActionsTests.class, "scroll-element-into-viewport-center.js"),
                webElement);
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
        WebElement webElement = mock(WebElement.class);
        javascriptActions.scrollToEndOf(webElement);
        verify((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight",
                webElement);
    }

    @Test
    void testScrollToStartOf()
    {
        WebElement webElement = mock(WebElement.class);
        javascriptActions.scrollToStartOf(webElement);
        verify((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollTop = 0", webElement);
    }

    @Test
    void testOpenNewWindow()
    {
        javascriptActions.openNewWindow();
        verify((JavascriptExecutor) webDriver).executeScript("window.open()");
    }

    @Test
    void testTriggerEvents()
    {
        WebElement webElement = mock(WebElement.class);
        String script = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');"
                + "evObj.initEvent('click', true, false); arguments[0].dispatchEvent(evObj);}"
                + " else if(document.createEventObject) { arguments[0].fireEvent('onclick');}";
        javascriptActions.triggerMouseEvents(webElement, "click");
        verify((JavascriptExecutor) webDriver).executeScript(script, webElement);
    }

    @Test
    void testClick()
    {
        WebElement webElement = mock(WebElement.class);
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
        WebElement webElement = mock(WebElement.class);
        mockIsFirefox(false);
        when(((JavascriptExecutor) webDriver).executeScript(ELEMENT_INNER_TEXT, webElement)).thenReturn(TEXT);
        assertEquals(TEXT, javascriptActions.getElementText(webElement));
    }

    @Test
    void testGetElementValue()
    {
        WebElement webElement = mock(WebElement.class);
        when(((JavascriptExecutor) webDriver).executeScript("return arguments[0].value", webElement))
                .thenReturn(TEXT);
        assertEquals(TEXT, javascriptActions.getElementValue(webElement));
    }

    @Test
    void testOnLoadBrowserConfig()
    {
        mockScriptExecution(GET_BROWSER_CONFIG_JS, BROWSER_CONFIG);
        javascriptActions.onLoad();
        assertEquals(USER_AGENT_VALUE, javascriptActions.getUserAgent());
        assertEquals(DEVICE_PIXEL_RATIO_VALUE, javascriptActions.getDevicePixelRatio());
    }

    @Test
    void testOnLoadMultipleTimes()
    {
        mockScriptExecution(GET_BROWSER_CONFIG_JS, BROWSER_CONFIG);
        javascriptActions.onLoad();
        javascriptActions.onLoad();
        verify((JavascriptExecutor) webDriver, times(1)).executeScript(GET_BROWSER_CONFIG_JS);
    }

    @Test
    void testGetElementAttributesValues()
    {
        WebElement webElement = mock(WebElement.class);
        Map<String, String> attributes = Map.of(TEXT, TEXT);
        when(((JavascriptExecutor) webDriver).executeScript(SCRIPT_GET_ELEMENT_ATTRIBUTES, webElement))
                .thenReturn(attributes);
        assertEquals(javascriptActions.getElementAttributes(webElement), attributes);
    }

    @Test
    void testSetElementTopPosition()
    {
        WebElement webElement = mock(WebElement.class);
        Long top = 10L;
        when(((JavascriptExecutor) webDriver).executeScript(String.format(SCRIPT_SET_TOP_POSITION, top), webElement))
                .thenReturn(top);
        int resultTop = javascriptActions.setElementTopPosition(webElement, top.intValue());
        assertEquals(top.intValue(), resultTop);
    }

    @Test
    void testGetViewportSize()
    {
        Map<String, Long> map = new HashMap<>();
        Long width = 375L;
        Long height = 600L;
        map.put("width", width);
        map.put("height", height);
        String viewportSize = "return {width: Math.max(document.documentElement.clientWidth, window.innerWidth || 0),"
                + "height: Math.max(document.documentElement.clientHeight, window.innerHeight || 0)}";
        when(((JavascriptExecutor) webDriver).executeScript(viewportSize)).thenReturn(map);
        Dimension size = javascriptActions.getViewportSize();
        assertEquals(new Dimension(width.intValue(), height.intValue()), size);
    }

    @Test
    void shouldScrollToTheStartOfWebElement()
    {
        WebElement webElement = mock(WebElement.class);
        javascriptActions.scrollToLeftOf(webElement);
        verify((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollLeft=0;", webElement);
    }

    @Test
    void shouldScrollToTheEndOfWebElement()
    {
        WebElement webElement = mock(WebElement.class);
        javascriptActions.scrollToRightOf(webElement);
        verify((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollLeft=arguments[0].scrollWidth;",
                webElement);
    }

    @Test
    void shouldExecuteScriptWaitingForScrollFinish()
    {
        javascriptActions.waitUntilScrollFinished();
        verify((JavascriptExecutor) webDriver).executeAsyncScript(
                ResourceUtils.loadResource(WebJavascriptActionsTests.class, "wait-for-scroll.js"));
    }

    @Test
    void shouldStopPageLoadingAndReturnResult()
    {
        String script =   "let before = document.readyState;"
                        + "window.stop();"
                        + "return {before: before, after: document.readyState};";
        Map<String, String> result = Map.of("before", "interactive", "after", "complete");
        when(((JavascriptExecutor) webDriver).executeScript(script)).thenReturn(result);
        assertEquals(result, javascriptActions.stopPageLoading());
    }

    private void mockScriptExecution(String script, Object result)
    {
        when(((JavascriptExecutor) webDriver).executeScript(script)).thenReturn(result);
    }

    private void mockIsFirefox(boolean firefox)
    {
        when(webDriverManager.isBrowserAnyOf(Browser.FIREFOX)).thenReturn(firefox);
    }
}
