/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.batch;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.vividus.util.property.PropertyMapper;
import org.vividus.util.property.PropertyParser;

@ExtendWith(TestLoggerFactoryExtension.class)
class BatchStorageTests
{
    private static final List<String> BATCH_NUMBERS = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
    private static final String BATCH = "batch-";
    private static final List<String> BATCH_KEYS = BATCH_NUMBERS.stream().map(n -> BATCH + n).collect(
            Collectors.toList());

    private static final Duration EXPECTED_DURATION = Duration.ofSeconds(300);
    private static final String DEFAULT_RESOURCE_LOCATION = "";
    private static final String TRUE = "true";
    private static final String DOT = ".";

    private static final List<String> DEFAULT_META_FILTERS = List.of("groovy: !skip");

    private static final String BATCH_2_NAME = "second batch name";
    private static final int BATCH_2_THREADS = 5;
    private static final Duration BATCH_2_TIMEOUT = Duration.ofHours(1);
    private static final String BATCH_2_META_FILTERS = "grooyv: !ignored";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";

    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(BatchStorage.class);

    private BatchStorage batchStorage;

    void initializeBatchStorage() throws IOException
    {
        var batchConfigurations = new HashMap<String, String>();
        addBatchConfiguration(batchConfigurations, 1);
        addBatchConfiguration(batchConfigurations, 0);
        addBatchConfiguration(batchConfigurations, 9);
        addBatchConfiguration(batchConfigurations, 2);
        addBatchConfiguration(batchConfigurations, 3);
        addBatchConfiguration(batchConfigurations, 4);
        addBatchConfiguration(batchConfigurations, 5);
        addBatchConfiguration(batchConfigurations, 6);
        addBatchConfiguration(batchConfigurations, 7);
        addBatchConfiguration(batchConfigurations, 8);
        addBatchConfiguration(batchConfigurations, 10);

        createBatchStorage(batchConfigurations);
    }

    private void addBatchConfiguration(Map<String, String> batchConfigurations, int i)
    {
        batchConfigurations.put(BATCH_KEYS.get(i) + ".resource-location", DEFAULT_RESOURCE_LOCATION);
    }

    private void createBatchStorage(Map<String, String> batchConfigurations) throws IOException
    {
        batchConfigurations.put("batch-1.fail-fast", "");
        batchConfigurations.put("batch-2.scenario.fail-fast", TRUE);
        batchConfigurations.put("batch-2.story.fail-fast", TRUE);
        batchConfigurations.put("batch-2.name", BATCH_2_NAME);
        batchConfigurations.put("batch-2.threads", Integer.toString(BATCH_2_THREADS));
        batchConfigurations.put("batch-2.story.execution-timeout", BATCH_2_TIMEOUT.toString());
        batchConfigurations.put("batch-2.meta-filters", BATCH_2_META_FILTERS);
        batchConfigurations.put("batch-2.fail-fast", TRUE);
        batchConfigurations.put("batch-2.variables.key1", VALUE_1);
        batchConfigurations.put("batch-2.variables.key2", VALUE_2);
        var propertyParser = mock(PropertyParser.class);
        when(propertyParser.getPropertiesByPrefix(BATCH)).thenReturn(batchConfigurations);

        var propertyMapper = new PropertyMapper(DOT, PropertyNamingStrategies.KEBAB_CASE, propertyParser, Set.of());
        batchStorage = new BatchStorage(propertyMapper, EXPECTED_DURATION, null, DEFAULT_META_FILTERS, false);
    }

    @Test
    void shouldGetBatchConfigurationByKey() throws IOException
    {
        initializeBatchStorage();
        var batchConfiguration = batchStorage.getBatchConfiguration(BATCH_KEYS.get(0));
        assertAll(
            () -> assertEquals(DEFAULT_RESOURCE_LOCATION, batchConfiguration.getResourceLocation()),
            () -> assertEquals(List.of(), batchConfiguration.getResourceIncludePatterns()),
            () -> assertEquals(List.of(), batchConfiguration.getResourceExcludePatterns())
        );
    }

    @Test
    void shouldGetAllBatchConfigurations() throws IOException
    {
        initializeBatchStorage();
        Map<String, BatchConfiguration> allBatches = batchStorage.getBatchConfigurations();
        assertThat(allBatches.keySet(), Matchers.contains(BATCH_KEYS.toArray()));
    }

    @Test
    void shouldGetBatchConfigurationInitializedWithDefaultValues() throws IOException
    {
        initializeBatchStorage();
        assertDefaultBatchConfiguration(BATCH_KEYS.get(0));
    }

    @Test
    void shouldGetBatchConfigurationInitializedWithNonDefaultValues() throws IOException
    {
        initializeBatchStorage();
        var config = batchStorage.getBatchConfiguration(BATCH_KEYS.get(1));
        assertAll(
            () -> assertEquals(BATCH_2_NAME, config.getName()),
            () -> assertEquals(BATCH_2_THREADS, config.getThreads()),
            () -> assertEquals(BATCH_2_TIMEOUT, config.getStoryExecutionTimeout()),
            () -> assertEquals(List.of(BATCH_2_META_FILTERS), config.getMetaFilters()),
            () -> assertTrue(config.isFailFast()),
            () -> assertTrue(config.isFailStoryFast()),
            () -> assertTrue(config.isFailScenarioFast()),
            () -> assertEquals(Map.of("key1", VALUE_1, "key2", VALUE_2), config.getVariables())
        );
    }

    @Test
    void shouldCreateNonExistentBatchConfiguration() throws IOException
    {
        initializeBatchStorage();
        assertDefaultBatchConfiguration("batch-100");
    }

    private void assertDefaultBatchConfiguration(String batchKey)
    {
        var config = batchStorage.getBatchConfiguration(batchKey);
        assertAll(
            () -> assertEquals(batchKey,  config.getName()),
            () -> assertNull(config.getThreads()),
            () -> assertEquals(EXPECTED_DURATION, config.getStoryExecutionTimeout()),
            () -> assertEquals(DEFAULT_META_FILTERS, config.getMetaFilters()),
            () -> assertFalse(config.isFailFast()),
            () -> assertNull(config.isFailStoryFast()),
            () -> assertNull(config.isFailScenarioFast()),
            () -> assertEquals(Map.of(), config.getVariables())
        );
    }

    @Test
    void shouldThrowErrorIfBatchResourceConfigurationDoesNotContainResourceLocation() throws IOException
    {
        initializeBatchStorage();
        var batchConfigurations = new HashMap<String, String>();
        batchConfigurations.put("batch-1.resource-include-patterns", "*.story");
        var exception = assertThrows(IllegalArgumentException.class,
                () -> createBatchStorage(batchConfigurations));
        assertEquals("'resource-location' is missing for batch-1", exception.getMessage());
    }

    @Test
    void shouldWarnAboutPlainSecondsUsage() throws IOException
    {
        var propertyParser = mock(PropertyParser.class);
        when(propertyParser.getPropertiesByPrefix(BATCH)).thenReturn(Map.of("batch-999.resource-location",
            DEFAULT_RESOURCE_LOCATION));

        var propertyMapper = new PropertyMapper(DOT, PropertyNamingStrategies.KEBAB_CASE, propertyParser, Set.of());
        batchStorage = new BatchStorage(propertyMapper, null, "300", DEFAULT_META_FILTERS, false);
        assertEquals(List.of(warn("Property `bdd.story-execution-timeout` is deprecated and will be removed"
            + " in VIVIDUS 0.7.0. Please use `story.execution-timeout` instead.")), LOGGER.getLoggingEvents());
        var config = batchStorage.getBatchConfiguration("batch-999");
        assertEquals(EXPECTED_DURATION, config.getStoryExecutionTimeout());
    }

    @Test
    void shouldThrowExceptionWhenBothPropertiesSpecified()
    {
        var propertyParser = mock(PropertyParser.class);
        when(propertyParser.getPropertiesByPrefix(BATCH)).thenReturn(Map.of("batch-998.resource-location",
                DEFAULT_RESOURCE_LOCATION));
        var duration = Duration.ofHours(111);
        var propertyMapper = new PropertyMapper(DOT, PropertyNamingStrategies.KEBAB_CASE, propertyParser, Set.of());
        var iae = assertThrows(IllegalArgumentException.class,
            () -> new BatchStorage(propertyMapper, duration, "202", DEFAULT_META_FILTERS, false));
        assertEquals(iae.getMessage(), "Conflicting properties are found: `bdd.story-execution-timeout`"
            + " and `story.execution-timeout`. Property `bdd.story-execution-timeout` is deprecated and will be"
            + " removed in VIVIDUS 0.7.0. Please use `story.execution-timeout` instead.");
    }

    @Test
    void shouldUseDefaultTimeout() throws IOException
    {
        var propertyParser = mock(PropertyParser.class);
        when(propertyParser.getPropertiesByPrefix(BATCH)).thenReturn(Map.of("batch-997.resource-location",
                DEFAULT_RESOURCE_LOCATION));

        var propertyMapper = new PropertyMapper(DOT, PropertyNamingStrategies.KEBAB_CASE, propertyParser, Set.of());
        batchStorage = new BatchStorage(propertyMapper, null, null, DEFAULT_META_FILTERS, false);
        var config = batchStorage.getBatchConfiguration("batch-997");
        assertEquals(Duration.ofHours(3), config.getStoryExecutionTimeout());
    }
}
