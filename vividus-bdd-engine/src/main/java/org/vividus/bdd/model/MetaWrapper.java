/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.Meta;

public class MetaWrapper
{
    private final Meta meta;

    public MetaWrapper(Meta meta)
    {
        this.meta = meta;
    }

    public boolean hasProperty(String name)
    {
        return meta.hasProperty(name);
    }

    public List<String> toTags()
    {
        List<String> tags = new ArrayList<>();
        for (String propertyName : meta.getPropertyNames())
        {
            String propertyValue = meta.getProperty(propertyName);
            tags.add(StringUtils.isNoneBlank(propertyValue) ? propertyName + "=" + propertyValue : propertyName);
        }
        return tags;
    }

    public Optional<String> getOptionalPropertyValue(String propertyName)
    {
        String value = meta.getProperty(propertyName);
        return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }
}
