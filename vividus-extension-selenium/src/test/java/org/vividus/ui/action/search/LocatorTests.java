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

package org.vividus.ui.action.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.testdouble.TestLocatorType;

class LocatorTests
{
    private static final String VALUE = "value";

    private Locator locator;

    @BeforeEach
    void beforeEach()
    {
        locator = locator(Visibility.VISIBLE);
    }

    @Test
    void testAddCompetingFilter()
    {
        locator.addFilter(TestLocatorType.FILTER, VALUE);
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
            () -> locator.addFilter(TestLocatorType.COMPETING_FILTER, VALUE));
        assertEquals("Competing attributes: 'Competing Filter' and 'Filter'", exception.getMessage());
    }

    @Test
    void testAddCompetingSearchAttribute()
    {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
            () -> locator.addFilter(TestLocatorType.COMPETING_FILTER, VALUE));
        assertEquals("Competing attributes: 'Competing Filter' and 'Search'", exception.getMessage());
    }

    @Test
    void testAddFilter()
    {
        locator.addFilter(TestLocatorType.FILTER, VALUE);
        assertEquals(1, locator.getFilterAttributes().size());
    }

    @Test
    void testAddTwoFiltersNoCompetence()
    {
        locator.addFilter(TestLocatorType.FILTER, VALUE);
        locator.addFilter(TestLocatorType.ADDITIONAL_FILTER, VALUE);
        assertEquals(2, locator.getFilterAttributes().size());
    }

    @Test
    void testAddFilterNotApplicable()
    {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
            () -> locator.addFilter(TestLocatorType.SEARCH, VALUE));
        assertEquals("Filter by attribute 'SEARCH' is not supported", exception.getMessage());
    }

    @Test
    void testAddOneFilterWithMultipleValues()
    {
        locator.addFilter(TestLocatorType.FILTER, VALUE);
        locator.addFilter(TestLocatorType.FILTER, VALUE);
        Map<LocatorType, List<String>> filterAttributes = locator.getFilterAttributes();
        assertEquals(1, filterAttributes.size());
        assertEquals(VALUE, filterAttributes.get(TestLocatorType.FILTER).get(0));
        assertEquals(VALUE, filterAttributes.get(TestLocatorType.FILTER).get(1));
    }

    @Test
    void testGetSearchAttributeType()
    {
        assertEquals(TestLocatorType.SEARCH, locator.getLocatorType());
    }

    @Test
    void testGetSearchParameters()
    {
        assertEquals(new SearchParameters(VALUE), locator.getSearchParameters());
    }

    @Test
    void testToString()
    {
        assertEquals(" Search: 'value'; Visibility: VISIBLE;", locator.toString());
    }

    static Stream<Arguments> locators()
    {
        return Stream.of(
            arguments(locator(Visibility.VISIBLE).addFilter(TestLocatorType.FILTER, VALUE),
                    "search 'value' (visible) with filter 'value'"),
            arguments(locator(Visibility.INVISIBLE).addFilter(TestLocatorType.FILTER, VALUE)
                                                   .addFilter(TestLocatorType.FILTER, VALUE)
                                                   .addFilter(TestLocatorType.ADDITIONAL_FILTER, VALUE),
                    "search 'value' (invisible) with filter 'value', 'value' and additional filter 'value'"),
            arguments(locator(Visibility.ALL),
                    "search 'value' (visible or invisible)")
        );
    }

    @MethodSource("locators")
    @ParameterizedTest
    void shouldContertLocatorToHumanReadableString(Locator locator, String expectedMessage)
    {
        assertEquals(expectedMessage, locator.toHumanReadableString());
    }

    private static Locator locator(Visibility visibility)
    {
        Locator locator = new Locator(TestLocatorType.SEARCH, VALUE);
        locator.getSearchParameters().setVisibility(visibility);
        return locator;
    }
}
