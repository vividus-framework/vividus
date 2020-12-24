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

package org.vividus.bdd.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class BatchExecutionConfigurationTests
{
    @Test
    void shouldSetNullWhenMetaFiltersStringIsNull()
    {
        BatchExecutionConfiguration configuration = new BatchExecutionConfiguration();
        configuration.setMetaFilters((String) null);
        assertNull(configuration.getMetaFilters());
    }

    @Test
    void shouldParseWhenMetaFiltersStringIsNotNull()
    {
        List<String> filters = List.of("+feature vividus", "-skip");
        BatchExecutionConfiguration configuration = new BatchExecutionConfiguration();
        configuration.setMetaFilters(String.join(",", filters));
        assertEquals(filters, configuration.getMetaFilters());
    }
}
