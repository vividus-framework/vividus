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

package org.vividus.bdd.context;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.variable.IVariablesFactory;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.bdd.variable.Variables;
import org.vividus.testcontext.SimpleTestContext;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class BddVariableContextTests
{
    private static final String SAVE_MESSAGE_TEMPLATE = "Saving a value '{}' into the '{}' variable '{}'";
    private static final String VARIABLE_KEY = "variableKey";
    private static final String KEY = "key";
    private static final String VALUE = "value";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(BddVariableContext.class);

    @Mock
    private IVariablesFactory variablesFactory;

    @InjectMocks
    private BddVariableContext bddVariableContext;

    @ParameterizedTest
    @EnumSource(value = VariableScope.class, mode = Mode.EXCLUDE, names = {"NEXT_BATCHES", "GLOBAL"})
    void testPutVariable(VariableScope variableScope)
    {
        bddVariableContext.setTestContext(new SimpleTestContext());
        Variables variables = new Variables();
        when(variablesFactory.createVariables()).thenReturn(variables);
        bddVariableContext.putVariable(variableScope, VARIABLE_KEY, VALUE);
        verifyScopedVariable(variableScope, variables);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
                info(SAVE_MESSAGE_TEMPLATE, VALUE, variableScope, VARIABLE_KEY))));
    }

    private void verifyScopedVariable(VariableScope variableScope, Variables variables)
    {
        Map<String, Object> scopedVariables = variables.getVariables(variableScope);
        assertThat(scopedVariables.entrySet(), hasSize(1));
        assertEquals(VALUE, scopedVariables.get(VARIABLE_KEY));
    }

    @Test
    void shouldFailToSetGlobalVariable()
    {
        VariableScope variableScope = VariableScope.GLOBAL;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> bddVariableContext.putVariable(variableScope, VARIABLE_KEY, VALUE));
        assertEquals("Setting of GLOBAL variables is forbidden", exception.getMessage());
        verifyNoInteractions(variablesFactory);
    }

    @Test
    void shouldPutVariablesIntoAllPassedScopes()
    {
        bddVariableContext.setTestContext(new SimpleTestContext());
        Set<VariableScope> variableScopes = Set.of(VariableScope.STEP, VariableScope.SCENARIO, VariableScope.STORY,
                VariableScope.NEXT_BATCHES);
        Variables variables = new Variables();
        when(variablesFactory.createVariables()).thenReturn(variables);
        bddVariableContext.putVariable(variableScopes, VARIABLE_KEY, VALUE);
        verifyScopedVariable(VariableScope.STEP, variables);
        verifyScopedVariable(VariableScope.SCENARIO, variables);
        verifyScopedVariable(VariableScope.STORY, variables);
        List<LoggingEvent> loggingEvents = logger.getLoggingEvents();
        assertThat(loggingEvents, hasSize(4));
        assertThat(loggingEvents, hasItems(
                info(SAVE_MESSAGE_TEMPLATE, VALUE, VariableScope.STEP, VARIABLE_KEY),
                info(SAVE_MESSAGE_TEMPLATE, VALUE, VariableScope.SCENARIO, VARIABLE_KEY),
                info(SAVE_MESSAGE_TEMPLATE, VALUE, VariableScope.STORY, VARIABLE_KEY),
                info(SAVE_MESSAGE_TEMPLATE, VALUE, VariableScope.NEXT_BATCHES, VARIABLE_KEY)));
        Map<String, Object> scopedVariables = variables.getVariables(VariableScope.NEXT_BATCHES);
        verify(variablesFactory).addNextBatchesVariable(VARIABLE_KEY, VALUE);
        assertThat(scopedVariables.entrySet(), empty());
    }

    @Test
    void testClearVariables()
    {
        bddVariableContext.setTestContext(new SimpleTestContext());
        VariableScope variableScope = VariableScope.SCENARIO;
        Variables variables = putVariable(variableScope);
        bddVariableContext.clearVariables(variableScope);
        assertThat(variables.getVariables(variableScope).entrySet(), empty());
    }

    @ParameterizedTest
    @EnumSource(VariableScope.class)
    void testGetVariable(VariableScope variableScope)
    {
        bddVariableContext.setTestContext(new SimpleTestContext());
        putVariable(variableScope);
        assertEquals(VALUE, bddVariableContext.getVariable(VARIABLE_KEY));
    }

    @SuppressWarnings({ "checkstyle:MultipleStringLiterals", "checkstyle:MultipleStringLiteralsExtended" })
    static Stream<Arguments> variablesProvider()
    {
        return Stream.of(
            arguments(VARIABLE_KEY,                      List.of(Map.of(KEY, VALUE)), List.of(Map.of(KEY, VALUE))),
            arguments(VARIABLE_KEY,                      null,                        null),
            arguments("",                                null,                        null),
            arguments("variableKey:defaultValue",        null,                        "defaultValue"),
            arguments("variableKey[0]",                  List.of(Map.of(KEY, VALUE)), Map.of(KEY, VALUE)),
            arguments("variableKey[0]:defaultValue",     List.of(),                   "defaultValue"),
            arguments("variableKey[0].key",              List.of(Map.of(KEY, VALUE)), VALUE),
            arguments("variableKey[0].key:defaultValue", List.of(),                   "defaultValue"),
            arguments("variableKey.key",                 Map.of(KEY, VALUE),          VALUE),
            arguments("variableKey.key",                 VALUE,                       VALUE),
            arguments("variableKey.key",                 null,                        null),
            arguments("variableKey.key:defaultValue",    Map.of(),                    "defaultValue"),
            arguments("variableKey[0]",                  List.of(Set.of(KEY)),        Set.of(KEY)),
            arguments("variableKey[7]",                  List.of(Map.of(KEY, VALUE)), null)
        );
    }

    @ParameterizedTest
    @MethodSource("variablesProvider")
    void shouldReturnVariable(String key, Object variable, Object expectedValue)
    {
        bddVariableContext.setTestContext(new SimpleTestContext());
        putVariable(VariableScope.STORY, variable);
        assertEquals(expectedValue, bddVariableContext.getVariable(key));
    }

    @Test
    void shouldTryToReturnSystemPropertyIfPropertyByNameIsNotFoundInContext()
    {
        bddVariableContext.setTestContext(new SimpleTestContext());
        putVariable(VariableScope.STORY, List.of());
        assertNull(bddVariableContext.getVariable(KEY));
        System.setProperty(KEY, VALUE);
        assertEquals(VALUE, bddVariableContext.getVariable(KEY));
        System.clearProperty(KEY);
    }

    @Test
    void shouldReturnNullIfPropertyNotFound()
    {
        bddVariableContext.setTestContext(new SimpleTestContext());
        putVariable(VariableScope.STORY, List.of());
        assertNull(bddVariableContext.getVariable(KEY));
    }

    @Test
    void testPutNextBatchesVariable()
    {
        Variables variables = new Variables();
        bddVariableContext.putVariable(VariableScope.NEXT_BATCHES, VARIABLE_KEY, VALUE);
        Map<String, Object> scopedVariables = variables.getVariables(VariableScope.NEXT_BATCHES);
        verify(variablesFactory).addNextBatchesVariable(VARIABLE_KEY, VALUE);
        assertTrue(scopedVariables.isEmpty());
    }

    @Test
    void testInitVariables()
    {
        bddVariableContext.setTestContext(new SimpleTestContext());
        bddVariableContext.initVariables();
        verify(variablesFactory).createVariables();
    }

    @Test
    void testClearAllVariables()
    {
        SimpleTestContext testContext = new SimpleTestContext();
        Class<Variables> key = Variables.class;
        testContext.put(key, new Variables());
        bddVariableContext.setTestContext(testContext);
        bddVariableContext.clearVariables();
        assertNull(testContext.get(key));
    }

    private Variables putVariable(VariableScope variableScope)
    {
        return putVariable(variableScope, VALUE);
    }

    private Variables putVariable(VariableScope variableScope, Object variable)
    {
        Variables variables = new Variables();
        variables.getVariables(variableScope).put(VARIABLE_KEY, variable);
        when(variablesFactory.createVariables()).thenReturn(variables);
        return variables;
    }
}
