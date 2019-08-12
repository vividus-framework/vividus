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

package org.vividus.selenium;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.Dimension;

class BrowserWindowSizeTests
{
    private static final String SIZE_800_X_600 = "800x600";
    private static final String WRONG_WIDTH_WAS_RETURNED = "Wrong width was returned";
    private static final String WRONG_HEIGHT_WAS_RETURNED = "Wrong height was returned";

    private BrowserWindowSize browserWindowSize;

    @Test
    void testGetWidthHeightWrong()
    {
        assertThrows(IllegalStateException.class, () -> new BrowserWindowSize("800"));
    }

    @Test
    void testGetWidthHeight()
    {
        browserWindowSize = new BrowserWindowSize(SIZE_800_X_600);
        assertEquals(800, browserWindowSize.getWidth(), WRONG_WIDTH_WAS_RETURNED);
        assertEquals(600, browserWindowSize.getHeight(), WRONG_HEIGHT_WAS_RETURNED);
    }

    @Test
    void testToDimension()
    {
        browserWindowSize = new BrowserWindowSize(SIZE_800_X_600);
        Dimension dimension = browserWindowSize.toDimension();
        assertThat("Dimension was not returned", dimension, instanceOf(Dimension.class));
        assertEquals(800, dimension.getWidth(), WRONG_WIDTH_WAS_RETURNED);
        assertEquals(600, dimension.getHeight(), WRONG_HEIGHT_WAS_RETURNED);
    }
}
