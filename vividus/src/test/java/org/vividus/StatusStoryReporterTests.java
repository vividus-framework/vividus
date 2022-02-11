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

package org.vividus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.StepCollector.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.exception.VerificationError;
import org.vividus.softassert.issue.KnownIssueIdentifier;
import org.vividus.softassert.issue.KnownIssueType;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.softassert.model.SoftAssertionError;

@ExtendWith(MockitoExtension.class)
class StatusStoryReporterTests
{
    @Mock private StoryReporter nextStoryReporter;
    @InjectMocks private StatusStoryReporter statusStoryReporter;

    @BeforeEach
    void beforeEach()
    {
        statusStoryReporter.setNext(nextStoryReporter);
    }

    @Test
    void testScenarioExcluded()
    {
        statusStoryReporter.scenarioExcluded(null, null);
        assertEquals(Optional.of(Status.SKIPPED), statusStoryReporter.getRunStatus());
        verify(nextStoryReporter).scenarioExcluded(null, null);
    }

    @Test
    void testIgnorable()
    {
        statusStoryReporter.ignorable(null);
        assertEquals(Optional.of(Status.SKIPPED), statusStoryReporter.getRunStatus());
        verify(nextStoryReporter).ignorable(null);
    }

    @Test
    void testPending()
    {
        statusStoryReporter.pending(null);
        assertEquals(Optional.of(Status.PENDING), statusStoryReporter.getRunStatus());
        verify(nextStoryReporter).pending(null);
    }

    @Test
    void testNotPerformed()
    {
        statusStoryReporter.notPerformed(null);
        assertEquals(Optional.of(Status.SKIPPED), statusStoryReporter.getRunStatus());
        verify(nextStoryReporter).notPerformed(null);
    }

    @Test
    void testBroken()
    {
        IOException throwable = new IOException();
        statusStoryReporter.failed(null, throwable);
        assertEquals(Optional.of(Status.BROKEN), statusStoryReporter.getRunStatus());
        verify(nextStoryReporter).failed(null, throwable);
    }

    @Test
    void testFailedAssertionError()
    {
        UUIDExceptionWrapper throwable = new UUIDExceptionWrapper(new AssertionError());
        statusStoryReporter.failed(null, throwable);
        assertEquals(Optional.of(Status.FAILED), statusStoryReporter.getRunStatus());
        verify(nextStoryReporter).failed(null, throwable);
    }

    @Test
    void testFailedVerificationError()
    {
        VerificationError verificationError = new VerificationError(null, List.of(new SoftAssertionError(null)));
        UUIDExceptionWrapper throwable = new UUIDExceptionWrapper(verificationError);
        statusStoryReporter.failed(null, throwable);
        assertEquals(Optional.of(Status.FAILED), statusStoryReporter.getRunStatus());
        verify(nextStoryReporter).failed(null, throwable);
    }

    @Test
    void testKnownIssuesOnly()
    {
        SoftAssertionError softAssertionError = new SoftAssertionError(null);
        softAssertionError.setKnownIssue(createKnownIssue(false));
        VerificationError verificationError = new VerificationError(null, List.of(softAssertionError));
        UUIDExceptionWrapper throwable = new UUIDExceptionWrapper(verificationError);
        statusStoryReporter.failed(null, throwable);
        assertEquals(Optional.of(Status.KNOWN_ISSUES_ONLY), statusStoryReporter.getRunStatus());
        verify(nextStoryReporter).failed(null, throwable);
    }

    @Test
    void testFailedWithPotentiallyKnownIssue()
    {
        SoftAssertionError softAssertionError = new SoftAssertionError(null);
        softAssertionError.setKnownIssue(createKnownIssue(true));
        VerificationError verificationError = new VerificationError(null, List.of(softAssertionError));
        UUIDExceptionWrapper throwable = new UUIDExceptionWrapper(verificationError);
        statusStoryReporter.failed(null, throwable);
        assertEquals(Optional.of(Status.FAILED), statusStoryReporter.getRunStatus());
        verify(nextStoryReporter).failed(null, throwable);
    }

    @Test
    void testSuccessful()
    {
        statusStoryReporter.successful(null);
        assertEquals(Optional.of(Status.PASSED), statusStoryReporter.getRunStatus());
        verify(nextStoryReporter).successful(null);
    }

    @Test
    void shouldSkipReportingForBeforeOrAfterStoriesSteps()
    {
        statusStoryReporter.beforeStoriesSteps(Stage.BEFORE);
        statusStoryReporter.successful(null);
        statusStoryReporter.afterStoriesSteps(Stage.BEFORE);
        assertEquals(Optional.empty(), statusStoryReporter.getRunStatus());
        InOrder ordered = inOrder(nextStoryReporter);
        ordered.verify(nextStoryReporter).beforeStoriesSteps(Stage.BEFORE);
        ordered.verify(nextStoryReporter).successful(null);
        ordered.verify(nextStoryReporter).afterStoriesSteps(Stage.BEFORE);
    }

    @Test
    void shouldEnableReportingBackAfterBeforeOrAfterStoriesSteps()
    {
        statusStoryReporter.beforeStoriesSteps(Stage.AFTER);
        statusStoryReporter.afterStoriesSteps(Stage.AFTER);
        statusStoryReporter.successful(null);
        assertEquals(Optional.of(Status.PASSED), statusStoryReporter.getRunStatus());
        verify(nextStoryReporter).successful(null);
        InOrder ordered = inOrder(nextStoryReporter);
        ordered.verify(nextStoryReporter).beforeStoriesSteps(Stage.AFTER);
        ordered.verify(nextStoryReporter).afterStoriesSteps(Stage.AFTER);
        ordered.verify(nextStoryReporter).successful(null);
    }

    @Test
    void testStatusPriorities()
    {
        statusStoryReporter.successful(null);
        statusStoryReporter.failed(null, null);
        statusStoryReporter.successful(null);
        assertEquals(Optional.of(Status.BROKEN), statusStoryReporter.getRunStatus());
        verify(nextStoryReporter).failed(null, null);
    }

    @Test
    void shouldSwitchKnownIssuesOnlyToPending()
    {
        SoftAssertionError softAssertionError = new SoftAssertionError(null);
        softAssertionError.setKnownIssue(createKnownIssue(false));
        VerificationError verificationError = new VerificationError(null, List.of(softAssertionError));
        UUIDExceptionWrapper throwable = new UUIDExceptionWrapper(verificationError);
        statusStoryReporter.failed(null, throwable);
        assertEquals(Optional.of(Status.KNOWN_ISSUES_ONLY), statusStoryReporter.getRunStatus());
        statusStoryReporter.pending(null);
        assertEquals(Optional.of(Status.PENDING), statusStoryReporter.getRunStatus());
    }

    private KnownIssue createKnownIssue(boolean potentiallyKnown)
    {
        KnownIssueIdentifier identifier = new KnownIssueIdentifier();
        identifier.setType(KnownIssueType.AUTOMATION);
        return new KnownIssue(null, identifier, potentiallyKnown);
    }
}
