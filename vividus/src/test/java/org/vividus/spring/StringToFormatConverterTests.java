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

package org.vividus.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jbehave.core.reporters.Format;
import org.junit.jupiter.api.Test;

class StringToFormatConverterTests
{
    private final StringToFormatConverter stringToFormatConverter = new StringToFormatConverter();

    @Test
    void testConvert()
    {
        assertEquals(Format.XML, stringToFormatConverter.convert("XML"));
    }

    @Test
    void testConvertIllegalFormat()
    {
        String unsupportedFormat = "JPG";
        var exception = assertThrows(IllegalArgumentException.class,
                () -> stringToFormatConverter.convert(unsupportedFormat));
        assertEquals("Unsupported format: " + unsupportedFormat + ". List of supported formats: [CONSOLE,"
                + " ANSI_CONSOLE, IDE_CONSOLE, TEAMCITY_CONSOLE, TXT, HTML, XML, JSON, HTML_TEMPLATE, XML_TEMPLATE,"
                + " JSON_TEMPLATE, STATS]", exception.getMessage());
    }
}
