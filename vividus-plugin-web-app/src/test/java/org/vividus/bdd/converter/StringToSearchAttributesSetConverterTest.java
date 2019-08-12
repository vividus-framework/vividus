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

package org.vividus.bdd.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;

class StringToSearchAttributesSetConverterTest
{
    private static final String COMA_SPACE = ", ";
    private static final String LOCATOR_ID = "By.id(id)";
    private static final String LOCATOR_XPATH = "By.xpath(//*[contains(@class, 'header')])";
    private static final String UNSUPPORTED_LOCATOR_MESSAGE = "Unsupported locator type: ";

    private final StringToSearchAttributesSetConverter converter = new StringToSearchAttributesSetConverter();

    @Test
    void testConvertValue()
    {
        Set<SearchAttributes> searchAttributesSet = new LinkedHashSet<>();
        searchAttributesSet.add(new SearchAttributes(ActionAttributeType.ID, "id"));
        searchAttributesSet.add(new SearchAttributes(ActionAttributeType.CSS_SELECTOR, "#id"));
        searchAttributesSet.add(new SearchAttributes(ActionAttributeType.CLASS_NAME, "clazz"));
        searchAttributesSet.add(new SearchAttributes(ActionAttributeType.LINK_TEXT, "url"));
        searchAttributesSet.add(new SearchAttributes(ActionAttributeType.ELEMENT_NAME, "button"));
        searchAttributesSet.add(new SearchAttributes(ActionAttributeType.XPATH,
            "//*[contains(normalize-space(@class), 'header')]"));
        searchAttributesSet.add(new SearchAttributes(ActionAttributeType.TAG_NAME, "h1"));
        searchAttributesSet.add(new SearchAttributes(ActionAttributeType.TAG_NAME, "a")
            .addFilter(ActionAttributeType.TEXT_PART, "text"));

        assertEquals(searchAttributesSet, converter.convertValue(LOCATOR_ID + ", By.cssSelector(#id), "
                + "By.className(clazz), By.linkText(url), By.name(button), " + LOCATOR_XPATH + ", By.tagName(h1), "
                + "By.partiallinktext(text)", SearchAttributes.class), "Not all locators are processed normally");
    }

    @Test
    void testConvertValueInvalidLocatorsType()
    {
        assertThrows(IllegalArgumentException.class,
            () ->  converter.convertValue("By.jquery(.a)" + COMA_SPACE + LOCATOR_ID, SearchAttributes.class),
            UNSUPPORTED_LOCATOR_MESSAGE + "jquery");
    }

    @Test
    void testConvertValueInvalidLocatorsFormat()
    {
        assertThrows(IllegalArgumentException.class,
            () -> converter.convertValue("To.xpath(.a)" + COMA_SPACE + LOCATOR_XPATH, SearchAttributes.class),
            UNSUPPORTED_LOCATOR_MESSAGE + "To.xpath");
    }

    @Test
    void testConvertValueEmptyStringLocators()
    {
        assertEquals(Collections.EMPTY_SET, converter.convertValue(StringUtils.EMPTY, SearchAttributes.class));
    }
}
