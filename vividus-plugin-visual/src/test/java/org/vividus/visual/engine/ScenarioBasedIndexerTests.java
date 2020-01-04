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

package org.vividus.visual.engine;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScenarioBasedIndexerTests
{
    private static final String NAME_1 = "name[1]";
    private static final String NAME_0 = "name[0]";
    private static final String NAME = "name";
    private ScenarioBasedIndexer indexer;

    @BeforeEach
    void beforeEach()
    {
        indexer = new ScenarioBasedIndexer();
    }

    @Test
    void shouldIndexBaselineName()
    {
        assertAll(
            () -> assertEquals(NAME_0, indexer.index(NAME)),
            () -> assertEquals(NAME_1, indexer.index(NAME)));
    }

    @Test
    void shouldResetCounter()
    {
        assertEquals(NAME_0, indexer.index(NAME));
        assertEquals(NAME_1, indexer.index(NAME));
        indexer.resetIndex();
        assertEquals(NAME_0, indexer.index(NAME));
    }
}
