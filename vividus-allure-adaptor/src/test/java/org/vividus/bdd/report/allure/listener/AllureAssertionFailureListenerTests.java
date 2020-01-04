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

package org.vividus.bdd.report.allure.listener;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.report.allure.IAllureStepReporter;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.softassert.model.SoftAssertionError;

import io.qameta.allure.model.Status;

@ExtendWith(MockitoExtension.class)
class AllureAssertionFailureListenerTests
{
    @Mock
    private IAllureStepReporter allureStepReporter;

    @Mock
    private AssertionFailedEvent assertionFailedEvent;

    @InjectMocks
    private AllureAssertionFailureListener listener;

    @Test
    void testOnAssertionFailure()
    {
        mockSoftAssertionError(null);
        listener.onAssertionFailure(assertionFailedEvent);
        verify(allureStepReporter).updateStepStatus(Status.FAILED);
    }

    @Test
    void testOnAssertionFailureKnownIssue()
    {
        mockSoftAssertionError(new KnownIssue(null, null, false));
        listener.onAssertionFailure(assertionFailedEvent);
        verify(allureStepReporter).updateStepStatus(null);
    }

    @Test
    void testOnAssertionFailureKnownIssueFixed()
    {
        KnownIssue knownIssue = new KnownIssue(null, null, false);
        knownIssue.setStatus("Closed");
        knownIssue.setResolution("Fixed");
        mockSoftAssertionError(knownIssue);
        listener.onAssertionFailure(assertionFailedEvent);
        verify(allureStepReporter).updateStepStatus(Status.FAILED);
    }

    private void mockSoftAssertionError(KnownIssue knownIssue)
    {
        SoftAssertionError softAssertionError = new SoftAssertionError(null);
        softAssertionError.setKnownIssue(knownIssue);
        when(assertionFailedEvent.getSoftAssertionError()).thenReturn(softAssertionError);
    }
}
