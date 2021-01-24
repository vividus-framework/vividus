/*
 * Copyright 2021 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vividus.bdd.report.allure.model.StatusPriority;

class StatusTests
{
    @ParameterizedTest
    @CsvSource({
        "BROKEN,BROKEN",
        "FAILED,FAILED",
        "KNOWN_ISSUES_ONLY,KNOWN_ISSUES_ONLY",
        "PENDING,PENDING",
        "CANCELED,SKIPPED",
        "PASSED,PASSED",
        "NOT_COVERED,PASSED"
    })
    void shouldCreateStatusFromStatusPriority(StatusPriority statusPriority, Status expected)
    {
        assertEquals(expected, Status.from(statusPriority));
    }

    @ParameterizedTest
    @CsvSource({
        "BROKEN,0",
        "FAILED,1",
        "PENDING,2",
        "KNOWN_ISSUES_ONLY,3",
        "SKIPPED,4",
        "PASSED,5",
        "NOT_COVERED,6"
    })
    void shouldReturnPriority(Status status, int expectedPriority)
    {
        assertEquals(expectedPriority, status.getPriority());
    }

    @Test
    void shouldReturnNotCoveredAsALowestPriority()
    {
        assertEquals(Status.NOT_COVERED, Status.getLowest());
    }
}
