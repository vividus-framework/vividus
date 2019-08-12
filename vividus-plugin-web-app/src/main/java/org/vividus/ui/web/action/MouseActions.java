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

import javax.inject.Inject;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;

public class MouseActions implements IMouseActions
{
    private static final String COULD_NOT_MOVE_ERROR_MESSAGE = "Could not move to the element because of an error: ";

    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IJavascriptActions javascriptActions;
    @Inject private ISoftAssert softAssert;
    @Inject private IWebDriverManager webDriverManager;

    @Override
    public void moveToElement(WebElement element)
    {
        if (element != null)
        {
            //Safari Driver doesn't scroll to element before moveTo action
            if (webDriverManager.isMobile() || webDriverManager.isTypeAnyOf(WebDriverType.SAFARI))
            {
                javascriptActions.scrollIntoView(element, true);
            }
            try
            {
                new Actions(webDriverProvider.get()).moveToElement(element).perform();
            }
            catch (MoveTargetOutOfBoundsException ex)
            {
                softAssert.recordFailedAssertion(COULD_NOT_MOVE_ERROR_MESSAGE + ex);
            }
        }
    }

    @Override
    public void contextClick(WebElement element)
    {
        if (element != null)
        {
            new Actions(webDriverProvider.get()).contextClick(element).perform();
        }
    }
}
