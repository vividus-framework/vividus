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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class BatchResourceConfigurationTests
{
    private static final String PATTERN = "testPattern";

    private final BatchResourceConfiguration batchResourceConfiguration = new BatchResourceConfiguration();

    @Test
    void testSetResourceIncludePatterns()
    {
        batchResourceConfiguration.setResourceIncludePatterns(PATTERN);
        assertPatterns(batchResourceConfiguration.getResourceIncludePatterns());
    }

    @Test
    void testGetResourceIncludePatterns()
    {
        batchResourceConfiguration.setResourceIncludePatterns(null);
        assertTrue(batchResourceConfiguration.getResourceIncludePatterns().isEmpty());
    }

    @Test
    void testSetResourceExcludePatterns()
    {
        batchResourceConfiguration.setResourceExcludePatterns(PATTERN);
        assertPatterns(batchResourceConfiguration.getResourceExcludePatterns());
    }

    @Test
    void testGetResourceExcludePatterns()
    {
        batchResourceConfiguration.setResourceExcludePatterns(null);
        assertTrue(batchResourceConfiguration.getResourceExcludePatterns().isEmpty());
    }

    @Test
    void testGetResourceExcludePatternsDefaultValueEmpty()
    {
        assertTrue(batchResourceConfiguration.getResourceExcludePatterns().isEmpty());
    }

    @Test
    void testGetResourceIncludePatternsDefaultValueEmpty()
    {
        assertTrue(batchResourceConfiguration.getResourceExcludePatterns().isEmpty());
    }

    @Test
    void shouldParseCommaSeparatedPatterns()
    {
        var batch = new BatchResourceConfiguration();
        batch.setResourceExcludePatterns(", 1,2, 3 , 4,");
        assertEquals(List.of("1", "2", "3", "4"), batch.getResourceExcludePatterns());
    }

    private void assertPatterns(List<String> patterns)
    {
        assertEquals(1, patterns.size());
        assertTrue(patterns.contains(PATTERN));
    }
}
