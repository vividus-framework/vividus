/*
 * Copyright 2021 the original author or authors.
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.cosmos.model.CosmosDbContainer;
import org.vividus.bdd.context.BddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.softassert.SoftAssert;
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith(MockitoExtension.class)
class CosmosDbStepsTests
{
    private static final int CREATED = 201;
    private static final int NO_CONTENT = 204;
    private static final int OK = 200;
    private static final String PARTITION = "partition";
    private static final String ID = "id";
    private static final int UNAUTHORIZED = 401;
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);
    private static final String RESULT_SET = "resultSet";
    private static final String RESULT = "{}";
    private static final String QUERY = "SELECT * FROM DEVS";
    private static final String CONTAINER = "container";
    private final CosmosDbContainer container = new CosmosDbContainer();

    @Mock private BddVariableContext bddVariableContext;
    @Mock private PropertyMappedCollection<CosmosDbContainer> containers;
    @Mock private CosmosDbService cosmosDbService;
    @Mock private SoftAssert softAssert;

    @InjectMocks private CosmosDbSteps dbSteps;

    @BeforeEach
    void beforeEach()
    {
        when(containers.get(CONTAINER, "Unable to find connetion details for Cosmos DB container: %s", CONTAINER))
            .thenReturn(container);
    }

    @Test
    void shouldQueryCosmosDatabase()
    {
        when(cosmosDbService.executeQuery(container, QUERY)).thenReturn(RESULT);
        dbSteps.query(QUERY, CONTAINER, SCOPES, RESULT_SET);
        verify(bddVariableContext).putVariable(SCOPES, RESULT_SET, RESULT);
        verifyNoInteractions(softAssert);
    }

    @Test
    void shouldInsertItem()
    {
        when(cosmosDbService.insert(container, RESULT)).thenReturn(UNAUTHORIZED);
        dbSteps.insert(RESULT, CONTAINER);
        verifyAssertion(CREATED);
    }

    @Test
    void shouldUpsertItem()
    {
        when(cosmosDbService.upsert(container, RESULT)).thenReturn(UNAUTHORIZED);
        dbSteps.upsert(RESULT, CONTAINER);
        verifyAssertion(OK);
    }

    @Test
    void shouldDeleteItem()
    {
        when(cosmosDbService.delete(container, RESULT)).thenReturn(UNAUTHORIZED);
        dbSteps.delete(RESULT, CONTAINER);
        verifyAssertion(NO_CONTENT);
    }

    @Test
    void shouldReadItemByIdAndPartition()
    {
        when(cosmosDbService.readById(container, ID, PARTITION)).thenReturn(RESULT);
        dbSteps.read(ID, PARTITION, CONTAINER, SCOPES, RESULT_SET);
        verify(bddVariableContext).putVariable(SCOPES, RESULT_SET, RESULT);
        verifyNoInteractions(softAssert);
    }

    private void verifyAssertion(int expected)
    {
        verify(softAssert).assertEquals("Query status code", expected, UNAUTHORIZED);
    }
}
