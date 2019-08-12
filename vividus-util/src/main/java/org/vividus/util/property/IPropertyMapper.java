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

public interface IPropertyMapper
{
    /**
     * Maps properties with specified prefix to the map with values representing objects created using property names
     * and values
     * @param <T> type of resulting objects
     * @param propertyPrefix Prefix of properties
     * @param valueType Map value type
     * @return map with values representing objects created using property names and values
     * @throws IOException if any error is occurred during mapping of properties to objects
     */
    <T> Map<String, T> readValues(String propertyPrefix, Class<T> valueType) throws IOException;
}
