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

package org.vividus.softassert.issue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.model.KnownIssue;

@ExtendWith(MockitoExtension.class)
class KnownIssueCheckerTests
{
    private static final String EXAMPLE_COM = ".*example.com";
    private static final String URL = "http://example.com";
    private static final String ANOTHER_URL = "http://different.com";
    private static final String CURRENT_PAGE_URL = "currentPageUrl";
    private static final String TEXT = "text";
    private static final String PATTERN = "pattern";
    private static final String CASE = "case";
    private static final String STEP = "step";
    private static final String SECOND_STEP = "step2";
    private static final String SUITE = "suite";
    private static final Optional<String> CLOSED = Optional.of("closed");
    private static final String FIRST_ISSUE = "UNIT-1";
    private static final String SECOND_ISSUE = "UNIT-2";
    private static final String THIRD_ISSUE = "UNIT-3";
    private static final String NOT_MATCHING_ASSERTION = "not matching assertion";
    private static final String NOT_MATCHING_SUITE = "not matching suite";
    private static final String NOT_MATCHING_STEP = "not matching step";
    private static final String DESCRIPTION = "description";

    @Mock private IKnownIssueProvider knownIssueProvider;
    @Mock private IIssueStateProvider issueStateProvider;
    @Mock private ITestInfoProvider testInfoProvider;
    @Mock private KnownIssueDataProvider knownIssueDataProvider;
    @InjectMocks private KnownIssueChecker knownIssueChecker;

    @BeforeEach
    void beforeEach()
    {
        knownIssueChecker.setTestInfoProvider(testInfoProvider);
        knownIssueChecker.setIssueStateProvider(issueStateProvider);
    }

    static Stream<Arguments> getKnownIssueWithNotNullTestInfoDataProvider()
    {
        //@formatter:off
        return Stream.of(
            Arguments.of(List.of(STEP), CASE, SUITE, false),
            Arguments.of(List.of(STEP), TEXT, SUITE, true),
            Arguments.of(List.of(STEP), CASE, null,  false),
            Arguments.of(List.of(STEP), TEXT, null,  true),
            Arguments.of(List.of(TEXT), CASE, SUITE, true),
            Arguments.of(List.of(STEP), null, null,  false),
            Arguments.of(List.of(TEXT), null, null,  true),
            Arguments.of(List.of(STEP), CASE, TEXT,  true),
            Arguments.of(List.of(STEP), null, SUITE, false),
            Arguments.of(List.of(STEP), null, TEXT,  true),
            Arguments.of(List.of(STEP), null, null,  false),
            Arguments.of(List.of(STEP, SECOND_STEP), null, null,  false),
            Arguments.of(List.of(TEXT, SECOND_STEP), null, null,  true)
        );
        //@formatter:on
    }

    @Test
    void testGetKnownIssue()
    {
        testGetKnownIssue(CLOSED);
    }

    @Test
    void testGetKnownIssueIfIssueStatusIsNull()
    {
        testGetKnownIssue(Optional.empty());
    }

    private void testGetKnownIssue(Optional<String> status)
    {
        mockKnownIssueIdentifiers();
        when(issueStateProvider.getIssueStatus(TEXT)).thenReturn(status);
        assertKnownIssue(false, TEXT, status);
    }

    @Test
    void testGetKnownIssueIfIssueStateProviderIsNull()
    {
        mockKnownIssueIdentifiers();
        knownIssueChecker.setIssueStateProvider(null);
        assertKnownIssue(false, TEXT);
    }

    @ParameterizedTest
    @MethodSource("getKnownIssueWithNotNullTestInfoDataProvider")
    void testGetKnownIssueIfPotentiallyKnownIsFalse(List<String> testSteps, String testCase, String testSuite,
            boolean isPotentiallyKnown)
    {
        knownIssueChecker.setDetectPotentiallyKnownIssues(true);
        mockKnownIssueIdentifiers();
        when(testInfoProvider.getTestInfo()).thenReturn(createTestInfo(new LinkedList<>(testSteps),
                testCase, testSuite));
        assertKnownIssue(isPotentiallyKnown, TEXT);
    }

    @Test
    void testGetKnownIssueWithResolution()
    {
        mockKnownIssueIdentifiers();
        Optional<String> resolution = Optional.of("resolution");
        when(issueStateProvider.getIssueStatus(TEXT)).thenReturn(CLOSED);
        when(issueStateProvider.getIssueResolution(TEXT)).thenReturn(resolution);
        assertKnownIssue(false, TEXT, CLOSED, resolution);
    }

    @Test
    void testGetKnownIssueWithDynamicDataProvider()
    {
        KnownIssueIdentifier identifier = createIdentifier();
        identifier.setDynamicPatterns(Map.of(CURRENT_PAGE_URL, EXAMPLE_COM));
        doReturn(Map.of(TEXT, identifier)).when(knownIssueProvider).getKnownIssueIdentifiers();
        when(knownIssueDataProvider.getData(CURRENT_PAGE_URL)).thenReturn(Optional.of(URL));

        assertKnownIssue(false, TEXT);
    }

    @Test
    void testGetKnownIssueWithDynamicDataProviderMismatchedData()
    {
        KnownIssueIdentifier identifier = createIdentifier();
        identifier.setDynamicPatterns(Map.of(CURRENT_PAGE_URL, EXAMPLE_COM));
        doReturn(Map.of(TEXT, identifier)).when(knownIssueProvider).getKnownIssueIdentifiers();
        when(knownIssueDataProvider.getData(CURRENT_PAGE_URL)).thenReturn(Optional.of(ANOTHER_URL));

        assertNull(knownIssueChecker.getKnownIssue(PATTERN));
    }

    @Test
    void testGetKnownIssueWithDynamicDataProviderReturnEmpty()
    {
        KnownIssueIdentifier identifier = createIdentifier();
        identifier.setDynamicPatterns(Map.of(CURRENT_PAGE_URL, EXAMPLE_COM));
        doReturn(Map.of(TEXT, identifier)).when(knownIssueProvider).getKnownIssueIdentifiers();
        when(knownIssueDataProvider.getData(CURRENT_PAGE_URL)).thenReturn(Optional.empty());

        assertNull(knownIssueChecker.getKnownIssue(PATTERN));
    }

    @Test
    void testGetKnownIssueWithRuntimeDataProvider()
    {
        KnownIssueIdentifier identifier = createIdentifier();
        identifier.setRuntimeDataPatterns(Map.of(CURRENT_PAGE_URL, EXAMPLE_COM));
        doReturn(Map.of(TEXT, identifier)).when(knownIssueProvider).getKnownIssueIdentifiers();
        when(knownIssueDataProvider.getData(CURRENT_PAGE_URL)).thenReturn(Optional.of(URL));

        assertKnownIssue(false, TEXT);
    }

    @Test
    void testGetKnownIssueWithRuntimeDataProviderMismatchedData()
    {
        KnownIssueIdentifier identifier = createIdentifier();
        identifier.setRuntimeDataPatterns(Map.of(CURRENT_PAGE_URL, EXAMPLE_COM));
        doReturn(Map.of(TEXT, identifier)).when(knownIssueProvider).getKnownIssueIdentifiers();
        when(knownIssueDataProvider.getData(CURRENT_PAGE_URL)).thenReturn(Optional.of(ANOTHER_URL));

        assertNull(knownIssueChecker.getKnownIssue(PATTERN));
    }

    @Test
    void testGetKnownIssueWithRuntimeDataProviderReturnEmpty()
    {
        KnownIssueIdentifier identifier = createIdentifier();
        identifier.setRuntimeDataPatterns(Map.of(CURRENT_PAGE_URL, EXAMPLE_COM));
        doReturn(Map.of(TEXT, identifier)).when(knownIssueProvider).getKnownIssueIdentifiers();
        when(knownIssueDataProvider.getData(CURRENT_PAGE_URL)).thenReturn(Optional.empty());

        assertNull(knownIssueChecker.getKnownIssue(PATTERN));
    }

    @Test
    void testGetKnownIssueNothingMatched()
    {
        assertNull(knownIssueChecker.getKnownIssue(NOT_MATCHING_ASSERTION));
    }

    @Test
    void testGetKnownIssueThatMatchedAllTestInfo()
    {
        Map<String, KnownIssueIdentifier> identifierMap = new HashMap<>();
        identifierMap.put(FIRST_ISSUE, createIdentifierWithTestSuitePattern(PATTERN, STEP, NOT_MATCHING_SUITE));
        identifierMap.put(SECOND_ISSUE, createIdentifier());
        identifierMap.put(THIRD_ISSUE, createIdentifierWithTestSuitePattern(PATTERN, NOT_MATCHING_STEP, SUITE));
        testGetKnownIssueFromSeveralCandidates(identifierMap, false);
    }

    @Test
    void testGetKnownIssueFirstWithMatchedAssertionPattern()
    {
        knownIssueChecker.setDetectPotentiallyKnownIssues(true);
        Map<String, KnownIssueIdentifier> identifierMap = new HashMap<>();
        identifierMap.put(FIRST_ISSUE, createIdentifierWithTestSuitePattern(NOT_MATCHING_ASSERTION, STEP, SUITE));
        identifierMap.put(SECOND_ISSUE, createIdentifierWithTestSuitePattern(PATTERN, NOT_MATCHING_STEP, SUITE));
        identifierMap.put(THIRD_ISSUE, createIdentifierWithTestSuitePattern(PATTERN, STEP, NOT_MATCHING_SUITE));
        testGetKnownIssueFromSeveralCandidates(identifierMap, true);
    }

    @Test
    void shouldNotFindPotentiallyKnownIssueWhenFeatureDisabled()
    {
        Map<String, KnownIssueIdentifier> identifierMap = new HashMap<>();
        identifierMap.put(FIRST_ISSUE, createIdentifierWithTestSuitePattern(NOT_MATCHING_ASSERTION, STEP, SUITE));
        identifierMap.put(SECOND_ISSUE, createIdentifierWithTestSuitePattern(PATTERN, NOT_MATCHING_STEP, SUITE));
        identifierMap.put(THIRD_ISSUE, createIdentifierWithTestSuitePattern(PATTERN, STEP, NOT_MATCHING_SUITE));
        when(testInfoProvider.getTestInfo()).thenReturn(createTestInfo(new LinkedList<>(List.of(STEP)), CASE, SUITE));
        doReturn(identifierMap).when(knownIssueProvider).getKnownIssueIdentifiers();
        assertNull(knownIssueChecker.getKnownIssue(PATTERN));
    }

    @Test
    void testGetKnownIssueWithTheHighestNumberOfMatchedPatterns()
    {
        Map<String, KnownIssueIdentifier> identifierMap = new HashMap<>();
        identifierMap.put(FIRST_ISSUE, createIdentifier(PATTERN, STEP));
        identifierMap.put(SECOND_ISSUE, createIdentifier());
        identifierMap.put(THIRD_ISSUE, createIdentifierWithTestCasePattern(PATTERN, STEP));
        testGetKnownIssueFromSeveralCandidates(identifierMap, false);
    }

    private void testGetKnownIssueFromSeveralCandidates(Map<String, KnownIssueIdentifier> identifierMap,
            boolean potentiallyKnown)
    {
        when(testInfoProvider.getTestInfo()).thenReturn(createTestInfo(new LinkedList<>(List.of(STEP)), CASE, SUITE));
        doReturn(identifierMap).when(knownIssueProvider).getKnownIssueIdentifiers();
        assertKnownIssue(potentiallyKnown, SECOND_ISSUE);
    }

    private void mockKnownIssueIdentifiers()
    {
        Map<String, KnownIssueIdentifier> identifierMap = new HashMap<>();
        identifierMap.put(TEXT, createIdentifier());
        doReturn(identifierMap).when(knownIssueProvider).getKnownIssueIdentifiers();
    }

    private KnownIssueIdentifier createIdentifier()
    {
        return createIdentifierWithTestSuitePattern(PATTERN, STEP, SUITE);
    }

    private KnownIssueIdentifier createIdentifier(String assertionPattern, String testStep)
    {
        KnownIssueIdentifier identifier = new KnownIssueIdentifier();
        identifier.setAssertionPattern(assertionPattern);
        identifier.setType(KnownIssueType.AUTOMATION);
        identifier.setTestStepPattern(testStep);
        identifier.setDescription(DESCRIPTION);
        return identifier;
    }

    private KnownIssueIdentifier createIdentifierWithTestCasePattern(String assertionPattern, String testStep)
    {
        KnownIssueIdentifier identifier = createIdentifier(assertionPattern, testStep);
        identifier.setTestCasePattern(CASE);
        return identifier;
    }

    private KnownIssueIdentifier createIdentifierWithTestSuitePattern(String assertionPattern, String testStep,
            String testSuite)
    {
        KnownIssueIdentifier identifier = createIdentifierWithTestCasePattern(assertionPattern, testStep);
        identifier.setTestSuitePattern(testSuite);
        return identifier;
    }

    private TestInfo createTestInfo(Deque<String> testSteps, String testCase, String testSuite)
    {
        TestInfo testInfo = new TestInfo();
        testInfo.setTestSteps(testSteps);
        testInfo.setTestCase(testCase);
        testInfo.setTestSuite(testSuite);
        return testInfo;
    }

    private void assertKnownIssue(boolean isPotentiallyKnown, String identifier)
    {
        assertKnownIssue(isPotentiallyKnown, identifier, Optional.empty());
    }

    private void assertKnownIssue(boolean isPotentiallyKnown, String identifier, Optional<String> status)
    {
        assertKnownIssue(isPotentiallyKnown, identifier, status, Optional.empty());
    }

    private void assertKnownIssue(boolean isPotentiallyKnown, String identifier, Optional<String> status,
            Optional<String> resolution)
    {
        KnownIssue actual = knownIssueChecker.getKnownIssue(PATTERN);
        assertEquals(isPotentiallyKnown, actual.isPotentiallyKnown());
        assertEquals(identifier, actual.getIdentifier());
        assertEquals(KnownIssueType.AUTOMATION, actual.getType());
        assertEquals(status, actual.getStatus());
        assertEquals(resolution, actual.getResolution());
        assertEquals(Optional.of(DESCRIPTION), actual.getDescription());
        assertFalse(actual.isFailTestCaseFast());
        assertFalse(actual.isFailTestSuiteFast());
    }
}
