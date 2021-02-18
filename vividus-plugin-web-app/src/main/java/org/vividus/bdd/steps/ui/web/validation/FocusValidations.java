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

package org.vividus.bdd.steps.ui.web.validation;

import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.FocusState;
import org.vividus.bdd.util.EnumUtils;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.WebJavascriptActions;

public class FocusValidations
{
    private final WebJavascriptActions javascriptActions;
    private final ISoftAssert softAssert;

    public FocusValidations(WebJavascriptActions javascriptActions, ISoftAssert softAssert)
    {
        this.javascriptActions = javascriptActions;
        this.softAssert = softAssert;
    }

    /**
     * Checks if the specified web-element is in specified focus state on the page
     * @param webElement Element to check
     * @param focusState Possible values: IN_FOCUS, NOT_IN_FOCUS
     * @return true if element is in desired focus-state
     */
    public boolean isElementInFocusState(WebElement webElement, FocusState focusState)
    {
        return softAssert.assertTrue("The context element is " + EnumUtils.toHumanReadableForm(focusState),
                focusState.isInState(javascriptActions, webElement));
    }
}
