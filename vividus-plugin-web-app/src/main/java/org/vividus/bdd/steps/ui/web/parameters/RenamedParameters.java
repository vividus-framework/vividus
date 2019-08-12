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

package org.vividus.bdd.steps.ui.web.parameters;

import java.lang.reflect.Type;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import org.jbehave.core.steps.Parameters;

public class RenamedParameters implements IRenamedParameters
{
    private final Parameters parameters;
    private final BiMap<String, String> nameMapping = HashBiMap.create();

    public RenamedParameters(Parameters parameters)
    {
        this.parameters = parameters;
    }

    @Override
    public Map<String, String> values()
    {
        return parameters.values().entrySet().stream().map(e ->
        {
            String newName = nameMapping.inverse().get(e.getKey());
            return newName == null ? e : new SimpleEntry<>(newName, e.getValue());
        }).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    @Override
    public <T> T valueAs(String name, Type type)
    {
        String oldName = getParameterOldName(name);
        return parameters.valueAs(oldName, type);
    }

    @Override
    public <T> T valueAs(String name, Type type, T defaultValue)
    {
        String oldName = getParameterOldName(name);
        return parameters.valueAs(oldName, type, defaultValue);
    }

    @Override
    public Parameters updateParameterName(String oldName, String newName)
    {
        nameMapping.put(newName, oldName);
        return this;
    }

    @Override
    public Parameters updateParameterNames(Map<String, String> replacementMap)
    {
        nameMapping.putAll(HashBiMap.create(replacementMap).inverse());
        return this;
    }

    private String getParameterOldName(String name)
    {
        String oldName = nameMapping.get(name);
        return oldName != null ? oldName : name;
    }
}
