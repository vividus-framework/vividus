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

import java.util.Map;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Browser;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.TextUtils;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.action.JavascriptActions;
import org.vividus.ui.web.listener.IWebApplicationListener;
import org.vividus.util.ResourceUtils;

public class WebJavascriptActions extends JavascriptActions implements IWebApplicationListener
{
    private static final String TRIGGER_EVENT_FORMAT = "if(document.createEvent){var evObj = document"
            + ".createEvent('MouseEvents');evObj.initEvent('%1$s', true, false); arguments[0].dispatchEvent(evObj);} "
            + "else if(document.createEventObject) { arguments[0].fireEvent('on%1$s');}";

    private final IWebDriverManager webDriverManager;

    private final ThreadLocal<BrowserConfig> browserConfig = ThreadLocal.withInitial(() -> {
        String userAgentKey = "userAgent";
        String devicePixelRatioKey = "devicePixelRatio";
        Map<String, ?> browserConfig = executeScript("return {"
                + userAgentKey + ": navigator.userAgent,"
                + devicePixelRatioKey + ": window.devicePixelRatio"
                + "}");
        String userAgent = (String) browserConfig.get(userAgentKey);
        double devicePixelRatio = ((Number) browserConfig.get(devicePixelRatioKey)).doubleValue();
        return new BrowserConfig(userAgent, devicePixelRatio);
    });

    public WebJavascriptActions(IWebDriverProvider webDriverProvider, IWebDriverManager webDriverManager)
    {
        super(webDriverProvider);
        this.webDriverManager = webDriverManager;
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
        return executeAsyncScript(ResourceUtils.loadResource(clazz, jsResourceName), args);
    }

    public void scrollIntoView(WebElement webElement, boolean alignedToTheTop)
    {
        executeScript("arguments[0].scrollIntoView(arguments[1])", webElement, alignedToTheTop);
    }

    public void scrollElementIntoViewportCenter(WebElement webElement)
    {
        executeAsyncScriptFromResource("scroll-element-into-viewport-center.js", webElement);
    }

    /**
     * Scrolls to the end of the page with dynamically loading content upon scrolling
     */
    public void scrollToEndOfPage()
    {
        executeAsyncScriptFromResource("scroll-to-end-of-page.js");
    }

    private void executeAsyncScriptFromResource(String resource, Object... args)
    {
        executeAsyncScriptFromResource(WebJavascriptActions.class, resource, args);
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
    public void openNewWindow()
    {
        executeScript("window.open()");
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

    @Override
    public void onLoad()
    {
        browserConfig.get();
    }

    public String getUserAgent()
    {
        return browserConfig.get().userAgent;
    }

    public double getDevicePixelRatio()
    {
        return browserConfig.get().devicePixelRatio;
    }

    public Map<String, String> getElementAttributes(WebElement webElement)
    {
        return executeScript("var attributes = arguments[0].attributes; var map = new Object();"
                + " for(i=0; i< attributes.length; i++){ map[attributes[i].name] = attributes[i].value; } return map;",
                webElement);
    }

    /**
     * Sets the top position of a positioned element.
     * @param top specifies the top position of the element including padding, scrollbar, border and margin
     *  for a given WebElement
     * @param webElement WebElement to set top for
     * @return origin top position of webElement
     */
    public int setElementTopPosition(WebElement webElement, int top)
    {
        Long originTop = executeScript(String.format("var originTop = arguments[0].getBoundingClientRect().top;"
                + " arguments[0].style.top = \"%dpx\"; return Math.round(originTop);", top), webElement);
        return originTop.intValue();
    }

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
        executeAsyncScriptFromResource("wait-for-scroll.js");
    }

    private static final class BrowserConfig
    {
        private final String userAgent;
        private final double devicePixelRatio;

        private BrowserConfig(String userAgent, double devicePixelRatio)
        {
            this.userAgent = userAgent;
            this.devicePixelRatio = devicePixelRatio;
        }
    }
}
