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

package org.vividus.selenium.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CapabilitiesValueTypeAdjusterTests
{
    @ParameterizedTest
    @CsvSource({
        "true,true",
        "true,TRUE",
        "false,false",
        "false,FALSE"
    })
    void testAdjustTypeAsBoolean(boolean expected, String value)
    {
        assertEquals(expected, CapabilitiesValueTypeAdjuster.adjustType(value));
    }

    @Test
    void testAdjustTypeAsString()
    {
        String value = "value";
        assertEquals(value, CapabilitiesValueTypeAdjuster.adjustType(value));
    }

    @Test
    void testAdjustTypeAsInt()
    {
        String value = "20";
        assertEquals(20, CapabilitiesValueTypeAdjuster.adjustType(value));
    }
}
