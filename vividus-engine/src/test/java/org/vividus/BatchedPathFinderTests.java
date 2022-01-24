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

package org.vividus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.batch.BatchResourceConfiguration;
import org.vividus.batch.BatchStorage;

@ExtendWith(MockitoExtension.class)
class BatchedPathFinderTests
{
    @Mock private PathFinder pathFinder;
    @Mock private BatchStorage batchStorage;
    @InjectMocks private BatchedPathFinder batchedPathFinder;

    @Test
    void shouldGetPathsWithCaching() throws IOException
    {
        BatchResourceConfiguration batchResourceConfiguration = new BatchResourceConfiguration();
        batchResourceConfiguration.setResourceLocation("testLocation");
        batchResourceConfiguration.setResourceIncludePatterns("testIncludePattern");
        batchResourceConfiguration.setResourceExcludePatterns("testExcludePattern");
        String batchKey = "batch1";
        when(batchStorage.getBatchResourceConfigurations()).thenReturn(Map.of(batchKey, batchResourceConfiguration));
        List<String> testPaths = List.of("testPath");
        when(pathFinder.findPaths(batchResourceConfiguration)).thenReturn(testPaths);
        Map<String, List<String>> expected = Map.of(batchKey, testPaths);
        assertEquals(expected, batchedPathFinder.getPaths());
        assertEquals(expected, batchedPathFinder.getPaths());
        verify(pathFinder).findPaths(batchResourceConfiguration);
        verify(batchStorage).getBatchResourceConfigurations();
        verifyNoMoreInteractions(batchStorage, pathFinder);
    }
}
