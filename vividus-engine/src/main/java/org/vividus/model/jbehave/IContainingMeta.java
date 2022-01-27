/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.model.jbehave;

import static org.vividus.model.MetaWrapper.META_VALUES_SEPARATOR;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public interface IContainingMeta
{
    List<Meta> getMeta();

    /**
     * Get unique <b>meta</b> value
     *
     * <p>If the <b>meta</b> does not exist or has no value, an empty {@link Optional} will be returned
     *
     * <p><i>Notes</i>
     * <ul>
     * <li><b>meta</b> value is trimmed upon returning</li>
     * <li><i>;</i> char is used as a separator for <b>meta</b> with multiple values</li>
     * </ul>
     *
     * @param metaName the meta name
     * @return the meta value
     * @throws NotUniqueMetaValueException if the <b>meta</b> has more than one value
     */
    default Optional<String> getUniqueMetaValue(String metaName) throws NotUniqueMetaValueException
    {
        Set<String> values = getMetaValues(metaName);
        if (values.size() > 1)
        {
            throw new NotUniqueMetaValueException(metaName, values);
        }
        return values.isEmpty() ? Optional.empty() : Optional.of(values.iterator().next());
    }

    /**
     * Get all <b>meta</b> values
     *
     * <p><i>Notes</i>
     * <ul>
     * <li><b>meta</b>s without value are ignored</li>
     * <li><b>meta</b> values are trimmed upon returning</li>
     * <li><i>;</i> char is used as a separator for <b>meta</b> with multiple values</li>
     * </ul>
     *
     * @param metaName the meta name
     * @return  the meta values
     */
    default Set<String> getMetaValues(String metaName)
    {
        return getMetaStream().filter(m -> metaName.equals(m.getName()))
                .map(Meta::getValue)
                .filter(StringUtils::isNotEmpty)
                .map(String::trim)
                .map(value -> StringUtils.splitPreserveAllTokens(value, META_VALUES_SEPARATOR))
                .flatMap(Stream::of)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Determine if scenario has <b>meta</b> with the name
     *
     * @param metaName the meta name
     * @return {@code true} if scenario has meta with the name
     */
    default boolean hasMetaWithName(String metaName)
    {
        return getMetaStream().anyMatch(m -> metaName.equals(m.getName()));
    }

    private Stream<Meta> getMetaStream()
    {
        return Optional.ofNullable(getMeta()).stream().flatMap(Collection::stream);
    }
}
