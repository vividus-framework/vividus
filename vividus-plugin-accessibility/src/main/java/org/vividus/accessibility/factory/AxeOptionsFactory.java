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

package org.vividus.accessibility.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.vividus.accessibility.model.axe.AxeOptions;
import org.vividus.util.property.IPropertyParser;

public class AxeOptionsFactory
{
    private static final String PREFIX = "accessibility.axe-core.tag.";

    private final Map<String, List<String>> defaultTags;
    private final Map<String, List<String>> userDefinedTags = new HashMap<>();

    public AxeOptionsFactory(IPropertyParser propertyParser)
    {
        List<String> wcag2xa = List.of("wcag2a", "wcag21a");

        List<String> wcag2xaa = new ArrayList<>(wcag2xa);
        wcag2xaa.add("wcag2aa");
        wcag2xaa.add("wcag21aa");
        wcag2xaa.add("wcag22aa");

        List<String> wcag2xaaa = new ArrayList<>(wcag2xaa);
        wcag2xaaa.add("wcag2aaa");

        defaultTags = Map.of(
            "WCAG2xA", wcag2xa,
            "WCAG2xAA", wcag2xaa,
            "WCAG2xAAA", wcag2xaaa
        );

        propertyParser.getPropertiesByPrefix(PREFIX).forEach((key, tags) -> userDefinedTags
                .put(
                    StringUtils.substringAfter(key, PREFIX),
                    Stream.of(tags.split(",")).map(String::strip).collect(Collectors.toList())
                ));
    }

    public AxeOptions createOptions(Pair<String, List<String>> standardOrRules)
    {
        String standard = standardOrRules.getLeft();
        if (standard != null)
        {
            if (defaultTags.containsKey(standard))
            {
                return AxeOptions.forTags(standard, defaultTags.get(standard));
            }

            if (userDefinedTags.containsKey(standard))
            {
                return AxeOptions.forTags(standard, userDefinedTags.get(standard));
            }

            return AxeOptions.forStandard(standard);
        }

        return AxeOptions.forRules(standardOrRules.getRight());
    }
}
