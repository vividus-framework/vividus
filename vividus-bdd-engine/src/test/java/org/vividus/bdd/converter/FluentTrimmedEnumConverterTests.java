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

package org.vividus.bdd.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.vividus.bdd.steps.ComparisonRule;

class FluentTrimmedEnumConverterTests
{
    private static final String VALUE = "value";
    private final FluentTrimmedEnumConverter fluentTrimmedEnumConverter = new FluentTrimmedEnumConverter();

    @ValueSource(strings = {"equal to ", " equal to "})
    @ParameterizedTest
    void testConversion(String valueToConvert)
    {
        assertEquals(ComparisonRule.EQUAL_TO,
                fluentTrimmedEnumConverter.convertValue(valueToConvert, ComparisonRule.class));
    }

    @Test
    void shouldUseFromStringMethod()
    {
        assertEquals(Convertable.VALUE_FROMSTRING, fluentTrimmedEnumConverter.convertValue(VALUE, Convertable.class));
    }

    @Test
    void shouldFallBackToDefaultConverterWhenNoMethodPresent()
    {
        assertEquals(NoMethod.VALUE, fluentTrimmedEnumConverter.convertValue(VALUE, NoMethod.class));
    }

    @Test
    void shouldFallBackToDefaultConverterIfTheMethodNotPublic()
    {
        assertEquals(UnAccessibleMethod.VALUE, fluentTrimmedEnumConverter.convertValue(VALUE,
                UnAccessibleMethod.class));
    }

    @Test
    void shouldFallBackToDefaultConverterIfTheMethodThrowsAnException()
    {
        assertEquals(Throwing.VALUE, fluentTrimmedEnumConverter.convertValue(VALUE,
                Throwing.class));
    }

    public enum Convertable
    {
        VALUE,
        VALUE_FROMSTRING;

        public static Convertable fromString(String toConvert)
        {
            return VALUE_FROMSTRING;
        }
    }

    public enum NoMethod
    {
        VALUE,
        VALUE_FROMSTRING;

        public static NoMethod parse(String toConvert)
        {
            return VALUE_FROMSTRING;
        }
    }

    public enum UnAccessibleMethod
    {
        VALUE,
        VALUE_FROMSTRING;

        protected static UnAccessibleMethod fromString(String toConvert)
        {
            return VALUE_FROMSTRING;
        }
    }

    public enum Throwing
    {
        VALUE,
        VALUE_FROMSTRING;

        public static Throwing fromString(String toConvert)
        {
            throw new IllegalArgumentException("unable to convert");
        }
    }
}
