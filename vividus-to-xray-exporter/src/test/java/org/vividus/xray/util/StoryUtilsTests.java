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

package org.vividus.xray.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.vividus.bdd.model.jbehave.Parameters;
import org.vividus.bdd.model.jbehave.Scenario;
import org.vividus.bdd.model.jbehave.Story;
import org.vividus.util.ResourceUtils;

class StoryUtilsTests
{
    private static final String LIFECYCLE_KEY = "lifecycle-key-";
    private static final String LIFECYCLE_VALUE = "lifecycle-val-";
    private static final String SCENARIO_KEY = "scenario-key-";
    private static final String SCENARIO_VALUE = "scenario-val-";

    private static final List<Scenario> SCENARIOS = StoryUtils.getFoldedScenarios(readStory("story.json"));

    @Test
    void shouldAppendLifecycleExamplesToScenarioWithoutLocalExamples() throws IOException
    {
        Parameters params = SCENARIOS.get(0).getExamples().getParameters();
        assertEquals(List.of(LIFECYCLE_KEY + 1, LIFECYCLE_KEY + 2), params.getNames());
        assertEquals(List.of(
            List.of(LIFECYCLE_VALUE + 11, LIFECYCLE_VALUE + 21),
            List.of(LIFECYCLE_VALUE + 12, LIFECYCLE_VALUE + 22)
        ), params.getValues());
    }

    @Test
    void shouldAppendLifecycleExamplesToScenarioWithSingleLocalExamples() throws IOException
    {
        Parameters params = SCENARIOS.get(1).getExamples().getParameters();
        assertEquals(List.of(
            SCENARIO_KEY + 1, SCENARIO_KEY + 2, LIFECYCLE_KEY + 1, LIFECYCLE_KEY + 2
        ), params.getNames());
        assertEquals(List.of(
            List.of(SCENARIO_VALUE + 11, SCENARIO_VALUE + 21, LIFECYCLE_VALUE + 11, LIFECYCLE_VALUE + 21),
            List.of(SCENARIO_VALUE + 11, SCENARIO_VALUE + 21, LIFECYCLE_VALUE + 12, LIFECYCLE_VALUE + 22)
        ), params.getValues());
    }

    @Test
    void shouldAppendLifecycleExamplesToScenarioWithMultipleLocalExamples() throws IOException
    {
        Parameters params = SCENARIOS.get(2).getExamples().getParameters();
        assertEquals(List.of(
            SCENARIO_KEY + 1, SCENARIO_KEY + 2, LIFECYCLE_KEY + 1, LIFECYCLE_KEY + 2
        ), params.getNames());
        assertEquals(List.of(
            List.of(SCENARIO_VALUE + 11, SCENARIO_VALUE + 21, LIFECYCLE_VALUE + 11, LIFECYCLE_VALUE + 21),
            List.of(SCENARIO_VALUE + 12, SCENARIO_VALUE + 22, LIFECYCLE_VALUE + 11, LIFECYCLE_VALUE + 21),
            List.of(SCENARIO_VALUE + 13, SCENARIO_VALUE + 23, LIFECYCLE_VALUE + 11, LIFECYCLE_VALUE + 21),
            List.of(SCENARIO_VALUE + 11, SCENARIO_VALUE + 21, LIFECYCLE_VALUE + 12, LIFECYCLE_VALUE + 22),
            List.of(SCENARIO_VALUE + 12, SCENARIO_VALUE + 22, LIFECYCLE_VALUE + 12, LIFECYCLE_VALUE + 22),
            List.of(SCENARIO_VALUE + 13, SCENARIO_VALUE + 23, LIFECYCLE_VALUE + 12, LIFECYCLE_VALUE + 22)
        ), params.getValues());
    }

    @Test
    void shouldNotProcessScenarioWithoutLifecycle()
    {
        Story story = mock(Story.class);
        Scenario scenario = mock(Scenario.class);
        when(story.getScenarios()).thenReturn(List.of(scenario));
        when(story.getLifecycle()).thenReturn(null);
        assertEquals(List.of(scenario), StoryUtils.getFoldedScenarios(story));
        verifyNoInteractions(scenario);
    }

    @Test
    void shouldHandleStoryWithOneScenario()
    {
        Parameters params = StoryUtils.getFoldedScenarios(readStory("single-scenario-story.json")).get(0)
                .getExamples().getParameters();
        assertEquals(List.of(
            SCENARIO_KEY + 1, SCENARIO_KEY + 2, LIFECYCLE_KEY + 1, LIFECYCLE_KEY + 2
        ), params.getNames());
        assertEquals(List.of(
            List.of(SCENARIO_VALUE + 11, SCENARIO_VALUE + 21, LIFECYCLE_VALUE + 11, LIFECYCLE_VALUE + 21)
        ), params.getValues());
    }

    private static Story readStory(String resource)
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false);
            String json = ResourceUtils.loadResource(StoryUtilsTests.class, resource);
            return mapper.readValue(json, Story.class);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
