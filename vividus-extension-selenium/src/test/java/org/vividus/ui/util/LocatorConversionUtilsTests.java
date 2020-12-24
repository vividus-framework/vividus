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

package org.vividus.ui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.testdouble.TestLocatorType;
import org.vividus.ui.action.search.ElementActionService;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.LocatorType;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;

@ExtendWith(MockitoExtension.class)
class LocatorConversionUtilsTests
{
    private static final String VALUE = "value";
    private static final String INVALID_LOCATOR_MESSAGE = "Invalid locator format. "
            + "Expected matches [(?:By\\.)?([a-zA-Z]+)\\((.+?)\\):?([a-zA-Z]*)?] Actual: [";
    private static final char CLOSING_BRACKET = ']';
    private static final String INVALID_LOCATOR = "To.xpath(.a)";

    @Mock private ElementActionService service;
    @InjectMocks private LocatorConversionUtils utils;

    static Stream<Arguments> actionAttributeSource()
    {
        return Stream.of(
            Arguments.of(createAttributes(TestLocatorType.SEARCH, VALUE, Visibility.INVISIBLE),
                "By.search(value):i"),
            Arguments.of(createAttributes(TestLocatorType.SEARCH, VALUE, Visibility.INVISIBLE),
                "search(value):i"),
            Arguments.of(createAttributes(TestLocatorType.SEARCH, VALUE, Visibility.ALL),
                "By.SeArCH(value):all"),
            Arguments.of(createAttributes(TestLocatorType.SEARCH, VALUE, Visibility.VISIBLE), "search(value)"),
            Arguments.of(new Locator(TestLocatorType.SEARCH, VALUE), "By.search(value)"),
            Arguments.of(createAttributes(TestLocatorType.SEARCH, VALUE, Visibility.INVISIBLE,
                TestLocatorType.FILTER, VALUE), "By.search(value):i->filter.filter(value)"),
            Arguments.of(createAttributes(TestLocatorType.SEARCH, VALUE, Visibility.ALL,
                TestLocatorType.FILTER, VALUE), "By.search(value):all->filter.filter(value)"),
            Arguments.of(createAttributes(TestLocatorType.SEARCH, VALUE, Visibility.INVISIBLE,
                TestLocatorType.FILTER, VALUE)
                .addFilter(TestLocatorType.FILTER, VALUE)
                .addFilter(TestLocatorType.FILTER, VALUE),
                "By.search(value):i->filter.filter(value).filter(value).filter(value)")
        );
    }

    @ParameterizedTest
    @MethodSource("actionAttributeSource")
    void testConvertToLocator(Locator expected, String testValue)
    {
        lenient().when(service.getSearchLocatorTypes()).thenReturn(Set.of(TestLocatorType.SEARCH));
        lenient().when(service.getFilterLocatorTypes()).thenReturn(Set.of(TestLocatorType.FILTER));
        assertEquals(expected, utils.convertToLocator(testValue));
    }

    @Test
    void testConvertToLocatorInvalidLocatorType()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> utils.convertToLocator("By.jquery(.a)"));
        assertEquals("Unsupported locator type: jquery", exception.getMessage());
    }

    @Test
    void testConvertToLocatorInvalidFilterType()
    {
        when(service.getSearchLocatorTypes()).thenReturn(Set.of(TestLocatorType.SEARCH));
        when(service.getFilterLocatorTypes()).thenReturn(Set.of(TestLocatorType.FILTER));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> utils
                .convertToLocator("By.search(id)->filter.filter(enabled).filter(text).notFilter(any)"));
        assertEquals("Unsupported filter type: notFilter", exception.getMessage());
    }

    @Test
    void testConvertToLocatorInvalidLocatorFormat()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> utils.convertToLocator(INVALID_LOCATOR));
        assertEquals(INVALID_LOCATOR_MESSAGE + INVALID_LOCATOR + CLOSING_BRACKET, exception.getMessage());
    }

    @Test
    void testConvertToLocatorEmptyStringLocator()
    {
        assertThrows(IllegalArgumentException.class,
            () -> utils.convertToLocator(StringUtils.EMPTY),
            INVALID_LOCATOR_MESSAGE + CLOSING_BRACKET);
    }

    @Test
    void testConvertToLocatorSet()
    {
        when(service.getSearchLocatorTypes()).thenReturn(Set.of(TestLocatorType.SEARCH));
        Locator locatorId = new Locator(TestLocatorType.SEARCH, VALUE + 1);
        Locator locatorClassName = new Locator(TestLocatorType.SEARCH, VALUE + 2);
        assertEquals(new HashSet<>(Arrays.asList(locatorId, locatorClassName)),
                utils.convertToLocatorSet("By.search(value1), By.search(value2)"));
    }

    private static Locator createAttributes(LocatorType type, String value, Visibility elementType)
    {
        SearchParameters searchParameters = new SearchParameters(value, elementType);
        return new Locator(type, searchParameters);
    }

    private static Locator createAttributes(LocatorType type, String value, Visibility elementType,
            LocatorType filter, String filterValue)
    {
        return createAttributes(type, value, elementType).addFilter(filter, filterValue);
    }
}
