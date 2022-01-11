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

package org.vividus;

import org.vividus.report.allure.model.StatusPriority;

public enum Status
{
    BROKEN(0),
    FAILED(1),
    PENDING(2),
    KNOWN_ISSUES_ONLY(3),
    SKIPPED(4),
    PASSED(5),
    NOT_COVERED(6);

    private static final Status LOWEST = NOT_COVERED;

    private final int priority;

    Status(int priority)
    {
        this.priority = priority;
    }

    public static Status from(StatusPriority statusPriority)
    {
        Status status = null;
        switch (statusPriority)
        {
            case BROKEN:
                status = Status.BROKEN;
                break;
            case FAILED:
                status = Status.FAILED;
                break;
            case PENDING:
                status = Status.PENDING;
                break;
            case KNOWN_ISSUES_ONLY:
                status = Status.KNOWN_ISSUES_ONLY;
                break;
            case CANCELED:
                status = Status.SKIPPED;
                break;
            case PASSED:
                status = Status.PASSED;
                break;
            case NOT_COVERED:
                status = Status.PASSED;
                break;
            default:
                break;
        }
        return status;
    }

    public static Status getLowest()
    {
        return LOWEST;
    }

    public int getPriority()
    {
        return priority;
    }
}
