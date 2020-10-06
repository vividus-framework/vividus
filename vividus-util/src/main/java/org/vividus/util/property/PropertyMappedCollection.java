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

package org.vividus.util.property;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

public class PropertyMappedCollection<T>
{
    private final Map<String, T> data;

    public PropertyMappedCollection(Map<String, T> data)
    {
        this.data = data;
    }

    /**
     * Provides data mapped from properties by key or empty {@code Optional} if no data is mapped
     * @param key Identifier
     * @return Value for key
     */
    public Optional<T> getNullable(String key)
    {
        return Optional.ofNullable(data.get(key));
    }

    /**
     * Provides data mapped from properties by key
     * @param key Identifier
     * @param message the {@link String#format(String, Object...)} exception message if no data by key is found
     * @param messageParams the optional values for the formatted exception message
     * @throws IllegalArgumentException if no data is found by key
     * @return Value for key
     */
    public T get(String key, String message, Object... messageParams)
    {
        T value = data.get(key);
        Validate.isTrue(value != null, message, messageParams);
        return value;
    }

    public Map<String, T> getData()
    {
        return data;
    }
}
