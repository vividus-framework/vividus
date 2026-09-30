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

package org.vividus.ui.web.playwright.steps;

import java.util.List;

import com.microsoft.playwright.Locator;

import org.jbehave.core.annotations.When;
import org.vividus.ui.web.playwright.UiContext;

public class KeyboardSteps
{
    private final UiContext uiContext;

    public KeyboardSteps(UiContext uiContext)
    {
        this.uiContext = uiContext;
    }

    /**
     * Step for interaction with page via keyboard. Interacts with the current context element if it's set,
     * otherwise interacts with the page.
     *
     * @param keys List of keys to be pressed. (Separator: ",")
     * @see <a href="https://playwright.dev/java/docs/api/class-keyboard">Keyboard</a>
     */
    @When("I press $keys on keyboard")
    public void pressKeys(List<String> keys)
    {
        Locator context = uiContext.getContext();
        keys.forEach(key -> {
            if (context != null)
            {
                context.press(key);
            }
            else
            {
                uiContext.getCurrentPage().keyboard().press(key);
            }
        });
    }
}
