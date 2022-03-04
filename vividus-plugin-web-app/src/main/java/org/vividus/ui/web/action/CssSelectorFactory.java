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

package org.vividus.ui.web.action;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.openqa.selenium.WebElement;
import org.vividus.util.ResourceUtils;

/**
 * <a href="https://www.w3schools.com/cssref/css_selectors.asp">CSS Selector</a> factory
 */
public class CssSelectorFactory implements ICssSelectorFactory
{
    public static final String CSS_SELECTOR_FACTORY_SCRIPT = ResourceUtils.loadResource(
            CssSelectorFactory.class, "css-selector-factory.js");

    private static final String CSS_SELECTOR_CALCULATION = CSS_SELECTOR_FACTORY_SCRIPT
            + "var source = arguments[0];\n"
            + "if (Array.isArray(source)) {\n"
            + "    return Array.prototype.slice.call(source).map(getCssSelectorForElement)\n"
            + "}\n"
            + "else {\n"
            + "    return getCssSelectorForElement(source);\n"
            + "}";

    @Inject private WebJavascriptActions javascriptActions;

    @Override
    public String getCssSelector(WebElement element)
    {
        return javascriptActions.executeScript(CSS_SELECTOR_CALCULATION, element);
    }

    @Override
    public String getCssSelector(Collection<WebElement> elements)
    {
        return getCssSelectors(elements).collect(Collectors.joining(","));
    }

    @Override
    public Stream<String> getCssSelectors(Collection<WebElement> elements)
    {
        List<String> elementCss = javascriptActions.executeScript(CSS_SELECTOR_CALCULATION, elements);
        return elementCss.stream();
    }
}
