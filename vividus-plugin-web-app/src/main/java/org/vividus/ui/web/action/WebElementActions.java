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

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.util.FormatUtil;

public class WebElementActions implements IWebElementActions
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WebElementActions.class);
    private static final char APOSTROPHE = '\'';
    private static final char QUOTE = '"';
    private static final int TEXT_TYPING_ATTEMPTS_LIMIT = 5;

    @Inject private IJavascriptActions javascriptActions;
    @Inject private IWebDriverManager webDriverManager;
    @Inject private ISoftAssert softAssert;

    @Override
    public String getCssValue(WebElement element, String propertyName)
    {
        if (element != null)
        {
            String cssValue = StringUtils.remove(element.getCssValue(propertyName), QUOTE);
            return StringUtils.remove(cssValue, APOSTROPHE);
        }
        return null;
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

    @SuppressWarnings("unchecked")
    @Override
    public void typeText(WebElement element, String text)
    {
        if (element != null)
        {
            String normalizedText = FormatUtil.normalizeLineEndings(text);
            element.clear();
            LOGGER.info("Entering text \"{}\" in element", normalizedText);
            if (webDriverManager.isTypeAnyOf(WebDriverType.SAFARI) && isElementContenteditable(element))
            {
                javascriptActions.executeScript("var element = arguments[0];element.innerHTML = arguments[1];", element,
                        normalizedText);
                return;
            }
            element.sendKeys(normalizedText);
            // Workaround for IExplore: https://github.com/seleniumhq/selenium/issues/805
            if (webDriverManager.isTypeAnyOf(WebDriverType.IEXPLORE) && Boolean.TRUE.equals(
                    ((Map<String, Object>) webDriverManager.getCapabilities().getCapability(WebDriverType.IE_OPTIONS))
                            .get("requireWindowFocus")))
            {
                int iterationsCounter = TEXT_TYPING_ATTEMPTS_LIMIT;
                while (iterationsCounter > 0 && !isValueEqualTo(element, normalizedText))
                {
                    element.clear();
                    LOGGER.info("Re-typing text \"{}\" to element", normalizedText);
                    element.sendKeys(normalizedText);
                    iterationsCounter--;
                }
                if (iterationsCounter == 0 && !isValueEqualTo(element, normalizedText))
                {
                    softAssert.recordFailedAssertion(String.format("The element is not filled correctly"
                            + " after %d typing attempt(s)", TEXT_TYPING_ATTEMPTS_LIMIT + 1));
                }
            }
        }
    }

    @Override
    public void addText(WebElement element, String text)
    {
        if (element != null)
        {
            String normalizedText = FormatUtil.normalizeLineEndings(text);
            LOGGER.info("Adding text \"{}\" into the element", normalizedText);

            // workaround for Safari and IE 11
            // Safari issue: https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/4467
            // IE 11 issue: https://github.com/SeleniumHQ/selenium/issues/3353
            boolean workaroundNeeded = webDriverManager.isTypeAnyOf(WebDriverType.SAFARI, WebDriverType.IEXPLORE)
                    && !element.findElements(By.xpath("//preceding-sibling::head"
                    + "[descendant::title[contains(text(),'Rich Text')]]")).isEmpty()
                    && isElementContenteditable(element);

            if (workaroundNeeded)
            {
                javascriptActions.executeScript(
                    "var text=arguments[0].innerHTML;arguments[0].innerHTML = text+arguments[1];", element,
                    text);
            }
            else
            {
                element.sendKeys(normalizedText);
            }
        }
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
    public boolean isPageVisibleAreaScrolledToElement(final WebElement element)
    {
        if (element != null)
        {
            int elementYlocation = element.getLocation().getY();
            return ((Boolean) javascriptActions.executeScript(
                    "return ((window.scrollY <= " + elementYlocation + ") && (" + elementYlocation
                            + "<= (window.scrollY + window.innerHeight)))")).booleanValue();
        }
        return false;
    }

    private static boolean isElementContenteditable(WebElement element)
    {
        return Boolean.valueOf(element.getAttribute("contenteditable"));
    }

    private boolean isValueEqualTo(WebElement element, String expectedValue)
    {
        return expectedValue.equals(javascriptActions.executeScript("return arguments[0].value;", element));
    }
}
