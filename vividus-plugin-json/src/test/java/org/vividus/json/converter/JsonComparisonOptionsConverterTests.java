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

package org.vividus.json.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.vividus.converter.FluentTrimmedEnumConverter;

import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Options;

class JsonComparisonOptionsConverterTests
{
    private final JsonComparisonOptionsConverter converter = new JsonComparisonOptionsConverter(
            new FluentEnumListConverter(new FluentTrimmedEnumConverter()));

    @ValueSource(strings = {
            "ignoring extra fields, ignoring_extra_array_items",
            "IGNORING_EXTRA_FIELDS, IGNORING_EXTRA_ARRAY_ITEMS"
    })
    @ParameterizedTest
    void shouldConvertValuesToOptions(String valueToConvert)
    {
        var options = converter.convertValue(valueToConvert, Options.class);
        assertTrue(options.contains(Option.IGNORING_EXTRA_FIELDS));
        assertTrue(options.contains(Option.IGNORING_EXTRA_ARRAY_ITEMS));
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", ""})
    void shouldConvertEmptyStringToEmptyOptions(String valueToConvert)
    {
        var options = converter.convertValue(valueToConvert, Options.class);
        assertEquals(Options.empty(), options);
    }
}
