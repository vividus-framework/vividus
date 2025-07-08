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

package org.vividus.ui.locator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LocatorParserTests
{
    private static final Locator FIRST_LOCATOR = new Locator("xpath", ".//");
    private static final Locator SECOND_LOCATOR = new Locator("cssSelector", "div");

    @ParameterizedTest
    @ValueSource(strings = {
            "By.xpath(.//), By.cssSelector(div)",
            " By.xpath(.//),  By.cssSelector(div) ",
            "By.xpath(.//),By.cssSelector(div)"
    })
    void convertStringToLocatorsTest(String input)
    {
        Set<Locator> expected = new HashSet<>();
        expected.add(FIRST_LOCATOR);
        expected.add(SECOND_LOCATOR);
        assertEquals(expected, LocatorParser.parseLocatorsCollection(input, Locator::new));
    }

    @Test
    void convertStringToLocatorsSourceIsBlankTest()
    {
        assertEquals(Set.of(), LocatorParser.parseLocatorsCollection("", Locator::new));
    }

    private static final class Locator
    {
        private final String type;
        private final String value;

        Locator(String type, String value)
        {
            this.type = type;
            this.value = value;
        }

        public String getType()
        {
            return type;
        }

        public String getValue()
        {
            return value;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            Locator that = (Locator) o;
            return Objects.equals(type, that.type) && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(type, value);
        }
    }
}
