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

package org.vividus.softassert.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.softassert.issue.KnownIssueIdentifier;
import org.vividus.softassert.issue.KnownIssueType;

class SoftAssertionErrorTests
{
    private static Stream<Arguments> knownIssueProvider()
    {
        return Stream.of(
            arguments(createKnownIssue(getIdentifier(false, false), "Not known Issue", true, "in progress", null),
                    false, false, false, false),
            arguments(createKnownIssue(getIdentifier(true, true), "Known Issue #1", false, "open", null),
                    true, true, true, true),
            arguments(createKnownIssue(getIdentifier(false, false), "Known Issue #2", false, "in review", "done"),
                    true, false, false, true),
            arguments(createKnownIssue(getIdentifier(false, false), "Known Issue #3", false, "closed", "fixed"),
                    true, false, false, false),
            arguments(null, false, false, false, false)
        );
    }

    private static KnownIssue createKnownIssue(KnownIssueIdentifier knownIssueIdentifier, String identifier,
            boolean failTestCaseFast, String status, String resolution)
    {
        KnownIssue knownIssue = new KnownIssue(identifier, knownIssueIdentifier, failTestCaseFast);
        knownIssue.setStatus(Optional.of(status));
        knownIssue.setResolution(Optional.ofNullable(resolution));
        return knownIssue;
    }

    private static KnownIssueIdentifier getIdentifier(boolean failTestCaseFast, boolean failTestSuiteFalse)
    {
        var identifier = new KnownIssueIdentifier();
        identifier.setType(KnownIssueType.AUTOMATION);
        identifier.setFailTestCaseFast(failTestCaseFast);
        identifier.setFailTestSuiteFast(failTestSuiteFalse);
        return identifier;
    }

    @ParameterizedTest
    @MethodSource("knownIssueProvider")
    void testKnownIssue(KnownIssue knownIssue, boolean expectedToBeKnownIssue, boolean failTestCaseFast,
            boolean failTestSuiteFalse, boolean notFixedKnownIssue)
    {
        SoftAssertionError softAssertionError = new SoftAssertionError(new AssertionError());
        softAssertionError.setKnownIssue(knownIssue);
        assertAll(
            () -> assertEquals(expectedToBeKnownIssue, softAssertionError.isKnownIssue()),
            () -> assertEquals(failTestCaseFast, softAssertionError.isFailTestCaseFast()),
            () -> assertEquals(failTestSuiteFalse, softAssertionError.isFailTestSuiteFast()),
            () -> assertEquals(notFixedKnownIssue, softAssertionError.isNotFixedKnownIssue())
        );
    }
}
