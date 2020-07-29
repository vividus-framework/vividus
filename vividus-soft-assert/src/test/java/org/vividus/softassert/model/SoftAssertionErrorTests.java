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
import org.vividus.softassert.issue.KnownIssueType;

class SoftAssertionErrorTests
{
    @ParameterizedTest
    @MethodSource("knownIssueProvider")
    void testKnownIssue(KnownIssue knownIssue, boolean expectedToBeKnownIssue)
    {
        SoftAssertionError softAssertionError = new SoftAssertionError(new AssertionError());
        softAssertionError.setKnownIssue(knownIssue);
        assertEquals(expectedToBeKnownIssue, softAssertionError.isKnownIssue());
    }

    private static Stream<Arguments> knownIssueProvider()
    {
        return Stream.of(
            Arguments.of(new KnownIssue("Not known Issue", KnownIssueType.AUTOMATION, true), false),
            Arguments.of(new KnownIssue("Known Issue", KnownIssueType.AUTOMATION, false), true),
            Arguments.of(null, false));
    }
}
