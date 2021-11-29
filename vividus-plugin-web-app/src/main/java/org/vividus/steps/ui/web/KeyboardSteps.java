/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.steps.ui.web;

import java.util.List;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.KeysUtils;
import org.vividus.steps.ui.web.validation.FocusValidations;
import org.vividus.ui.context.IUiContext;

public class KeyboardSteps
{
    private final IUiContext uiContext;
    private final FocusValidations focusValidations;

    public KeyboardSteps(IUiContext uiContext, FocusValidations focusValidations)
    {
        this.uiContext = uiContext;
        this.focusValidations = focusValidations;
    }

    /**
     * Step for interaction with page via keyboard. Interacts with the context element in focus.
     * @param keys List of keys to be pressed. (Separator: ",")
     * @see <a href="https://seleniumhq.github.io/selenium/docs/api/java/org/openqa/selenium/Keys.html">
     * <i>Keys</i></a>
     */
    @When("I press $keys on keyboard")
    public void pressKeys(List<String> keys)
    {
        SearchContext searchContext = uiContext.getSearchContext();
        WebElement element = searchContext instanceof WebDriver
                ? searchContext.findElement(By.xpath("//body"))
                : (WebElement) searchContext;
        if (focusValidations.isElementInFocusState(element, FocusState.IN_FOCUS))
        {
            element.sendKeys(KeysUtils.keysToCharSequenceArray(keys));
        }
    }
}
