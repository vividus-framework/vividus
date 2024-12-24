/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.selenium;

import java.util.Map;
import java.util.Optional;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.CssSelectorFactory;
import org.vividus.ui.web.action.WebJavascriptActions;

public class WebContextSourceCodeProvider implements WebAppContextSourceCodeProvider
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WebContextSourceCodeProvider.class);

    private final IUiContext uiContext;
    private final WebJavascriptActions webJavascriptActions;

    protected WebContextSourceCodeProvider(IUiContext uiContext, WebJavascriptActions webJavascriptActions)
    {
        this.uiContext = uiContext;
        this.webJavascriptActions = webJavascriptActions;
    }

    @Override
    public Optional<String> getSourceCode()
    {
        SearchContext searchContext = uiContext.getSearchContext();
        String sourceCode = null;
        boolean elementInContext = searchContext instanceof WebElement;
        if (elementInContext)
        {
            sourceCode = getElementSource(searchContext);
        }
        else if (searchContext instanceof WebDriver webDriver)
        {
            sourceCode = webDriver.getPageSource();
        }
        return Optional.ofNullable(sourceCode);
    }

    @Override
    public Map<String, String> getShadowDomSourceCode()
    {
        Optional<SearchContext> searchContextOpt = uiContext.getOptionalSearchContextSafely();
        if (searchContextOpt.isPresent())
        {
            SearchContext searchContext = searchContextOpt.get();
            return searchContext instanceof WebElement ? getShadowRootSource("arguments[0]", searchContext)
                                : getShadowRootSource("document.documentElement");
        }
        return Map.of();
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
                + "                           sources.set('Shadow dom source. Selector: '"
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
            return webJavascriptActions.executeScript("return arguments[0].outerHTML;", searchContext);
        }
        catch (StaleElementReferenceException exception)
        {
            LOGGER.debug("Unable to get sources of the stale element");
            return null;
        }
    }
}
