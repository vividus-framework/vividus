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

package org.vividus.bdd.steps.ui.web.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.junit.jupiter.api.Test;

class RenamedParametersTests
{
    private static final String NEW_NAME1 = "newName1";
    private static final String NEW_NAME2 = "newName2";
    private static final String NAME = "name";
    private static final String BUTTON_NAME = "buttonName";
    private static final String VALUE = "value";
    private final Parameters initialParameters = new ExamplesTable("|name|\n|value|").getRowAsParameters(0);

    @Test
    void testUpdateParameterName()
    {
        Parameters actualParameters = new RenamedParameters(initialParameters).updateParameterName(NAME, BUTTON_NAME);
        assertEquals(VALUE, actualParameters.valueAs(BUTTON_NAME, String.class));
    }

    @Test
    void testMapParametersToNullValue()
    {
        Parameters actualParameters = new RenamedParameters(initialParameters).updateParameterName("wrongName",
                BUTTON_NAME);
        assertNull(actualParameters.valueAs(BUTTON_NAME, String.class, null));
    }

    @Test
    void testValues()
    {
        Parameters actualParameters = new RenamedParameters(initialParameters).updateParameterName("", "");
        assertEquals(initialParameters.values(), actualParameters.values());
    }

    @Test
    void testValueAsTwoParams()
    {
        RenamedParameters renamedParameters = new RenamedParameters(initialParameters);
        assertEquals(initialParameters.<String>valueAs(NAME, String.class),
                renamedParameters.<String>valueAs(NAME, String.class));
    }

    @Test
    void testValueAsThreeParams()
    {
        RenamedParameters renamedParameters = new RenamedParameters(initialParameters);
        assertEquals(initialParameters.<String>valueAs(NAME, String.class, null),
                renamedParameters.<String>valueAs(NAME, String.class, null));
    }

    @Test
    void testUpdateParameterNames()
    {
        Parameters initialParameters = new ExamplesTable("|oldName1|oldName2|\n|value1|value2|").getRowAsParameters(0);
        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put("oldName1", NEW_NAME1);
        replacementMap.put("oldName2", NEW_NAME2);
        Parameters actualParameters = new RenamedParameters(initialParameters).updateParameterNames(replacementMap);
        assertEquals("value1", actualParameters.valueAs(NEW_NAME1, String.class, null));
        assertEquals("value2", actualParameters.valueAs(NEW_NAME2, String.class, null));
    }

    @Test
    void testValuesAfterUpdateParameterName()
    {
        Parameters actualParameters = new RenamedParameters(initialParameters).updateParameterName(NAME, BUTTON_NAME);
        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put(BUTTON_NAME, VALUE);
        assertEquals(expectedValues, actualParameters.values());
    }
}
