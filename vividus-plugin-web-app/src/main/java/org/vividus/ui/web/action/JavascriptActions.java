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

import java.util.Map;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.TextUtils;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.web.listener.IWebApplicationListener;
import org.vividus.ui.web.model.Position;
import org.vividus.util.ResourceUtils;

public class JavascriptActions implements IJavascriptActions, IWebApplicationListener
{
    private static final String TRIGGER_EVENT_FORMAT = "if(document.createEvent){var evObj = document"
            + ".createEvent('MouseEvents');evObj.initEvent('%1$s', true, false); arguments[0].dispatchEvent(evObj);} "
            + "else if(document.createEventObject) { arguments[0].fireEvent('on%1$s');}";

    private final IWebDriverProvider webDriverProvider;
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

    public JavascriptActions(IWebDriverProvider webDriverProvider, IWebDriverManager webDriverManager)
    {
        this.webDriverProvider = webDriverProvider;
        this.webDriverManager = webDriverManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T executeScript(String script, Object... args)
    {
        return (T) getJavascriptExecutor().executeScript(script, args);
    }

    @Override
    public <T> T executeScriptFromResource(Class<?> clazz, String jsResourceName, Object... args)
    {
        return executeScript(ResourceUtils.loadResource(clazz, jsResourceName), args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T executeAsyncScript(String script, Object... args)
    {
        return (T) getJavascriptExecutor().executeAsyncScript(script, args);
    }

    @Override
    public <T> T executeAsyncScriptFromResource(Class<?> clazz, String jsResourceName, Object... args)
    {
        return executeAsyncScript(ResourceUtils.loadResource(clazz, jsResourceName), args);
    }

    @Override
    public void scrollIntoView(WebElement webElement, boolean alignedToTheTop)
    {
        executeScript("arguments[0].scrollIntoView(arguments[1])", webElement, alignedToTheTop);
    }

    @Override
    public void scrollToEndOfPage()
    {
        executeAsyncScriptFromResource(JavascriptActions.class, "scroll-to-end-of-page.js");
    }

    @Override
    public void scrollToStartOfPage()
    {
        executeScript("if (window.PageYOffset || document.documentElement.scrollTop > 0){ window.scrollTo(0,0);}");
    }

    @Override
    public void scrollToEndOf(WebElement element)
    {
        executeScript("arguments[0].scrollTo(0, arguments[0].scrollHeight)", element);
    }

    @Override
    public void scrollToStartOf(WebElement element)
    {
        executeScript("arguments[0].scrollTop = 0", element);
    }

    @Override
    public void openPageUrlInNewWindow(String pageUrl)
    {
        executeScript("window.open(arguments[0])", pageUrl);
    }

    @Override
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

    @Override
    public String getElementText(WebElement webElement)
    {
        return getFormattedInnerText("arguments[0]", webElement);
    }

    @Override
    public String getPageText()
    {
        return getFormattedInnerText("document.body");
    }

    private String getFormattedInnerText(String context, Object... args)
    {
        boolean firefox = webDriverManager.isTypeAnyOf(WebDriverType.FIREFOX);
        String innerTextJs = String.format(firefox ? "return %s.textContent" : "return %s.innerText", context);
        return TextUtils.normalizeText(executeScript(innerTextJs, args));
    }

    @Override
    public String getElementValue(WebElement webElement)
    {
        return executeScript("return arguments[0].value", webElement);
    }

    @Override
    public void onLoad()
    {
        browserConfig.get();
    }

    @Override
    public String getUserAgent()
    {
        return browserConfig.get().userAgent;
    }

    @Override
    public double getDevicePixelRatio()
    {
        return browserConfig.get().devicePixelRatio;
    }

    @Override
    public Map<String, String> getElementAttributes(WebElement webElement)
    {
        return executeScript("var attributes = arguments[0].attributes; var map = new Object();"
                + " for(i=0; i< attributes.length; i++){ map[attributes[i].name] = attributes[i].value; } return map;",
                webElement);
    }

    @Override
    public Position getElementPosition(WebElement webElement)
    {
        Map<String, Long> result = executeScript("var coordinates = arguments[0].getBoundingClientRect();"
                + " var left = Math.round(coordinates.left), top = Math.round(coordinates.top); return {left, top};",
                webElement);
        return new Position(result.get("top").intValue(), result.get("left").intValue());
    }

    @Override
    public int setElementTopPosition(WebElement webElement, int top)
    {
        Long originTop = executeScript(String.format("var originTop = arguments[0].getBoundingClientRect().top;"
                + " arguments[0].style.top = \"%dpx\"; return Math.round(originTop);", top), webElement);
        return originTop.intValue();
    }

    @Override
    public Dimension getViewportSize()
    {
        Map<String, Long> result = executeScript(
                "return {width: Math.max(document.documentElement.clientWidth, window.innerWidth || 0),"
                        + "height: Math.max(document.documentElement.clientHeight, window.innerHeight || 0)}");
        return new Dimension(result.get("width").intValue(), result.get("height").intValue());
    }

    private JavascriptExecutor getJavascriptExecutor()
    {
        return (JavascriptExecutor) webDriverProvider.get();
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
