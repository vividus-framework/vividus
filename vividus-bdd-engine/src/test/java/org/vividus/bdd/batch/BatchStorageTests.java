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
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vividus.util.property.IPropertyMapper;
import org.vividus.util.property.PropertyMapper;
import org.vividus.util.property.PropertyParser;

class BatchStorageTests
{
    private static final String BATCH_LOADER_PROPERTY_PREFIX = "bdd.story-loader.batch-";

    private static final List<String> BATCH_NUMBERS = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
    private static final List<String> BATCH_KEYS = BATCH_NUMBERS.stream().map(n -> "batch-" + n).collect(
            Collectors.toList());

    private static final String DEFAULT_RESOURCE_LOCATION = "";

    private static final long DEFAULT_TIMEOUT = 300;
    private static final List<String> DEFAULT_META_FILTERS = List.of("groovy: !skip");

    private static final String BATCH_2_NAME = "second batch name";
    private static final int BATCH_2_THREADS = 5;
    private static final Duration BATCH_2_TIMEOUT = Duration.ofHours(1);
    private static final String BATCH_2_META_FILTERS = "grooyv: !ignored";

    private BatchStorage batchStorage;

    @BeforeEach
    void beforeEach() throws IOException
    {
        Map<String, String> batchResourceConfigurations = new HashMap<>();
        addBatchResourceConfiguration(batchResourceConfigurations, 1);
        addBatchResourceConfiguration(batchResourceConfigurations, 0);
        addBatchResourceConfiguration(batchResourceConfigurations, 9);
        addBatchResourceConfiguration(batchResourceConfigurations, 2);
        addBatchResourceConfiguration(batchResourceConfigurations, 3);
        addBatchResourceConfiguration(batchResourceConfigurations, 4);
        addBatchResourceConfiguration(batchResourceConfigurations, 5);
        addBatchResourceConfiguration(batchResourceConfigurations, 6);
        addBatchResourceConfiguration(batchResourceConfigurations, 7);
        addBatchResourceConfiguration(batchResourceConfigurations, 8);
        addBatchResourceConfiguration(batchResourceConfigurations, 10);

        PropertyParser propertyParser = mock(PropertyParser.class);
        when(propertyParser.getPropertiesByPrefix(BATCH_LOADER_PROPERTY_PREFIX)).thenReturn(
                batchResourceConfigurations);
        when(propertyParser.getPropertiesByPrefix("bdd.batch-")).thenReturn(Map.of(
            "bdd.batch-1.fail-fast", "",
            "bdd.batch-2.name", BATCH_2_NAME,
            "bdd.batch-2.threads", Integer.toString(BATCH_2_THREADS),
            "bdd.batch-2.story-execution-timeout", BATCH_2_TIMEOUT.toString(),
            "bdd.batch-2.meta-filters", BATCH_2_META_FILTERS,
            "bdd.batch-2.fail-fast", "true"
        ));

        IPropertyMapper propertyMapper = new PropertyMapper(".", PropertyNamingStrategies.KEBAB_CASE, propertyParser,
                Set.of());
        batchStorage = new BatchStorage(propertyMapper, Long.toString(DEFAULT_TIMEOUT), DEFAULT_META_FILTERS, false);
    }

    private void addBatchResourceConfiguration(Map<String, String> batchResourceConfigurations, int i)
    {
        batchResourceConfigurations.put(BATCH_LOADER_PROPERTY_PREFIX + BATCH_NUMBERS.get(i) + ".resource-location",
                DEFAULT_RESOURCE_LOCATION);
    }

    @Test
    void shouldGetBatchResourceConfigurationByKey()
    {
        BatchResourceConfiguration batch = batchStorage.getBatchResourceConfiguration(BATCH_KEYS.get(0));
        assertAll(
            () -> assertEquals(DEFAULT_RESOURCE_LOCATION, batch.getResourceLocation()),
            () -> assertEquals(List.of(), batch.getResourceIncludePatterns()),
            () -> assertEquals(List.of(), batch.getResourceExcludePatterns())
        );
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
        assertAll(
            () -> assertEquals(BATCH_2_NAME, config.getName()),
            () -> assertEquals(BATCH_2_THREADS, config.getThreads()),
            () -> assertEquals(BATCH_2_TIMEOUT, config.getStoryExecutionTimeout()),
            () -> assertEquals(List.of(BATCH_2_META_FILTERS), config.getMetaFilters()),
            () -> assertTrue(config.isFailFast())
        );
    }

    @Test
    void shouldCreateNonExistentBatchExecutionConfiguration()
    {
        assertDefaultBatchExecutionConfiguration("batch-100");
    }

    private void assertDefaultBatchExecutionConfiguration(String batchKey)
    {
        BatchExecutionConfiguration config = batchStorage.getBatchExecutionConfiguration(batchKey);
        assertAll(
            () -> assertEquals(batchKey,  config.getName()),
            () -> assertNull(config.getThreads()),
            () -> assertEquals(Duration.ofSeconds(DEFAULT_TIMEOUT), config.getStoryExecutionTimeout()),
            () -> assertEquals(DEFAULT_META_FILTERS, config.getMetaFilters()),
            () -> assertFalse(config.isFailFast())
        );
    }
}
