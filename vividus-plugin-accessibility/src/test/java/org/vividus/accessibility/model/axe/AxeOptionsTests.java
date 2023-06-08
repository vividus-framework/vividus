/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.accessibility.model.axe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class AxeOptionsTests
{
    private static final String STANDARD = "WCAG2A";

    @Test
    void shouldReturnRulesAsString()
    {
        assertEquals("button-name, duplicate-id-active rules",
                AxeOptions.forRules(List.of("button-name", "duplicate-id-active")).toString());
    }

    @Test
    void shouldReturnRuleAsString()
    {
        assertEquals("html-has-lang rule", AxeOptions.forRules(List.of("html-has-lang")).toString());
    }

    @Test
    void shouldReturnStandardAsString()
    {
        assertEquals(STANDARD, AxeOptions.forStandard("wcag2a").toString());
    }

    @Test
    void shouldReturnKeyAsString()
    {
        assertEquals(STANDARD, AxeOptions.forTags(STANDARD, List.of("tag1", "tag2", "tag3")).toString());
    }
}
