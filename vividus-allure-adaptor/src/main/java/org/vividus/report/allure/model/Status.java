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

package org.vividus.report.allure.model;

import org.vividus.JBehaveFailureUnwrapper;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.exception.VerificationError;
import org.vividus.softassert.model.SoftAssertionError;

public enum Status
{
    BROKEN(io.qameta.allure.model.Status.BROKEN, 0),
    FAILED(io.qameta.allure.model.Status.FAILED, 1),
    PENDING(io.qameta.allure.model.Status.SKIPPED, 2),
    KNOWN_ISSUES_ONLY(null, 3),
    SKIPPED(io.qameta.allure.model.Status.SKIPPED, 4),
    PASSED(io.qameta.allure.model.Status.PASSED, 5),
    NOT_COVERED(io.qameta.allure.model.Status.PASSED, 6);

    private static final Status LOWEST = NOT_COVERED;

    private final io.qameta.allure.model.Status allureStatus;
    private final int priority;

    Status(io.qameta.allure.model.Status allureStatus, int priority)
    {
        this.allureStatus = allureStatus;
        this.priority = priority;
    }

    public static Status from(AssertionFailedEvent event)
    {
        return event.getSoftAssertionError().isNotFixedKnownIssue() ? KNOWN_ISSUES_ONLY : FAILED;
    }

    public static Status from(Throwable throwable)
    {
        Throwable unwrappedThrowable = JBehaveFailureUnwrapper.unwrapCause(throwable);
        if (unwrappedThrowable instanceof AssertionError)
        {
            AssertionError assertionError = (AssertionError) unwrappedThrowable;
            return isNotFixedKnownIssuesOnly(assertionError) ? KNOWN_ISSUES_ONLY : FAILED;
        }
        return BROKEN;
    }

    private static boolean isNotFixedKnownIssuesOnly(AssertionError assertionError)
    {
        return assertionError instanceof VerificationError && ((VerificationError) assertionError)
                .getErrors()
                .stream()
                .allMatch(SoftAssertionError::isNotFixedKnownIssue);
    }

    public static Status getLowest()
    {
        return LOWEST;
    }

    public int getPriority()
    {
        return priority;
    }

    public io.qameta.allure.model.Status getAllureStatus()
    {
        return allureStatus;
    }
}
