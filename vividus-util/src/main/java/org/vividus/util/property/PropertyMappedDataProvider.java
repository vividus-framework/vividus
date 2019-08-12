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

package org.vividus.util.property;

import java.io.IOException;
import java.util.Map;

public class PropertyMappedDataProvider<T>
{
    private final Map<String, T> data;

    public PropertyMappedDataProvider(IPropertyMapper propertyMapper, String propertyPrefix, Class<T> valueType)
            throws IOException
    {
        data = propertyMapper.readValues(propertyPrefix, valueType);
    }

    /**
     * Provides data mapped from properties by key
     * @param key Identifier
     * @return Value for key
     */
    public T get(String key)
    {
        T value = data.get(key);
        if (value == null)
        {
            throw new IllegalArgumentException("No entry is found for key: " + key);
        }
        return value;
    }

    public Map<String, T> getData()
    {
        return data;
    }
}
