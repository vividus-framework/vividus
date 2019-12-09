/*
 * Copyright 2019 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.model.KnownIssue;

@ExtendWith(MockitoExtension.class)
class KnownIssueCheckerTests
{
    private static final String EXAMPLE_COM = ".*example.com";
    private static final String CURRENT_PAGE_URL = "currentPageUrl";
    private static final String TEXT = "text";
    private static final String PATTERN = "pattern";
    private static final String CASE = "case";
    private static final String STEP = "step";
    private static final String SUITE = "suite";
    private static final String CLOSED = "closed";
    private static final String FIRST_ISSUE = "UNIT-1";
    private static final String SECOND_ISSUE = "UNIT-2";
    private static final String THIRD_ISSUE = "UNIT-3";
    private static final String NOT_MATCHING_ASSERTION = "not matching assertion";
    private static final String NOT_MATCHING_SUITE = "not matching suite";
    private static final String NOT_MATCHING_STEP = "not matching step";

    @Mock
    private IKnownIssueProvider knownIssueProvider;

    @Mock
    private IIssueStateProvider issueStateProvider;

    @Mock
    private ITestInfoProvider testInfoProvider;

    @InjectMocks
    private KnownIssueChecker knownIssueChecker;

    static Stream<Arguments> getKnownIssueWithNotNullTestInfoDataProvider()
    {
        //@formatter:off
        return Stream.of(
            Arguments.of(STEP, CASE, SUITE, false),
            Arguments.of(STEP, TEXT, SUITE, true),
            Arguments.of(null, CASE, null,  false),
            Arguments.of(null, TEXT, null,  true),
            Arguments.of(TEXT, CASE, SUITE, true),
            Arguments.of(STEP, null, null,  false),
            Arguments.of(TEXT, null, null,  true),
            Arguments.of(STEP, CASE, TEXT,  true),
            Arguments.of(null, null, SUITE, false),
            Arguments.of(null, null, TEXT,  true),
            Arguments.of(null, null, null,  false)
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
        testGetKnownIssue(null);
    }

    private void testGetKnownIssue(String status)
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
    void testGetKnownIssueIfPotentiallyKnownIsFalse(String testStep, String testCase, String testSuite,
            boolean isPotentiallyKnown)
    {
        mockKnownIssueIdentifiers();
        when(testInfoProvider.getTestInfo()).thenReturn(createTestInfo(testStep, testCase, testSuite));
        assertKnownIssue(isPotentiallyKnown, TEXT);
    }

    @Test
    void testGetKnownIssueWithResolution()
    {
        mockKnownIssueIdentifiers();
        String resolution = "resolution";
        when(issueStateProvider.getIssueStatus(TEXT)).thenReturn(CLOSED);
        when(issueStateProvider.getIssueResolution(TEXT)).thenReturn(resolution);
        assertKnownIssue(false, TEXT, CLOSED, resolution);
    }

    @Test
    void testGetKnownIssueWithDynamicDataProvider()
    {
        KnownIssueIdentifier identifier = createIdentifier();
        identifier.setDynamicPatterns(Collections.singletonMap(CURRENT_PAGE_URL, EXAMPLE_COM));
        Mockito.doReturn(Collections.singletonMap(TEXT, identifier)).when(knownIssueProvider)
            .getKnownIssueIdentifiers();
        IKnownIssueDataProvider dataProvider = mock(IKnownIssueDataProvider.class);
        Map<String, IKnownIssueDataProvider> providersMap = Collections.singletonMap(CURRENT_PAGE_URL, dataProvider);
        when(dataProvider.getData()).thenReturn(Optional.of("http://example.com"));
        knownIssueChecker.setKnownIssueDataProviders(providersMap);

        assertKnownIssue(false, TEXT);
    }

    @Test
    void testGetKnownIssueWithDynamicDataProviderMismatchedData()
    {
        KnownIssueIdentifier identifier = createIdentifier();
        identifier.setDynamicPatterns(Collections.singletonMap(CURRENT_PAGE_URL, EXAMPLE_COM));
        Mockito.doReturn(Collections.singletonMap(TEXT, identifier)).when(knownIssueProvider)
            .getKnownIssueIdentifiers();
        IKnownIssueDataProvider dataProvider = mock(IKnownIssueDataProvider.class);
        Map<String, IKnownIssueDataProvider> providersMap = Collections.singletonMap(CURRENT_PAGE_URL, dataProvider);
        when(dataProvider.getData()).thenReturn(Optional.of("http://different.com"));
        knownIssueChecker.setKnownIssueDataProviders(providersMap);

        assertNull(knownIssueChecker.getKnownIssue(PATTERN));
    }

    @Test
    void testGetKnownIssueWithDynamicDataProviderReturnEmpty()
    {
        KnownIssueIdentifier identifier = createIdentifier();
        identifier.setDynamicPatterns(Collections.singletonMap(CURRENT_PAGE_URL, EXAMPLE_COM));
        Mockito.doReturn(Collections.singletonMap(TEXT, identifier)).when(knownIssueProvider)
                .getKnownIssueIdentifiers();
        IKnownIssueDataProvider dataProvider = mock(IKnownIssueDataProvider.class);
        Map<String, IKnownIssueDataProvider> providersMap = Collections.singletonMap(CURRENT_PAGE_URL, dataProvider);
        when(dataProvider.getData()).thenReturn(Optional.empty());
        knownIssueChecker.setKnownIssueDataProviders(providersMap);

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
        Map<String, KnownIssueIdentifier> identifierMap = new HashMap<>();
        identifierMap.put(FIRST_ISSUE, createIdentifierWithTestSuitePattern(NOT_MATCHING_ASSERTION, STEP, SUITE));
        identifierMap.put(SECOND_ISSUE, createIdentifierWithTestSuitePattern(PATTERN, NOT_MATCHING_STEP, SUITE));
        identifierMap.put(THIRD_ISSUE, createIdentifierWithTestSuitePattern(PATTERN, STEP, NOT_MATCHING_SUITE));
        testGetKnownIssueFromSeveralCandidates(identifierMap, true);
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
        when(testInfoProvider.getTestInfo()).thenReturn(createTestInfo(STEP, CASE, SUITE));
        Mockito.doReturn(identifierMap).when(knownIssueProvider).getKnownIssueIdentifiers();
        assertKnownIssue(potentiallyKnown, SECOND_ISSUE);
    }

    private void mockKnownIssueIdentifiers()
    {
        Map<String, KnownIssueIdentifier> identifierMap = new HashMap<>();
        identifierMap.put(TEXT, createIdentifier());
        Mockito.doReturn(identifierMap).when(knownIssueProvider).getKnownIssueIdentifiers();
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

    private TestInfo createTestInfo(String testStep, String testCase, String testSuite)
    {
        TestInfo testInfo = new TestInfo();
        testInfo.setTestStep(testStep);
        testInfo.setTestCase(testCase);
        testInfo.setTestSuite(testSuite);
        return testInfo;
    }

    private void assertKnownIssue(boolean isPotentiallyKnown, String identifier)
    {
        assertKnownIssue(isPotentiallyKnown, identifier, null);
    }

    private void assertKnownIssue(boolean isPotentiallyKnown, String identifier, String status)
    {
        assertKnownIssue(isPotentiallyKnown, identifier, status, null);
    }

    private void assertKnownIssue(boolean isPotentiallyKnown, String identifier, String status, String resolution)
    {
        KnownIssue actual = knownIssueChecker.getKnownIssue(PATTERN);
        assertEquals(isPotentiallyKnown, actual.isPotentiallyKnown());
        assertEquals(identifier, actual.getIdentifier());
        assertEquals(KnownIssueType.AUTOMATION, actual.getType());
        assertEquals(status, actual.getStatus());
        assertEquals(resolution, actual.getResolution());
    }
}
