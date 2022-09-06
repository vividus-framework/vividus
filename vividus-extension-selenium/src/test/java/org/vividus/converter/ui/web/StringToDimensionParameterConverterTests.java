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

package org.vividus.converter.ui.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.Dimension;

class StringToDimensionParameterConverterTests
{
    private final StringToDimensionParameterConverter dimensionConverter = new StringToDimensionParameterConverter();

    @Test
    void shouldConvertStringToDimensionParameter()
    {
        var sizeAsString = "2048x1080";
        var actual = dimensionConverter.convertValue(sizeAsString, null);
        assertEquals(new Dimension(2048, 1080), actual);
    }

    @Test
    void testConversionError()
    {
        var exception = assertThrows(IllegalArgumentException.class, () -> dimensionConverter.convert("800x"));
        assertEquals("Provided size = 800x has wrong format. Example of correct format: 800x600",
                exception.getMessage());
    }

    @Test
    void testSuccessfulConversion()
    {
        var actual = dimensionConverter.convert("800x600");
        assertEquals(new Dimension(800, 600), actual);
    }
}
