/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.steps.ui.web.generic.steps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.hamcrest.Description;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ui.web.validation.IDescriptiveSoftAssert;
import org.vividus.ui.validation.matcher.ElementNumberMatcher;
import org.vividus.ui.web.action.ICssSelectorFactory;

@ExtendWith(MockitoExtension.class)
class ParametrizedValidationsTests
{
    private static final String ELEMENTS_MESSAGE = "elements";

    private static final String THE_NUMBER_OF_ELEMENTS_WITH_SPECIFIED_PARAMETERS =
            "The number of elements with specified parameters";

    @Mock
    private IDescriptiveSoftAssert descriptiveSoftAssert;

    @Mock
    private ICssSelectorFactory cssSelectorFactory;

    @InjectMocks
    private ParameterizedValidations parameterizedValidations;

    @Test
    void testAssertNumber()
    {
        List<WebElement> elements = Collections.singletonList(mock(WebElement.class));
        parameterizedValidations.assertNumber(ELEMENTS_MESSAGE, ComparisonRule.EQUAL_TO, 1, elements);
        verify(descriptiveSoftAssert).assertThat(eq(THE_NUMBER_OF_ELEMENTS_WITH_SPECIFIED_PARAMETERS),
                eq(THE_NUMBER_OF_ELEMENTS_WITH_SPECIFIED_PARAMETERS), eq(elements), any(ElementNumberMatcher.class));
    }

    @Test
    void testAssertNumberWithLocationReporting()
    {
        List<WebElement> elements = Arrays.asList(mock(WebElement.class), mock(WebElement.class));
        Description description = mock(Description.class);
        when(cssSelectorFactory.getCssSelectors(elements)).thenReturn(Stream.of("value"));
        when(descriptiveSoftAssert.assertThat(eq(THE_NUMBER_OF_ELEMENTS_WITH_SPECIFIED_PARAMETERS), eq(elements),
                argThat(m ->
                {
                    m.describeMismatch(elements, description);
                    return true;
                }))).thenReturn(true);
        parameterizedValidations.assertNumberWithLocationReporting(ELEMENTS_MESSAGE, ComparisonRule.EQUAL_TO, 1,
                elements);
        verify(description).appendText(".\nFound elements CSS selectors:\nvalue\n");
    }
}
