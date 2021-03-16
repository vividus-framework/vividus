/*
 * Copyright 2021 the original author or authors.
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

package org.vividus.bdd.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FailureTests
{
    private static final String STEP = "Given I am on the main application page";
    private static final String SCENARIO = "Verify main page";
    private static final String STORY = "Sample";
    private static final String MESSAGE = "message";
    @Mock private RunningStory runningStory;

    @Test
    void shouldCreateFailure()
    {
        mockFailure(STORY, SCENARIO, STEP);
        Failure failure = Failure.from(runningStory, MESSAGE);
        Assertions.assertAll(
                () -> assertEquals(STORY, failure.getStory()),
                () -> assertEquals(SCENARIO, failure.getScenario()),
                () -> assertEquals(STEP, failure.getStep()),
                () -> assertEquals(MESSAGE, failure.getMessage()));
    }

    @Test
    void shouldCreateFailureWithAnEmptyScenarioTitleIfNoScenarioIsRunning()
    {
        RunningScenario runningScenario = mock(RunningScenario.class);
        when(runningStory.getRunningScenario()).thenReturn(runningScenario);
        when(runningStory.getName()).thenReturn(STORY);
        when(runningStory.getRunningSteps()).thenReturn(new LinkedList<>(List.of(STEP)));
        Failure failure = Failure.from(runningStory, MESSAGE);
        Assertions.assertAll(
                () -> assertEquals(STORY, failure.getStory()),
                () -> assertEquals("", failure.getScenario()),
                () -> assertEquals(STEP, failure.getStep()),
                () -> assertEquals(MESSAGE, failure.getMessage()));
    }

    private void mockFailure(String storyName, String scenatioTitle, String step)
    {
        RunningScenario runningScenario = mock(RunningScenario.class);
        when(runningStory.getRunningScenario()).thenReturn(runningScenario);
        when(runningScenario.getTitle()).thenReturn(scenatioTitle);
        when(runningStory.getName()).thenReturn(storyName);
        when(runningStory.getRunningSteps()).thenReturn(new LinkedList<>(List.of(step, "other")));
    }
}
