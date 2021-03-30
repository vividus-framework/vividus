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

import java.util.stream.Collectors;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.vividus.azure.cosmos.model.CosmosDbAccount;
import org.vividus.azure.cosmos.model.CosmosDbContainer;
import org.vividus.azure.cosmos.model.CosmosDbDatabase;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.PropertyMappedCollection;

public class CosmosDbService
{
    private final LoadingCache<CosmosDbContainer, CosmosContainer> containers = CacheBuilder.newBuilder()
            .build(
                new CacheLoader<CosmosDbContainer, CosmosContainer>()
                {
                    @Override
                    public CosmosContainer load(CosmosDbContainer cosmosDbContainer)
                    {
                        String databaseKey = cosmosDbContainer.getDbKey();
                        CosmosDbDatabase db = databases.get(databaseKey,
                                "Database configuration not found for the key: %s", databaseKey);
                        String accountKey = db.getAccountKey();
                        CosmosDbAccount account = accounts.get(accountKey,
                                "Account configuration not found for the key: %s", accountKey);
                        return  new CosmosClientBuilder().endpoint(account.getEndpoint())
                                                         .key(account.getKey())
                                                         .connectionSharingAcrossClientsEnabled(true)
                                                         .buildClient()
                                                         .getDatabase(db.getId())
                                                         .getContainer(cosmosDbContainer.getId());
                    }
                });

    private final JsonUtils jsonUtils;
    private final PropertyMappedCollection<CosmosDbAccount> accounts;
    private final PropertyMappedCollection<CosmosDbDatabase> databases;

    public CosmosDbService(JsonUtils jsonUtils, PropertyMappedCollection<CosmosDbAccount> accounts,
            PropertyMappedCollection<CosmosDbDatabase> databases)
    {
        this.jsonUtils = jsonUtils;
        this.accounts = accounts;
        this.databases = databases;
    }

    public String executeQuery(CosmosDbContainer cosmosDbContainer, String query)
    {
        return containers.getUnchecked(cosmosDbContainer).queryItems(query, null, JsonNode.class)
                                                         .stream()
                                                         .collect(Collectors.collectingAndThen(Collectors.toList(),
                                                             jsonUtils::toPrettyJson));
    }

    public String readById(CosmosDbContainer cosmosDbContainer, String id, String partition)
    {
        return jsonUtils.toPrettyJson(containers.getUnchecked(cosmosDbContainer)
                                                .readItem(id, new PartitionKey(partition), JsonNode.class)
                                                .getItem());
    }

    public int insert(CosmosDbContainer cosmosDbContainer, String data)
    {
        return containers.getUnchecked(cosmosDbContainer)
                         .createItem(jsonUtils.readTree(data))
                         .getStatusCode();
    }

    public int upsert(CosmosDbContainer cosmosDbContainer, String data)
    {
        return  containers.getUnchecked(cosmosDbContainer)
                          .upsertItem(jsonUtils.readTree(data))
                          .getStatusCode();
    }

    public int delete(CosmosDbContainer cosmosDbContainer, String data)
    {
        return containers.getUnchecked(cosmosDbContainer)
                         .deleteItem(jsonUtils.readTree(data), null)
                         .getStatusCode();
    }
}
