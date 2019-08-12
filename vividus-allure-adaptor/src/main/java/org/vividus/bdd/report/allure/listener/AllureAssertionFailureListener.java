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

package org.vividus.bdd.report.allure.listener;

import javax.inject.Inject;

import com.google.common.eventbus.Subscribe;

import org.vividus.bdd.report.allure.IAllureStepReporter;
import org.vividus.bdd.report.allure.model.StatusPriority;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.model.SoftAssertionError;

import io.qameta.allure.model.Status;

public class AllureAssertionFailureListener
{
    @Inject private IAllureStepReporter allureStepReporter;

    @Subscribe
    public void onAssertionFailure(AssertionFailedEvent event)
    {
        SoftAssertionError softAssertionError = event.getSoftAssertionError();
        Status status = (softAssertionError.isKnownIssue() && !softAssertionError.getKnownIssue().isFixed()
                ? StatusPriority.KNWON_ISSUES_ONLY : StatusPriority.FAILED).getStatusModel();
        allureStepReporter.updateStepStatus(status);
    }
}
