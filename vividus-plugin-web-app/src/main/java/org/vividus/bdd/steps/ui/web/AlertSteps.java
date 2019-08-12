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

import static org.openqa.selenium.support.ui.ExpectedConditions.alertIsPresent;
import static org.openqa.selenium.support.ui.ExpectedConditions.not;

import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.ui.web.action.AlertActions.Action;
import org.vividus.ui.web.action.IAlertActions;

@TakeScreenshotOnFailure
public class AlertSteps
{
    @Inject private IAlertActions alertActions;

    @Inject private IBaseValidations baseValidations;

    /**
     * Accepts an action in an <b>alert</b> with matching text by clicking on the 'OK' button or dismiss it by clicking
     * 'Cancel' button
     * <p>
     * An <b>alert</b> is a small box that appears on the display screen to give you an information or to warn you about
     * a potentially damaging operation.
     * </p>
     * Unlike dialog boxes, alert boxes do not require any user input.
     * However, you need to acknowledge the alert box by clicking on the 'OK' button to make it go away.
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds an appropriate <b>alert</b> with matching <b>message</b></li>
     * <li>Performs the corresponding an action</li>
     * </ul>
     * @param alertAction Action to perform: accept or dismiss
     * @param comparisonRule String comparison rule: "is equal to", "contains", "does not contain"
     * @param message The text message of the <b>alert</b>
     */
    @When("I $alertAction alert with message which $comparisonRule `$message`")
    public void processAlert(Action alertAction, StringComparisonRule comparisonRule, String message)
    {
        alertActions.processAlert(comparisonRule.createMatcher(message), alertAction);
    }

    /**
     * Checks that there is an <b>alert</b> on the page
     * <p>
     * An <b>alert</b> is a small box that appears on the display screen
     * to give you an information or to warn you about a
     * potentially damaging operation.
     * Unlike dialog boxes, alert boxes do not require any user input.
     * However, you need to acknowledge the alert box by clicking on the 'OK' button to make it go away.
     */
    @Then("an alert is present")
    public void doesAlertExist()
    {
        baseValidations.assertExpectedCondition("An alert is present", alertIsPresent());
    }

    /**
     * Checks that there is no any <b>alerts</b> on the page
     * <p>
     * An <b>alert</b> is a small box that appears on the display screen
     * to give you an information or to warn you about a
     * potentially damaging operation.
     * Unlike dialog boxes, alert boxes do not require any user input.
     * However, you need to acknowledge the alert box by clicking on the 'OK' button to make it go away.
     */
    @Then("an alert is not present")
    public void doesAlertNotExist()
    {
        baseValidations.assertExpectedCondition("An alert is not present", not(alertIsPresent()));
    }
}
