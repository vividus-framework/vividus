/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.vividus.bdd.resource.BddResourceLoader;
import org.vividus.bdd.resource.ResourceBatch;

@ExtendWith(MockitoExtension.class)
class PathFinderTests
{
    private static final String PATTERN_SEPARATOR = ",";
    private static final String STORY_INCLUDE_PATTERN = "*.story";
    private static final String STORY_EXCLUDE_PATTERN = "Some.story";
    private static final String OTHER_INCLUDE_PATTERN = "*.json";
    private static final String OTHER_EXCLUDE_PATTERN = "Some.json";

    @Mock
    private BddResourceLoader bddResourceLoader;

    @InjectMocks
    private PathFinder pathFinder;

    @Test
    void testFindPaths() throws IOException
    {
        ResourceBatch resourceBatch = new ResourceBatch();
        resourceBatch.setResourceLocation("story/uat");
        resourceBatch.setResourceIncludePatterns(STORY_INCLUDE_PATTERN + PATTERN_SEPARATOR + OTHER_INCLUDE_PATTERN);
        resourceBatch.setResourceExcludePatterns(STORY_EXCLUDE_PATTERN + PATTERN_SEPARATOR + OTHER_EXCLUDE_PATTERN);
        String uri1 = "uri:resource1";
        Resource resource1 = mockResource(uri1);
        String uri2 = "uri:resource2";
        Resource resource2 = mockResource(uri2);
        String uri3 = "uri:resource3";
        Resource resource3 = mockResource(uri3);
        mockResources(resourceBatch, STORY_INCLUDE_PATTERN, resource3);
        mockResources(resourceBatch, STORY_EXCLUDE_PATTERN);
        mockResources(resourceBatch, OTHER_INCLUDE_PATTERN, resource2, resource1);
        mockResources(resourceBatch, OTHER_EXCLUDE_PATTERN, resource2);
        List<String> paths = pathFinder.findPaths(resourceBatch);
        assertEquals(List.of(uri1, uri3), paths);
    }

    private Resource mockResource(String uri) throws IOException
    {
        return when(mock(Resource.class).getURI()).thenReturn(URI.create(uri)).getMock();
    }

    private void mockResources(ResourceBatch resourceBatch, String pattern, Resource... resources)
    {
        doReturn(resources).when(bddResourceLoader).getResources(resourceBatch.getResourceLocation(), pattern);
    }
}
