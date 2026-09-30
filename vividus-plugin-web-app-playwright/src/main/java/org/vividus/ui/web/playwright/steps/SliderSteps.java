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

import static org.hamcrest.Matchers.equalTo;

import com.microsoft.playwright.Locator;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

public class SliderSteps
{
    private static final String SET_VALUE_JS =
            "(slider, value) => { slider.value = value;"
                    + " slider.dispatchEvent(new Event('input', { bubbles: true }));"
                    + " slider.dispatchEvent(new Event('change', { bubbles: true })); }";
    private static final String VALUE_ATTRIBUTE = "value";
    private static final String SLIDER_VALUE = "Slider value";

    private final UiContext uiContext;
    private final ISoftAssert softAssert;

    public SliderSteps(UiContext uiContext, ISoftAssert softAssert)
    {
        this.uiContext = uiContext;
        this.softAssert = softAssert;
    }

    /**
     * Sets the value of the slider (input element with type "range").
     *
     * @param value   The value to set.
     * @param locator The locator used to find a slider.
     * @see <a href="https://www.w3schools.com/jsref/dom_obj_range.asp">more about sliders</a>
     */
    @When("I set value `$value` in slider located by `$locator`")
    public void setSliderValue(String value, PlaywrightLocator locator)
    {
        Locator slider = uiContext.locateElement(locator);
        slider.evaluate(SET_VALUE_JS, value);
    }

    /**
     * Checks the value of the slider (input element with type "range").
     *
     * @param value   The expected value.
     * @param locator The locator used to find a slider.
     * @see <a href="https://www.w3schools.com/jsref/dom_obj_range.asp">more about sliders</a>
     */
    @Then("value `$value` is selected in slider located by `$locator`")
    public void verifySliderValue(String value, PlaywrightLocator locator)
    {
        Locator slider = uiContext.locateElement(locator);
        softAssert.assertThat(SLIDER_VALUE, slider.getAttribute(VALUE_ATTRIBUTE), equalTo(value));
    }
}
