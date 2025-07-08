/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.action.search;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public interface ElementVisibility
{
    static <T extends Enum<T> & ElementVisibility> T getElementType(String input, Class<T> enumClass)
    {
        Validate.isTrue(StringUtils.isNotBlank(input), "Visibility type can not be empty. %s",
                getExpectedVisibilityMessage(enumClass));

        String inputInUpperCase = input.toUpperCase().trim();
        return Stream.of(enumClass.getEnumConstants())
                .filter(v -> v.name().startsWith(inputInUpperCase))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Illegal visibility type '%s'. %s", input, getExpectedVisibilityMessage(enumClass))));
    }

    private static <T extends Enum<T> & ElementVisibility> String getExpectedVisibilityMessage(Class<T> enumClass)
    {
        return Stream.of(enumClass.getEnumConstants()).map(Enum::name)
                .map(String::toLowerCase)
                .map(type -> StringUtils.wrap(type, '\''))
                .collect(Collectors.joining(", ", "Expected one of ", ""));
    }
}
