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

package org.vividus.selenium.mobileapp.screenshot.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoordsUtilTests
{
    @ParameterizedTest
    @CsvSource({"2.49, 5", "2.5, 5", "2.51, 6"})
    void shouldMultiplyAndProvideRoundedIntResult(double dpr, int expectedResult)
    {
        int coordinate = 2;
        int multipliedCoordinate = CoordsUtil.multiply(coordinate, dpr);
        assertEquals(expectedResult, multipliedCoordinate);
    }
}
