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

package org.vividus.zephyr.model;

import java.util.stream.Stream;

public enum TestCaseStatus
{
    BROKEN(0, "broken"),
    FAILED(1, "failed"),
    UNKNOWN(2, ""),
    PENDING(3, "pending"),
    SKIPPED(4, "notPerformed"),
    PASSED(5, "successful");

    private final String name;
    private final int priority;

    TestCaseStatus(int priority, String name)
    {
        this.priority = priority;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public static TestCaseStatus fromString(String s) throws IllegalArgumentException
    {
        return Stream.of(TestCaseStatus.values())
                     .filter(v -> v.getName().equals(s))
                     .findFirst()
                     .orElse(TestCaseStatus.UNKNOWN);
    }
}
