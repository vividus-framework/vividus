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

package org.vividus.issue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.softassert.issue.TestInfo;

@ExtendWith(MockitoExtension.class)
class TestInfoProviderTests
{
    private static final String TEST_SUITE = "suite";
    private static final String TEST_STEP = "step";

    @Mock
    private RunContext runContext;

    @InjectMocks
    private TestInfoProvider testInfoProvider;

    @Test
    void testGetTestInfo()
    {
        String testCase = "case";
        Scenario scenario = new Scenario(testCase, new ArrayList<>());
        RunningScenario runningScenario = new RunningScenario();
        runningScenario.setScenario(scenario);
        RunningStory runningStory = prepareGetRunningStory();
        runningStory.setRunningScenario(runningScenario);
        String testStep2 = "step2";
        runningStory.setRunningSteps(new LinkedList<>(List.of(TEST_STEP, testStep2)));
        assertEqualsTestInfo(TEST_SUITE, testCase, List.of(TEST_STEP, testStep2), testInfoProvider.getTestInfo());
    }

    @Test
    void testGetTestInfoRunningStoryIsNull()
    {
        when(runContext.getRunningStory()).thenReturn(null);
        assertEqualsTestInfo(null, null, null, testInfoProvider.getTestInfo());
    }

    @Test
    void testGetTestInfoRunningScenarioIsNull()
    {
        prepareGetRunningStory();
        assertEqualsTestInfo(TEST_SUITE, null, List.of(), testInfoProvider.getTestInfo());
    }

    @Test
    void testGetTestInfoScenarioIsNull()
    {
        RunningStory runningStory = prepareGetRunningStory();
        runningStory.setRunningScenario(new RunningScenario());
        runningStory.putRunningStep(TEST_STEP);
        assertEqualsTestInfo(TEST_SUITE, null, List.of(TEST_STEP), testInfoProvider.getTestInfo());
    }

    private void assertEqualsTestInfo(String expectedSuite, String expectedCase,
            List<String> expectedSteps, TestInfo actual)
    {
        assertEquals(expectedSuite, actual.getTestSuite());
        assertEquals(expectedCase, actual.getTestCase());
        assertEquals(expectedSteps, actual.getTestSteps());
    }

    private RunningStory prepareGetRunningStory()
    {
        RunningStory runningStory = new RunningStory();
        Story story = new Story();
        story.namedAs(TEST_SUITE);
        runningStory.setStory(story);
        when(runContext.getRunningStory()).thenReturn(runningStory);
        return runningStory;
    }
}
