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

package org.vividus.bdd.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ResourceBatchTests
{
    private static final String PATTERN = "testPattern";

    private final ResourceBatch resourceBatch = new ResourceBatch();

    @Test
    void testSetResourceIncludePatterns()
    {
        resourceBatch.setResourceIncludePatterns(PATTERN);
        assertPatterns(resourceBatch.getResourceIncludePatterns());
    }

    @Test
    void testGetResourceIncludePatterns()
    {
        resourceBatch.setResourceIncludePatterns(null);
        assertTrue(resourceBatch.getResourceIncludePatterns().isEmpty());
    }

    @Test
    void testSetResourceExcludePatterns()
    {
        resourceBatch.setResourceExcludePatterns(PATTERN);
        assertPatterns(resourceBatch.getResourceExcludePatterns());
    }

    @Test
    void testGetResourceExcludePatterns()
    {
        resourceBatch.setResourceExcludePatterns(null);
        assertTrue(resourceBatch.getResourceExcludePatterns().isEmpty());
    }

    @Test
    void testGetResourceExcludePatternsDefaultValueEmpty()
    {
        assertTrue(resourceBatch.getResourceExcludePatterns().isEmpty());
    }

    @Test
    void testGetResourceIncludePatternsDefaultValueEmpty()
    {
        assertTrue(resourceBatch.getResourceExcludePatterns().isEmpty());
    }

    private void assertPatterns(List<String> patterns)
    {
        assertEquals(1, patterns.size());
        assertTrue(patterns.contains(PATTERN));
    }
}
