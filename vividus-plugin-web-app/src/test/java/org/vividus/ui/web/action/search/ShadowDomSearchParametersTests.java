/*
 * Copyright 2019-2021 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.vividus.ui.action.search.Visibility;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

class ShadowDomSearchParametersTests
{
    private static final String TARGET = "h1";
    private static final String UPPER_HOST = "div";
    private static final String INNER_HOST1 = "div1";
    private static final String INNER_HOST2 = "div2";

    @Test
    void testShadowDomSearchParametersStringArgConstructor()
    {
        ShadowDomSearchParameters parameters = new ShadowDomSearchParameters(
                TARGET, UPPER_HOST, new String[] { INNER_HOST1, INNER_HOST2 });
        assertEquals(TARGET, parameters.getValue());
        assertArrayEquals(new String[] { INNER_HOST1, INNER_HOST2 }, parameters.getInnerShadowHosts());
        assertEquals(UPPER_HOST, parameters.getUpperShadowHost());
        assertEquals(Visibility.VISIBLE, parameters.getVisibility());
        assertTrue(parameters.isWaitForElement());
    }

    @Test
    void verifyHashCodeAndEquals()
    {
        EqualsVerifier.simple().suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
            .forClass(ShadowDomSearchParameters.class).verify();
    }
}
