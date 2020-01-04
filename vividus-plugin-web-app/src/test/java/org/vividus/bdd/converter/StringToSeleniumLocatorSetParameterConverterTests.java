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

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ByXPath;

class StringToSeleniumLocatorSetParameterConverterTests
{
    private static final Type SET_BY = new TypeToken<Set<By>>() { }.getType();

    private final StringToSeleniumLocatorSetParameterConverter converter =
            new StringToSeleniumLocatorSetParameterConverter();

    static Stream<Arguments> typeParameters()
    {
        return Stream.of(
        Arguments.of(SET_BY, true),
        Arguments.of(new TypeToken<HashSet<By>>() { } .getType(), false),
        Arguments.of(new TypeToken<TreeSet<By>>() { } .getType(), false),
        Arguments.of(new TypeToken<Set<ByXPath>>() { } .getType(), false),
        Arguments.of(new TypeToken<Set<String>>() { } .getType(), false),
        Arguments.of(new TypeToken<By>() { } .getType(), false),
        Arguments.of(new TypeToken<List<By>>() { } .getType(), false)
        );
    }

    @ParameterizedTest
    @MethodSource("typeParameters")
    void acceptTest(Type type, boolean result)
    {
        assertEquals(converter.accept(type), result);
    }

    @Test
    void convertTest()
    {
        Set<By> expected = new HashSet<>();
        expected.add(By.xpath(".//a"));
        assertEquals(expected, converter.convertValue("By.xpath(.//a)", SET_BY));
    }
}
