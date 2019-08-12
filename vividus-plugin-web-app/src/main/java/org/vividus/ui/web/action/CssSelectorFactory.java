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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.openqa.selenium.WebElement;

/**
 * <a href="http://www.w3schools.com/cssref/css_selectors.asp">CSS Selector</a> factory
 */
public class CssSelectorFactory implements ICssSelectorFactory
{
    private static final String CSS_SELECTOR_FACTORY_SCRIPT_FILENAME = "css-selector-factory.js";

    @Inject private IJavascriptActions javascriptActions;

    @Override
    public String getCssSelector(WebElement element)
    {
        String elementCss = javascriptActions.executeScriptFromResource(CssSelectorFactory.class,
                CSS_SELECTOR_FACTORY_SCRIPT_FILENAME, element);
        return escapeSharpSymbol(elementCss);
    }

    private String escapeSharpSymbol(String elementCss)
    {
        if (elementCss.length() > 1)
        {
            char firstIdChar = elementCss.charAt(1);
            if (elementCss.charAt(0) == '#' && Character.isDigit(firstIdChar))
            {
                return "#\\" + Integer.toHexString(firstIdChar) + elementCss.substring(2);
            }
        }
        return elementCss;
    }

    @Override
    public String getCssSelector(Collection<WebElement> elements)
    {
        return getCssSelectors(elements).collect(Collectors.joining(","));
    }

    @Override
    public Stream<String> getCssSelectors(Collection<WebElement> elements)
    {
        List<String> elementCss = javascriptActions.executeScriptFromResource(CssSelectorFactory.class,
                CSS_SELECTOR_FACTORY_SCRIPT_FILENAME, elements);
        return elementCss.stream().map(this::escapeSharpSymbol);
    }
}
