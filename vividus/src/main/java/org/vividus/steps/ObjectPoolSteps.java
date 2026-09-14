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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.context.VariableContext;
import org.vividus.testcontext.TestContext;
import org.vividus.variable.VariableScope;

public class ObjectPoolSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectPoolSteps.class);

    private static final Object TAKEN_OBJECTS_KEY = ObjectPoolSteps.class;

    private final boolean returnObjectsAfterStoryCompletion;
    private final TestContext testContext;
    private final VariableContext variableContext;

    private final Map<String, ObjectPool> pools = new ConcurrentHashMap<>();

    public ObjectPoolSteps(boolean returnObjectsAfterStoryCompletion, TestContext testContext,
            VariableContext variableContext)
    {
        this.returnObjectsAfterStoryCompletion = returnObjectsAfterStoryCompletion;
        this.testContext = testContext;
        this.variableContext = variableContext;
    }

    /**
     * Initializes an object pool with the provided data. The pool is shared between all threads and stories, so it
     * can be populated only once: any attempt to initialize an already existing pool results in an error.
     *
     * @param poolName The unique name of the object pool.
     * @param data     The xref:ROOT:glossary.adoc#_examplestable[ExamplesTable] with the objects to put into the pool,
     *                 every row is stored as a separate object represented by a map of the column names to the values.
     */
    @Given("I initialize object pool `$poolName` with data:$data")
    public void initializeObjectPool(String poolName, ExamplesTable data)
    {
        List<Map<String, String>> objects = toShuffledObjects(data);
        ObjectPool existingPool = pools.putIfAbsent(poolName, new ObjectPool(objects));
        Validate.validState(existingPool == null, "The object pool with the name '%s' is already initialized",
                poolName);
        logPoolSize(poolName, objects, "The object pool with the name '{}' is initialized with {} element(s)");
    }

    /**
     * Adds objects to an already initialized object pool. The added objects are shuffled and appended to the pool, so
     * they become available for subsequent take operations. Adding objects to a pool that has not been initialized
     * results in an error.
     *
     * @param poolName The name of the object pool to add the objects to.
     * @param data     The xref:ROOT:glossary.adoc#_examplestable[ExamplesTable] with the objects to add to the pool,
     *                 every row is stored as a separate object represented by a map of the column names to the values.
     */
    @Given("I add objects to object pool `$poolName`:$data")
    public void addObjectsToPool(String poolName, ExamplesTable data)
    {
        ObjectPool pool = getInitializedPool(poolName);
        List<Map<String, String>> objects = toShuffledObjects(data);
        pool.add(objects);
        logPoolSize(poolName, objects, "The object pool with the name '{}' is replenished with {} element(s)");
    }

    /**
     * Saves the number of objects currently available in the pool to the variable. Taken objects are not included until
     * they are returned back to the pool. The saved number can be used to iterate over the remaining objects. The size
     * is a snapshot: if another thread adds objects to the same pool at the same time, the saved number can be
     * inaccurate.
     *
     * @param poolName     The name of the object pool to get the size of.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName The name of the variable to store the pool size.
     */
    @When("I save size of object pool `$poolName` to $scopes variable `$variableName`")
    public void saveObjectPoolSize(String poolName, Set<VariableScope> scopes, String variableName)
    {
        int size = getInitializedPool(poolName).size();
        variableContext.putVariable(scopes, variableName, size);
        LOGGER.atInfo().addArgument(poolName).addArgument(size)
                .log("The size of the object pool with the name '{}' is {}");
    }

    /**
     * Takes a random object from the pool and saves it to the variable. The taken object is removed from the pool, so
     * it can't be taken again until it is returned back. The objects are returned back to the pool after the story only
     * if the {@code object-pool.return-objects-after-story-completion} property is set to {@code true}.
     *
     * @param poolName     The name of the object pool to take the object from.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName The name of the variable to store the taken object.
     */
    @When("I take object from pool `$poolName` and save it to $scopes variable `$variableName`")
    public void takeObjectFromPool(String poolName, Set<VariableScope> scopes, String variableName)
    {
        ObjectPool pool = getInitializedPool(poolName);
        Map<String, String> object = pool.take(poolName);
        if (returnObjectsAfterStoryCompletion)
        {
            getTakenObjects().computeIfAbsent(poolName, key -> new ArrayList<>()).add(object);
        }
        variableContext.putVariable(scopes, variableName, object);
    }

    @AfterStory
    public void returnObjectsToPools()
    {
        if (returnObjectsAfterStoryCompletion)
        {
            getTakenObjects().forEach((poolName, objects) -> objects.forEach(pools.get(poolName)::giveBack));
        }
    }

    private ObjectPool getInitializedPool(String poolName)
    {
        ObjectPool pool = pools.get(poolName);
        Validate.validState(pool != null, "The object pool with the name '%s' is not initialized", poolName);
        return pool;
    }

    private static List<Map<String, String>> toShuffledObjects(ExamplesTable data)
    {
        List<Map<String, String>> objects = data.getRows();
        Collections.shuffle(objects);
        return objects;
    }

    private static void logPoolSize(String poolName, List<Map<String, String>> objects, String message)
    {
        LOGGER.atInfo().addArgument(poolName).addArgument(objects::size).log(message);
    }

    private Map<String, List<Map<String, String>>> getTakenObjects()
    {
        return testContext.get(TAKEN_OBJECTS_KEY, HashMap::new);
    }

    private static final class ObjectPool
    {
        private final Deque<Map<String, String>> objects;

        private ObjectPool(List<Map<String, String>> objects)
        {
            this.objects = new ConcurrentLinkedDeque<>(objects);
        }

        private Map<String, String> take(String poolName)
        {
            Map<String, String> object = objects.poll();
            Validate.validState(object != null, "The object pool with the name '%s' is empty", poolName);
            return object;
        }

        private void giveBack(Map<String, String> object)
        {
            objects.add(object);
        }

        private void add(List<Map<String, String>> objectsToAdd)
        {
            objects.addAll(objectsToAdd);
        }

        private int size()
        {
            return objects.size();
        }
    }
}
