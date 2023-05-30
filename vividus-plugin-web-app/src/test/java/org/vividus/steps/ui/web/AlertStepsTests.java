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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Alert;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.web.action.AlertActions;
import org.vividus.ui.web.action.AlertActions.Action;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AlertStepsTests
{
    private static final String ALERT_PRESENT = "An alert is present";
    private static final String NO_ALERT_IS_PRESENT = "No alert is present";

    @Mock private AlertActions alertActions;
    @Mock private ISoftAssert softAssert;
    @Mock private IBaseValidations baseValidations;
    @InjectMocks private AlertSteps alertSteps;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AlertSteps.class);

    @Test
    void shouldProcessAlertWhenItIsPresent()
    {
        var action = Action.ACCEPT;
        var message = "message";
        var comparisonRule = StringComparisonRule.CONTAINS;
        Alert alert = mock();
        when(alert.getText()).thenReturn(message);
        when(alertActions.switchToAlert()).thenReturn(Optional.of(alert));
        when(softAssert.assertThat(eq("Alert message"), eq(message),
                argThat(matcher -> "a string containing \"message\"".equals(matcher.toString())))).thenReturn(true);

        alertSteps.processAlert(action, comparisonRule, message);

        verify(alertActions).processAlert(action, alert);
    }

    @Test
    void shouldNotProcessAlertWhenItIsNotPresent()
    {
        var action = Action.ACCEPT;
        var message = "other message";
        var comparisonRule = StringComparisonRule.IS_EQUAL_TO;

        when(alertActions.switchToAlert()).thenReturn(Optional.empty());
        alertSteps.processAlert(action, comparisonRule, message);
        verify(softAssert).recordFailedAssertion(NO_ALERT_IS_PRESENT);
        verifyNoMoreInteractions(alertActions);
    }

    @Test
    void testDoesAlertExist()
    {
        alertSteps.doesAlertExist();
        verify(baseValidations).assertExpectedCondition(eq(ALERT_PRESENT),
                argThat(condition -> "alert to be present".equals(condition.toString())));
    }

    @Test
    void testDoesAlertNotExist()
    {
        alertSteps.doesAlertNotExist();
        verify(baseValidations).assertExpectedCondition(eq("An alert is not present"),
                argThat(condition -> "condition to not be valid: alert to be present".equals(condition.toString())));
    }

    @Test
    void testTypeTextAndAcceptAlertExist()
    {
        var text = "abc";
        Alert alert = mock();
        when(alertActions.switchToAlert()).thenReturn(Optional.of(alert));
        alertSteps.typeTextAndAcceptAlert(text);
        verify(alert).sendKeys(text);
        verify(alertActions).processAlert(Action.ACCEPT, alert);
        assertThat(logger.getLoggingEvents(), is(List.of(info("Typing text '{}' into the alert", text))));
    }

    @Test
    void shouldRecordFailedAssertionIfNoAlertIsPresentOnAttemptToTypeTextAndAcceptIt()
    {
        when(alertActions.switchToAlert()).thenReturn(Optional.empty());
        alertSteps.typeTextAndAcceptAlert("text that should never be typed");
        verify(softAssert).recordFailedAssertion(NO_ALERT_IS_PRESENT);
        verifyNoMoreInteractions(alertActions);
    }
}
