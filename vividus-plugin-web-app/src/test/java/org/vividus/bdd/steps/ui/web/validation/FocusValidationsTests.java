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

package org.vividus.bdd.steps.ui.web.validation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.bdd.steps.ui.web.FocusState;
import org.vividus.ui.web.action.IJavascriptActions;

@ExtendWith(MockitoExtension.class)
class FocusValidationsTests
{
    @Mock
    private IJavascriptActions javascriptActions;

    @Mock
    private IDescriptiveSoftAssert softAssert;

    @Mock
    private WebElement webElement;

    @InjectMocks
    private FocusValidations focusValidations;

    @Test
    void test()
    {
        when(javascriptActions.executeScript("return arguments[0]==document.activeElement", webElement))
                .thenReturn(true);
        focusValidations.isElementInFocusState(webElement, FocusState.IN_FOCUS);
        verify(softAssert).assertTrue("The context element is in focus", true);
    }
}
