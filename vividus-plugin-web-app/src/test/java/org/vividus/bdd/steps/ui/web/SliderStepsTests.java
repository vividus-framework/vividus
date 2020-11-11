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

package org.vividus.bdd.steps.ui.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.ui.web.util.LocatorUtil;

@ExtendWith(MockitoExtension.class)
class SliderStepsTests
{
    private static final String XPATH = ".//xpath";
    private static final String VALUE = "1";

    @Mock
    private WebJavascriptActions javascriptActions;

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private ISoftAssert softAssert;

    @InjectMocks
    private SliderSteps sliderSteps;

    @Test
    void setSliderValueNoSliderTest()
    {
        sliderSteps.setSliderValue(VALUE, XPATH);
        verifyNoInteractions(javascriptActions);
    }

    @Test
    void setSliderValueTest()
    {
        WebElement webElement = mock(WebElement.class);
        mockBaseValidations("Slider to select value in", webElement);
        sliderSteps.setSliderValue(VALUE, XPATH);
        verify(javascriptActions).executeScript("arguments[0].value=arguments[1]", webElement, VALUE);
    }

    @Test
    void verifySliderValueTestNoSlider()
    {
        sliderSteps.verifySliderValue(VALUE, XPATH);
        verifyNoInteractions(softAssert);
    }

    @SuppressWarnings("unchecked")
    @Test
    void verifySliderValueTest()
    {
        WebElement webElement = mock(WebElement.class);
        mockBaseValidations("Slider to verify value in", webElement);
        when(webElement.getAttribute("value")).thenReturn(VALUE);
        sliderSteps.verifySliderValue(VALUE, XPATH);
        verify(softAssert).assertThat(eq("Slider value"), eq(VALUE), any(IsEqual.class));
    }

    private void mockBaseValidations(String businessDescription, WebElement foundElement)
    {
        when(baseValidations.assertIfElementExists(businessDescription, new Locator(WebLocatorType.XPATH,
                LocatorUtil.getXPath(XPATH)))).thenReturn(foundElement);
    }
}
