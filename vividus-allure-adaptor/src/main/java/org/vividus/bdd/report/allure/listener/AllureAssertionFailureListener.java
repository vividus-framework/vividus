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

package org.vividus.bdd.report.allure.listener;

import com.google.common.eventbus.Subscribe;

import org.vividus.bdd.report.allure.IAllureStepReporter;
import org.vividus.bdd.report.allure.model.StatusPriority;
import org.vividus.softassert.event.AssertionFailedEvent;

import io.qameta.allure.model.Status;

public class AllureAssertionFailureListener
{
    private final IAllureStepReporter allureStepReporter;

    public AllureAssertionFailureListener(IAllureStepReporter allureStepReporter)
    {
        this.allureStepReporter = allureStepReporter;
    }

    @Subscribe
    public void onAssertionFailure(AssertionFailedEvent event)
    {
        Status status = StatusPriority.from(event).getStatusModel();
        allureStepReporter.updateStepStatus(status);
    }
}
