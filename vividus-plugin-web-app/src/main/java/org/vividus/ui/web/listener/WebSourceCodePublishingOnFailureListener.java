/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.ui.web.listener;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.listener.AbstractSourceCodePublishingOnFailureListener;
import org.vividus.ui.web.action.CssSelectorFactory;
import org.vividus.ui.web.action.WebJavascriptActions;

public class WebSourceCodePublishingOnFailureListener extends AbstractSourceCodePublishingOnFailureListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSourceCodePublishingOnFailureListener.class);

    private final IUiContext uiContext;
    private final WebJavascriptActions webJavascriptActions;

    protected WebSourceCodePublishingOnFailureListener(IAttachmentPublisher attachmentPublisher, IWebDriverProvider
             webDriverProvider, IUiContext uiContext, WebJavascriptActions webJavascriptActions)
    {
        super(attachmentPublisher, webDriverProvider, "HTML");
        this.uiContext = uiContext;
        this.webJavascriptActions = webJavascriptActions;
    }

    @Override
    protected Map<String, String> getSourceCode()
    {
        SearchContext searchContext = uiContext.getSearchContext();
        String sourceCode = null;
        boolean elementInContext = searchContext instanceof WebElement;
        if (elementInContext)
        {
            sourceCode = getElementSource(searchContext);
        }
        else if (searchContext instanceof WebDriver)
        {
            sourceCode = ((WebDriver) searchContext).getPageSource();
        }
        Map<String, String> sources = new LinkedHashMap<>();
        if (sourceCode != null)
        {
            sources.put(APPLICATION_SOURCE_CODE, sourceCode);
        }
        sources.putAll(getShadowDomSourceCode(elementInContext, searchContext));
        return sources;
    }

    private Map<String, String> getShadowDomSourceCode(boolean elementInContext, SearchContext searchContext)
    {
        return elementInContext ? getShadowRootSource("arguments[0]", searchContext)
                                : getShadowRootSource("document.documentElement");
    }

    private Map<String, String> getShadowRootSource(String rootElement, Object... args)
    {
        return webJavascriptActions.executeScript(
            CssSelectorFactory.CSS_SELECTOR_FACTORY_SCRIPT
                + "const sources = new Map();\n"
                + "function getShadowSource(element) {\n"
                + "    Array.from(element.querySelectorAll('*'))"
                + "         .filter(node => node.shadowRoot)"
                + "         .forEach(e => {\n"
                + "                           sources.set('Shadow dom sources. Selector: '"
                + "                               + getCssSelectorForElement(e),"
                + "                           e.shadowRoot.innerHTML);\n"
                + "                           getShadowSource(e.shadowRoot);\n"
                + "          })\n"
                + "}\n"
                + "getShadowSource(" + rootElement + ");\n"
                + "return Object.fromEntries(sources)", args);
    }

    private String getElementSource(SearchContext searchContext)
    {
        try
        {
            return ((WebElement) searchContext).getAttribute("innerHTML");
        }
        catch (StaleElementReferenceException exception)
        {
            LOGGER.debug("Unable to get sources of the stale element");
            return null;
        }
    }
}
