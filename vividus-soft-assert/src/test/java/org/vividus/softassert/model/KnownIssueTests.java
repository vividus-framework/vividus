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

package org.vividus.softassert.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vividus.softassert.issue.KnownIssueIdentifier;
import org.vividus.softassert.issue.KnownIssueType;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

class KnownIssueTests
{
    private KnownIssue knownIssue;

    @BeforeEach
    void beforeEach()
    {
        KnownIssueIdentifier identifier = new KnownIssueIdentifier();
        identifier.setType(KnownIssueType.AUTOMATION);
        knownIssue = new KnownIssue("id", identifier, false);
    }

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
        knownIssue.setStatus(Optional.ofNullable(status));
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
        knownIssue.setResolution(Optional.ofNullable(resolution));
        knownIssue.setStatus(Optional.ofNullable(status));
        assertEquals(expectedToBeFixed, knownIssue.isFixed());
    }

    @Test
    void verifyHashCodeAndEquals()
    {
        EqualsVerifier.simple().suppress(Warning.ALL_FIELDS_SHOULD_BE_USED).forClass(KnownIssue.class).verify();
    }
}
