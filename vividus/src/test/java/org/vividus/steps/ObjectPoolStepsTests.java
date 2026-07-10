/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.steps;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.TestContext;
import org.vividus.variable.VariableScope;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class ObjectPoolStepsTests
{
    private static final String POOL_NAME = "users";
    private static final String VARIABLE_NAME = "user";
    private static final String POOL_IS_EMPTY_ERROR = "The object pool with the name 'users' is empty";
    private static final String INIT_LOG_MESSAGE = "The object pool with the name '{}' is initialized with {} "
            + "element(s)";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);
    private static final ExamplesTable DATA = new ExamplesTable("""
            |login  |password|
            |user-1 |pass-1  |
            |user-2 |pass-2  |
            |user-3 |pass-3  |""");

    private final List<Object> takenObjects = new ArrayList<>();

    @Mock private VariableContext variableContext;

    private final TestContext testContext = new SimpleTestContext();
    private final TestLogger logger = TestLoggerFactory.getTestLogger(ObjectPoolSteps.class);

    @Test
    void shouldTakeRandomDistinctObjectsFromPool()
    {
        captureTakenObjects();
        ObjectPoolSteps steps = new ObjectPoolSteps(false, testContext, variableContext);
        steps.initializeObjectPool(POOL_NAME, DATA);

        int poolSize = DATA.getRows().size();
        drainPool(steps, poolSize);

        assertEquals(poolSize, takenObjects.size());
        assertEquals(poolSize, Set.copyOf(takenObjects).size());
        assertTrue(DATA.getRows().containsAll(takenObjects));
        assertThat(logger.getLoggingEvents(), is(List.of(info(INIT_LOG_MESSAGE, POOL_NAME, 3))));
    }

    @Test
    void shouldProhibitRepeatedInitializationOfSamePool()
    {
        ObjectPoolSteps steps = new ObjectPoolSteps(false, testContext, variableContext);
        steps.initializeObjectPool(POOL_NAME, DATA);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> steps.initializeObjectPool(POOL_NAME, DATA));
        assertEquals("The object pool with the name 'users' is already initialized", exception.getMessage());
        assertThat(logger.getLoggingEvents(), is(List.of(info(INIT_LOG_MESSAGE, POOL_NAME, 3))));
    }

    @Test
    void shouldFailToTakeObjectFromNotInitializedPool()
    {
        ObjectPoolSteps steps = new ObjectPoolSteps(false, testContext, variableContext);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> steps.takeObjectFromPool(POOL_NAME, SCOPES, VARIABLE_NAME));
        assertEquals("The object pool with the name 'users' is not initialized", exception.getMessage());
        verifyNoInteractions(variableContext);
        assertThat(logger.getLoggingEvents(), is(List.of()));
    }

    @Test
    void shouldFailToTakeObjectFromEmptyPool()
    {
        ObjectPoolSteps steps = new ObjectPoolSteps(false, testContext, variableContext);
        steps.initializeObjectPool(POOL_NAME, new ExamplesTable("|login|\n|user-1|"));
        steps.takeObjectFromPool(POOL_NAME, SCOPES, VARIABLE_NAME);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> steps.takeObjectFromPool(POOL_NAME, SCOPES, VARIABLE_NAME));
        assertEquals(POOL_IS_EMPTY_ERROR, exception.getMessage());
        assertThat(logger.getLoggingEvents(), is(List.of(info(INIT_LOG_MESSAGE, POOL_NAME, 1))));
    }

    @Test
    void shouldReturnObjectsToPoolAfterStoryWhenEnabled()
    {
        captureTakenObjects();
        ObjectPoolSteps steps = new ObjectPoolSteps(true, testContext, variableContext);
        int poolSize = DATA.getRows().size();
        steps.initializeObjectPool(POOL_NAME, DATA);

        drainPool(steps, poolSize);
        steps.returnObjectsToPools();
        drainPool(steps, poolSize);

        List<Object> firstStoryObjects = takenObjects.subList(0, poolSize);
        List<Object> secondStoryObjects = takenObjects.subList(poolSize, 2 * poolSize);
        assertEquals(Set.copyOf(firstStoryObjects), Set.copyOf(secondStoryObjects));
    }

    @Test
    void shouldNotReturnObjectsToPoolAfterStoryWhenDisabled()
    {
        ObjectPoolSteps steps = new ObjectPoolSteps(false, testContext, variableContext);
        steps.initializeObjectPool(POOL_NAME, DATA);
        drainPool(steps, DATA.getRows().size());

        steps.returnObjectsToPools();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> steps.takeObjectFromPool(POOL_NAME, SCOPES, VARIABLE_NAME));
        assertEquals(POOL_IS_EMPTY_ERROR, exception.getMessage());
    }

    private void drainPool(ObjectPoolSteps steps, int poolSize)
    {
        for (int i = 0; i < poolSize; i++)
        {
            steps.takeObjectFromPool(POOL_NAME, SCOPES, VARIABLE_NAME);
        }
    }

    private void captureTakenObjects()
    {
        doAnswer(invocation ->
        {
            takenObjects.add(invocation.getArgument(2));
            return null;
        }).when(variableContext).putVariable(eq(SCOPES), eq(VARIABLE_NAME), any());
    }
}
