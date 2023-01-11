/*
 * Copyright 2019-2023 the original author or authors.
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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;

public class WebElementActions implements IWebElementActions
{
    private static final char APOSTROPHE = '\'';
    private static final char QUOTE = '"';

    private final WebJavascriptActions javascriptActions;

    public WebElementActions(WebJavascriptActions javascriptActions)
    {
        this.javascriptActions = javascriptActions;
    }

    @Override
    public String getCssValue(WebElement element, String propertyName)
    {
        String cssValue = StringUtils.remove(element.getCssValue(propertyName), QUOTE);
        return StringUtils.remove(cssValue, APOSTROPHE);
    }

    @Override
    public String getPseudoElementContent(final WebElement element)
    {
        String scriptFormat = "return window.getComputedStyle(arguments[0],'%1$s').getPropertyValue('content')";

        String content = javascriptActions.executeScript(String.format(scriptFormat, ":before"), element);
        if (content == null)
        {
            content = javascriptActions.executeScript(String.format(scriptFormat, ":after"), element);
        }
        // CHROME only: The script returns content in single or double quotes depending on the browser
        return content == null || content.isEmpty() || "none".equals(content)
                ? "" : content.substring(1, content.length() - 1);
    }

    @Override
    public List<String> getAllPseudoElementsContent()
    {
        String script = "var nodeList = document.querySelectorAll('*');var i;var contentList = [];"
                + "for (i = 0; i < nodeList.length; i++){"
                + "var valueBefore = window.getComputedStyle(nodeList[i],':before').getPropertyValue('content');"
                + "var valueAfter = window.getComputedStyle(nodeList[i],':after').getPropertyValue('content');"
                + "if (valueBefore != '' && valueBefore != undefined)"
                + "{contentList[contentList.length] = valueBefore;}"
                + "if (valueAfter != '' && valueAfter != undefined)" + "{contentList[contentList.length] = valueAfter;}"
                + "}return contentList;";
        return javascriptActions.executeScript(script);
    }

    @Override
    public String getPageText()
    {
        return javascriptActions.getPageText();
    }

    @Override
    public String getElementText(WebElement element)
    {
        if (element != null)
        {
            String textContent = element.getText();
            return textContent != null && !textContent.isEmpty() ? textContent
                    : javascriptActions.getElementText(element);
        }
        return null;
    }

    @Override
    public boolean isElementVisible(WebElement element)
    {
        return isElementVisible(element, false);
    }

    private boolean isElementVisible(WebElement element, boolean scrolled)
    {
        if (!element.isDisplayed())
        {
            if (!scrolled)
            {
                javascriptActions.scrollIntoView(element, true);
                return isElementVisible(element, true);
            }
            return false;
        }
        return true;
    }
}
