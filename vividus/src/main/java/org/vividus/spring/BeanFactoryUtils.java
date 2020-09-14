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

package org.vividus.spring;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.ListableBeanFactory;

public final class BeanFactoryUtils
{
    private BeanFactoryUtils()
    {
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> mergeLists(ListableBeanFactory beanFactory, String listBeanNamePrefix)
    {
        return streamOfBeansByNamePrefix(beanFactory, listBeanNamePrefix, List.class)
                .map(m -> (List<T>) m)
                .flatMap(List::stream)
                .collect(toList());
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> mergeMaps(ListableBeanFactory beanFactory, String mapBeanNamePrefix)
    {
        return streamOfBeansByNamePrefix(beanFactory, mapBeanNamePrefix, Map.class)
                .map(m -> (Map<K, V>) m)
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private static <T> Stream<T> streamOfBeansByNamePrefix(ListableBeanFactory beanFactory, String beanNamePrefix,
            Class<T> requiredType)
    {
        return Stream.of(beanFactory.getBeanNamesForType(requiredType))
                .filter(name -> name.startsWith(beanNamePrefix))
                .map(name -> beanFactory.getBean(name, requiredType));
    }
}
