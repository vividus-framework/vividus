/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.steps.ui.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DimensionTests
{
    private static final int DIMENSION_VALUE = 100;

    @Mock
    private org.openqa.selenium.Dimension mockedSeleniumDimension;

    @Test
    void testGetDimensionWidthElement()
    {
        when(mockedSeleniumDimension.getWidth()).thenReturn(DIMENSION_VALUE);
        int result = Dimension.WIDTH.getDimension(mockedSeleniumDimension);
        assertEquals(DIMENSION_VALUE, result);
    }

    @Test
    void testGetDimensionHeightElement()
    {
        when(mockedSeleniumDimension.getHeight()).thenReturn(DIMENSION_VALUE);
        int result = Dimension.HEIGHT.getDimension(mockedSeleniumDimension);
        assertEquals(DIMENSION_VALUE, result);
    }
}
