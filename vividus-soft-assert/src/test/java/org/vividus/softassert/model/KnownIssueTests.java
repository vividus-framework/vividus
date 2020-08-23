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

package org.vividus.softassert.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vividus.softassert.issue.KnownIssueType;

class KnownIssueTests
{
    private final KnownIssue knownIssue = new KnownIssue("id", KnownIssueType.AUTOMATION, false);

    @ParameterizedTest
    @CsvSource({
        "'',     false",
        "  ,     false",
        "done,   false",
        "closed, true",
        "CLOSED, true"
    })
    void testIsClosedStatus(String status, boolean expectedToBeClosed)
    {
        knownIssue.setStatus(status);
        assertEquals(expectedToBeClosed, knownIssue.isClosed());
    }

    @ParameterizedTest
    @CsvSource({
        "done,       ,   false",
        "'',     closed, false",
        " ,      closed, false",
        "fixed,  '',     false",
        "fixed,  closed, true",
        "done,   closed, true",
        "FIXED,  closed, true",
        "DONE,   closed, true"
    })
    void testIsFixedResolution(String resolution, String status, boolean expectedToBeFixed)
    {
        knownIssue.setResolution(resolution);
        knownIssue.setStatus(status);
        assertEquals(expectedToBeFixed, knownIssue.isFixed());
    }
}
