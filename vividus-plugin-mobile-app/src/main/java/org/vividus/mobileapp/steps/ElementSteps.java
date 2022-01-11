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

package org.vividus.mobileapp.steps;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Map;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.remote.RemoteWebElement;
import org.vividus.monitor.TakeScreenshotOnFailure;
import org.vividus.selenium.WebDriverUtil;
import org.vividus.selenium.manager.IGenericWebDriverManager;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.JavascriptActions;
import org.vividus.ui.action.search.Locator;

@TakeScreenshotOnFailure
public class ElementSteps
{
    private final IGenericWebDriverManager genericWebDriverManager;
    private final JavascriptActions javascriptActions;
    private final IBaseValidations baseValidations;

    public ElementSteps(JavascriptActions javascriptActions, IGenericWebDriverManager genericWebDriverManager,
            IBaseValidations baseValidations)
    {
        this.javascriptActions = javascriptActions;
        this.genericWebDriverManager = genericWebDriverManager;
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

    public enum PickerWheelDirection
    {
        NEXT, PREVIOUS;
    }
}
