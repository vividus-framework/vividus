/*
 * Copyright 2019-2020 the original author or authors.
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

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.ui.web.action.IJavascriptActions;
import org.vividus.ui.web.context.IWebUiContext;

public class WebElementHighlighter implements IWebElementHighlighter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WebElementHighlighter.class);

    private static final String ENABLE_HIGHLIGHT_JS = "arguments[0].style.border=\"7px solid yellow\";"
            + "arguments[0].style.boxShadow=\"0px 0px 70px 20px red\"";
    private static final String DISABLE_HIGHLIGHT_JS = "arguments[0].style.border=\"\";"
            + "arguments[0].style.boxShadow=\"\"";

    @Inject private IJavascriptActions javascriptActions;
    @Inject private IWebUiContext webUiContext;

    @Override
    public <T> T takeScreenshotWithHighlights(Supplier<T> screenshotSupplier)
    {
        try
        {
            changeState(this::enableHighlighting);
            return screenshotSupplier.get();
        }
        finally
        {
            changeState(this::disableHighlighting);
        }
    }

    private void changeState(Consumer<WebElement> stateUpdater)
    {
        SearchContext searchContext = webUiContext.getSearchContext();
        if (searchContext instanceof WebElement)
        {
            stateUpdater.accept((WebElement) searchContext);
        }
        webUiContext.getAssertingWebElements().forEach(stateUpdater);
    }

    private void enableHighlighting(WebElement element)
    {
        executeJS(ENABLE_HIGHLIGHT_JS, element);
    }

    private void disableHighlighting(WebElement element)
    {
        executeJS(DISABLE_HIGHLIGHT_JS, element);
    }

    private void executeJS(String script, Object... args)
    {
        try
        {
            javascriptActions.executeScript(script, args);
        }
        catch (WebDriverException e)
        {
            LOGGER.warn("Can't highlight the element: {}", e.getMessage());
        }
    }
}
