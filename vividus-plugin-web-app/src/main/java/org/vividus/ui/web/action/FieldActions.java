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

package org.vividus.ui.web.action;

import java.util.List;

import javax.inject.Inject;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.vividus.softassert.ISoftAssert;

public class FieldActions implements IFieldActions
{
    @Inject private ISoftAssert softAssert;
    @Inject private IWebWaitActions waitActions;
    @Inject private IWebElementActions webElementActions;

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
            field.sendKeys(Keys.chord(Keys.CONTROL, "a") + Keys.BACK_SPACE);
        }
    }
}
