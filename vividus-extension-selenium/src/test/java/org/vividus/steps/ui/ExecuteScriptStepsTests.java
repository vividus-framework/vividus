/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.steps.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.web.model.JsArgument;
import org.vividus.steps.ui.web.model.JsArgumentType;
import org.vividus.ui.action.JavascriptActions;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class ExecuteScriptStepsTests
{
    private static final String JS_RESULT_ASSERTION_MESSAGE = "Returned result is not null";
    private static final String JS_CODE = "return 'value'";
    private static final String JS_ARGUMENT_ERROR_MESSAGE = "Please, specify command argument values and types";
    private static final String VALUE = "value";
    private static final String BODY = "body";
    private static final Set<VariableScope> VARIABLE_SCOPE = Set.of(VariableScope.SCENARIO);
    private static final String VARIABLE_NAME = "variableName";

    @Mock
    private JavascriptActions javascriptActions;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private VariableContext variableContext;

    @InjectMocks
    private ExecuteScriptSteps executeScriptSteps;

    static Stream<Arguments> executeJavascriptWithArguments()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
                Arguments.of("document.querySelector(arguments[0])", createJsArgument(JsArgumentType.STRING, BODY),                        BODY                       ),
                Arguments.of("remote:throttle",                      createJsArgument(JsArgumentType.OBJECT, "{\"condition\": \"Wifi\"}"), Map.of("condition", "Wifi"))
        );
        // CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource("executeJavascriptWithArguments")
    void testExecuteJavascriptWithStringArguments(String jsCode, JsArgument argument, Object arg)
    {
        executeScriptSteps.executeJavascriptWithArguments(jsCode, Collections.singletonList(argument));
        verify(javascriptActions).executeScript(jsCode, arg);
    }

    @Test
    void testExecuteJavascriptWithEmptyArguments()
    {
        String jsCode = "document.readyState";
        executeScriptSteps.executeJavascriptWithArguments(jsCode, List.of());
        verify(javascriptActions).executeScript(jsCode);
    }

    @ParameterizedTest
    @CsvSource({
            ",       body",
            "OBJECT,     "
    })
    void testExecuteJavascriptWithArgumentsNoType(JsArgumentType type, String value)
    {
        List<JsArgument> arguments = List.of(createJsArgument(type, value));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> executeScriptSteps.executeJavascriptWithArguments("document.querySelector(arguments[0])",
                        arguments));
        assertEquals(JS_ARGUMENT_ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void testGettingValueFromJS()
    {
        when(javascriptActions.executeScript(JS_CODE)).thenReturn(VALUE);
        when(softAssert.assertNotNull(JS_RESULT_ASSERTION_MESSAGE, VALUE)).thenReturn(true);
        executeScriptSteps.saveValueFromJS(JS_CODE, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(variableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, VALUE);
    }

    @Test
    void testGettingValueFromJSNullIsReturned()
    {
        executeScriptSteps.saveValueFromJS(JS_CODE, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(softAssert).assertNotNull(JS_RESULT_ASSERTION_MESSAGE, null);
        verifyNoInteractions(variableContext);
    }

    private static JsArgument createJsArgument(JsArgumentType type, String value)
    {
        JsArgument argument = new JsArgument();
        argument.setType(type);
        argument.setValue(value);
        return argument;
    }
}
