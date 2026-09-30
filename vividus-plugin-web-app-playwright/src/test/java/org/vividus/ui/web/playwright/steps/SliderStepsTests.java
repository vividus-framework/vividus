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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Locator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

@ExtendWith(MockitoExtension.class)
class SliderStepsTests
{
    private static final String VALUE = "42";

    @Mock private UiContext uiContext;
    @Mock private PlaywrightLocator locator;
    @Mock private Locator slider;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private SliderSteps steps;

    @Test
    void shouldSetSliderValue()
    {
        when(uiContext.locateElement(locator)).thenReturn(slider);
        steps.setSliderValue(VALUE, locator);
        verify(slider).evaluate(
                "(slider, value) => { slider.value = value; slider.dispatchEvent(new Event('input', "
                        + "{ bubbles: true })); slider.dispatchEvent(new Event('change', { bubbles: true })); }",
                VALUE);
    }

    @Test
    void shouldVerifySliderValue()
    {
        when(uiContext.locateElement(locator)).thenReturn(slider);
        when(slider.getAttribute("value")).thenReturn(VALUE);
        steps.verifySliderValue(VALUE, locator);
        verify(softAssert).assertThat(eq("Slider value"), eq(VALUE), argThat(matcher -> matcher.matches(VALUE)));
    }
}
