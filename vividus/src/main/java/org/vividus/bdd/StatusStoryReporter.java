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

package org.vividus.bdd;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.jbehave.core.model.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.softassert.exception.VerificationError;

public class StatusStoryReporter extends ChainedStoryReporter implements IRunStatusProvider
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusStoryReporter.class);

    private final AtomicReference<Optional<Status>> status = new AtomicReference<>(Optional.empty());

    @Override
    public Optional<Status> getRunStatus()
    {
        Optional<Status> toReturn = status.get();
        LOGGER.atError().addArgument(toReturn).log("Status to return: {}");
        return toReturn;
    }

    @Override
    public void scenarioNotAllowed(Scenario scenario, String filter)
    {
        changeStatus(Status.SKIPPED);
        super.scenarioNotAllowed(scenario, filter);
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
        Optional<Status> updatedStatus = status.updateAndGet(
            currentStatus -> currentStatus.isEmpty() || currentStatus.get().getPriority() > newStatus.getPriority()
                    ? Optional.of(newStatus) : currentStatus);
        LOGGER.atError().addArgument(updatedStatus).log("Status changed to: {}");
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
