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

package org.vividus.selenium.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;

import ru.yandex.qatools.ashot.shooting.cutter.CutStrategy;

@ExtendWith(MockitoExtension.class)
class AdjustingCutStrategyTests
{
    private static final int HEADER_ADJUSTMENT = 1;
    private static final int HEADER_HEIGHT = 200;

    @Mock
    private CutStrategy cutStrategy;

    private AdjustingCutStrategy adjustingCutStrategy;

    @Mock
    private WebDriver webDriver;

    @BeforeEach
    void beforeEach()
    {
        adjustingCutStrategy = new AdjustingCutStrategy(cutStrategy, HEADER_ADJUSTMENT);
    }

    @Test
    void testNonAdjustedHeaderHeightIsReturnedBeforeScrolling()
    {
        when(cutStrategy.getHeaderHeight(webDriver)).thenReturn(HEADER_HEIGHT);
        assertEquals(HEADER_HEIGHT, adjustingCutStrategy.getHeaderHeight(webDriver));
    }

    @Test
    void testAdjustedHeaderHeightIsReturnedAfterScrolling()
    {
        when(cutStrategy.getHeaderHeight(webDriver)).thenReturn(HEADER_HEIGHT);
        adjustingCutStrategy.getHeaderHeight(webDriver);
        assertEquals(HEADER_HEIGHT + HEADER_ADJUSTMENT, adjustingCutStrategy.getHeaderHeight(webDriver));
    }

    @Test
    void testNonAdjustedFooterHeightIsReturned()
    {
        int footerHeight = 100;
        when(cutStrategy.getFooterHeight(webDriver)).thenReturn(footerHeight);
        assertEquals(footerHeight, adjustingCutStrategy.getFooterHeight(webDriver));
    }
}
