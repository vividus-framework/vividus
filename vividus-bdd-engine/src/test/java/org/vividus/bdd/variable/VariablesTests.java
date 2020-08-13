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

package org.vividus.bdd.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VariablesTests
{
    private static final String A_B = "a:b";
    private static final String VARIABLE_KEY = "variableKey";
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String DEFAULT_VALUE = "defaultValue";

    @SuppressWarnings({ "checkstyle:MultipleStringLiterals", "checkstyle:MultipleStringLiteralsExtended" })
    static Stream<Arguments> variablesProvider()
    {
        //CHECKSTYLE:OFF
        return Stream.of(
                arguments(VARIABLE_KEY,                      VARIABLE_KEY, List.of(Map.of(KEY, VALUE)), List.of(Map.of(KEY, VALUE))),
                arguments(VARIABLE_KEY,                      VARIABLE_KEY, null,                        null),
                arguments("",                                VARIABLE_KEY, null,                        null),
                arguments("variableKey:defaultValue",        VARIABLE_KEY, null,                        DEFAULT_VALUE),
                arguments("variableKey[0]",                  VARIABLE_KEY, List.of(Map.of(KEY, VALUE)), Map.of(KEY, VALUE)),
                arguments("variableKey[0].key",              VARIABLE_KEY, Map.of(KEY, VALUE),          VALUE),
                arguments("variableKey[0]",                  VARIABLE_KEY, null,                        null),
                arguments("variableKey[0]:defaultValue",     VARIABLE_KEY, List.of(),                   DEFAULT_VALUE),
                arguments("variableKey[0].key",              VARIABLE_KEY, List.of(Map.of(KEY, VALUE)), VALUE),
                arguments("variableKey[0].key:defaultValue", VARIABLE_KEY, List.of(),                   DEFAULT_VALUE),
                arguments("variableKey.key",                 VARIABLE_KEY, Map.of(KEY, VALUE),          VALUE),
                arguments("variableKey.key",                 VARIABLE_KEY, VALUE,                       VALUE),
                arguments("variableKey.key",                 VARIABLE_KEY, null,                        null),
                arguments("variableKey.key:defaultValue",    VARIABLE_KEY, Map.of(),                    DEFAULT_VALUE),
                arguments("a.b:NULL",                        "a.b",        VALUE,                       VALUE),
                arguments(A_B,                               A_B,          VALUE,                       VALUE),
                arguments("variableKey[0]",                  VARIABLE_KEY, List.of(Set.of(KEY)),        Set.of(KEY)),
                arguments("variableKey[7]",                  VARIABLE_KEY, List.of(Map.of(KEY, VALUE)), null)
        );
        //CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource("variablesProvider")
    void shouldReturnVariable(String key, String variableKey, Object variable, Object expectedValue)
    {
        Variables variables = new Variables(Map.of());
        variables.putStoryVariable(variableKey, variable);
        assertEquals(expectedValue, variables.getVariable(key));
    }

    @Test
    void shouldTryToReturnSystemPropertyIfPropertyByNameIsNotFoundInContext()
    {
        Variables variables = new Variables(Map.of());
        variables.putStoryVariable(VARIABLE_KEY, List.of());
        assertNull(variables.getVariable(KEY));
        System.setProperty(KEY, VALUE);
        assertEquals(VALUE, variables.getVariable(KEY));
        System.clearProperty(KEY);
    }

    @Test
    void shouldReturnNullIfPropertyNotFound()
    {
        Variables variables = new Variables(Map.of());
        variables.putStoryVariable(VARIABLE_KEY, List.of());
        assertNull(variables.getVariable(KEY));
    }

    @Test
    void shouldReturnBatchVariable()
    {
        Variables variables = new Variables(Map.of(VARIABLE_KEY, VALUE));
        assertEquals(VALUE, variables.getVariable(VARIABLE_KEY));
    }

    @Test
    void shouldReturnStoryVariable()
    {
        Variables variables = new Variables(Map.of());
        variables.putStoryVariable(VARIABLE_KEY, VALUE);
        assertEquals(VALUE, variables.getVariable(VARIABLE_KEY));
    }

    @Test
    void shouldReturnScenarioVariable()
    {
        Variables variables = new Variables(Map.of());
        variables.putScenarioVariable(VARIABLE_KEY, VALUE);
        assertEquals(VALUE, variables.getVariable(VARIABLE_KEY));
    }

    @Test
    void shouldReturnStepVariable()
    {
        Variables variables = new Variables(Map.of());
        variables.initStepVariables();
        variables.putStepVariable(VARIABLE_KEY, VALUE);
        assertEquals(VALUE, variables.getVariable(VARIABLE_KEY));
    }

    @Test
    void shouldClearStepVariables()
    {
        Variables variables = new Variables(Map.of());
        variables.initStepVariables();
        variables.putStepVariable(VARIABLE_KEY, VALUE);
        variables.clearStepVariables();
        assertNull(variables.getVariable(VARIABLE_KEY));
    }

    @Test
    void shouldSkipCleanOfEmptyStepVariables()
    {
        Variables variables = new Variables(Map.of());
        variables.clearStepVariables();
        assertNull(variables.getVariable(VARIABLE_KEY));
    }
}
