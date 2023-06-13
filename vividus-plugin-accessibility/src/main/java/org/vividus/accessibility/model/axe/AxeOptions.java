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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

public final class AxeOptions
{
    private static final Map<String, List<String>> DEFAULT_STANDARDS;

    static
    {
        List<String> wcag2xa = List.of("wcag2a", "wcag21a");

        List<String> wcag2xaa = new ArrayList<>(wcag2xa);
        wcag2xaa.add("wcag2aa");
        wcag2xaa.add("wcag21aa");
        wcag2xaa.add("wcag22aa");

        List<String> wcag2xaaa = new ArrayList<>(wcag2xaa);
        wcag2xaaa.add("wcag2aaa");

        DEFAULT_STANDARDS = Map.of(
            "WCAG2xA", wcag2xa,
            "WCAG2xAA", wcag2xaa,
            "WCAG2xAAA", wcag2xaaa
        );
    }

    private final String type;
    private final String key;
    private final List<String> values;

    private AxeOptions(String type, String key, List<String> values)
    {
        this.type = type;
        this.key = key;
        this.values = values;
    }

    public String getType()
    {
        return type;
    }

    public List<String> getValues()
    {
        return values;
    }

    public static AxeOptions forStandard(String standard)
    {
        List<String> tags = Optional.ofNullable(DEFAULT_STANDARDS.get(standard))
                .orElseGet(() -> List.of(standard.toLowerCase()));
        return new AxeOptions("tag", standard, tags);
    }

    public static AxeOptions forRules(List<String> rules)
    {
        return new AxeOptions("rule", null, rules);
    }

    @JsonIgnore
    public String getStandardOrRulesAsString()
    {
        if (key != null)
        {
            return key + " standard";
        }

        String postfix = values.size() > 1 ? " rules" : " rule";
        return String.join(", ", values) + postfix;
    }
}
