/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.redis.steps;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.annotations.When;

import redis.clients.jedis.Jedis;

public class RedisSteps
{
    private final Map<String, String> connections;

    public RedisSteps(Map<String, String> connections)
    {
        this.connections = connections;
    }

    /**
     * Flushes (removes all keys from) the specified Redis database on a given Redis instance.
     *
     * @param dbIndex     the non-negative index of the Redis database to flush
     * @param instanceKey the key identifying the Redis instance in the connections map
     */
    @When("I flush `$dbIndex` database on `$instanceKey` Redis instance")
    public void flushDatabase(int dbIndex, String instanceKey)
    {
        Validate.isTrue(dbIndex >= 0, "Database index must be a non-negative integer");
        String connection = connections.get(instanceKey);
        Validate.isTrue(connection != null, "Connection with key '%s' does not exist",
                instanceKey);

        try (Jedis jedis = new Jedis(connection))
        {
            jedis.select(dbIndex);
            jedis.flushDB();
        }
    }
}
