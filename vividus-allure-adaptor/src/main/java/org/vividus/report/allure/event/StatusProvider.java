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

package org.vividus.report.allure.event;

import org.vividus.softassert.exception.VerificationError;

import io.qameta.allure.model.Status;

public final class StatusProvider
{
    private StatusProvider()
    {
    }

    public static Status getStatus(Throwable throwable)
    {
        return throwable instanceof AssertionError ? getStatus((AssertionError) throwable) : Status.BROKEN;
    }

    private static Status getStatus(AssertionError assertionError)
    {
        return isOngoingKnownIssuesOnly(assertionError) ? null : Status.FAILED;
    }

    private static boolean isOngoingKnownIssuesOnly(AssertionError assertionError)
    {
        if (assertionError instanceof VerificationError)
        {
            return ((VerificationError) assertionError).isOngoingKnownIssuesOnly();
        }
        return false;
    }
}
