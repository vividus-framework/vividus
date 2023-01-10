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

package org.vividus.mobileapp.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ZoomConfigurationTests
{
    @ParameterizedTest
    @CsvSource({
            "-1 ,0  ,0  ,0  ,-1 ,top             ",
            "0  ,-1 ,0  ,0  ,-1 ,bottom          ",
            "0  ,0  ,-1 ,0  ,-1 ,right           ",
            "0  ,0  ,0  ,-1 ,-1 ,left            ",
            "100,0  ,0  ,0  ,100,top             ",
            "0  ,100,0  ,0  ,100,bottom          ",
            "0  ,0  ,100,0  ,100,right           ",
            "0  ,0  ,0  ,100,100,left            ",
            "30 ,70 ,0  ,0  ,100,total vertical  ",
            "0  ,0  ,40 ,60 ,100,total horizontal",
            "80 ,35 ,0  ,0  ,115,total vertical  ",
            "0  ,0  ,52 ,60 ,112,total horizontal"
    })
    void shouldFailIfIndentPercentageIsNotValid(int topIndent, int bottomIndent, int leftIndent, int rightIndent,
            int invalidPercentage, String invalidIndentName)
    {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> new ZoomConfiguration(topIndent, bottomIndent, leftIndent, rightIndent));
        assertEquals(String.format("The %s indent percentage value must be between 0 and 99, but got: %s",
                invalidIndentName, invalidPercentage), exception.getMessage());
    }
}
