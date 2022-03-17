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

package org.vividus.variable;

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
    private static final int TWO_HUNDRED = 200;
    private static final String STEP = "step";
    private static final String KEY3 = "key3";
    private static final String STORY = "story";
    private static final String KEY2 = "key2";
    private static final String SCENARIO = "scenario";
    private static final String KEY1 = "key1";
    private static final String A_B = "a:b";
    private static final String VARIABLE_KEY = "variableKey";
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final Pojo POJO = new Pojo(VALUE);
    private static final String DEFAULT_VALUE = "defaultValue";

    @SuppressWarnings({ "checkstyle:MultipleStringLiterals", "checkstyle:MultipleStringLiteralsExtended" })
    static Stream<Arguments> variablesProvider()
    {
        //CHECKSTYLE:OFF
        return Stream.of(
                arguments(VARIABLE_KEY,                           VARIABLE_KEY, List.of(Map.of(KEY, VALUE)),    List.of(Map.of(KEY, VALUE))),
                arguments(VARIABLE_KEY,                           VARIABLE_KEY, null,                           null),
                arguments("",                                     VARIABLE_KEY, null,                           null),
                arguments("variableKey:defaultValue",             VARIABLE_KEY, null,                           DEFAULT_VALUE),
                arguments("variableKey:defaultValue:with:colons", VARIABLE_KEY, null,                           "defaultValue:with:colons"),
                arguments("variableKey[0]",                       VARIABLE_KEY, List.of(Map.of(KEY, VALUE)),    Map.of(KEY, VALUE)),
                arguments("variableKey[0].key",                   VARIABLE_KEY, Map.of(KEY, VALUE),             VALUE),
                arguments("variableKey[0]",                       VARIABLE_KEY, null,                           null),
                arguments("variableKey[0]:defaultValue",          VARIABLE_KEY, List.of(),                      DEFAULT_VALUE),
                arguments("variableKey[0].key",                   VARIABLE_KEY, List.of(Map.of(KEY, VALUE)),    VALUE),
                arguments("variableKey[0].value",                 VARIABLE_KEY, List.of(Map.of(VALUE, KEY)),    KEY),
                arguments("variableKey[0].key:defaultValue",      VARIABLE_KEY, List.of(),                      DEFAULT_VALUE),
                arguments("variableKey.key",                      VARIABLE_KEY, Map.of(KEY, VALUE),             VALUE),
                arguments("variableKey.key",                      VARIABLE_KEY, VALUE,                          VALUE),
                arguments("variableKey.key",                      VARIABLE_KEY, null,                           null),
                arguments("variableKey.key:defaultValue",         VARIABLE_KEY, Map.of(),                       DEFAULT_VALUE),
                arguments("a.b:NULL",                             "a.b",        VALUE,                          VALUE),
                arguments(A_B,                                    A_B,          VALUE,                          VALUE),
                arguments("variableKey[0]",                       VARIABLE_KEY, List.of(Set.of(KEY)),           Set.of(KEY)),
                arguments("variableKey[7]",                       VARIABLE_KEY, List.of(Map.of(KEY, VALUE)),    null),
                arguments("variableKey.key",                      VARIABLE_KEY, Map.of(KEY, List.of(VALUE)),    List.of(VALUE)),
                arguments("variableKey.key[0]",                   VARIABLE_KEY, Map.of(KEY, List.of(VALUE)),    VALUE),
                arguments("variableKey.key",                      VARIABLE_KEY, Map.of(KEY, TWO_HUNDRED),       TWO_HUNDRED),
                arguments("variableKey[0].key.name",              VARIABLE_KEY, List.of(Map.of(KEY, POJO)),     VALUE),
                arguments("variableKey[0].name",                  VARIABLE_KEY, List.of(POJO),                  VALUE),
                arguments("variableKey.key[0].name",              VARIABLE_KEY, Map.of(KEY, List.of(POJO)),     VALUE),
                arguments("variableKey.name",                     VARIABLE_KEY, POJO,                           VALUE),
                arguments("variableKey.key.key.name",             VARIABLE_KEY, Map.of(KEY, Map.of(KEY, POJO)), VALUE),
                arguments("variableKey.notExists",                VARIABLE_KEY, POJO,                           POJO)
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
        assertNull(variables.getVariable(KEY));
        System.setProperty(KEY, VALUE);
        assertEquals(VALUE, variables.getVariable(KEY));
        System.clearProperty(KEY);
    }

    @Test
    void shouldTryToReturnEnvironmentVariableIfPropertyByNameIsNotFoundInContext()
    {
        Map.Entry<String, String> environmentVariable = System.getenv().entrySet().iterator().next();
        Variables variables = new Variables(Map.of());
        assertEquals(environmentVariable.getValue(), variables.getVariable(environmentVariable.getKey()));
    }

    @Test
    void shouldReturnNullIfPropertyNotFound()
    {
        Variables variables = new Variables(Map.of());
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

    @Test
    void shouldReturnMergedVariables()
    {
        Variables variables = new Variables(Map.of());
        variables.initStepVariables();
        variables.putScenarioVariable(KEY1, SCENARIO);
        variables.putScenarioVariable(KEY2, SCENARIO);
        variables.putScenarioVariable(KEY3, SCENARIO);
        variables.putStoryVariable(KEY2, STORY);
        variables.putStoryVariable(KEY3, STORY);
        variables.putStoryVariable(KEY3, STEP);
        assertEquals(Map.of(KEY1, SCENARIO, KEY2, STORY, KEY3, STEP), variables.getVariables());
    }

    private static final class Pojo
    {
        @SuppressWarnings("unused")
        private final String name;

        private Pojo(String name)
        {
            this.name = name;
        }
    }
}
