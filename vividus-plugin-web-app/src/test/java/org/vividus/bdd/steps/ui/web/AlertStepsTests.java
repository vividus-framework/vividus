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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.ui.web.action.AlertActions;
import org.vividus.ui.web.action.AlertActions.Action;

@ExtendWith(MockitoExtension.class)
class AlertStepsTests
{
    @Mock
    private AlertActions alertActions;

    @Mock
    private IBaseValidations baseValidations;

    @InjectMocks
    private AlertSteps alertSteps;

    @Test
    void testProcessAlert()
    {
        Action action = Action.ACCEPT;
        String message = "message";
        alertSteps.processAlert(action, StringComparisonRule.CONTAINS, message);
        verify(alertActions).processAlert(argThat(arg -> arg.toString().contains(message)), eq(action));
    }

    @Test
    void testDoesAlertExist()
    {
        alertSteps.doesAlertExist();
        verify(baseValidations).assertExpectedCondition(eq("An alert is present"),
                argThat(condition -> "alert to be present".equals(condition.toString())));
    }

    @Test
    void testDoesAlertNotExist()
    {
        alertSteps.doesAlertNotExist();
        verify(baseValidations).assertExpectedCondition(eq("An alert is not present"),
                argThat(condition -> "condition to not be valid: alert to be present".equals(condition.toString())));
    }
}
