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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;
import org.jbehave.core.steps.ParameterConverters.FunctionalParameterConverter;
import org.jbehave.core.steps.Parameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.accessibility.model.htmlcs.HtmlCsCheckOptions;
import org.vividus.accessibility.model.htmlcs.ViolationLevel;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.context.UiContext;
import org.vividus.ui.web.action.search.WebLocatorType;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class})
class ParametersToAccessibilityCheckOptionsConverterTests
{
    private static final String NOTICE = "notice";

    private static final String WARNING = "warning";

    private static final TestLogger LOGGER = TestLoggerFactory
            .getTestLogger(AbstractAccessibilityCheckOptionsConverter.class);

    @Mock private UiContext uiContext;
    @Mock private ISearchActions searchActions;

    @InjectMocks private ParametersToAccessibilityCheckOptionsConverter converter;

    @Test
    void shouldConvertExamplesTableToOptionsWithMandatoryOptions()
    {
        ExamplesTable table = mockTable("|standard|level|\n|WCAG2AAA|error|");
        HtmlCsCheckOptions checkOptions = converter.convertValue(table.getRowAsParameters(0),
                HtmlCsCheckOptions.class);

        Assertions.assertAll(
                () -> assertEquals("WCAG2AAA", checkOptions.getStandard()),
                () -> assertEquals(ViolationLevel.ERROR, checkOptions.getLevel()),
                () -> assertEquals(List.of(WARNING, NOTICE), checkOptions.getIgnore()),
                () -> assertTrue(checkOptions.getRootElement().isEmpty()),
                () -> assertThat(checkOptions.getHideElements(), is(empty())),
                () -> assertThat(checkOptions.getElementsToCheck(), is(empty())),
                () -> assertNull(checkOptions.getInclude()));
        verifyNoInteractions(searchActions);
        assertThat(LOGGER.getLoggingEvents(), empty());
    }

    @Test
    void shouldConvertExamplesTableToOptionsWithAllOptions()
    {
        ExamplesTable table = mockTable(
                "|standard|level|elementsToCheck |elementsToIgnore|violationsToIgnore|violationsToCheck|"
            + "\n|WCAG2AA |error|check           |ignore          |toIgnore          |toCheck          |");
        WebElement rootElement = mock(WebElement.class);
        when(uiContext.getSearchContext()).thenReturn(rootElement);
        WebElement checkElement = mock(WebElement.class);
        mockFindElement("check", List.of(checkElement));
        String ignoreKey = "ignore";
        mockFindElement(ignoreKey, List.of());

        HtmlCsCheckOptions checkOptions = converter.convertValue(table.getRowAsParameters(0),
                HtmlCsCheckOptions.class);

        Assertions.assertAll(
                () -> assertEquals("WCAG2AA", checkOptions.getStandard()),
                () -> assertEquals(ViolationLevel.ERROR, checkOptions.getLevel()),
                () -> assertEquals(List.of("toIgnore", WARNING, NOTICE), checkOptions.getIgnore()),
                () -> assertEquals(rootElement, checkOptions.getRootElement().get()),
                () -> assertEquals(List.of(checkElement), checkOptions.getElementsToCheck()),
                () -> assertEquals(List.of(), checkOptions.getHideElements()),
                () -> assertEquals(List.of("toCheck"), checkOptions.getInclude()));
        Locator locator = new Locator(WebLocatorType.ID, ignoreKey);
        locator.getSearchParameters().setVisibility(Visibility.ALL);
        assertThat(LOGGER.getLoggingEvents(), is(List.of(info("No elements found by {}", locator))));
    }

    @Test
    void shouldSetElementsToCheckToNullIfNoElementsByLocatorsWereNotFound()
    {
        ExamplesTable table = mockTable(
                "|standard|level|elementsToCheck |"
            + "\n|WCAG2AA |error|empty-check     |");

        mockFindElement("empty-check", List.of());

        HtmlCsCheckOptions checkOptions = converter.convertValue(table.getRowAsParameters(0),
                HtmlCsCheckOptions.class);

        assertNull(checkOptions.getElementsToCheck());
    }

    private void mockFindElement(String id, List<WebElement> webElements)
    {
        doReturn(webElements).when(searchActions)
                .findElements(argThat(l -> id.equals(l.getSearchParameters().getValue())
                        && WebLocatorType.ID.equals(l.getLocatorType())
                        && Visibility.ALL.equals(l.getSearchParameters().getVisibility())));
    }

    @CsvSource({"|level|%n|error|, standard", "|standard|%n|wcag2a|, level"})
    @ParameterizedTest
    void shouldThrowExpcetionWhenMandatoryParameterIsNotSet(String tableAsString, String notSetParameterName)
    {
        Parameters row = mockTable(String.format(tableAsString)).getRowAsParameters(0);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> converter.convertValue(row, HtmlCsCheckOptions.class));
        assertEquals(notSetParameterName + " should be set", exception.getMessage());
        verifyNoInteractions(searchActions, uiContext);
        assertThat(LOGGER.getLoggingEvents(), empty());
    }

    private static ExamplesTable mockTable(String table)
    {
        ParameterConverters parameterConverters = new ParameterConverters();
        parameterConverters.addConverters(new FluentEnumConverter(),
                new FunctionalParameterConverter<String, Set<Locator>>(value -> Set.of(new Locator(
                        WebLocatorType.ID, value))) { });
        return new ExamplesTableFactory(new Keywords(), null, parameterConverters, new ParameterControls(),
                new TableParsers(parameterConverters), new TableTransformers()).createExamplesTable(table);
    }
}
