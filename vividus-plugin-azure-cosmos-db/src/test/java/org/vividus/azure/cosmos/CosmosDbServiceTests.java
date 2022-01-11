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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.cosmos.model.CosmosDbAccount;
import org.vividus.azure.cosmos.model.CosmosDbContainer;
import org.vividus.azure.cosmos.model.CosmosDbDatabase;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith(MockitoExtension.class)
class CosmosDbServiceTests
{
    private static final String UNCHECKED = "unchecked";
    private static final String ENDPOINT = "https://azure.cosmos";
    private static final String ACCOUNT_KEY = "accountKey";
    private static final String DB_KEY = "dbKey";
    private static final String PARTITION = "partition";
    private static final String ID = "id";
    private static final String JSON_STRING = "{\"key\": \"value\"}";
    private static final String QUERY = "SELECT * FROM ITEMS";
    private static final String DB = "db";
    private static final String KEY = "key";

    private final JsonUtils jsonUtils = new JsonUtils();
    private final JsonNode node = jsonUtils.readTree(JSON_STRING);
    private CosmosDbService cosmosDbService;

    @Mock private CosmosContainer container;
    @Mock private CosmosDatabase database;
    @Mock private PropertyMappedCollection<CosmosDbAccount> accounts;
    @Mock private PropertyMappedCollection<CosmosDbDatabase> databases;

    @BeforeEach
    void beforeEach()
    {
        CosmosDbDatabase database = new CosmosDbDatabase();
        database.setAccountKey(ACCOUNT_KEY);
        database.setId(DB);
        when(databases.get(DB_KEY, "Database configuration not found for the key: %s", DB_KEY)).thenReturn(database);
        CosmosDbAccount cosmosDbAccount = new CosmosDbAccount();
        cosmosDbAccount.setEndpoint(ENDPOINT);
        cosmosDbAccount.setKey(KEY);
        when(accounts.get(ACCOUNT_KEY, "Account configuration not found for the key: %s", ACCOUNT_KEY))
            .thenReturn(cosmosDbAccount);
        cosmosDbService = new CosmosDbService(jsonUtils, accounts, databases);
    }

    @Test
    void shouldQueryDatabase()
    {
        testWithContainer((cosmosDbContainer, container) -> {
            @SuppressWarnings(UNCHECKED)
            CosmosPagedIterable<JsonNode> result = mock(CosmosPagedIterable.class);
            when(result.stream()).thenReturn(Stream.of(node));
            when(container.queryItems(QUERY, null, JsonNode.class)).thenReturn(result);
            String resultSet = cosmosDbService.executeQuery(cosmosDbContainer, QUERY);
            assertEquals(String.format("[ {%n  \"key\" : \"value\"%n} ]"), resultSet);
        }, "query");
    }

    @Test
    void shouldDeleteItem()
    {
        testWithContainer((cosmosDbContainer, container) -> {
            @SuppressWarnings(UNCHECKED)
            CosmosItemResponse<Object> response = mock(CosmosItemResponse.class);
            when(response.getStatusCode()).thenReturn(101);
            when(container.deleteItem(node, null)).thenReturn(response);
            assertEquals(101, cosmosDbService.delete(cosmosDbContainer, JSON_STRING));
        }, "delete");
    }

    @Test
    void shouldInsertItem()
    {
        testWithContainer((cosmosDbContainer, container) -> {
            @SuppressWarnings(UNCHECKED)
            CosmosItemResponse<JsonNode> response = mock(CosmosItemResponse.class);
            when(response.getStatusCode()).thenReturn(102);
            when(container.createItem(node)).thenReturn(response);
            assertEquals(102, cosmosDbService.insert(cosmosDbContainer, JSON_STRING));
        }, "insert");
    }

    @Test
    void shouldUpsertItem()
    {
        testWithContainer((cosmosDbContainer, container) -> {
            @SuppressWarnings(UNCHECKED)
            CosmosItemResponse<JsonNode> response = mock(CosmosItemResponse.class);
            when(response.getStatusCode()).thenReturn(102);
            when(container.upsertItem(node)).thenReturn(response);
            assertEquals(102, cosmosDbService.upsert(cosmosDbContainer, JSON_STRING));
        }, "upsert");
    }

    @Test
    void shouldReadItem()
    {
        testWithContainer((cosmosDbContainer, container) -> {
            @SuppressWarnings(UNCHECKED)
            CosmosItemResponse<JsonNode> response = mock(CosmosItemResponse.class);
            when(response.getItem()).thenReturn(node);
            when(container.readItem(ID, new PartitionKey(PARTITION), JsonNode.class)).thenReturn(response);
            assertEquals(String.format("{%n  \"key\" : \"value\"%n}"),
                    cosmosDbService.readById(cosmosDbContainer, ID, PARTITION));
        }, "http://azure.com/read");
    }

    private void testWithContainer(BiConsumer<CosmosDbContainer, CosmosContainer> testToRun, String containerId)
    {
        CosmosDbContainer dbContainer = new CosmosDbContainer();
        dbContainer.setId(containerId);
        dbContainer.setDbKey(DB_KEY);
        try (CosmosClient client = mock(CosmosClient.class);
                MockedConstruction<CosmosClientBuilder> builderConstructor =
                mockConstruction(CosmosClientBuilder.class, (mock, context) -> {
                    when(mock.endpoint(ENDPOINT)).thenReturn(mock);
                    when(mock.key(KEY)).thenReturn(mock);
                    when(mock.connectionSharingAcrossClientsEnabled(true)).thenReturn(mock);
                    when(mock.buildClient()).thenReturn(client);
                }))
        {
            when(client.getDatabase(DB)).thenReturn(database);
            when(database.getContainer(containerId)).thenReturn(container);
            testToRun.accept(dbContainer, container);
            assertThat(builderConstructor.constructed(), hasSize(1));
        }
    }
}
