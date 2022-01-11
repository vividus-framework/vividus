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

package org.vividus.steps.html;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Set;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.util.ResourceUtils;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class HtmlStepsTests
{
    private static final String TEXT = "Example Domain";
    private static final String NUMBER_OF_ELEMENTS_FOUND_FORMAT = "Number of elements found by CSS selector '%s'";
    private static final String VARIABLE_NAME = "variableName";
    private static final String HREF = "href";
    private static final String HTML_CONTENT = ResourceUtils.loadResource(HtmlStepsTests.class, "index.html");

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private VariableContext variableContext;

    @InjectMocks
    private HtmlSteps htmlSteps;

    @ParameterizedTest
    @CsvSource({
            "body > thead > tr > td, 0",
            "body p                , 2"
    })
    void testElementContainsDataByCssSelectorNotFound(String selector, int found)
    {
        elementContainsDataByCssSelector(selector, found, false);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void testElementContainsDataByCssSelector()
    {
        elementContainsDataByCssSelector("body > div > h1", 1, true);
        verify(softAssert).assertEquals("Element found by css selector contains expected data", TEXT, TEXT);
    }

    private void elementContainsDataByCssSelector(String selector, int size, boolean result)
    {
        mockFoundElements(selector, size, result);
        htmlSteps.elementContainsDataByCssSelector(HTML_CONTENT, TEXT, selector);
        verifyFoundElements(selector, size);
    }

    @ParameterizedTest
    @CsvSource({
            "body > div > h1, 1, true",
            "body > p       , 0, false"
    })
    void testDoesElementByCssSelectorExist(String selector, int size, boolean result)
    {
        mockFoundElements(selector, size, result);
        htmlSteps.doesElementByCssSelectorExist(selector, HTML_CONTENT, ComparisonRule.EQUAL_TO, 1);
        verifyFoundElements(selector, size);
    }

    @Test
    void testSaveAttributeValueOfElementByCssSelector()
    {
        String selector = "body > div > p:nth-child(3) > a";
        mockFoundElements(selector, 1, true);
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        htmlSteps.saveAttributeValueOfElementByCssSelector(HREF, HTML_CONTENT, selector, scopes, VARIABLE_NAME);
        verifyFoundElements(selector, 1);
        verify(variableContext).putVariable(scopes, VARIABLE_NAME, "http://www.iana.org/domains/example");
    }

    @Test
    void testSaveAttributeValueOfElementByCssSelectorNotFound()
    {
        String selector = "body > div > p:nth-child(4)";
        mockFoundElements(selector, 0, false);
        htmlSteps.saveAttributeValueOfElementByCssSelector(HTML_CONTENT, HREF, selector,
                Set.of(VariableScope.SCENARIO), VARIABLE_NAME);
        verifyFoundElements(selector, 0);
        verifyNoInteractions(variableContext);
    }

    @Test
    void shouldSaveDataOfElementFoundByCssSelector()
    {
        String selector = "h1";
        mockFoundElements(selector, 1, true);
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        htmlSteps.saveData(DataType.TEXT, HTML_CONTENT, selector, scopes, VARIABLE_NAME);
        verifyFoundElements(selector, 1);
        verify(variableContext).putVariable(scopes, VARIABLE_NAME, TEXT);
    }

    private void mockFoundElements(String selector, int size, boolean result)
    {
        lenient().when(softAssert.assertThat(eq(String.format(NUMBER_OF_ELEMENTS_FOUND_FORMAT, selector)), eq(size),
                elementsMatcher())).thenReturn(result);
    }

    private void verifyFoundElements(String selector, int size)
    {
        verify(softAssert).assertThat(eq(String.format(NUMBER_OF_ELEMENTS_FOUND_FORMAT, selector)), eq(size),
                elementsMatcher());
    }

    private Matcher<? super Integer> elementsMatcher()
    {
        return argThat(m -> "a value equal to <1>".equals(m.toString()));
    }
}
