/*
 * Copyright 2019-2021 the original author or authors.
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
import java.util.Comparator;
import java.util.function.UnaryOperator;

public interface IPropertyMapper
{
    /**
     * Maps properties with specified prefix to the object created using property names and values
     * @param <T> type of resulting object
     * @param propertyPrefix Prefix of properties
     * @param resultType Resulting object type
     * @return object created using property names and values
     * @throws IOException if any error is occurred during mapping of properties to objects
     */
    <T> T readValue(String propertyPrefix, Class<T> resultType) throws IOException;

    /**
     * Maps properties with specified prefix to the collection with values representing objects created
     * using property names and their values
     * @param <T> type of resulting objects
     * @param propertyPrefix Prefix of properties
     * @param valueType Collection value type
     * @return collection with values representing objects created using property names and their values
     * @throws IOException if any error is occurred during mapping of properties to objects
     */
    <T> PropertyMappedCollection<T> readValues(String propertyPrefix, Class<T> valueType) throws IOException;

    /**
     * Maps properties with specified prefix to the collection with values representing objects created
     * using property names and their values.
     * <br>
     * Steps performed at this function:
     * <ol>
     * <li>find properties by the <b>propertyPrefix</b></li>
     * <li>find properties by the <b>basePropertyPrefix</b></li>
     * <li>each property found by the <b>basePropertyPrefix</b> is added to the properties found by the
     * <b>propertyPrefix</b> if it's missing</li>
     * <li>map resulting properties to the collection of objects</li>
     * </ol>
     * @param <T> type of resulting objects
     * @param propertyPrefix Prefix of properties
     * @param basePropertyPrefix Prefix of base properties
     * @param valueType Collection value type
     * @return collection with values representing objects created using property names and their values
     * @throws IOException if any error is occurred during mapping of properties to objects
     */
    <T> PropertyMappedCollection<T> readValues(String propertyPrefix, String basePropertyPrefix, Class<T> valueType)
            throws IOException;

    /**
     * Maps properties with specified prefix to the collection with values representing objects created
     * using property names and their values
     * @param <T> type of resulting objects
     * @param propertyPrefix Prefix of properties
     * @param keyMapper a mapping operator to change keys
     * @param valueType Collection value type
     * @return collection with values representing objects created using property names and their values
     * @throws IOException if any error is occurred during mapping of properties to objects
     */
    <T> PropertyMappedCollection<T> readValues(String propertyPrefix, UnaryOperator<String> keyMapper,
            Class<T> valueType) throws IOException;

    /**
     * Maps properties with specified prefix to the ordered collection with values representing objects created
     * using property names and their values
     * @param <T> type of resulting objects
     * @param propertyPrefix Prefix of properties
     * @param keyMapper a mapping operator to change keys
     * @param keyComparator a comparator that will be used to order the resulting collection
     * @param valueType Collection value type
     * @return ordered collection with values representing objects created using property names and their values
     * @throws IOException if any error is occurred during mapping of properties to objects
     */
    <T> PropertyMappedCollection<T> readValues(String propertyPrefix, UnaryOperator<String> keyMapper,
            Comparator<String> keyComparator, Class<T> valueType) throws IOException;
}
