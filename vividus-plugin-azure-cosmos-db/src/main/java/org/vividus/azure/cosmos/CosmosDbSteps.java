/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.azure.cosmos;

import java.util.Set;
import java.util.function.Function;

import org.jbehave.core.annotations.When;
import org.vividus.azure.cosmos.model.CosmosDbContainer;
import org.vividus.bdd.context.BddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.softassert.SoftAssert;
import org.vividus.util.property.PropertyMappedCollection;

public class CosmosDbSteps
{
    private static final int CREATED = 201;
    private static final int NO_CONTENT = 204;
    private static final int OK = 200;

    private final BddVariableContext bddVariableContext;
    private final PropertyMappedCollection<CosmosDbContainer> containers;
    private final CosmosDbService cosmosDbService;
    private final SoftAssert softAssert;

    public CosmosDbSteps(BddVariableContext bddVariableContext, PropertyMappedCollection<CosmosDbContainer> containers,
            CosmosDbService cosmosDbService, SoftAssert softAssert)
    {
        this.bddVariableContext = bddVariableContext;
        this.containers = containers;
        this.cosmosDbService = cosmosDbService;
        this.softAssert = softAssert;
    }

    /**
     * Reads an item by its id and partition key and saves the result into scoped variable
     * @param id            The item id
     * @param partition     The partition value
     * @param containerKey  The container key
     * @param scopes        The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variables scopes<br>
     *                      <i>Available scopes:</i>
     *                      <ul>
     *                      <li><b>STEP</b> - the variable will be available only within the step,
     *                      <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                      <li><b>STORY</b> - the variable will be available within the whole story,
     *                      <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                      </ul>scopes
     * @param variableName  The variable name to store result.
     */
    @When("I read item with `$id` id and `$partition` partition from CosmosDB container `$containerKey`"
            + " and save result to $scopes variable `$variableName`")
    public void read(String id, String partition, String containerKey, Set<VariableScope> scopes, String variableName)
    {
        String result = executeWithin(containerKey, c -> cosmosDbService.readById(c, id, partition));
        bddVariableContext.putVariable(scopes, variableName, result);
    }

    /**
     * Executes a query against Cosmos DB container and saves the result into scoped variable
     * @param query         The query to execute
     * @param containerKey  The container key
     * @param scopes        The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variables scopes<br>
     *                      <i>Available scopes:</i>
     *                      <ul>
     *                      <li><b>STEP</b> - the variable will be available only within the step,
     *                      <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                      <li><b>STORY</b> - the variable will be available within the whole story,
     *                      <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                      </ul>scopes
     * @param variableName  The variable name to store result.
     */
    @When("I execute `$query` query against CosmosDB container `$containerKey` and save result to $scopes"
            + " variable `$variableName`")
    public void query(String query, String containerKey, Set<VariableScope> scopes, String variableName)
    {
        String result = executeWithin(containerKey, c -> cosmosDbService.executeQuery(c, query));
        bddVariableContext.putVariable(scopes, variableName, result);
    }

    /**
     * Inserts the data into Cosmos DB container and verifies request status code to be 201.
     * @param data         The date to insert
     * @param containerKey The container key
     */
    @When("I insert `$data` into CosmosDB container `$containerKey`")
    public void insert(String data, String containerKey)
    {
        executeAndVerify(CREATED, containerKey, c -> cosmosDbService.insert(c, data));
    }

    /**
     * Upserts the data in the Cosmos DB container and verifies request status code to be 200.
     * @param data         The data to upsert
     * @param containerKey The containerKey
     */
    @When("I upsert `$data` into CosmosDB container `$containerKey`")
    public void upsert(String data, String containerKey)
    {
        executeAndVerify(OK, containerKey, c -> cosmosDbService.upsert(c, data));
    }

    /**
     * Deletes an items from Cosmos DB container and verifies request status code to be 204.
     * @param data         The data to delete
     * @param containerKey The containerKey
     */
    @When("I delete `$data` from CosmosDB container `$containerKey`")
    public void delete(String data, String containerKey)
    {
        executeAndVerify(NO_CONTENT, containerKey, c -> cosmosDbService.delete(c, data));
    }

    private void executeAndVerify(int expected, String containerKey, Function<CosmosDbContainer, Integer> toExecute)
    {
        int statusCode = executeWithin(containerKey, toExecute);
        softAssert.assertEquals("Query status code", expected, statusCode);
    }

    private <T> T executeWithin(String containerKey, Function<CosmosDbContainer, T> toExecute)
    {
        CosmosDbContainer cosmosDbContainer = containers.get(containerKey,
            "Unable to find connetion details for Cosmos DB container: %s", containerKey);
        return toExecute.apply(cosmosDbContainer);
    }
}
