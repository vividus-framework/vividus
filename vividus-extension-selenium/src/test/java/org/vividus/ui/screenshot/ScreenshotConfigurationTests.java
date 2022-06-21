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

package org.vividus.ui.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.action.search.Locator;

@ExtendWith(MockitoExtension.class)
class ScreenshotConfigurationTests
{
    @Mock private Locator locator;

    @Test
    void shouldSetAndGetElementToIgnore()
    {
        ScreenshotConfiguration config = new ScreenshotConfiguration();
        assertEquals(Set.of(), config.getElementsToIgnore());
        config.setElementsToIgnore(Set.of(locator));
        assertEquals(Set.of(locator), config.getElementsToIgnore());
    }

    @Test
    void shouldSetAndGetAreaToIgnore()
    {
        ScreenshotConfiguration config = new ScreenshotConfiguration();
        assertEquals(Set.of(), config.getAreasToIgnore());
        config.setAreasToIgnore(Set.of(locator));
        assertEquals(Set.of(locator), config.getAreasToIgnore());
    }
}
