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

package org.vividus.ui.web.action.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

class ActionAttributeTypeTests
{
    @Test
    void getSearchTypes()
    {
        Set<ActionAttributeType> searchTypes = Set.of(
                ActionAttributeType.LINK_TEXT,
                ActionAttributeType.LINK_URL,
                ActionAttributeType.LINK_URL_PART,
                ActionAttributeType.CASE_SENSITIVE_TEXT,
                ActionAttributeType.CASE_INSENSITIVE_TEXT,
                ActionAttributeType.TOOLTIP,
                ActionAttributeType.XPATH,
                ActionAttributeType.CSS_SELECTOR,
                ActionAttributeType.TAG_NAME,
                ActionAttributeType.IMAGE_SRC,
                ActionAttributeType.IMAGE_SRC_PART,
                ActionAttributeType.BUTTON_NAME,
                ActionAttributeType.FIELD_NAME,
                ActionAttributeType.CHECKBOX_NAME,
                ActionAttributeType.ELEMENT_NAME,
                ActionAttributeType.ID,
                ActionAttributeType.CLASS_NAME);
        assertEquals(searchTypes, ActionAttributeType.getSearchTypes());
    }

    @Test
    void getFilterTypes()
    {
        Set<ActionAttributeType> filterTypes = Set.of(
                ActionAttributeType.LINK_URL,
                ActionAttributeType.LINK_URL_PART,
                ActionAttributeType.CASE_SENSITIVE_TEXT,
                ActionAttributeType.TOOLTIP,
                ActionAttributeType.IMAGE_SRC_PART,
                ActionAttributeType.TEXT_PART,
                ActionAttributeType.PLACEHOLDER,
                ActionAttributeType.STATE,
                ActionAttributeType.DROP_DOWN_STATE,
                ActionAttributeType.VALIDATION_ICON_SOURCE,
                ActionAttributeType.RELATIVE_TO_PARENT_WIDTH,
                ActionAttributeType.CLASS_ATTRIBUTE_PART,
                ActionAttributeType.FIELD_TEXT,
                ActionAttributeType.FIELD_TEXT_PART,
                ActionAttributeType.DROP_DOWN_TEXT);
        assertEquals(filterTypes, ActionAttributeType.getFilterTypes());
    }
}
