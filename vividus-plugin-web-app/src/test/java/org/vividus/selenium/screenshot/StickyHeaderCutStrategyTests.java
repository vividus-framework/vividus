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

import org.junit.jupiter.api.Test;

class StickyHeaderCutStrategyTests
{
    private static final StickyHeaderCutStrategy STRATEGY = new StickyHeaderCutStrategy(1, 2);

    @Test
    void shouldReturnZeroForTheFirstCallAndReturnValueForOthers()
    {
        assertEquals(0, getHeaderHeight());
        assertEquals(1, getHeaderHeight());
        assertEquals(1, getHeaderHeight());
    }

    @Test
    void shouldAlwaysReturnValueOfFooter()
    {
        assertEquals(2, getFooterHeight());
        assertEquals(2, getFooterHeight());
    }

    private int getHeaderHeight()
    {
        return STRATEGY.getHeaderHeight(null);
    }

    private int getFooterHeight()
    {
        return STRATEGY.getFooterHeight(null);
    }
}
