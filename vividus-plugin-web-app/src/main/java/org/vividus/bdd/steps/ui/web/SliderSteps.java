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

package org.vividus.bdd.steps.ui.web;

import static org.hamcrest.Matchers.equalTo;

import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.IJavascriptActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.util.LocatorUtil;

@TakeScreenshotOnFailure
public class SliderSteps
{
    @Inject private IBaseValidations baseValidations;
    @Inject private ISoftAssert softAssert;
    @Inject private IJavascriptActions javascriptActions;

    /**
     * Step sets value of slider (input element with type = "range")
     * using javascript script.
     * @param value A value to set
     * @param xpath Xpath to slider
     * @see <a href="https://www.w3schools.com/jsref/dom_obj_range.asp"> <i>more about sliders</i></a>
     */
    @When("I select the value '$value' in a slider by the xpath '$xpath'")
    public void setSliderValue(String value, String xpath)
    {
        WebElement slider = baseValidations.assertIfElementExists("Slider to select value in",
                new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(xpath)));
        if (null != slider)
        {
            javascriptActions.executeScript("arguments[0].value=arguments[1]", slider, value);
        }
    }

    /**
     * Step checks value of slider (input element with type = "range")
     * @param value A value to check
     * @param xpath Xpath to slider
     * @see <a href="https://www.w3schools.com/jsref/dom_obj_range.asp"> <i>more about sliders</i></a>
     */
    @Then("the value '$value' is selected in a slider by the xpath '$xpath'")
    public void verifySliderValue(String value, String xpath)
    {
        WebElement slider = baseValidations.assertIfElementExists("Slider to verify value in",
                new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(xpath)));
        if (null != slider)
        {
            softAssert.assertThat("Slider value", slider.getAttribute("value"), equalTo(value));
        }
    }
}
