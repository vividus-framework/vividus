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

package org.vividus.ui.mobile.action.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.vividus.ui.action.search.ByLocatorSearch;

class AppiumLocatorTypeTests
{
    private static final String VALUE = "value";

    @ParameterizedTest
    @CsvSource({
            "By.xpath:,                 XPATH",
            "AppiumBy.accessibilityId:, ACCESSIBILITY_ID",
            "AppiumBy.iOSClassChain:,   IOS_CLASS_CHAIN",
            "By.id:,                    ID"
    })
    void shouldBuildBy(String prefix, AppiumLocatorType locatorType)
    {
        assertEquals(prefix + StringUtils.SPACE +  VALUE, locatorType.buildBy(VALUE).toString());
    }

    @ParameterizedTest
    @EnumSource(names = { "TEXT_PART", "TEXT", "ATTRIBUTE" })
    void shouldNotBuildBy(AppiumLocatorType locatorType)
    {
        assertThrows(UnsupportedOperationException.class, () -> locatorType.buildBy(VALUE));
    }

    @ParameterizedTest
    @CsvSource({
        "Appium XPath    , XPATH           ",
        "Accessibility Id, ACCESSIBILITY_ID",
        "iOS Class Chain , IOS_CLASS_CHAIN ",
        "Id              , ID              "
    })
    void testAppiumLocatorType(String attributeName, AppiumLocatorType locatorType)
    {
        assertEquals(attributeName, locatorType.getAttributeName());
        assertEquals(ByLocatorSearch.class, locatorType.getActionClass());
        assertEquals(locatorType.name(), locatorType.getKey());
        assertThat(locatorType.getCompetingTypes(), is(empty()));
    }
}
