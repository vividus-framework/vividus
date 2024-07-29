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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.softassert.SoftAssert;
import org.vividus.ui.web.playwright.action.PlaywrightJavascriptActions;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class ExecuteScriptStepsTests
{
    private static final String JS_RESULT_ASSERTION_MESSAGE = "Returned result is not null";
    private static final String JS_CODE = "return 'value'";
    private static final String VALUE = "value";
    private static final Set<VariableScope> VARIABLE_SCOPE = Set.of(VariableScope.SCENARIO);
    private static final String VARIABLE_NAME = "variableName";

    @Mock private PlaywrightJavascriptActions playwrightJavascriptActions;
    @Mock private SoftAssert softAssert;
    @Mock private VariableContext variableContext;

    @InjectMocks private ExecuteScriptSteps executeScriptSteps;

    @Test
    void shouldExecuteJavaScript()
    {
        String jsCode = "document.querySelector('[name=\"vividus-logo\"]').remove()";
        executeScriptSteps.executeJavascript(jsCode);
        verify(playwrightJavascriptActions).executeScript(jsCode);
    }

    @Test
    void shouldExecuteJavaScriptAndSaveValue()
    {
        when(playwrightJavascriptActions.executeScript("async () => {%n%s%n}".formatted(JS_CODE))).thenReturn(VALUE);
        when(softAssert.assertNotNull(JS_RESULT_ASSERTION_MESSAGE, VALUE)).thenReturn(true);
        executeScriptSteps.saveValueFromJS(JS_CODE, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(variableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, VALUE);
    }

    @Test
    void shouldExecuteJavaScriptAndHandleNullValue()
    {
        executeScriptSteps.saveValueFromJS(JS_CODE, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(softAssert).assertNotNull(JS_RESULT_ASSERTION_MESSAGE, null);
        verifyNoInteractions(variableContext);
    }
}
