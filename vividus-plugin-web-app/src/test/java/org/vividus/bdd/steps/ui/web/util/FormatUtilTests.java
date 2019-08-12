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

package org.vividus.bdd.steps.ui.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FormatUtilTests
{
    private static final String BUTTON_NAME_CAMEL = "buttonName";
    private static final String BUTTON_NAME = "BUTTON_NAME";

    @Test
    void testChangeUpperUnderscoreToCamel()
    {
        assertEquals(BUTTON_NAME_CAMEL, FormatUtil.changeUpperUnderscoreToCamel(BUTTON_NAME));
    }

    @Test
    void testChangeCamelToUpperUnderscore()
    {
        assertEquals(BUTTON_NAME, FormatUtil.changeCamelToUpperUnderscore(BUTTON_NAME_CAMEL));
    }
}
