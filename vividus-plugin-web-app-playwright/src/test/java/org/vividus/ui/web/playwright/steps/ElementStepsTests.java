/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.BoundingBox;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.ui.web.CssValidationResult;
import org.vividus.ui.web.action.JavascriptActions;
import org.vividus.ui.web.action.ResourceFileLoader;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.action.ElementActions;
import org.vividus.ui.web.playwright.assertions.PlaywrightLocatorAssertions;
import org.vividus.ui.web.playwright.assertions.PlaywrightSoftAssert;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;
import org.vividus.ui.web.playwright.locator.Visibility;
import org.vividus.util.ResourceUtils;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class ElementStepsTests
{
    private static final String XPATH = "xpath";
    private static final String VARIABLE_NAME = "variableName";
    private static final String LOCATOR_VALUE = "div";
    private static final String ATTRIBUTE_NAME = "attributeName";
    private static final String ATTRIBUTE_VALUE = "attributeValue";
    private static final String CSS_NAME = "cssName";
    private static final String CSS_VALUE = "cssValue";
    private static final String FILE_PATH = "filePath";
    private static final Set<VariableScope> VARIABLE_SCOPE = Set.of(VariableScope.STORY);

    @Mock private UiContext uiContext;
    @Mock private ISoftAssert softAssert;
    @Mock private VariableContext variableContext;
    @Mock private ElementActions elementActions;
    @Mock private PlaywrightSoftAssert playwrightSoftAssert;
    @Mock private ResourceFileLoader resourceFileLoader;
    @Mock private JavascriptActions javascriptActions;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @InjectMocks private ElementSteps steps;

    @ParameterizedTest
    // CHECKSTYLE:OFF
    @CsvSource(quoteCharacter = '`', value = {
            ".//a,   3, 3, The number of elements found by 'xpath(.//a) with visibility: visible' is 3",
            ".//div, 4, 3, `The number of elements found by 'xpath(.//div) with visibility: visible' is 4, but 3 were expected`",
            "/h1,    3, 1, `The number of elements found by 'xpath(/h1) with visibility: visible' is 3, but 1 was expected`",
            "/h2,    1, 0, `The number of elements found by 'xpath(/h2) with visibility: visible' is 1, but 0 were expected`"
    })
    // CHECKSTYLE:ON
    void shouldRecordAssertionOnValidationIfNumberOfElementIsEqualToExpected(String locatorValue, int actualNumber,
            int expectedNumber, String assertionMessage)
    {
        var playwrightLocator = new PlaywrightLocator(XPATH, locatorValue);
        Locator locator = mock();
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        when(locator.count()).thenReturn(actualNumber);
        steps.assertElementsNumber(playwrightLocator, ComparisonRule.EQUAL_TO, expectedNumber);
        verify(softAssert).recordAssertion(actualNumber == expectedNumber, assertionMessage);
    }

    @ParameterizedTest
    // CHECKSTYLE:OFF
    @CsvSource(quoteCharacter = '`', value = {
            "a,   3, 2, true, `The number of elements found by 'css(a) with visibility: visible' is 3, it is greater than 2`",
            "div, 3, 4, false, `The number of elements found by 'css(div) with visibility: visible' is 3, but it is not greater than 4`"
    })
    // CHECKSTYLE:ON
    void shouldRecordAssertionOnValidationIfNumberOfElementMatchesToExpected(String locatorValue, int actualNumber,
            int expectedNumber, boolean passed, String assertionMessage)
    {
        var playwrightLocator = new PlaywrightLocator("css", locatorValue);
        Locator locator = mock();
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        when(locator.count()).thenReturn(actualNumber);
        steps.assertElementsNumber(playwrightLocator, ComparisonRule.GREATER_THAN, expectedNumber);
        verify(softAssert).recordAssertion(passed, assertionMessage);
    }

    @Test
    void shouldSaveElementCoordinatesAndSize()
    {
        var playwrightLocator = new PlaywrightLocator(XPATH, LOCATOR_VALUE);
        Locator locator = mock();
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        BoundingBox box = new BoundingBox();
        box.x = 1;
        box.y = 2;
        box.height = 3;
        box.width = 4;
        when(locator.boundingBox()).thenReturn(box);

        steps.saveElementCoordinatesAndSize(playwrightLocator, VARIABLE_SCOPE, VARIABLE_NAME);

        verify(variableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, Map.of(
            "x", box.x,
            "y", box.y,
            "height", box.height,
            "width", box.width
        ));
    }

    @Test
    void shouldSaveNumberOfElementsToVariable()
    {
        int elementCount = 5;
        var playwrightLocator = new PlaywrightLocator(XPATH, LOCATOR_VALUE);
        Locator locator = mock();
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        when(locator.count()).thenReturn(elementCount);
        steps.saveNumberOfElementsToVariable(playwrightLocator, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(variableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, elementCount);
    }

    @Test
    void shouldSaveContextElementAttributeValueToVariable()
    {
        Locator locator = mock();
        when(uiContext.getCurrentContexOrPageRoot()).thenReturn(locator);
        when(locator.getAttribute(ATTRIBUTE_NAME)).thenReturn(ATTRIBUTE_VALUE);
        steps.saveContextElementAttributeValueToVariable(ATTRIBUTE_NAME, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(variableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, ATTRIBUTE_VALUE);
        verifyNoInteractions(softAssert);
    }

    @Test
    void shouldSaveAttributeValueOfElement()
    {
        var playwrightLocator = new PlaywrightLocator(XPATH, LOCATOR_VALUE);
        Locator locator = mock();
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        when(locator.getAttribute(ATTRIBUTE_NAME)).thenReturn(ATTRIBUTE_VALUE);
        steps.saveAttributeValueOfElement(ATTRIBUTE_NAME, playwrightLocator, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(variableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, ATTRIBUTE_VALUE);
        verifyNoInteractions(softAssert);
    }

    @Test
    void shouldNotSaveAttributeValueOfContextIfAttributeIsNotPresent()
    {
        Locator locator = mock();
        when(uiContext.getCurrentContexOrPageRoot()).thenReturn(locator);
        when(locator.getAttribute(ATTRIBUTE_NAME)).thenReturn(null);
        steps.saveContextElementAttributeValueToVariable(ATTRIBUTE_NAME, VARIABLE_SCOPE, VARIABLE_NAME);
        verifyIfAttributeIsNotPresent();
    }

    @Test
    void shouldNotSaveAttributeValueOfElementIfAttributeIsNotPresent()
    {
        var playwrightLocator = new PlaywrightLocator(XPATH, LOCATOR_VALUE);
        Locator locator = mock();
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        when(locator.getAttribute(ATTRIBUTE_NAME)).thenReturn(null);
        steps.saveAttributeValueOfElement(ATTRIBUTE_NAME, playwrightLocator, VARIABLE_SCOPE, VARIABLE_NAME);
        verifyIfAttributeIsNotPresent();
    }

    @Test
    void shouldUploadFile() throws IOException
    {
        File file = mock();
        Path filePath = mock();
        when(resourceFileLoader.loadFile(FILE_PATH)).thenReturn(file);
        when(file.toPath()).thenReturn(filePath);
        try (var playwrightAssertionsStaticMock = mockStatic(PlaywrightAssertions.class))
        {
            var playwrightLocator = new PlaywrightLocator(XPATH, LOCATOR_VALUE);
            Locator fileInputLocator = mock();
            when(uiContext.locateElement(playwrightLocator)).thenReturn(fileInputLocator);
            LocatorAssertions locatorAssertions = mock();
            playwrightAssertionsStaticMock.when(() -> PlaywrightAssertions.assertThat(fileInputLocator))
                    .thenReturn(locatorAssertions);
            doNothing().when(playwrightSoftAssert)
                    .runAssertion(eq("A file input element is not found"), argThat(runnable -> {
                        runnable.run();
                        return true;
                    }));
            steps.uploadFile(playwrightLocator, FILE_PATH);
            var ordered = inOrder(locatorAssertions, fileInputLocator);
            ordered.verify(locatorAssertions).hasCount(1);
            ordered.verify(fileInputLocator).setInputFiles(filePath);
            ordered.verifyNoMoreInteractions();
        }
    }

    @Test
    void shouldAssertElementCssProperty()
    {
        Locator locator = mock();
        when(uiContext.getCurrentContexOrPageRoot()).thenReturn(locator);
        when(elementActions.getCssValue(locator, CSS_NAME)).thenReturn(CSS_VALUE);
        steps.assertElementCssProperty(CSS_NAME, StringComparisonRule.IS_EQUAL_TO, CSS_VALUE);
        verify(softAssert).assertThat(eq("Element css property value is"), eq(CSS_VALUE),
                argThat(matcher -> matcher.matches(CSS_VALUE)));
    }

    @ParameterizedTest
    @CsvSource({ "VISIBLE", "ALL" })
    void shouldAssertElementsNumberInState(Visibility visibility)
    {
        var playwrightLocator = getLocatorWithVisibility(visibility);
        Locator locator = mock();
        int number = 1;
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        when(locator.count()).thenReturn(number);
        Locator element = mock();
        when(locator.all()).thenReturn(Collections.singletonList(element));
        ComparisonRule comparisonRule = ComparisonRule.EQUAL_TO;
        ElementState state = spy(ElementState.ENABLED);
        try (var playwrightLocatorAssertions = mockStatic(PlaywrightLocatorAssertions.class))
        {
            doNothing().when(playwrightSoftAssert).runAssertion(eq("Element state is not ENABLED"),
                    argThat(runnable -> {
                        runnable.run();
                        return true;
                    }));
            steps.assertElementsNumberInState(state, playwrightLocator, comparisonRule, number);
            playwrightLocatorAssertions.verify(() -> PlaywrightLocatorAssertions.assertElementEnabled(element, false));
        }
    }

    @Test
    void shouldThrowAnExceptionIfLocatorVisibilityAndStateToCheckAreTheSame()
    {
        var visibility = Visibility.VISIBLE;
        var state = ElementState.VISIBLE;
        PlaywrightLocator locator = getLocatorWithVisibility(Visibility.VISIBLE);
        var illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> steps.assertElementsNumberInState(state, locator, ComparisonRule.EQUAL_TO, 0));
        assertEquals(String.format(
                "Locator visibility: %s and the state: %s to validate are the same."
                + " This makes no sense. Please consider validation of elements size instead.",
                visibility, state), illegalArgumentException.getMessage());
    }

    @Test
    void shouldThrowAnExceptionIfLocatorVisibilityAndStateToCheckAreDifferent()
    {
        var visibility = Visibility.VISIBLE;
        var state = ElementState.NOT_VISIBLE;
        PlaywrightLocator locator = getLocatorWithVisibility(visibility);
        var illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> steps.assertElementsNumberInState(state, locator, ComparisonRule.EQUAL_TO, 0));
        assertEquals(String.format("Contradictory input parameters. Locator visibility: '%s', the state: '%s'.",
                visibility, state), illegalArgumentException.getMessage());
    }

    @SuppressWarnings("PMD.NcssCount")
    @Test
    void testDoesElementHasCssProperties()
    {
        final String examplesTableAsString = """
                |cssName   |comparisonRule |expectedValue |
                |cssKey1   |IS_EQUAL_TO    |cssValue1     |
                |-css-key2 |IS_EQUAL_TO    |cssValue2     |
                |css-key3  |IS_EQUAL_TO    |cssValue3     |
                """;
        final ExamplesTable examplesTable = new ExamplesTable(examplesTableAsString);
        final String cssKey1 = "cssKey1";
        final String cssValue1 = "cssValue1";
        final String cssKey2 = "-css-key2";
        final String cssValue2 = "cssValue2";
        final String cssValue3 = "cssValue3";
        Locator locator = mock();
        ElementHandle elementHandle = mock();
        when(locator.elementHandle()).thenReturn(elementHandle);

        when(uiContext.getCurrentContexOrPageRoot()).thenReturn(locator);
        try (MockedStatic<ResourceUtils> resourceUtils =
                     mockStatic(org.vividus.util.ResourceUtils.class))
        {
            resourceUtils.when(() -> org.vividus.util.ResourceUtils
                    .loadResource("org/vividus/ui/web/get-element-computed-css-func.js")).thenReturn("script");
            String getAllCssScript = "([el]) => {scriptreturn getComputedStyleAsMap(el)}";
            Map<String, String> elementCss = Map.of(cssKey1, cssValue1, "cssKey2", cssValue2);
            when(javascriptActions.executeScript(getAllCssScript, elementHandle)).thenReturn(elementCss);

            when(softAssert.assertThat(eq("Element has CSS property 'cssKey1' containing value 'cssValue1'"),
                    eq(cssValue1), argThat(matcher -> matcher.toString().contains(cssValue1))))
                    .thenReturn(true);
            when(softAssert.assertThat(eq("Element has CSS property '-css-key2' containing value 'cssValue2'"),
                    eq(cssValue2), argThat(matcher -> matcher.toString().contains(cssValue2))))
                    .thenReturn(true);
            when(softAssert.assertThat(eq("Element has CSS property 'css-key3' containing value 'cssValue3'"),
                    eq(null), argThat(matcher -> matcher.toString().contains(cssValue3))))
                    .thenReturn(false);

            steps.doesElementHasCssProperties(examplesTable);
        }
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, List<CssValidationResult>>> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(attachmentPublisher).publishAttachment(eq("templates/css_validation_result.ftl"),
                argumentCaptor.capture(), eq("Css validation results"));
        List<CssValidationResult> actualCssValidationResults = argumentCaptor.getValue().get("cssResults");
        assertEquals(3, actualCssValidationResults.size());

        CssValidationResult result2 = actualCssValidationResults.get(1);
        assertEquals(cssKey2, result2.getCssName());
        assertEquals(cssValue2, result2.getCssActualValue());
        assertEquals(StringComparisonRule.IS_EQUAL_TO, result2.getComparisonRule());
        assertEquals(cssValue2, result2.getCssExpectedValue());
        assertTrue(result2.isPassed());
    }

    private static PlaywrightLocator getLocatorWithVisibility(Visibility visibility)
    {
        var locator = new PlaywrightLocator(XPATH, LOCATOR_VALUE);
        locator.setVisibility(visibility);
        return locator;
    }

    private void verifyIfAttributeIsNotPresent()
    {
        verify(softAssert).recordFailedAssertion(String.format("The '%s' attribute does not exist", ATTRIBUTE_NAME));
        verifyNoInteractions(variableContext);
    }
}
