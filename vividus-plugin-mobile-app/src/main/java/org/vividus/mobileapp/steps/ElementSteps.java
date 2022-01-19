/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.mobileapp.steps;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Map;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.remote.RemoteWebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.monitor.TakeScreenshotOnFailure;
import org.vividus.selenium.WebDriverUtil;
import org.vividus.selenium.manager.IGenericWebDriverManager;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.ElementActions;
import org.vividus.ui.action.JavascriptActions;
import org.vividus.ui.action.search.Locator;

@TakeScreenshotOnFailure
public class ElementSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ElementSteps.class);
    private static final double PERCENT = 100.0;

    private final IGenericWebDriverManager genericWebDriverManager;
    private final JavascriptActions javascriptActions;
    private final ElementActions elementActions;
    private final IBaseValidations baseValidations;

    public ElementSteps(JavascriptActions javascriptActions, IGenericWebDriverManager genericWebDriverManager,
            ElementActions elementActions, IBaseValidations baseValidations)
    {
        this.javascriptActions = javascriptActions;
        this.genericWebDriverManager = genericWebDriverManager;
        this.elementActions = elementActions;
        this.baseValidations = baseValidations;
    }

    /**
     * Select a next or previous picker wheel value in date picker
     * @param direction direction of next value, either <b>NEXT</b> or <b>PREVIOUS</b>
     * @param offset offset for pick from a middle of a wheel
     * @param locator locator to find a <i>XCUIElementTypePickerWheel</i> element
     */
    @When("I select $direction value with `$offset` offset in picker wheel located `$locator`")
    public void selectPickerWheelValue(PickerWheelDirection direction, double offset, Locator locator)
    {
        isTrue(genericWebDriverManager.isIOSNativeApp(), "Picker wheel selection is supported only for iOS platform");
        baseValidations.assertElementExists("Picker wheel", locator)
                       .filter(element ->
                       {
                           String pickerTag = "XCUIElementTypePickerWheel";
                           String tag = element.getTagName();
                           isTrue(pickerTag.equals(tag), "Located element must have %s type, but got %s", pickerTag,
                               tag);
                           return true;
                       })
                       .map(element -> WebDriverUtil.unwrap(element, RemoteWebElement.class))
                       .map(RemoteWebElement::getId)
                       .ifPresent(id -> javascriptActions.executeScript("mobile: selectPickerWheelValue",
                           Map.of("order", direction.name().toLowerCase(),
                                  "element", id,
                                  "offset", offset)));
    }

    /**
     * Sets the value of the slider to the number. The number must be greater than or equal to 0. Make
     * sure the passed number does not exceed the right limit of the slider, this may lead to unexpected
     * failure.
     * <br>
     * @param locator locator to find a <i>android.widget.SeekBar</i> element
     * @param number the number to set on the slider (greater than or equal to 0)
     */
    @When("I set value of Android slider located `$locator` to `$number`")
    public void setAndroidSliderValue(Locator locator, double number)
    {
        isTrue(genericWebDriverManager.isAndroidNativeApp(), "The step is supported only for Android platform");
        isTrue(number >= 0, "The target slider number must be greater than or equal to 0, but got %s", number);
        setSliderValue(locator, number);
    }

    /**
     * Sets the value of the slider to the percents. The percent value must be between 0 and 100.
     * <br>
     * <br>
     * <b>The accuracy of setting the slider value is not guaranteed and may vary depending on the device screen
     * resolution.</b>
     * <br>
     * @param locator locator to find a <i>XCUIElementTypeSlider</i> element
     * @param percent the percent number to set on the slider (0:100)
     */
    @SuppressWarnings("MagicNumber")
    @When("I set value of iOS slider located `$locator` to `$percent` percents")
    public void setIOSSliderValue(Locator locator, int percent)
    {
        isTrue(genericWebDriverManager.isIOSNativeApp(), "The step is supported only for iOS platform");
        isTrue(percent >= 0 && percent <= 100,
                "The target slider percent value must be between 0 and 100 inclusively, but got %s", percent);
        setSliderValue(locator, percent / PERCENT);
    }

    private void setSliderValue(Locator locator, double value)
    {
        baseValidations.assertElementExists("The slider", locator).ifPresent(slider ->
        {
            slider.sendKeys(Double.toString(value));
            LOGGER.atInfo().addArgument(() -> elementActions.getElementText(slider))
                           .log("The slider value is set to {}");
        });
    }

    public enum PickerWheelDirection
    {
        NEXT, PREVIOUS;
    }
}
