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
import java.util.Map.Entry;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.KeysManager;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.util.FormatUtils;

public class FieldActions implements IFieldActions
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldActions.class);

    private final IWebDriverManager webDriverManager;
    private final WebJavascriptActions javascriptActions;
    private final IWebElementActions webElementActions;
    private final IWebWaitActions waitActions;
    private final KeysManager keysManager;
    private final ISoftAssert softAssert;

    public FieldActions(IWebDriverManager webDriverManager, WebJavascriptActions javascriptActions,
            IWebElementActions webElementActions, IWebWaitActions waitActions, KeysManager keysManager,
            ISoftAssert softAssert)
    {
        this.webDriverManager = webDriverManager;
        this.javascriptActions = javascriptActions;
        this.webElementActions = webElementActions;
        this.waitActions = waitActions;
        this.keysManager = keysManager;
        this.softAssert = softAssert;
    }

    @Override
    public void selectItemInDropDownList(Select select, String text, boolean addition)
    {
        if (select != null)
        {
            boolean multiple = select.isMultiple();
            if (!multiple && addition)
            {
                softAssert
                        .recordFailedAssertion("Multiple selecting is not available to single select drop down");
                return;
            }
            boolean selected = selectOptions(select, text, addition, multiple);
            String assertionMessage = multiple ? String.format(
                    "Items with the text '%s' are selected from a drop down", text) : String.format(
                    "Item with the text '%s' is selected from a drop down", text);
            softAssert.assertTrue(assertionMessage, selected);
            waitActions.waitForPageLoad();
        }
    }

    private boolean selectOptions(Select select, String text, boolean addition, boolean multiple)
    {
        boolean selected = false;
        List<WebElement> options = select.getOptions();
        for (int i = 0; i < options.size(); i++)
        {
            WebElement currentOption = options.get(i);
            String optionValue = webElementActions.getElementText(currentOption).trim();
            if (text.equals(optionValue))
            {
                select.selectByIndex(i);
                selected = true;
                if (!multiple)
                {
                    break;
                }
            }
            else if (currentOption.isSelected() && !addition && multiple)
            {
                select.deselectByIndex(i);
            }
        }
        return selected;
    }

    @Override
    public void clearFieldUsingKeyboard(WebElement field)
    {
        if (field != null)
        {
            Entry<Keys, String> controllingKey = keysManager.getOsIndependentControlKey();
            LOGGER.atInfo()
                    .addArgument(controllingKey::getValue)
                    .log("Attempting to clear field with [{} + A, Backspace] keys sequence");
            field.sendKeys(Keys.chord(controllingKey.getKey(), "a") + Keys.BACK_SPACE);
        }
    }

    @Override
    public void addText(WebElement element, String text)
    {
        if (element != null)
        {
            String normalizedText = FormatUtils.normalizeLineEndings(text);
            LOGGER.info("Adding text \"{}\" into the element", normalizedText);

            // workaround for Safari and IE 11
            // Safari issue: https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/4467
            // IE 11 issue: https://github.com/SeleniumHQ/selenium/issues/3353
            boolean workaroundNeeded = webDriverManager.isBrowserAnyOf(Browser.SAFARI, Browser.IE)
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
                typeText(element, normalizedText);
            }
        }
    }

    @Override
    public void typeText(WebElement element, String text)
    {
        try
        {
            element.sendKeys(text);
        }
        catch (ElementNotInteractableException e)
        {
            softAssert.recordFailedAssertion(e);
        }
    }

    @Override
    public boolean isElementContenteditable(WebElement element)
    {
        return Boolean.parseBoolean(element.getAttribute("contenteditable"));
    }
}
