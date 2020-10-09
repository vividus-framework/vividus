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

package org.vividus.ui.mobile.action.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AppiumHowTests
{
    private static final String VALUE = "value";

    @ParameterizedTest
    @CsvSource({
        "By.xpath:          , XPATH           ",
        "By.AccessibilityId:, ACCESSIBILITY_ID",
        "By.IosClassChain:  , IOS_CLASS_CHAIN "
    })
    void testBuildBy(String prefix, AppiumHow how)
    {
        assertEquals(prefix + StringUtils.SPACE +  VALUE, how.buildBy(VALUE).toString());
    }
}
