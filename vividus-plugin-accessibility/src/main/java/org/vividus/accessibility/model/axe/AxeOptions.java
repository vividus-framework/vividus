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

import java.util.List;

public final class AxeOptions
{
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
        return forTags(standard.toUpperCase(), List.of(standard.toLowerCase()));
    }

    public static AxeOptions forTags(String key, List<String> tags)
    {
        return new AxeOptions("tag", key, tags);
    }

    public static AxeOptions forRules(List<String> rules)
    {
        return new AxeOptions("rule", null, rules);
    }

    @Override
    public String toString()
    {
        if (key != null)
        {
            return key;
        }

        String postfix = values.size() > 1 ? " rules" : " rule";
        return String.join(", ", values) + postfix;
    }
}
