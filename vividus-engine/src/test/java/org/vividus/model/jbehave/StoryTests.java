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

package org.vividus.model.jbehave;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableBiConsumer;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Lifecycle.ExecutionType;
import org.jbehave.core.reporters.JsonOutput;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.jbehave.core.steps.Timing;
import org.junit.jupiter.api.Test;
import org.vividus.util.ResourceUtils;

class StoryTests
{
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final String STEP = "step";
    private static final String META_KEY = "metaKey";
    private static final String META_VALUE = "metaValue";
    private static final String STORY_PATH = "storyPath";
    private static final String SCENARIO_TITLE = "scenarioTitle";
    private static final String COMMENT = "comment";
    private static final String TABLE_VALUE = "table-value-";
    private static final String LIFECYCLE_KEY = "lifecycle-key-";
    private static final String LIFECYCLE_VALUE = "lifecycle-val-";
    private static final String SCENARIO_KEY = "scenario-key-";
    private static final String SCENARIO_VALUE = "scenario-val-";
    private static final String STORY = "story.json";

    private static final org.jbehave.core.model.Story TEST_STORY = new org.jbehave.core.model.Story(STORY_PATH);
    private static final org.jbehave.core.model.Scenario TEST_SCENARIO = new org.jbehave.core.model.Scenario(
            SCENARIO_TITLE, new org.jbehave.core.model.Meta(List.of(META_KEY + StringUtils.SPACE + META_VALUE)));

    @Test
    void shouldDeserializeStoryWithoutExamples() throws Exception
    {
        performTest((reporter, out) ->
        {
            reporter.beforeStory(TEST_STORY, false);
            reporter.beforeScenarios();
            reporter.beforeScenario(TEST_SCENARIO);
            reportStep(reporter, Stage.BEFORE, ExecutionType.USER);
            reportStep(reporter, null, null);
            reportStep(reporter, Stage.AFTER, ExecutionType.USER);
            reporter.afterScenario(mockTiming());
            reporter.afterScenarios();
            reporter.afterStory(false);

            Story story = MAPPER.readValue(out.toString(StandardCharsets.UTF_8), Story.class);
            assertNull(story.getLifecycle());
            assertEquals(STORY_PATH, story.getPath());
            Scenario scenario = ensureSingleElement(story.getScenarios());
            assertTimings(scenario);
            assertEquals(SCENARIO_TITLE, scenario.getTitle());
            assertNull(scenario.getExamples());
            verifyMeta(ensureSingleElement(scenario.getMeta()), META_KEY, META_VALUE);
            verifyStep(ensureSingleElement(scenario.getBeforeUserScenarioSteps()), COMMENT, STEP);
            verifyStep(ensureSingleElement(scenario.getSteps()), COMMENT, STEP);
            verifyStep(ensureSingleElement(scenario.getAfterUserScenarioSteps()), COMMENT, STEP);
        });
    }

    @Test
    void shouldDeserializeStoryExamples() throws Exception
    {
        performTest((reporter, out) ->
        {
            ExamplesTable table = new ExamplesTable("|table-key|\n|table-value-1|\n|table-value-2|");
            org.jbehave.core.model.Lifecycle jbehaveLifecycle = new org.jbehave.core.model.Lifecycle(table);

            reporter.beforeStory(TEST_STORY, false);
            reporter.lifecycle(jbehaveLifecycle);
            reporter.beforeScenarios();
            reporter.beforeScenario(TEST_SCENARIO);
            reporter.beforeExamples(List.of(STEP), table);
            reporter.example(table.getRow(0), -1);
            reportStep(reporter, Stage.BEFORE, ExecutionType.USER);
            reportStep(reporter, null, null);
            reportStep(reporter, Stage.AFTER, ExecutionType.USER);
            reporter.afterExamples();
            reporter.afterScenario(mockTiming());
            reporter.afterScenarios();
            reporter.afterStory(false);

            Story story = MAPPER.readValue(out.toString(StandardCharsets.UTF_8), Story.class);
            assertEquals(STORY_PATH, story.getPath());
            Lifecycle lifecycle = story.getLifecycle();
            verifyParameters(lifecycle.getParameters());
            Scenario scenario = ensureSingleElement(story.getScenarios());
            assertTimings(scenario);
            assertEquals(SCENARIO_TITLE, scenario.getTitle());
            assertNull(scenario.getBeforeUserScenarioSteps());
            assertNull(scenario.getSteps());
            assertNull(scenario.getAfterUserScenarioSteps());
            verifyMeta(ensureSingleElement(scenario.getMeta()), META_KEY, META_VALUE);
            Examples examples = scenario.getExamples();
            verifyParameters(examples.getParameters());
            assertNotNull(examples);
            Example example = ensureSingleElement(examples.getExamples());
            verifyStep(ensureSingleElement(example.getBeforeUserScenarioSteps()), COMMENT, STEP);
            verifyStep(ensureSingleElement(example.getSteps()), COMMENT, STEP);
            verifyStep(ensureSingleElement(example.getAfterUserScenarioSteps()), COMMENT, STEP);
        });
    }

    @Test
    void shouldAppendLifecycleExamplesToScenarioWithoutLocalExamples()
    {
        Parameters params = readStory(STORY).getFoldedScenarios().get(0).getExamples().getParameters();
        assertEquals(List.of(LIFECYCLE_KEY + 1, LIFECYCLE_KEY + 2), params.getNames());
        assertEquals(List.of(
            List.of(LIFECYCLE_VALUE + 11, LIFECYCLE_VALUE + 21),
            List.of(LIFECYCLE_VALUE + 12, LIFECYCLE_VALUE + 22)
        ), params.getValues());
    }

    @Test
    void shouldAppendLifecycleExamplesToScenarioWithSingleLocalExamples()
    {
        Parameters params = readStory(STORY).getFoldedScenarios().get(1).getExamples().getParameters();
        assertEquals(List.of(
            SCENARIO_KEY + 1, SCENARIO_KEY + 2, LIFECYCLE_KEY + 1, LIFECYCLE_KEY + 2
        ), params.getNames());
        assertEquals(List.of(
            List.of(SCENARIO_VALUE + 11, SCENARIO_VALUE + 21, LIFECYCLE_VALUE + 11, LIFECYCLE_VALUE + 21),
            List.of(SCENARIO_VALUE + 11, SCENARIO_VALUE + 21, LIFECYCLE_VALUE + 12, LIFECYCLE_VALUE + 22)
        ), params.getValues());
    }

    @Test
    void shouldAppendLifecycleExamplesToScenarioWithMultipleLocalExamples()
    {
        Parameters params = readStory(STORY).getFoldedScenarios().get(2).getExamples().getParameters();
        assertEquals(List.of(
            SCENARIO_KEY + 1, SCENARIO_KEY + 2, LIFECYCLE_KEY + 1, LIFECYCLE_KEY + 2
        ), params.getNames());
        assertEquals(List.of(
            List.of(SCENARIO_VALUE + 11, SCENARIO_VALUE + 21, LIFECYCLE_VALUE + 11, LIFECYCLE_VALUE + 21),
            List.of(SCENARIO_VALUE + 11, SCENARIO_VALUE + 21, LIFECYCLE_VALUE + 12, LIFECYCLE_VALUE + 22),
            List.of(SCENARIO_VALUE + 12, SCENARIO_VALUE + 22, LIFECYCLE_VALUE + 11, LIFECYCLE_VALUE + 21),
            List.of(SCENARIO_VALUE + 12, SCENARIO_VALUE + 22, LIFECYCLE_VALUE + 12, LIFECYCLE_VALUE + 22),
            List.of(SCENARIO_VALUE + 13, SCENARIO_VALUE + 23, LIFECYCLE_VALUE + 11, LIFECYCLE_VALUE + 21),
            List.of(SCENARIO_VALUE + 13, SCENARIO_VALUE + 23, LIFECYCLE_VALUE + 12, LIFECYCLE_VALUE + 22)
        ), params.getValues());
    }

    @Test
    void shouldNotProcessScenarioWithoutLifecycle()
    {
        Story story = spy(Story.class);
        Scenario scenario = mock(Scenario.class);
        story.setScenarios(List.of(scenario));
        assertEquals(List.of(scenario), story.getFoldedScenarios());
        verifyNoInteractions(scenario);
    }

    @Test
    void shouldNotProcessScenarioWithoutLifecycleParameters()
    {
        Story story = spy(Story.class);
        Scenario scenario = mock(Scenario.class);
        story.setScenarios(List.of(scenario));
        story.setLifecycle(mock(Lifecycle.class));
        assertEquals(List.of(scenario), story.getFoldedScenarios());
        verifyNoInteractions(scenario);
    }

    @Test
    void shouldHandleStoryWithOneScenario()
    {
        Parameters params = readStory("single-scenario-story.json").getFoldedScenarios().get(0).getExamples()
                .getParameters();
        assertEquals(List.of(
            SCENARIO_KEY + 1, SCENARIO_KEY + 2, LIFECYCLE_KEY + 1, LIFECYCLE_KEY + 2
        ), params.getNames());
        assertEquals(List.of(
            List.of(SCENARIO_VALUE + 11, SCENARIO_VALUE + 21, LIFECYCLE_VALUE + 11, LIFECYCLE_VALUE + 21)
        ), params.getValues());
    }

    private static void reportStep(StoryReporter reporter, Stage stage, ExecutionType type)
    {
        reporter.beforeScenarioSteps(stage, type);
        reporter.beforeStep(new org.jbehave.core.model.Step(StepExecutionType.EXECUTABLE, STEP));
        reporter.comment(STEP);
        reporter.afterScenarioSteps(stage, type);
    }

    private void verifyParameters(Parameters parameters)
    {
        assertEquals(List.of("table-key"), parameters.getNames());
        assertEquals(List.of(List.of(TABLE_VALUE + 1), List.of(TABLE_VALUE + 2)), parameters.getValues());
    }

    private static void performTest(FailableBiConsumer<StoryReporter, ByteArrayOutputStream, Exception> testables)
            throws Exception
    {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                PrintStream stream = new PrintStream(out, false, StandardCharsets.UTF_8))
        {
            StoryReporter reporter = new JsonOutput(stream, new Properties(), new LocalizedKeywords());
            testables.accept(reporter, out);
        }
    }

    private <T> T ensureSingleElement(List<T> values)
    {
        assertThat(values, hasSize(1));
        return values.get(0);
    }

    private static void verifyMeta(Meta meta, String name, String value)
    {
        assertEquals(name, meta.getName());
        assertEquals(value, meta.getValue());
    }

    private static void verifyStep(Step step, String comment, String value)
    {
        assertEquals(comment, step.getOutcome());
        assertEquals(value, step.getValue());
    }

    private static Timing mockTiming()
    {
        Timing timing = mock(Timing.class);
        when(timing.getStart()).thenReturn(1L);
        when(timing.getEnd()).thenReturn(2L);
        return timing;
    }

    private static void assertTimings(Scenario scenario)
    {
        assertEquals(1L, scenario.getStart());
        assertEquals(2L, scenario.getEnd());
    }

    private static Story readStory(String resource)
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false);
            String json = ResourceUtils.loadResource(StoryTests.class, resource);
            return mapper.readValue(json, Story.class);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
