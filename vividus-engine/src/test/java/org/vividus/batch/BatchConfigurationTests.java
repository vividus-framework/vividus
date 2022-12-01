/*
 * Copyright 2019-2022 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class BatchConfigurationTests
{
    private static final String PATTERN = "testPattern";

    private final BatchConfiguration batchConfiguration = new BatchConfiguration();

    @Test
    void testSetResourceIncludePatterns()
    {
        batchConfiguration.setResourceIncludePatterns(PATTERN);
        assertPatterns(batchConfiguration.getResourceIncludePatterns());
    }

    @Test
    void testGetResourceIncludePatterns()
    {
        batchConfiguration.setResourceIncludePatterns(null);
        assertTrue(batchConfiguration.getResourceIncludePatterns().isEmpty());
    }

    @Test
    void testSetResourceExcludePatterns()
    {
        batchConfiguration.setResourceExcludePatterns(PATTERN);
        assertPatterns(batchConfiguration.getResourceExcludePatterns());
    }

    @Test
    void testGetResourceExcludePatterns()
    {
        batchConfiguration.setResourceExcludePatterns(null);
        assertTrue(batchConfiguration.getResourceExcludePatterns().isEmpty());
    }

    @Test
    void testGetResourceExcludePatternsDefaultValueEmpty()
    {
        assertTrue(batchConfiguration.getResourceExcludePatterns().isEmpty());
    }

    @Test
    void testGetResourceIncludePatternsDefaultValueEmpty()
    {
        assertTrue(batchConfiguration.getResourceExcludePatterns().isEmpty());
    }

    @Test
    void shouldParseCommaSeparatedPatterns()
    {
        var batch = new BatchConfiguration();
        batch.setResourceExcludePatterns(", 1,2, 3 , 4,");
        assertEquals(List.of("1", "2", "3", "4"), batch.getResourceExcludePatterns());
    }

    @Test
    void shouldSetNullWhenMetaFiltersStringIsNull()
    {
        var configuration = new BatchConfiguration();
        configuration.setMetaFilters((String) null);
        assertNull(configuration.getMetaFilters());
    }

    @Test
    void shouldParseWhenMetaFiltersStringIsNotNull()
    {
        List<String> filters = List.of("+feature vividus", "-skip");
        var configuration = new BatchConfiguration();
        configuration.setMetaFilters(String.join(",", filters));
        assertEquals(filters, configuration.getMetaFilters());
    }

    private void assertPatterns(List<String> patterns)
    {
        assertEquals(1, patterns.size());
        assertTrue(patterns.contains(PATTERN));
    }
}
