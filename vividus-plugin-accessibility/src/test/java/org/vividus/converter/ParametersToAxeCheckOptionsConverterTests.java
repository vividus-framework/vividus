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

package org.vividus.converter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.FunctionalParameterConverter;
import org.jbehave.core.steps.Parameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.accessibility.model.axe.AxeCheckOptions;
import org.vividus.accessibility.model.axe.AxeOptions;
import org.vividus.accessibility.model.axe.EnableableProperty;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.web.action.search.WebLocatorType;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class})
class ParametersToAxeCheckOptionsConverterTests
{
    private static final String RULE = "rule";
    private static final String ELEMENT_TO_CHECK = "element-to-check";
    private static final String ELEMENT_TO_IGNORE = "element-to-ignore";
    private static final String REPORTER = "v2";
    private static final String COLOR_CONTRAST_RULE = "color-contrast-enhanced";

    @Mock private WebElement elementToCheck;
    @Mock private WebElement elementToIgnore;
    @Mock private ISearchActions searchActions;

    @InjectMocks private ParametersToAxeCheckOptionsConverter converter;

    @Test
    void shouldConvertValueToParametersCheckMinimalParameters()
    {
        // CHECKSTYLE:OFF
        String table = "|violationsToCheck      |" + System.lineSeparator()
                     + "|color-contrast-enhanced|";
        // CHECKSTYLE:ON

        Parameters params = createTable(table).getRowsAsParameters(true).get(0);
        AxeCheckOptions options = converter.convertValue(params, null);
        AxeOptions axeRun = options.getRunOnly();
        assertAll(
            () -> assertEquals(RULE, axeRun.getType()),
            () -> assertEquals(List.of(COLOR_CONTRAST_RULE), axeRun.getValues()),
            () -> assertTrue(options.getRootElement().isEmpty()),
            () -> assertTrue(options.getRules().isEmpty()),
            () -> assertEquals(REPORTER, options.getReporter()),
            () -> assertEquals(List.of(), options.getElementsToCheck()),
            () -> assertEquals(List.of(), options.getHideElements())
        );
    }

    @Test
    void shouldConvertValueToParametersCheckStandard()
    {
        // CHECKSTYLE:OFF
        String table = "|standard|violationsToIgnore     |elementsToCheck |elementsToIgnore |" + System.lineSeparator()
                     + "|WCAG2A  |color-contrast-enhanced|element-to-check|element-to-ignore|";
        // CHECKSTYLE:ON

        mockFindElement(ELEMENT_TO_CHECK, List.of(elementToCheck));
        mockFindElement(ELEMENT_TO_IGNORE, List.of(elementToIgnore));

        Parameters params = createTable(table).getRowsAsParameters(true).get(0);
        AxeCheckOptions options = converter.convertValue(params, null);
        AxeOptions axeRun = options.getRunOnly();
        Map<String, EnableableProperty> rules = options.getRules();
        assertAll(
            () -> assertEquals("tag", axeRun.getType()),
            () -> assertEquals(List.of("wcag2a"), axeRun.getValues()),
            () -> assertTrue(options.getRootElement().isEmpty()),
            () -> assertFalse(rules.get(COLOR_CONTRAST_RULE).isEnabled()),
            () -> assertEquals(REPORTER, options.getReporter()),
            () -> assertEquals(List.of(elementToCheck), options.getElementsToCheck()),
            () -> assertEquals(List.of(elementToIgnore), options.getHideElements())
        );
    }

    @Test
    void shouldConvertValueToParametersCheckViolations()
    {
        // CHECKSTYLE:OFF
        String table = "|violationsToCheck   |violationsToIgnore     |elementsToCheck |elementsToIgnore |" + System.lineSeparator()
                     + "|aria-required-parent|color-contrast-enhanced|element-to-check|element-to-ignore|";
        // CHECKSTYLE:ON

        mockFindElement(ELEMENT_TO_CHECK, List.of(elementToCheck));
        mockFindElement(ELEMENT_TO_IGNORE, List.of(elementToIgnore));

        Parameters params = createTable(table).getRowsAsParameters(true).get(0);
        AxeCheckOptions options = converter.convertValue(params, null);
        AxeOptions axeRun = options.getRunOnly();
        Map<String, EnableableProperty> rules = options.getRules();
        assertAll(
            () -> assertEquals(RULE, axeRun.getType()),
            () -> assertEquals(List.of("aria-required-parent"), axeRun.getValues()),
            () -> assertTrue(options.getRootElement().isEmpty()),
            () -> assertFalse(rules.get(COLOR_CONTRAST_RULE).isEnabled()),
            () -> assertEquals(REPORTER, options.getReporter()),
            () -> assertEquals(List.of(elementToCheck), options.getElementsToCheck()),
            () -> assertEquals(List.of(elementToIgnore), options.getHideElements())
        );
    }

    static Stream<Arguments> invalidParameters()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
                arguments("|violationsToCheck   |standard|" + System.lineSeparator()
                        + "|aria-required-parent|wcag2a  |",
                        "Either 'standard' or 'violationsToCheck' is allowed"),
                arguments("|violationsToCheck|violationsToIgnore|" + System.lineSeparator()
                        + "|rule1,rule2,rule3|rule2,rule3,rule4 |",
                        "The following violations are specified as both to ignore and to check: rule2, rule3"),
                arguments("|elementsToCheck |elementsToIgnore |" + System.lineSeparator()
                        + "|element-to-check|element-to-ignore|",
                        "Either 'standard' or 'violationsToCheck' must be set")
        );
        // CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource("invalidParameters")
    void shouldFailConversion(String table, String message)
    {
        Parameters params = createTable(table).getRowsAsParameters(true).get(0);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> converter.convertValue(params, null));
        assertEquals(message, thrown.getMessage());
    }

    private void mockFindElement(String id, List<WebElement> webElements)
    {
        doReturn(webElements).when(searchActions)
                .findElements(argThat(l -> id.equals(l.getSearchParameters().getValue())
                        && WebLocatorType.ID.equals(l.getLocatorType())
                        && Visibility.ALL.equals(l.getSearchParameters().getVisibility())));
    }

    private static ExamplesTable createTable(String table)
    {
        ParameterConverters parameterConverters = new ParameterConverters();
        parameterConverters.addConverters(new FunctionalParameterConverter<String, Set<Locator>>(
                value -> Set.of(new Locator(WebLocatorType.ID, value))) { });
        return new ExamplesTableFactory(new Keywords(), null, parameterConverters, new ParameterControls(),
                new TableParsers(parameterConverters), new TableTransformers()).createExamplesTable(table);
    }
}
