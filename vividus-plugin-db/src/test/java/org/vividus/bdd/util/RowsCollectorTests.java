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

package org.vividus.bdd.util;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Map;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RowsCollectorTests
{
    private static final HashCode HASH_INT = Hashing.adler32().hashInt(1);
    private static final String KEY = "key";
    private static final String VALUE = "Value";
    private static final Map<String, Object> MAP = Map.of(KEY, VALUE);
    private static final List<Pair<HashCode, Map<String, Object>>> ROWS = List.of(Pair.of(HASH_INT, MAP),
            Pair.of(HASH_INT, MAP));

    @Test
    void shouldLeaveOnlyUniqueRowsAsDistinctFilter()
    {
        Assertions.assertEquals(Map.of(HASH_INT, MAP), ROWS.stream().collect(RowsCollector.DISTINCT.get()));
    }

    @Test
    void shouldLeaveStreamAsIsAsNOOPFilter()
    {
        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
            () -> ROWS.stream().collect(RowsCollector.NOOP.get()));
        assertThat(exception.getMessage(), containsString("Duplicate key"));
    }
}
