/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

class DesiredCapabilitiesMergerTests
{
    @SuppressWarnings({"MultipleStringLiterals", "MultipleStringLiteralsExtended"})
    @Test
    void shouldMergeSourceCapabilitieIntoBaseCapabilities()
    {
        DesiredCapabilities base = new DesiredCapabilities(Map.of(
            "key-1", "value-1",
            "key-2", "value-base",
            "key-3", Map.of(
                     "map-key-1", "map-value-1",
                     "map-key-2", "map-value-base"
                     ),
            "key-5", Map.of()
        ));

        DesiredCapabilities source = new DesiredCapabilities(Map.of(
            "key-4", "value-4",
            "key-2", "value-source",
            "key-3", Map.of(
                     "map-key-3", "map-value-3",
                     "map-key-2", "map-value-source"
                     ),
            "key-5", "value-5"
        ));

        DesiredCapabilities merged = DesiredCapabilitiesMerger.merge(base, source);

        assertEquals(Map.of(
            "key-1", "value-1",
            "key-2", "value-source",
            "key-4", "value-4",
            "key-3", Map.of(
                     "map-key-1", "map-value-1",
                     "map-key-3", "map-value-3",
                     "map-key-2", "map-value-source"
                     ),
            "key-5", "value-5"
        ), merged.asMap());
    }
}
