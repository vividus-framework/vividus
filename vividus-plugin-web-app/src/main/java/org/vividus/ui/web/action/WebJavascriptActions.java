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

import java.util.Map;

import com.google.common.eventbus.Subscribe;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Browser;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.TextUtils;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.testcontext.TestContext;
import org.vividus.ui.ViewportSizeProvider;
import org.vividus.ui.action.JavascriptActions;
import org.vividus.ui.web.event.DeviceMetricsOverrideEvent;
import org.vividus.util.ResourceUtils;

public class WebJavascriptActions extends JavascriptActions
        implements ViewportSizeProvider, org.vividus.ui.web.action.JavascriptActions
{
    private static final String TRIGGER_EVENT_FORMAT = "if(document.createEvent){var evObj = document"
            + ".createEvent('MouseEvents');evObj.initEvent('%1$s', true, false); arguments[0].dispatchEvent(evObj);} "
            + "else if(document.createEventObject) { arguments[0].fireEvent('on%1$s');}";

    private static final String SCROLL_ELEMENT_INTO_VIEWPORT_CENTER =
        loadScript("scroll-element-into-viewport-center.js");

    private static final String WAIT_FOR_SCROLL = loadScript("wait-for-scroll.js");

    private static final String SCROLL_TO_END_OF_PAGE = loadScript("scroll-to-end-of-page.js");

    private final IWebDriverManager webDriverManager;
    private final TestContext testContext;

    private int stickyHeaderSizePercentage;

    public WebJavascriptActions(IWebDriverProvider webDriverProvider, IWebDriverManager webDriverManager,
            TestContext testContext)
    {
        super(webDriverProvider);
        this.webDriverManager = webDriverManager;
        this.testContext = testContext;
    }

    /**
     * Loads script from resource, executes it and returns result
     * @param <T> the type of the returned result
     * @param clazz Class to search resource relatively
     * @param jsResourceName script input parameters
     * @param args script input parameters
     * @return result of the script execution
     */
    public <T> T executeScriptFromResource(Class<?> clazz, String jsResourceName, Object... args)
    {
        return executeScript(ResourceUtils.loadResource(clazz, jsResourceName), args);
    }

    @SuppressWarnings("unchecked")
    public <T> T executeAsyncScript(String script, Object... args)
    {
        return (T) getJavascriptExecutor().executeAsyncScript(script, args);
    }

    /**
     * Loads an asynchronous script from resource, executes it and returns result
     * @param <T> the type of returned result
     * @param clazz Class to search resource relatively
     * @param jsResourceName script input parameters
     * @param args script input parameters
     * @return result of the script execution
     */
    public <T> T executeAsyncScriptFromResource(Class<?> clazz, String jsResourceName, Object... args)
    {
        return executeAsyncScript(loadScript(clazz, jsResourceName), args);
    }

    private static String loadScript(Class<?> clazz, String jsResourceName)
    {
        return ResourceUtils.loadResource(clazz, jsResourceName);
    }

    private static String loadScript(String jsResourceName)
    {
        return loadScript(WebJavascriptActions.class, jsResourceName);
    }

    public void scrollIntoView(WebElement webElement, boolean alignedToTheTop)
    {
        executeScript("arguments[0].scrollIntoView(arguments[1])", webElement, alignedToTheTop);
        waitUntilScrollFinished();
    }

    public void scrollElementIntoViewportCenter(WebElement webElement)
    {
        executeAsyncScript(SCROLL_ELEMENT_INTO_VIEWPORT_CENTER, webElement, stickyHeaderSizePercentage);
    }

    /**
     * Scrolls to the end of the page with dynamically loading content upon scrolling
     */
    public void scrollToEndOfPage()
    {
        executeAsyncScript(SCROLL_TO_END_OF_PAGE);
    }

    /**
     * Scrolls page to the top (0, 0)
     */
    public void scrollToStartOfPage()
    {
        executeScript("if (window.PageYOffset || document.documentElement.scrollTop > 0){ window.scrollTo(0,0);}");
    }

    public void scrollToEndOf(WebElement element)
    {
        executeScript("arguments[0].scrollTop = arguments[0].scrollHeight", element);
    }

    public void scrollToStartOf(WebElement element)
    {
        executeScript("arguments[0].scrollTop = 0", element);
    }

    /**
     * Opens new window
     */
    public void openNewTab()
    {
        executeScript("window.open()");
    }

    public void closeCurrentTab()
    {
        executeScript("window.close()");
    }

    public void triggerMouseEvents(WebElement webElement, String... eventTypes)
    {
        StringBuilder script = new StringBuilder();
        for (String eventType : eventTypes)
        {
            script.append(String.format(TRIGGER_EVENT_FORMAT, eventType));
        }
        executeScript(script.toString(), webElement);
    }

    public void click(WebElement webElement)
    {
        executeScript("arguments[0].click()", webElement);
    }

    public String getElementText(WebElement webElement)
    {
        return getFormattedInnerText("arguments[0]", webElement);
    }

    public String getPageText()
    {
        return getFormattedInnerText("document.body");
    }

    public Map<String, String> stopPageLoading()
    {
        return executeScript(
            "let before = document.readyState;"
          + "window.stop();"
          + "return {before: before, after: document.readyState};");
    }

    private String getFormattedInnerText(String context, Object... args)
    {
        boolean firefox = webDriverManager.isBrowserAnyOf(Browser.FIREFOX);
        String innerTextJs = String.format(firefox ? "return %s.textContent" : "return %s.innerText", context);
        return TextUtils.normalizeText(executeScript(innerTextJs, args));
    }

    public String getElementValue(WebElement webElement)
    {
        return executeScript("return arguments[0].value", webElement);
    }

    public double getDevicePixelRatio()
    {
        return testContext.get(DevicePixelRatio.class,
                () -> ((Number) executeScript("return window.devicePixelRatio;")).doubleValue());
    }

    public Map<String, String> getElementAttributes(WebElement webElement)
    {
        return executeScript("var attributes = arguments[0].attributes; var map = new Object();"
                + " for(i=0; i< attributes.length; i++){ map[attributes[i].name] = attributes[i].value; } return map;",
                webElement);
    }

    @Override
    public Dimension getViewportSize()
    {
        Map<String, Long> result = executeScript(
                "return {width: Math.max(document.documentElement.clientWidth, window.innerWidth || 0),"
                        + "height: Math.max(document.documentElement.clientHeight, window.innerHeight || 0)}");
        return new Dimension(result.get("width").intValue(), result.get("height").intValue());
    }

    public void scrollToLeftOf(WebElement element)
    {
        executeScript("arguments[0].scrollLeft=0;", element);
    }

    public void scrollToRightOf(WebElement element)
    {
        executeScript("arguments[0].scrollLeft=arguments[0].scrollWidth;", element);
    }

    /**
     * Waits for the scroll events finish via async JS script execution;
     */
    public void waitUntilScrollFinished()
    {
        executeAsyncScript(WAIT_FOR_SCROLL);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onDeviceMetricsOverride(DeviceMetricsOverrideEvent event)
    {
        clearDevicePixelRatio();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onAfterWebDriverQuitEvent(AfterWebDriverQuitEvent event)
    {
        clearDevicePixelRatio();
    }

    private void clearDevicePixelRatio()
    {
        testContext.remove(DevicePixelRatio.class);
    }

    public void setStickyHeaderSizePercentage(int stickyHeaderSizePercentage)
    {
        this.stickyHeaderSizePercentage = stickyHeaderSizePercentage;
    }

    private record DevicePixelRatio()
    {
    }
}
