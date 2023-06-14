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

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

public final class AxeOptions
{
    private static final String TT_V_5_STANDARD = "TTv5";

    private static final Map<Pattern, Function<Matcher, String>> CASE_SENSITIVE_TAGS = Map.of(
        compile("ACT", CASE_INSENSITIVE), m -> m.group().toUpperCase(),
        compile(TT_V_5_STANDARD, CASE_INSENSITIVE), m -> TT_V_5_STANDARD,
        compile("TT(\\d+)\\.([a-z]+)", CASE_INSENSITIVE), m -> "TT" + m.group(1) + "." + m.group(2).toLowerCase()
    );

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
        return new AxeOptions("tag", standard, createTags(standard));
    }

    private static List<String> createTags(String standard)
    {
        if (DEFAULT_STANDARDS.containsKey(standard))
        {
            return DEFAULT_STANDARDS.get(standard);
        }

        for (Map.Entry<Pattern, Function<Matcher, String>> tagEntry : CASE_SENSITIVE_TAGS.entrySet())
        {
            Matcher tagMatcher = tagEntry.getKey().matcher(standard);
            if (tagMatcher.matches())
            {
                return List.of(tagEntry.getValue().apply(tagMatcher));
            }
        }

        return List.of(standard.toLowerCase());
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
