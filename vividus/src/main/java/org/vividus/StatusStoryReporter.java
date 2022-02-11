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

package org.vividus;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.steps.StepCollector.Stage;
import org.vividus.softassert.exception.VerificationError;

public class StatusStoryReporter extends ChainedStoryReporter implements IRunStatusProvider
{
    private final AtomicReference<Optional<Status>> status = new AtomicReference<>(Optional.empty());
    private final AtomicBoolean recordStatus = new AtomicBoolean(true);

    @Override
    public void beforeStoriesSteps(Stage stage)
    {
        recordStatus.set(false);
        super.beforeStoriesSteps(stage);
    }

    @Override
    public void afterStoriesSteps(Stage stage)
    {
        super.afterStoriesSteps(stage);
        recordStatus.set(true);
    }

    @Override
    public Optional<Status> getRunStatus()
    {
        return status.get();
    }

    @Override
    public void scenarioExcluded(Scenario scenario, String filter)
    {
        changeStatus(Status.SKIPPED);
        super.scenarioExcluded(scenario, filter);
    }

    @Override
    public void successful(String step)
    {
        changeStatus(Status.PASSED);
        super.successful(step);
    }

    @Override
    public void ignorable(String step)
    {
        changeStatus(Status.SKIPPED);
        super.ignorable(step);
    }

    @Override
    public void pending(String step)
    {
        changeStatus(Status.PENDING);
        super.pending(step);
    }

    @Override
    public void notPerformed(String step)
    {
        changeStatus(Status.SKIPPED);
        super.notPerformed(step);
    }

    @Override
    public void failed(String step, Throwable throwable)
    {
        Throwable cause = JBehaveFailureUnwrapper.unwrapCause(throwable);
        changeStatus(getStatus(cause));
        super.failed(step, throwable);
    }

    private void changeStatus(Status newStatus)
    {
        if (recordStatus.get())
        {
            status.updateAndGet(currentStatus ->
                    currentStatus.isEmpty() || currentStatus.get().getPriority() > newStatus.getPriority()
                            ? Optional.of(newStatus) : currentStatus);
        }
    }

    private static Status getStatus(Throwable throwable)
    {
        return throwable instanceof AssertionError ? getStatus((AssertionError) throwable) : Status.BROKEN;
    }

    private static Status getStatus(AssertionError assertionError)
    {
        return isOngoingKnownIssuesOnly(assertionError) ? Status.KNOWN_ISSUES_ONLY : Status.FAILED;
    }

    private static boolean isOngoingKnownIssuesOnly(AssertionError assertionError)
    {
        return assertionError instanceof VerificationError && ((VerificationError) assertionError)
                .isOngoingKnownIssuesOnly();
    }
}
