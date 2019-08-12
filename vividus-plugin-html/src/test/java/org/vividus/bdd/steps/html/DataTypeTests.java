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

package org.vividus.bdd.steps.html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DataTypeTests
{
    private static final String DATA = "data";
    private static final String TEXT = "text";
    private static final Element ELEMENT = mock(Element.class);

    @BeforeAll
    static void beforeAll()
    {
        when(ELEMENT.text()).thenReturn(TEXT);
        when(ELEMENT.data()).thenReturn(DATA);
    }

    private static Stream<Arguments> dataProvider()
    {
        return Stream.of(Arguments.of(DataType.DATA, DATA,
                         Arguments.of(DataType.TEXT, TEXT)));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void shouldExtractAppropriateData(DataType toTest, String expectedValue)
    {
        assertEquals(expectedValue, toTest.get(ELEMENT));
    }
}
