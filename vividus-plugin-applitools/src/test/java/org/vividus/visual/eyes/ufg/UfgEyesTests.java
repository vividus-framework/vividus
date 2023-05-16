/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.visual.eyes.ufg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.applitools.eyes.EyesRunner;
import com.applitools.eyes.TestResultContainer;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.TestResultsSummary;

import org.junit.jupiter.api.Test;

class UfgEyesTests
{
    @Test
    void shouldReturnResultsForTest()
    {
        EyesRunner eyesRunner = mock(EyesRunner.class);
        TestResultsSummary testResultsSummary = mock(TestResultsSummary.class);
        when(eyesRunner.getAllTestResults(false)).thenReturn(testResultsSummary);
        TestResultContainer testResultContainer = mock(TestResultContainer.class);
        when(testResultsSummary.getAllResults()).thenReturn(new TestResultContainer[] { testResultContainer });
        TestResults testResults = mock(TestResults.class);
        when(testResultContainer.getTestResults()).thenReturn(testResults);

        UfgEyes eyes = new UfgEyes(eyesRunner);

        assertEquals(List.of(testResults), eyes.getTestResults());
    }
}
