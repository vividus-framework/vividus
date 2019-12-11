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

package org.vividus.ui.web.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.web.model.Position;
import org.vividus.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class JavascriptActionsTests
{
    private static final String SCROLL_TO_END_OF_PAGE = "scroll-to-end-of-page.js";
    private static final String TEXT = "text";
    private static final String BODY_INNER_TEXT = "return document.body.innerText";
    private static final String ELEMENT_INNER_TEXT = "return arguments[0].innerText";
    private static final String SCRIPT_GET_ELEMENT_ATTRIBUTES = "var attributes = arguments[0].attributes;"
            + " var map = new Object(); for(i=0; i< attributes.length; i++)"
            + "{ map[attributes[i].name] = attributes[i].value; } return map;";
    private static final String SCRIPT_GET_ELEMENT_POSITION = "var coordinates = arguments[0].getBoundingClientRect();"
            + " var left = Math.round(coordinates.left), top = Math.round(coordinates.top); return {left, top};";
    private static final String TOP = "top";
    private static final String LEFT = "left";
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

    @Mock
    private WebElement mockedWebElement;

    @Mock(extraInterfaces = { JavascriptExecutor.class, HasCapabilities.class })
    private WebDriver webDriver;

    @InjectMocks
    private JavascriptActions javascriptActions;

    @BeforeEach
    void beforeEach()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
    }

    @Test
    void testExecuteSript()
    {
        String script = "someScript";
        String arg1 = "arg1";
        String arg2 = "arg2";
        javascriptActions.executeScript(script, arg1, arg2);
        verify((JavascriptExecutor) webDriver).executeScript(script, arg1, arg2);
    }

    @Test
    void testExecuteScriptFromResource()
    {
        javascriptActions.executeScriptFromResource(JavascriptActions.class, SCROLL_TO_END_OF_PAGE);
        verify((JavascriptExecutor) webDriver)
                .executeScript(ResourceUtils.loadResource(JavascriptActions.class, SCROLL_TO_END_OF_PAGE));
    }

    @Test
    void testExecuteAsyncScriptFromResource()
    {
        javascriptActions.executeAsyncScriptFromResource(JavascriptActions.class, SCROLL_TO_END_OF_PAGE);
        verify((JavascriptExecutor) webDriver)
                .executeAsyncScript(ResourceUtils.loadResource(JavascriptActions.class, SCROLL_TO_END_OF_PAGE));
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
        javascriptActions.scrollIntoView(mockedWebElement, true);
        verify((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView(arguments[1])",
                mockedWebElement, true);
    }

    @Test
    void testScrollToEndOfPage()
    {
        javascriptActions.scrollToEndOfPage();
        verify((JavascriptExecutor) webDriver).executeAsyncScript(
                ResourceUtils.loadResource(JavascriptActionsTests.class, SCROLL_TO_END_OF_PAGE));
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
        javascriptActions.scrollToEndOf(mockedWebElement);
        verify((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollTo(0, arguments[0].scrollHeight)",
                mockedWebElement);
    }

    @Test
    void testScrollToStartOf()
    {
        javascriptActions.scrollToStartOf(mockedWebElement);
        verify((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollTop = 0", mockedWebElement);
    }

    @Test
    void testOpenPageUrlInNewWindow()
    {
        String pageUrl = "url";
        javascriptActions.openPageUrlInNewWindow(pageUrl);
        verify((JavascriptExecutor) webDriver).executeScript("window.open(arguments[0])", pageUrl);
    }

    @Test
    void testTriggerEvents()
    {
        String script = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');"
                + "evObj.initEvent('click', true, false); arguments[0].dispatchEvent(evObj);}"
                + " else if(document.createEventObject) { arguments[0].fireEvent('onclick');}";
        javascriptActions.triggerMouseEvents(mockedWebElement, "click");
        verify((JavascriptExecutor) webDriver).executeScript(script, mockedWebElement);
    }

    @Test
    void testClick()
    {
        javascriptActions.click(mockedWebElement);
        verify((JavascriptExecutor) webDriver).executeScript("arguments[0].click()", mockedWebElement);
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
        mockIsFirefox(false);
        when(((JavascriptExecutor) webDriver).executeScript(ELEMENT_INNER_TEXT, mockedWebElement)).thenReturn(TEXT);
        assertEquals(TEXT, javascriptActions.getElementText(mockedWebElement));
    }

    @Test
    void testGetElementValue()
    {
        when(((JavascriptExecutor) webDriver).executeScript("return arguments[0].value", mockedWebElement))
                .thenReturn(TEXT);
        assertEquals(TEXT, javascriptActions.getElementValue(mockedWebElement));
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
        Map<String, String> attributes = new HashMap<>();
        attributes.put(TEXT, TEXT);
        when(((JavascriptExecutor) webDriver).executeScript(SCRIPT_GET_ELEMENT_ATTRIBUTES, mockedWebElement))
                .thenReturn(attributes);
        assertEquals(javascriptActions.getElementAttributes(mockedWebElement), attributes);
    }

    @Test
    void testGetElementPosition()
    {
        Map<String, Long> map = new HashMap<>();
        Long top = 1L;
        Long left = 2L;
        map.put(TOP, top);
        map.put(LEFT, left);
        when(((JavascriptExecutor) webDriver).executeScript(SCRIPT_GET_ELEMENT_POSITION, mockedWebElement))
                .thenReturn(map);
        Position position = javascriptActions.getElementPosition(mockedWebElement);
        assertEquals(position.getTop(), top.intValue());
        assertEquals(position.getLeft(), left.intValue());
    }

    @Test
    void testSetElementTopPosition()
    {
        Long top = 10L;
        when(((JavascriptExecutor) webDriver).executeScript(String.format(SCRIPT_SET_TOP_POSITION, top),
                mockedWebElement)).thenReturn(top);
        int resultTop = javascriptActions.setElementTopPosition(mockedWebElement, top.intValue());
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

    private void mockScriptExecution(String script, Object result)
    {
        when(((JavascriptExecutor) webDriver).executeScript(script)).thenReturn(result);
    }

    private void mockIsFirefox(boolean firefox)
    {
        when(webDriverManager.isTypeAnyOf(WebDriverType.FIREFOX)).thenReturn(firefox);
    }
}
