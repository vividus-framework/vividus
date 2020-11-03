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

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.softassert.issue.KnownIssueIdentifier;
import org.vividus.softassert.issue.KnownIssueType;

class SoftAssertionErrorTests
{
    @ParameterizedTest
    @MethodSource("knownIssueProvider")
    void testKnownIssue(KnownIssue knownIssue, boolean expectedToBeKnownIssue, boolean failTestCaseFast,
            boolean failTestSuiteFalse)
    {
        SoftAssertionError softAssertionError = new SoftAssertionError(new AssertionError());
        softAssertionError.setKnownIssue(knownIssue);
        assertEquals(expectedToBeKnownIssue, softAssertionError.isKnownIssue());
        assertEquals(failTestCaseFast, softAssertionError.isFailTestCaseFast());
        assertEquals(failTestCaseFast, softAssertionError.isFailTestSuiteFast());
    }

    private static Stream<Arguments> knownIssueProvider()
    {
        return Stream.of(
            Arguments.of(new KnownIssue("Not known Issue", getIdentifier(false, false), true), false, false, false),
            Arguments.of(new KnownIssue("Known Issue #1", getIdentifier(true, true), false), true, true, true),
            Arguments.of(new KnownIssue("Known Issue #2", getIdentifier(false, false), false), true, false, false),
            Arguments.of(null, false, false, false));
    }

    private static KnownIssueIdentifier getIdentifier(boolean failTestCaseFast, boolean failTestSuiteFalse)
    {
        KnownIssueIdentifier identifier = new KnownIssueIdentifier();
        identifier.setType(KnownIssueType.AUTOMATION);
        identifier.setFailTestCaseFast(failTestCaseFast);
        identifier.setFailTestSuiteFast(failTestSuiteFalse);
        return identifier;
    }
}
