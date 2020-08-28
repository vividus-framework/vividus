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

package org.vividus.visual.eyes.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.visual.screenshot.IgnoreStrategy;

class ApplitoolsVisualCheckTests
{
    private final ApplitoolsVisualCheck visualCheck = new ApplitoolsVisualCheck();

    @Test
    void shouldUseEmptySetsAsDefaultIgnores()
    {
        visualCheck.buildIgnores();
        assertEquals(Map.of(IgnoreStrategy.AREA, Set.of(), IgnoreStrategy.ELEMENT, Set.of()),
                visualCheck.getElementsToIgnore());
    }

    @Test
    void shouldFillElementsToIgnoreWithValues()
    {
        Set<SearchAttributes> element = Set.of(new SearchAttributes(ActionAttributeType.ID, "element"));
        Set<SearchAttributes> area = Set.of(new SearchAttributes(ActionAttributeType.ID, "area"));
        visualCheck.setElementsToIgnore(element);
        visualCheck.setAreasToIgnore(area);
        visualCheck.buildIgnores();
        assertEquals(Map.of(IgnoreStrategy.AREA, area, IgnoreStrategy.ELEMENT, element),
                visualCheck.getElementsToIgnore());
    }
}
