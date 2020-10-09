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

package org.vividus.bdd.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Properties;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExcelTableTransformerTests
{
    private static final String LINE_BREAK_REPLACEMENT = "lineBreakReplacement";
    private static final String DATA = "data";
    private static final String COLUMN = "column";
    private static final String ADDRESSES = "addresses";
    private static final String INCREMENT = "increment";
    private static final String RANGE = "range";
    private static final String RANGE_VALUE = "B4:B6";
    private static final String EXTEND_RANGE_VALUE = "A1:B3";
    private static final String JOIN_VALUES = "joinValues";
    private static final String TRUE = "true";

    private final ExcelTableTransformer transformer = new ExcelTableTransformer();

    private final TableProperties properties = new TableProperties(new Properties());

    @BeforeEach
    void beforeEach()
    {
        properties.getProperties().setProperty("path", "/TestTemplate.xlsx");
        properties.getProperties().setProperty("sheet", "RepeatingData");
    }

    @Test
    void testCheckConcurrentConditionsWithTwoPropertiesThrowException()
    {
        properties.getProperties().setProperty(COLUMN, DATA);
        properties.getProperties().setProperty(RANGE, RANGE_VALUE);
        properties.getProperties().setProperty(ADDRESSES, "1,3,5");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform("", null, properties));
        assertEquals("Only one ExamplesTable property must be set, but found both 'range' and 'addresses'",
                exception.getMessage());
    }

    @Test
    void testCheckConcurrentConditionsWithoutPropertiesThrowException()
    {
        properties.getProperties().setProperty(COLUMN, DATA);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform("", null, properties));
        assertEquals("One of ExamplesTable properties must be set: either 'range' or 'addresses'",
                exception.getMessage());
    }

    @Test
    void testTransformWithUsingRangeWithIncrementJoining()
    {
        properties.getProperties().setProperty(COLUMN, DATA);
        properties.getProperties().setProperty(RANGE, RANGE_VALUE);
        properties.getProperties().setProperty(INCREMENT, "2");
        properties.getProperties().setProperty(JOIN_VALUES, TRUE);
        String actualResult = transformer.transform("", null, properties);
        assertEquals("|data|\n|OPEN CLOSED|", actualResult);
    }

    @Test
    void testTransformWithUsingRangeWithoutIncrement()
    {
        properties.getProperties().setProperty(COLUMN, DATA);
        properties.getProperties().setProperty(RANGE, RANGE_VALUE);
        String actualResult = transformer.transform("", null, properties);
        assertEquals("|data|\n|OPEN|\n|PENDING|\n|CLOSED|", actualResult);
    }

    @Test
    void testTransformWithUsingAddresses()
    {
        properties.getProperties().setProperty(COLUMN, DATA);
        properties.getProperties().setProperty(ADDRESSES, "B4;B6");
        String actualResult = transformer.transform("", null, properties);
        assertEquals("|data|\n|OPEN|\n|CLOSED|", actualResult);
    }

    @Test
    void testTransformWithCellContainsNewLine()
    {
        properties.getProperties().setProperty(COLUMN, DATA);
        properties.getProperties().setProperty(ADDRESSES, "B7;B8");
        properties.getProperties().setProperty(JOIN_VALUES, TRUE);
        String actualResult = transformer.transform("", null, properties);
        assertEquals("|data|\n|CLOSED CLOSED |", actualResult);
    }

    @Test
    void testTransformWithCellContainsNewLineWithReplaceParameter()
    {
        properties.getProperties().setProperty(COLUMN, DATA);
        properties.getProperties().setProperty(ADDRESSES, "B6;B8");
        properties.getProperties().setProperty(JOIN_VALUES, TRUE);
        properties.getProperties().setProperty(LINE_BREAK_REPLACEMENT, " ");
        String actualResult = transformer.transform("", null, properties);
        assertEquals("|data|\n|CLOSED CLOSED  |", actualResult);
    }

    @Test
    void testTrasformWithoutColumnProperty()
    {
        properties.getProperties().setProperty(RANGE, EXTEND_RANGE_VALUE);
        String actualResult = transformer.transform("", null, properties);
        assertEquals("|name|status|\n|First|OPEN|\n|Second|OPEN|", actualResult);
    }

    @Test
    void testTrasformWithoutColumnWithReplaceParameter()
    {
        properties.getProperties().setProperty(RANGE, "A7:B8");
        properties.getProperties().setProperty(LINE_BREAK_REPLACEMENT, "!");
        String actualResult = transformer.transform("", null, properties);
        assertEquals("|Sixth|CLOSED|\n|Seventh|CLOSED !|", actualResult);
    }
}
