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

package org.vividus.steps.ui.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.locator.Locator;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.util.XpathLocatorUtils;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.action.search.WebLocatorType;

@ExtendWith(MockitoExtension.class)
class SliderStepsTests
{
    private static final String XPATH = ".//xpath";
    private static final String VALUE = "1";
    private static final String SET_VALUE_JS = "arguments[0].value=arguments[1]";
    private static final String VALUE_ATTRIBUTE = "value";
    private static final String SLIDER_VALUE = "Slider value";

    @Mock
    private WebJavascriptActions javascriptActions;

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private ISoftAssert softAssert;

    @InjectMocks
    private SliderSteps sliderSteps;

    private final Locator locator = new Locator(WebLocatorType.XPATH, XPATH);

    @Test
    void setSliderValueNoSliderDeprecatedTest()
    {
        sliderSteps.setSliderValue(VALUE, XPATH);
        verifyNoInteractions(javascriptActions);
    }

    @Test
    void setSliderValueNoSliderTest()
    {
        sliderSteps.setSliderValue(VALUE, locator);
        verifyNoInteractions(javascriptActions);
    }

    @Test
    void setSliderValueDeprecatedTest()
    {
        WebElement webElement = mock(WebElement.class);
        mockBaseValidations("Slider to select value in", webElement);
        sliderSteps.setSliderValue(VALUE, XPATH);
        verify(javascriptActions).executeScript(SET_VALUE_JS, webElement, VALUE);
    }

    @Test
    void setSliderValueTest()
    {
        WebElement webElement = mock(WebElement.class);
        mockBaseValidationsElementExists("The slider to set the value", webElement);
        sliderSteps.setSliderValue(VALUE, locator);
        verify(javascriptActions).executeScript(SET_VALUE_JS, webElement, VALUE);
    }

    @Test
    void verifySliderValueTestNoSliderDeprecated()
    {
        sliderSteps.verifySliderValue(VALUE, XPATH);
        verifyNoInteractions(softAssert);
    }

    @Test
    void verifySliderValueTestNoSlider()
    {
        sliderSteps.verifySliderValue(VALUE, locator);
        verifyNoInteractions(softAssert);
    }

    @SuppressWarnings("unchecked")
    @Test
    void verifySliderValueDeprecatedTest()
    {
        WebElement webElement = mock(WebElement.class);
        mockBaseValidations("Slider to verify value in", webElement);
        when(webElement.getAttribute(VALUE_ATTRIBUTE)).thenReturn(VALUE);
        sliderSteps.verifySliderValue(VALUE, XPATH);
        verify(softAssert).assertThat(eq(SLIDER_VALUE), eq(VALUE), any(IsEqual.class));
    }

    @Test
    void verifySliderValueTest()
    {
        WebElement webElement = mock(WebElement.class);
        mockBaseValidationsElementExists("The slider to validate the value", webElement);
        when(webElement.getAttribute(VALUE_ATTRIBUTE)).thenReturn(VALUE);
        sliderSteps.verifySliderValue(VALUE, locator);
        verify(softAssert).assertThat(eq(SLIDER_VALUE), eq(VALUE), any(IsEqual.class));
    }

    private void mockBaseValidations(String businessDescription, WebElement foundElement)
    {
        when(baseValidations.assertIfElementExists(businessDescription, new Locator(WebLocatorType.XPATH,
                XpathLocatorUtils.getXPath(XPATH)))).thenReturn(foundElement);
    }

    private void mockBaseValidationsElementExists(String businessDescription, WebElement foundElement)
    {
        when(baseValidations.assertElementExists(businessDescription, locator)).thenReturn(Optional.of(foundElement));
    }
}
