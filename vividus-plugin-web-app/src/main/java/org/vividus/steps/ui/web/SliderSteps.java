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

package org.vividus.steps.ui.web;

import static org.hamcrest.Matchers.equalTo;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.vividus.annotation.Replacement;
import org.vividus.selenium.locator.Locator;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.util.XpathLocatorUtils;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.action.search.WebLocatorType;

import jakarta.inject.Inject;

@TakeScreenshotOnFailure
public class SliderSteps
{
    private static final String SET_VALUE_JS = "arguments[0].value=arguments[1]";
    private static final String VALUE_ATTRIBUTE = "value";
    private static final String SLIDER_VALUE = "Slider value";

    @Inject private IBaseValidations baseValidations;
    @Inject private ISoftAssert softAssert;
    @Inject private WebJavascriptActions javascriptActions;

    /**
     * Step sets value of slider (input element with type = "range")
     * using javascript script.
     * @param value A value to set
     * @param xpath Xpath to slider
     * @see <a href="https://www.w3schools.com/jsref/dom_obj_range.asp"> <i>more about sliders</i></a>
     * @deprecated Use step: "When I select value `$value` in slider located by `$locator`"
     */
    @Deprecated(since = "0.6.2", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0",
            replacementFormatPattern = "When I set value `%1$s` in slider located by `xpath(%2$s)`")
    @When("I select the value '$value' in a slider by the xpath '$xpath'")
    public void setSliderValue(String value, String xpath)
    {
        WebElement slider = baseValidations.assertIfElementExists("Slider to select value in",
                new Locator(WebLocatorType.XPATH, XpathLocatorUtils.getXPath(xpath)));
        if (null != slider)
        {
            javascriptActions.executeScript(SET_VALUE_JS, slider, value);
        }
    }

    /**
     * Step sets value of slider (input element with type = "range")
     * using javascript script.
     * @param value A value to set
     * @param locator Locator to slider
     * @see <a href="https://www.w3schools.com/jsref/dom_obj_range.asp"> <i>more about sliders</i></a>
     */
    @When("I set value `$value` in slider located by `$locator`")
    public void setSliderValue(String value, Locator locator)
    {
        baseValidations.assertElementExists("The slider to set the value", locator).ifPresent(
                s -> javascriptActions.executeScript(SET_VALUE_JS, s, value));
    }

    /**
     * Step checks value of slider (input element with type = "range")
     * @param value A value to check
     * @param xpath Xpath to slider
     * @see <a href="https://www.w3schools.com/jsref/dom_obj_range.asp"> <i>more about sliders</i></a>
     * @deprecated Use step: "Then value `$value` is selected in slider located by `$locator`"
     */
    @Deprecated(since = "0.6.2", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0",
            replacementFormatPattern = "Then value `%1$s` is selected in slider located by `%2$s`")
    @Then("the value '$value' is selected in a slider by the xpath '$xpath'")
    public void verifySliderValue(String value, String xpath)
    {
        WebElement slider = baseValidations.assertIfElementExists("Slider to verify value in",
                new Locator(WebLocatorType.XPATH, XpathLocatorUtils.getXPath(xpath)));
        if (null != slider)
        {
            softAssert.assertThat(SLIDER_VALUE, slider.getAttribute(VALUE_ATTRIBUTE), equalTo(value));
        }
    }

    /**
     * Step checks value of slider (input element with type = "range")
     * @param value A value to check
     * @param locator Locator to slider
     * @see <a href="https://www.w3schools.com/jsref/dom_obj_range.asp"> <i>more about sliders</i></a>
     */
    @Then("value `$value` is selected in slider located by `$locator`")
    public void verifySliderValue(String value, Locator locator)
    {
        baseValidations.assertElementExists("The slider to validate the value", locator).ifPresent(
                s -> softAssert.assertThat(SLIDER_VALUE, s.getAttribute(VALUE_ATTRIBUTE), equalTo(value)));
    }
}
