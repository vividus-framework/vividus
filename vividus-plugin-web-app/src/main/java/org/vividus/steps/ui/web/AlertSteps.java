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

import static org.openqa.selenium.support.ui.ExpectedConditions.alertIsPresent;
import static org.openqa.selenium.support.ui.ExpectedConditions.not;

import java.util.function.Consumer;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.web.action.AlertActions.Action;
import org.vividus.ui.web.action.IAlertActions;

@TakeScreenshotOnFailure
public class AlertSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AlertSteps.class);
    private static final String ALERT_PRESENT = "An alert is present";

    private final IAlertActions alertActions;
    private final IBaseValidations baseValidations;
    private final ISoftAssert softAssert;

    public AlertSteps(IAlertActions alertActions, IBaseValidations baseValidations, ISoftAssert softAssert)
    {
        this.alertActions = alertActions;
        this.baseValidations = baseValidations;
        this.softAssert = softAssert;
    }

    private void handleAlert(Consumer<Alert> alertHandler)
    {
        alertActions.switchToAlert().ifPresentOrElse(alertHandler,
                () -> softAssert.recordFailedAssertion("No alert is present"));
    }

    /**
     * Accepts an action in an <b>alert</b> with matching text by clicking on the 'OK' button or dismiss it by clicking
     * 'Cancel' button
     * <p>
     * An <b>alert</b> is a small box that appears on the display screen to give you an information or to warn you about
     * a potentially damaging operation.
     * You need to acknowledge the alert box by clicking on the 'OK' button to make it go away.
     * </p>
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
        handleAlert(alert -> {
            if (softAssert.assertThat("Alert message", alert.getText(), comparisonRule.createMatcher(message)))
            {
                alertActions.processAlert(alertAction, alert);
            }
        });
    }

    /**
     * Types text into the <b>alert</b> input field and then accept the alert by clicking the 'OK' button.
     * <p>
     * An <b>alert</b> is a small dialog box that appears on the display screen to provide information or to warn about
     * a potentially damaging operation. To proceed, the user needs to confirm or dismiss the alert by clicking
     * the 'OK' or 'Cancel' button, respectively.
     * </p>
     * <p>
     * Note: This step is applicable only for alerts with a single input field.
     * For authorization alerts, basic URL authorization should be used.
     * For example, https://user:pass@domain.com.
     * </p>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Check if the alert is present on the page</li>
     * <li>Type text into the alert input field</li>
     * <li>Accept the alert</li>
     * </ul>
     * @param text String The text to be typed into the alert popup.
     */
    @When("I type text `$text` in alert and accept it")
    public void typeTextAndAcceptAlert(String text)
    {
        handleAlert(alert -> {
            LOGGER.atInfo().addArgument(text).log("Typing text '{}' into the alert");
            alert.sendKeys(text);
            alertActions.processAlert(Action.ACCEPT, alert);
        });
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
        baseValidations.assertExpectedCondition(ALERT_PRESENT, alertIsPresent());
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
