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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;

class LocatorTypeTests
{
    static Stream<Arguments> dataSet()
    {
        return Stream.of(
            arguments(WebLocatorType.XPATH, "XPath", XpathSearch.class),
            arguments(WebLocatorType.ID, "Id", BySeleniumLocatorSearch.class),
            arguments(WebLocatorType.TAG_NAME, "Tag name", BySeleniumLocatorSearch.class)
        );
    }

    @MethodSource("dataSet")
    @ParameterizedTest
    void testWebAttributeType(WebLocatorType type, String name, Class<?> actionClass)
    {
        assertEquals(type.getAttributeName(), name);
        assertEquals(type.getActionClass(), actionClass);
        assertThat(type.getCompetingTypes(), empty());
        assertEquals(type.name(), type.getKey());
    }

    @Test
    void testLocatorFactoryMethodForShadowDom()
    {
        Locator locator = WebLocatorType.SHADOW_CSS_SELECTOR.buildLocator("div1; div2; h1");
        assertEquals(locator.getLocatorType(), WebLocatorType.SHADOW_CSS_SELECTOR);
        assertEquals(locator.getSearchParameters().getClass(), ShadowDomSearchParameters.class);
        ShadowDomSearchParameters parameters = (ShadowDomSearchParameters) locator.getSearchParameters();
        assertEquals(parameters.getValue(), "h1");
        assertEquals(parameters.getUpperShadowHost(), "div1");
        assertArrayEquals(parameters.getInnerShadowHosts(), new String[]{"div2"});
    }

    @Test
    void testLocatorFactoryMethodForCss()
    {
        String value = "#test";
        Locator locator = WebLocatorType.CSS_SELECTOR.buildLocator(value);
        assertEquals(locator.getLocatorType(), WebLocatorType.CSS_SELECTOR);
        assertEquals(locator.getSearchParameters().getClass(), SearchParameters.class);
        assertEquals(locator.getSearchParameters().getValue(), value);
    }
}
