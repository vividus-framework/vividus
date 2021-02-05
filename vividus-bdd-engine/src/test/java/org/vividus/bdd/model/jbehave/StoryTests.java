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

package org.vividus.bdd.model.jbehave;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableBiConsumer;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.reporters.JsonOutput;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.Timing;
import org.junit.jupiter.api.Test;

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
            reportStep(reporter, Stage.BEFORE);
            reportStep(reporter, null);
            reportStep(reporter, Stage.AFTER);
            reporter.afterScenario(mock(Timing.class));
            reporter.afterScenarios();
            reporter.afterStory(false);

            Story story = MAPPER.readValue(out.toString(StandardCharsets.UTF_8), Story.class);
            assertNull(story.getLifecycle());
            assertEquals(STORY_PATH, story.getPath());
            Scenario scenario = ensureSingleElement(story.getScenarios());
            assertEquals(SCENARIO_TITLE, scenario.getTitle());
            assertNull(scenario.getExamples());
            verifyMeta(ensureSingleElement(scenario.getMeta()), META_KEY, META_VALUE);
            verifyStep(ensureSingleElement(scenario.getBeforeScenarioSteps()), COMMENT, STEP);
            verifyStep(ensureSingleElement(scenario.getSteps()), COMMENT, STEP);
            verifyStep(ensureSingleElement(scenario.getAfterScenarioSteps()), COMMENT, STEP);
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
            reporter.lifecyle(jbehaveLifecycle);
            reporter.beforeScenarios();
            reporter.beforeScenario(TEST_SCENARIO);
            reporter.beforeExamples(List.of(STEP), table);
            reporter.example(table.getRow(0), -1);
            reportStep(reporter, Stage.BEFORE);
            reportStep(reporter, null);
            reportStep(reporter, Stage.AFTER);
            reporter.afterExamples();
            reporter.afterScenario(mock(Timing.class));
            reporter.afterScenarios();
            reporter.afterStory(false);

            Story story = MAPPER.readValue(out.toString(StandardCharsets.UTF_8), Story.class);
            assertEquals(STORY_PATH, story.getPath());
            Lifecycle lifecycle = story.getLifecycle();
            verifyParameters(lifecycle.getParameters());
            Scenario scenario = ensureSingleElement(story.getScenarios());
            assertEquals(SCENARIO_TITLE, scenario.getTitle());
            assertNull(scenario.getBeforeScenarioSteps());
            assertNull(scenario.getSteps());
            assertNull(scenario.getAfterScenarioSteps());
            verifyMeta(ensureSingleElement(scenario.getMeta()), META_KEY, META_VALUE);
            Examples examples = scenario.getExamples();
            verifyParameters(examples.getParameters());
            assertNotNull(examples);
            Example example = ensureSingleElement(examples.getExamples());
            verifyStep(ensureSingleElement(example.getBeforeScenarioSteps()), COMMENT, STEP);
            verifyStep(ensureSingleElement(example.getSteps()), COMMENT, STEP);
            verifyStep(ensureSingleElement(example.getAfterScenarioSteps()), COMMENT, STEP);
        });
    }

    private static void reportStep(StoryReporter reporter, Stage stage)
    {
        reporter.beforeScenarioSteps(stage);
        reporter.beforeStep(STEP);
        reporter.comment(STEP);
        reporter.afterScenarioSteps(stage);
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
}
