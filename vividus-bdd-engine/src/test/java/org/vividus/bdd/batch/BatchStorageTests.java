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

package org.vividus.bdd.batch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vividus.util.property.IPropertyMapper;

class BatchStorageTests
{
    private static final List<String> BATCH_NUMBERS = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
    private static final List<String> BATCH_KEYS = BATCH_NUMBERS.stream().map(n -> "batch-" + n).collect(
            Collectors.toList());

    private static final long DEFAULT_TIMEOUT = 300;
    private static final List<String> DEFAULT_META_FILTERS = List.of("groovy: !skip");

    private static final String BATCH_2_NAME = "second batch name";
    private static final int BATCH_2_THREADS = 5;
    private static final Duration BATCH_2_TIMEOUT = Duration.ofHours(1);
    private static final String BATCH_2_META_FILTERS = "grooyv: !ignored";

    private BatchStorage batchStorage;

    private Map<String, BatchResourceConfiguration> batchResourceConfigurations;

    @BeforeEach
    void beforeEach() throws IOException
    {
        batchResourceConfigurations = new HashMap<>();
        batchResourceConfigurations.put(BATCH_NUMBERS.get(1), new BatchResourceConfiguration());
        batchResourceConfigurations.put(BATCH_NUMBERS.get(0), new BatchResourceConfiguration());
        batchResourceConfigurations.put(BATCH_NUMBERS.get(9), new BatchResourceConfiguration());
        batchResourceConfigurations.put(BATCH_NUMBERS.get(2), new BatchResourceConfiguration());
        batchResourceConfigurations.put(BATCH_NUMBERS.get(3), new BatchResourceConfiguration());
        batchResourceConfigurations.put(BATCH_NUMBERS.get(4), new BatchResourceConfiguration());
        batchResourceConfigurations.put(BATCH_NUMBERS.get(5), new BatchResourceConfiguration());
        batchResourceConfigurations.put(BATCH_NUMBERS.get(6), new BatchResourceConfiguration());
        batchResourceConfigurations.put(BATCH_NUMBERS.get(7), new BatchResourceConfiguration());
        batchResourceConfigurations.put(BATCH_NUMBERS.get(8), new BatchResourceConfiguration());
        batchResourceConfigurations.put(BATCH_NUMBERS.get(10), new BatchResourceConfiguration());

        BatchExecutionConfiguration batchExecutionConfiguration1 = new BatchExecutionConfiguration();
        batchExecutionConfiguration1.setMetaFilters((String) null);
        batchExecutionConfiguration1.setIgnoreFailure(null);
        BatchExecutionConfiguration batchExecutionConfiguration2 = new BatchExecutionConfiguration();
        batchExecutionConfiguration2.setName(BATCH_2_NAME);
        batchExecutionConfiguration2.setThreads(BATCH_2_THREADS);
        batchExecutionConfiguration2.setStoryExecutionTimeout(BATCH_2_TIMEOUT);
        batchExecutionConfiguration2.setMetaFilters(BATCH_2_META_FILTERS);
        batchExecutionConfiguration2.setIgnoreFailure(false);
        Map<String, BatchExecutionConfiguration> batchExecutionConfigurations = new HashMap<>();
        batchExecutionConfigurations.put(BATCH_NUMBERS.get(0), batchExecutionConfiguration1);
        batchExecutionConfigurations.put(BATCH_NUMBERS.get(1), batchExecutionConfiguration2);

        IPropertyMapper propertyMapper = mock(IPropertyMapper.class);
        when(propertyMapper.readValues("bdd.story-loader.batch-", BatchResourceConfiguration.class)).thenReturn(
                batchResourceConfigurations);
        when(propertyMapper.readValues("bdd.batch-", BatchExecutionConfiguration.class)).thenReturn(
                batchExecutionConfigurations);

        batchStorage = new BatchStorage(propertyMapper, Long.toString(DEFAULT_TIMEOUT), DEFAULT_META_FILTERS, true);
    }

    @Test
    void shouldGetBatchResourceConfigurationByKey()
    {
        BatchResourceConfiguration batch = batchStorage.getBatchResourceConfiguration(BATCH_KEYS.get(0));
        assertEquals(batchResourceConfigurations.get(BATCH_NUMBERS.get(0)), batch);
    }

    @Test
    void shouldGetAllBatchResourceConfigurations()
    {
        Map<String, BatchResourceConfiguration> allBatches = batchStorage.getBatchResourceConfigurations();
        assertThat(allBatches.keySet(), Matchers.contains(BATCH_KEYS.toArray()));
    }

    @Test
    void shouldGetBatchExecutionConfigurationInitializedWithDefaultValues()
    {
        assertDefaultBatchExecutionConfiguration(BATCH_KEYS.get(0));
    }

    @Test
    void shouldGetBatchExecutionConfigurationInitializedWithNonDefaultValues()
    {
        BatchExecutionConfiguration config = batchStorage.getBatchExecutionConfiguration(BATCH_KEYS.get(1));
        assertAll(() ->
        {
            assertEquals(BATCH_2_NAME, config.getName());
            assertEquals(BATCH_2_THREADS, config.getThreads());
            assertEquals(BATCH_2_TIMEOUT, config.getStoryExecutionTimeout());
            assertEquals(List.of(BATCH_2_META_FILTERS), config.getMetaFilters());
            assertFalse(config.isIgnoreFailure());
        });
    }

    @Test
    void shouldCreateNonExistentBatchExecutionConfiguration()
    {
        assertDefaultBatchExecutionConfiguration("batch-100");
    }

    private void assertDefaultBatchExecutionConfiguration(String batchKey)
    {
        BatchExecutionConfiguration config = batchStorage.getBatchExecutionConfiguration(batchKey);
        assertAll(() -> {
            assertEquals(batchKey,  config.getName());
            assertNull(config.getThreads());
            assertEquals(Duration.ofSeconds(DEFAULT_TIMEOUT), config.getStoryExecutionTimeout());
            assertEquals(DEFAULT_META_FILTERS, config.getMetaFilters());
            assertTrue(config.isIgnoreFailure());
        });
    }
}
