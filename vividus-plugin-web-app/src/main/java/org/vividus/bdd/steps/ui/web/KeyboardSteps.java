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

package org.vividus.bdd.steps.ui.web;

import java.util.List;

import javax.inject.Inject;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.validation.IFocusValidations;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.KeysUtils;
import org.vividus.ui.web.context.IWebUiContext;

public class KeyboardSteps
{
    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IWebUiContext webUiContext;
    @Inject private IFocusValidations focusValidations;

    /**
     * Step for interaction with page via keyboard. Interacts with the context element in focus.
     * @param keys List of keys to be pressed. (Separator: ",")
     * @see <a href="https://seleniumhq.github.io/selenium/docs/api/java/org/openqa/selenium/Keys.html">
     * <i>Keys</i></a>
     */
    @When("I press $keys on keyboard")
    public void pressKeys(List<String> keys)
    {
        WebElement element = webUiContext.getSearchContext() instanceof WebDriver
                ? webDriverProvider.get().findElement(By.xpath("//body"))
                : webUiContext.getSearchContext(WebElement.class);
        if (focusValidations.isElementInFocusState(element, FocusState.IN_FOCUS))
        {
            element.sendKeys(KeysUtils.keysToCharSequenceArray(keys));
        }
    }
}
