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

package org.vividus.visual.eyes.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.web.action.search.WebLocatorType;

class ApplitoolsVisualCheckTests
{
    private final ApplitoolsVisualCheck visualCheck = new ApplitoolsVisualCheck();

    @Test
    void shouldUseEmptySetsAsDefaultIgnores()
    {
        assertEquals(Set.of(), visualCheck.getElementsToIgnore());
        assertEquals(Set.of(), visualCheck.getAreasToIgnore());
    }

    @Test
    void shouldFillElementsToIgnoreWithValues()
    {
        Set<Locator> elements = Set.of(new Locator(WebLocatorType.ID, "element"));
        Set<Locator> areas = Set.of(new Locator(WebLocatorType.ID, "area"));
        visualCheck.setElementsToIgnore(elements);
        visualCheck.setAreasToIgnore(areas);
        assertEquals(elements, visualCheck.getElementsToIgnore());
        assertEquals(areas, visualCheck.getAreasToIgnore());
    }
}
