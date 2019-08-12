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

package org.vividus.bdd.steps.ui.web.validation;

import javax.inject.Inject;

import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.FocusState;
import org.vividus.ui.web.action.IJavascriptActions;

public class FocusValidations implements IFocusValidations
{
    @Inject private IJavascriptActions javascriptActions;
    @Inject private IHighlightingSoftAssert softAssert;

    @Override
    public boolean isElementInFocusState(WebElement webElement, FocusState focusState)
    {
        return softAssert.assertTrue(
                "The context element is " + focusState.name().toLowerCase().replace('_', ' '),
                focusState.isInState(javascriptActions, webElement));
    }
}
