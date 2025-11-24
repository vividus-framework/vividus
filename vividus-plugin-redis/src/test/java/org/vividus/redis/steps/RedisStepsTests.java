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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import redis.clients.jedis.Jedis;

@ExtendWith(TestLoggerFactoryExtension.class)
class RedisStepsTests
{
    private static final String INSTANCE = "instance";
    private static final String LOCALHOST = "localhost";
    private static final Map<String, String> CONNECTIONS = Map.of(INSTANCE, LOCALHOST);

    private final TestLogger logger = TestLoggerFactory.getTestLogger(RedisSteps.class);

    @Test
    void shouldFlushDatabase()
    {
        try (var jedisConstruction = Mockito.mockConstruction(Jedis.class, (mock, context) -> {
            assertEquals(1, context.getCount());
            assertEquals(LOCALHOST, context.arguments().get(0));
        }))
        {
            int index = 2;
            RedisSteps redisSteps = new RedisSteps(CONNECTIONS);
            redisSteps.flushDatabase(index, INSTANCE);

            Jedis jedisMock = jedisConstruction.constructed().get(0);
            verify(jedisMock).select(index);
            verify(jedisMock).flushDB();
            verify(jedisMock).close();
            assertThat(logger.getLoggingEvents(), is(List.of(info("The database with {} ID has been flushed", index))));
        }
    }

    @Test
    void shouldThrowExceptionForUnknownInstance()
    {
        RedisSteps redisSteps = new RedisSteps(CONNECTIONS);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> redisSteps.flushDatabase(0, "unknown"));
        assertEquals("Connection with key 'unknown' does not exist", thrown.getMessage());
        assertThat(logger.getLoggingEvents(), is(List.of()));
    }

    @Test
    void shouldThrowExceptionForNegativeDbIndex()
    {
        RedisSteps redisSteps = new RedisSteps(CONNECTIONS);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> redisSteps.flushDatabase(-1, INSTANCE));
        assertEquals("Database index must be a non-negative integer", thrown.getMessage());
        assertThat(logger.getLoggingEvents(), is(List.of()));
    }
}
