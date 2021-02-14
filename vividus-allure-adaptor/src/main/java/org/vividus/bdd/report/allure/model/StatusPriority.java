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

package org.vividus.bdd.report.allure.model;

import java.util.function.Function;

import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.model.SoftAssertionError;

import io.qameta.allure.model.Status;

public enum StatusPriority
{
    BROKEN(Status.BROKEN, io.qameta.allure.entity.Status.BROKEN, 0),
    FAILED(Status.FAILED, io.qameta.allure.entity.Status.FAILED, 1),
    KNOWN_ISSUES_ONLY(null, io.qameta.allure.entity.Status.UNKNOWN, 2),
    PENDING(Status.SKIPPED, io.qameta.allure.entity.Status.SKIPPED, 3),
    CANCELED(Status.SKIPPED, io.qameta.allure.entity.Status.SKIPPED, 4),
    PASSED(Status.PASSED, io.qameta.allure.entity.Status.PASSED, 5),
    NOT_COVERED(Status.PASSED, io.qameta.allure.entity.Status.PASSED, 6);

    private static final StatusPriority LOWEST = NOT_COVERED;

    private final Status statusModel;
    private final io.qameta.allure.entity.Status statusEntity;
    private final int priority;

    StatusPriority(Status statusModel, io.qameta.allure.entity.Status statusEntity, int priority)
    {
        this.statusModel = statusModel;
        this.statusEntity = statusEntity;
        this.priority = priority;
    }

    public static StatusPriority from(Status status)
    {
        return getStatusPriority(status, StatusPriority::getStatusModel);
    }

    public static StatusPriority from(io.qameta.allure.entity.Status status)
    {
        return getStatusPriority(status, StatusPriority::getStatusEntity);
    }

    public static StatusPriority from(AssertionFailedEvent event)
    {
        SoftAssertionError softAssertionError = event.getSoftAssertionError();
        return isNotFixedKnownIssue(softAssertionError) ? KNOWN_ISSUES_ONLY : FAILED;
    }

    private static boolean isNotFixedKnownIssue(SoftAssertionError softAssertionError)
    {
        return softAssertionError.isKnownIssue() && !softAssertionError.getKnownIssue().isFixed();
    }

    private static <T> StatusPriority getStatusPriority(T status, Function<StatusPriority, T> statusFromPriority)
    {
        for (StatusPriority statusPriority : values())
        {
            if (statusFromPriority.apply(statusPriority) == status)
            {
                return statusPriority;
            }
        }
        throw new IllegalArgumentException("No StatusPriority is found for Status = " + status);
    }

    public static StatusPriority getLowest()
    {
        return LOWEST;
    }

    public Status getStatusModel()
    {
        return statusModel;
    }

    public io.qameta.allure.entity.Status getStatusEntity()
    {
        return statusEntity;
    }

    public int getPriority()
    {
        return priority;
    }
}
