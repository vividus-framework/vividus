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

package org.vividus.context;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.TestContext;
import org.vividus.util.EnumUtils;
import org.vividus.variable.IVariablesFactory;
import org.vividus.variable.VariableScope;
import org.vividus.variable.Variables;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class VariableTestContextTests
{
    private static final String SAVE_MESSAGE_TEMPLATE = "Saving a value '{}' into the {} variable '{}'";
    private static final String VARIABLE_KEY = "variableKey";
    private static final String VALUE = "value";
    private static final String STEP = "step";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(VariableTestContext.class);

    @Mock private IVariablesFactory variablesFactory;
    @Spy private final TestContext testContext = new SimpleTestContext();
    @InjectMocks private VariableTestContext variableTestContext;

    @Test
    void shouldInitVariables()
    {
        variableTestContext.initVariables();
        verify(variablesFactory).createVariables();
    }

    @ParameterizedTest
    @EnumSource(value = VariableScope.class, mode = Mode.EXCLUDE, names = {"STEP", "NEXT_BATCHES"})
    void shouldPutVariablePerScope(VariableScope variableScope)
    {
        Variables variables = new Variables(Map.of());
        when(variablesFactory.createVariables()).thenReturn(variables);
        variableTestContext.putVariable(variableScope, VARIABLE_KEY, VALUE);
        assertEquals(VALUE, variables.getVariable(VARIABLE_KEY));
        String scope = EnumUtils.toHumanReadableForm(variableScope);
        assertThat(logger.getLoggingEvents(), equalTo(
                List.of(info(SAVE_MESSAGE_TEMPLATE, VALUE, scope, VARIABLE_KEY))));
    }

    @Test
    void shouldPutStepVariable()
    {
        Variables variables = new Variables(Map.of());
        when(variablesFactory.createVariables()).thenReturn(variables);
        variableTestContext.initStepVariables();
        variableTestContext.putVariable(VariableScope.STEP, VARIABLE_KEY, VALUE);
        assertEquals(VALUE, variables.getVariable(VARIABLE_KEY));
        assertThat(logger.getLoggingEvents(), equalTo(
                List.of(info(SAVE_MESSAGE_TEMPLATE, VALUE, STEP, VARIABLE_KEY))));
    }

    @Test
    void shouldPutBinaryDataIntoVariable()
    {
        Variables variables = new Variables(Map.of());
        when(variablesFactory.createVariables()).thenReturn(variables);
        variableTestContext.initStepVariables();
        var bytes = VALUE.getBytes(StandardCharsets.UTF_8);
        variableTestContext.putVariable(VariableScope.STEP, VARIABLE_KEY, bytes);
        assertEquals(bytes, variables.getVariable(VARIABLE_KEY));
        assertThat(logger.getLoggingEvents(), equalTo(
                List.of(info("Saving {} kB of binary data into the {} variable '{}'", 0.005d, STEP, VARIABLE_KEY))));
    }

    @Test
    void shouldPutNextBatchesVariable()
    {
        Variables variables = new Variables(Map.of());
        variableTestContext.putVariable(VariableScope.NEXT_BATCHES, VARIABLE_KEY, VALUE);
        verify(variablesFactory).addNextBatchesVariable(VARIABLE_KEY, VALUE);
        assertNull(variables.getVariable(VARIABLE_KEY));
    }

    @Test
    void shouldPutVariablesIntoAllPassedScopes()
    {
        Set<VariableScope> variableScopes = Set.of(VariableScope.STEP, VariableScope.SCENARIO, VariableScope.STORY,
                VariableScope.NEXT_BATCHES);
        Variables variables = new Variables(Map.of());
        variables.initStepVariables();
        when(variablesFactory.createVariables()).thenReturn(variables);
        variableTestContext.putVariable(variableScopes, VARIABLE_KEY, VALUE);
        assertEquals(VALUE, variables.getVariable(VARIABLE_KEY));
        variables.clearStepVariables();
        assertEquals(VALUE, variables.getVariable(VARIABLE_KEY));
        variables.clearScenarioVariables();
        assertEquals(VALUE, variables.getVariable(VARIABLE_KEY));
        List<LoggingEvent> loggingEvents = logger.getLoggingEvents();
        assertThat(loggingEvents, hasSize(4));
        assertThat(loggingEvents, hasItems(
                info(SAVE_MESSAGE_TEMPLATE, VALUE, STEP, VARIABLE_KEY),
                info(SAVE_MESSAGE_TEMPLATE, VALUE, "scenario", VARIABLE_KEY),
                info(SAVE_MESSAGE_TEMPLATE, VALUE, "story", VARIABLE_KEY),
                info(SAVE_MESSAGE_TEMPLATE, VALUE, "next batches", VARIABLE_KEY)));
        verify(variablesFactory).addNextBatchesVariable(VARIABLE_KEY, VALUE);
    }

    @Test
    void shouldClearBatchVariables()
    {
        Class<Variables> key = Variables.class;
        testContext.put(key, new Variables(Map.of()));
        variableTestContext.clearBatchVariables();
        assertNull(testContext.get(key));
    }

    @Test
    void shouldClearScenarioVariables()
    {
        Variables variables = new Variables(Map.of());
        variables.putScenarioVariable(VARIABLE_KEY, VALUE);
        when(variablesFactory.createVariables()).thenReturn(variables);
        variableTestContext.clearScenarioVariables();
        assertNull(variables.getVariable(VARIABLE_KEY));
    }

    @Test
    void shouldClearStepVariables()
    {
        Variables variables = new Variables(Map.of());
        variables.initStepVariables();
        variables.putStepVariable(VARIABLE_KEY, VALUE);
        when(variablesFactory.createVariables()).thenReturn(variables);
        variableTestContext.clearStepVariables();
        assertNull(variables.getVariable(VARIABLE_KEY));
    }

    @Test
    void shouldReturnStepVariable()
    {
        Variables variables = new Variables(Map.of());
        variables.initStepVariables();
        variables.putStepVariable(VARIABLE_KEY, VALUE);
        when(variablesFactory.createVariables()).thenReturn(variables);
        assertEquals(VALUE, variableTestContext.getVariable(VARIABLE_KEY));
    }

    @Test
    void shoulReturnVariables()
    {
        Variables variables = mock(Variables.class);
        when(variablesFactory.createVariables()).thenReturn(variables);
        variableTestContext.getVariables();
        verify(variables).getVariables();
    }
}
