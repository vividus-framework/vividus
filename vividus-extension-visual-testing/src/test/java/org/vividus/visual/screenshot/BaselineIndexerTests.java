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

package org.vividus.visual.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class BaselineIndexerTests
{
    private static final String BASELINE = "baseline";
    private static final String INDEXER = "indexer";

    private BaselineIndexer baselineIndexer;

    @Test
    void shouldReturnSameBaselineIfIndexerNameNotSet()
    {
        baselineIndexer = new BaselineIndexer(Map.of(), Optional.of(INDEXER));
        assertEquals(BASELINE, baselineIndexer.createIndexedBaseline(BASELINE));
    }

    @Test
    void shouldReturnSameBaselineIfIndexerNameSetButItDoesntExist()
    {
        baselineIndexer = new BaselineIndexer(Map.of(), Optional.empty());
        assertEquals(BASELINE, baselineIndexer.createIndexedBaseline(BASELINE));
    }

    @Test
    void shouldModifyBaselineName()
    {
        var indexer = mock(IScreenshotIndexer.class);
        baselineIndexer = new BaselineIndexer(Map.of(INDEXER, indexer), Optional.of(INDEXER));
        var indexedBaseline = "baseline-1";
        when(indexer.index(BASELINE)).thenReturn(indexedBaseline);
        assertEquals(indexedBaseline, baselineIndexer.createIndexedBaseline(BASELINE));
    }
}
