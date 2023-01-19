/*
 * Copyright 2019-2023 the original author or authors.
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.testdouble.TestLocatorType;
import org.vividus.ui.action.search.ElementActionService;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.LocatorPattern;
import org.vividus.ui.action.search.LocatorType;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith(MockitoExtension.class)
class LocatorConversionUtilsTests
{
    private static final String VALUE = "value";
    private static final String INVALID_LOCATOR_MESSAGE = "Invalid locator format. "
            + "Expected matches [(?:By\\.)?([a-zA-Z-]+)\\((.*)\\)(:(.*))?] Actual: [";
    private static final char CLOSING_BRACKET = ']';
    private static final String INVALID_LOCATOR = "To.xpath(.a)";
    private static final String SEARCH = "search";

    @Mock private ElementActionService service;
    @Mock private PropertyMappedCollection<LocatorPattern> dynamicLocators;

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
                "By.search(value):i->filter.filter(value).filter(value).filter(value)"),
            Arguments.of(createAttributes(TestLocatorType.SEARCH, "value:not([attribute*='test'])",
                Visibility.INVISIBLE, TestLocatorType.FILTER, VALUE),
                "By.search(value:not([attribute*='test'])):i->filter.filter(value)")
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
    void shouldThrowAnExceptionIfDynamicLocatorUsesNotExistingType()
    {
        var locatorPattern = new LocatorPattern("dah-ro-fus", "11111");
        when(dynamicLocators.getNullable("jquery")).thenReturn(Optional.of(locatorPattern));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> utils.convertToLocator("jquery(.a)"));
        assertEquals("Unsupported locator type: dah-ro-fus", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "By.searchFor(111122), Search for 111122",
            "'By.searchFor(some, coma separated\\,value)', 'Search for some, coma separated\\,value'"})
    void shouldCreateDynamicLocatorUsingBaseType(String locator, String actual)
    {
        lenient().when(service.getSearchLocatorTypes()).thenReturn(Set.of(TestLocatorType.SEARCH));
        var locatorPattern = new LocatorPattern(SEARCH, "Search for %s");
        when(dynamicLocators.getNullable("searchfor")).thenReturn(Optional.of(locatorPattern));
        assertEquals(createAttributes(TestLocatorType.SEARCH, actual, Visibility.VISIBLE),
                utils.convertToLocator(locator));
    }

    @Test
    void shouldCreateDynamicLocatorUsingBaseTypeAndEscapedCommaInValues()
    {
        lenient().when(service.getSearchLocatorTypes()).thenReturn(Set.of(TestLocatorType.SEARCH));
        var locatorPattern = new LocatorPattern(SEARCH, "%s my dear %s%s");
        when(dynamicLocators.getNullable("fusrodah")).thenReturn(Optional.of(locatorPattern));
        assertEquals(createAttributes(TestLocatorType.SEARCH, "Hello, my dear friend!", Visibility.VISIBLE),
                utils.convertToLocator("By.fusRoDah(Hello\\,, friend,!)"));
    }

    @Test
    void shouldCreateLocatorsWithHyphen()
    {
        lenient().when(service.getSearchLocatorTypes()).thenReturn(Set.of(TestLocatorType.SEARCH));
        var locatorPattern = new LocatorPattern(SEARCH, "Fus %s dah!");
        when(dynamicLocators.getNullable("fus-ro-dah")).thenReturn(Optional.of(locatorPattern));
        assertEquals(createAttributes(TestLocatorType.SEARCH, "Fus ro dah!", Visibility.VISIBLE),
                utils.convertToLocator("By.fus-ro-dah(ro)"));
    }

    @Test
    void shouldCreateDynamicLocatorWithoutParameters()
    {
        lenient().when(service.getSearchLocatorTypes()).thenReturn(Set.of(TestLocatorType.SEARCH));
        var pattern = "pattern";
        var locatorPattern = new LocatorPattern(SEARCH, pattern);
        when(dynamicLocators.getNullable("parameterless")).thenReturn(Optional.of(locatorPattern));
        assertEquals(createAttributes(TestLocatorType.SEARCH, pattern, Visibility.VISIBLE),
                utils.convertToLocator("parameterLess()"));
    }

    @Test
    void shouldFailIfPassedParametersNumberLessThanExpected()
    {
        lenient().when(service.getSearchLocatorTypes()).thenReturn(Set.of(TestLocatorType.SEARCH));
        var pattern = "Just %s pattern to get %s tested";
        var locatorPattern = new LocatorPattern(SEARCH, pattern);
        when(dynamicLocators.getNullable("justtest")).thenReturn(Optional.of(locatorPattern));
        var iae = assertThrows(IllegalArgumentException.class,
            () -> utils.convertToLocator("justTest(xPath)"));
        assertEquals("The pattern `Just %s pattern to get %s tested` expecting `2` parameters, but got `1`",
            iae.getMessage());
    }

    @Test
    void shouldEscapeQuotesIfLocatorXpath()
    {
        lenient().when(service.getSearchLocatorTypes()).thenReturn(Set.of(TestLocatorType.XPATH));
        var locatorPattern = new LocatorPattern("xpath", ".//*[@*='%s' or text()=\"%1$s\"]");
        when(dynamicLocators.getNullable("anyattribute")).thenReturn(Optional.of(locatorPattern));
        var actual = utils.convertToLocator("anyAttribute(Don't be a \"fool\")");
        assertEquals(".//*[@*[normalize-space()=concat(\"Don't be a \", '\"', \"fool\", '\"')] "
                + "or text()[normalize-space()=concat(\"Don't be a \", '\"', \"fool\", '\"')]]",
            actual.getSearchParameters().getValue());
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
    void testConvertToLocatorInvalidVisibilityType()
    {
        when(service.getSearchLocatorTypes()).thenReturn(Set.of(TestLocatorType.SEARCH));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> utils.convertToLocator("By.search(id):invalid"));
        assertEquals("Illegal visibility type 'invalid'. Expected one of 'visible', 'invisible', 'all'",
                exception.getMessage());
    }

    @Test
    void testConvertToLocatorEmptyVisibilityType()
    {
        when(service.getSearchLocatorTypes()).thenReturn(Set.of(TestLocatorType.SEARCH));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> utils.convertToLocator("By.search(id):"));
        assertEquals("Visibility type can not be empty. Expected one of 'visible', 'invisible', 'all'",
                exception.getMessage());
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
