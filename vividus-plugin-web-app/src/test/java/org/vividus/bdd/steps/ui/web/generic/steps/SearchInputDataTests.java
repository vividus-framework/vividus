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

package org.vividus.bdd.steps.ui.web.generic.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.IActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.Visibility;

class SearchInputDataTests
{
    private static final String NAME = "name";
    private static final String XPATH_BODY = "|xpath|\n|//body|";
    private static final String BODY_XPATH = "//body";
    private static final String VALUE = "value";

    @Test
    void testSearchAttributesCreation()
    {
        SearchInputData data = createSearchInputData(XPATH_BODY, ActionAttributeType.XPATH);
        assertEquals(ActionAttributeType.XPATH, data.getSearchAttributes().getSearchAttributeType());
        assertEquals(BODY_XPATH, data.getSearchAttributes().getSearchParameters().getValue());
    }

    @Test
    void testSearchAttributesCreationNoValue()
    {
        assertNull(createSearchInputData("|xpath|\n||", ActionAttributeType.XPATH).getSearchAttributes());
    }

    @Test
    void testSearchAttributesCreationAlienParameter()
    {
        SearchAttributes searchAttributes = createSearchInputData("|cssSelector|\n|.selector|",
                ActionAttributeType.XPATH).getSearchAttributes();
        assertNull(searchAttributes);
    }

    @Test
    void testSearchAttributesCreationSpecifiedVisibilityFalse()
    {
        SearchInputData data = createSearchInputData("|xpath|visibility|\n|//body|False|", ActionAttributeType.XPATH);
        assertEquals(ActionAttributeType.XPATH, data.getSearchAttributes().getSearchAttributeType());
        assertEquals(BODY_XPATH, data.getSearchAttributes().getSearchParameters().getValue());
        assertFalse(data.getSearchAttributes().getSearchParameters().isVisibility(Visibility.VISIBLE));
    }

    @Test
    void testSearchAttributesCreationSpecifiedVisibilityFalseTrue()
    {
        SearchInputData data = createSearchInputData("|xpath|visibility|\n|//body|true|", ActionAttributeType.XPATH);
        assertEquals(ActionAttributeType.XPATH, data.getSearchAttributes().getSearchAttributeType());
        assertEquals(BODY_XPATH, data.getSearchAttributes().getSearchParameters().getValue());
        assertTrue(data.getSearchAttributes().getSearchParameters().isVisibility(Visibility.VISIBLE));
    }

    @Test
    void testSearchAttributesCreationAddFilter()
    {
        SearchInputData data = createSearchInputData("|xpath|state|\n|//body|enabled|",
                ActionAttributeType.XPATH, ActionAttributeType.STATE);
        assertEquals(ActionAttributeType.XPATH, data.getSearchAttributes().getSearchAttributeType());
        assertEquals(BODY_XPATH, data.getSearchAttributes().getSearchParameters().getValue());
        assertEquals(Collections.singletonList("enabled"),
                data.getSearchAttributes().getFilterAttributes().get(ActionAttributeType.STATE));
    }

    @Test
    void testSearchAttributesCreationButtonName()
    {
        SearchInputData data = createSearchInputData("|buttonName|\n|name|", ActionAttributeType.BUTTON_NAME);
        assertEquals(ActionAttributeType.BUTTON_NAME, data.getSearchAttributes().getSearchAttributeType());
        assertEquals(NAME, data.getSearchAttributes().getSearchParameters().getValue());
    }

    @Test
    void testAdditionalParametersCreationCssProperty()
    {
        assertEquals("property", createSearchInputData("|cssProperty|\n|property|").getCssProperty());
    }

    @Test
    void testAdditionalParametersCreationCssValue()
    {
        assertEquals(VALUE, createSearchInputData("|cssProperty|cssValue|\n|property|value|").getCssValue());
    }

    @Test
    void testAdditionalParametersCreationCssValuePart()
    {
        assertEquals("part", createSearchInputData("|cssProperty|cssValuePart|\n|property|part|").getCssValuePart());
    }

    @Test
    void testAdditionalParametersCreationAbsWidth()
    {
        assertEquals(100, createSearchInputData("|absWidth|\n|100|").getAbsWidth().intValue());
    }

    @Test
    void testAdditionalParametersCreationAbsWidthNull()
    {
        assertNull(createSearchInputData(XPATH_BODY).getAbsWidth());
    }

    @Test
    void testAdditionalParametersCreationAttribute()
    {
        WebElementAttribute expected = new WebElementAttribute(NAME, VALUE);
        WebElementAttribute actual = createSearchInputData("|attributeName|attributeValue|\n|name|value|")
                .getAttribute();
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getValue(), actual.getValue());
    }

    @Test
    void testAdditionalParametersCreationAditionalAttributes()
    {
        WebElementAttribute actual = createSearchInputData(
                "|attributeName|attributeValue|attributeName1|attributeValue1|\n|name|value|name1|value1|")
                        .getAdditionalAttributes().get(0);
        WebElementAttribute expected = new WebElementAttribute("name1", "value1");
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getValue(), actual.getValue());
    }

    private SearchInputData createSearchInputData(String table, IActionAttributeType... actionAttributeTypes)
    {
        return new SearchInputData(new ExamplesTable(table).getRowAsParameters(0), actionAttributeTypes);
    }
}
