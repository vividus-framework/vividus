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

package org.vividus.bdd.model.jbehave;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableBiConsumer;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.reporters.JsonOutput;
import org.jbehave.core.reporters.StoryReporter;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;

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

    private static final org.jbehave.core.model.Story TEST_STORY = new org.jbehave.core.model.Story(STORY_PATH);
    private static final org.jbehave.core.model.Scenario TEST_SCENARIO = new org.jbehave.core.model.Scenario(
            SCENARIO_TITLE, new org.jbehave.core.model.Meta(List.of(META_KEY + StringUtils.SPACE + META_VALUE)));

    @Test
    void shouldDeserializeStoryWithoutExamples() throws Exception
    {
        performTest((reporter, out) ->
        {
            reporter.beforeStory(TEST_STORY, false);
            reporter.beforeScenario(TEST_SCENARIO);
            reporter.beforeScenarioSteps(null);
            reporter.beforeStep(STEP);
            reporter.comment(STEP);
            reporter.afterScenarioSteps(null);
            reporter.afterScenario();
            reporter.afterStory(false);

            Story story = MAPPER.readValue(new String(out.toByteArray(), StandardCharsets.UTF_8), Story.class);
            assertEquals(STORY_PATH, story.getPath());
            Scenario scenario = ensureSingleElement(story.getScenarios());
            assertEquals(SCENARIO_TITLE, scenario.getTitle());
            assertNull(scenario.getExamples());
            verifyMeta(ensureSingleElement(scenario.getMeta()), META_KEY, META_VALUE);
            verifyStep(ensureSingleElement(getStepsField(scenario)), COMMENT, STEP);
        });
    }

    @Test
    void shouldDeserializeStoryExamples() throws Exception
    {
        performTest((reporter, out) ->
        {
            ExamplesTable table = new ExamplesTable("|key|\n|value|");
            Lifecycle lifecycle = new Lifecycle(table);

            reporter.beforeStory(TEST_STORY, false);
            reporter.lifecyle(lifecycle);
            reporter.beforeScenario(TEST_SCENARIO);
            reporter.beforeExamples(List.of(STEP), ExamplesTable.EMPTY);
            reporter.example(table.getRow(0), -1);
            reporter.beforeScenarioSteps(null);
            reporter.beforeStep(STEP);
            reporter.comment(STEP);
            reporter.afterScenarioSteps(null);
            reporter.afterExamples();
            reporter.afterScenario();
            reporter.afterStory(false);

            Story story = MAPPER.readValue(new String(out.toByteArray(), StandardCharsets.UTF_8), Story.class);
            assertEquals(STORY_PATH, story.getPath());
            Scenario scenario = ensureSingleElement(story.getScenarios());
            assertEquals(SCENARIO_TITLE, scenario.getTitle());
            assertNull(getStepsField(scenario));
            verifyMeta(ensureSingleElement(scenario.getMeta()), META_KEY, META_VALUE);
            Examples examples = scenario.getExamples();
            assertNotNull(examples);
            Example example = ensureSingleElement(examples.getExamples());
            verifyStep(ensureSingleElement(example.getSteps()), COMMENT, STEP);
        });
    }

    @SuppressWarnings("unchecked")
    private static List<Step> getStepsField(Object target) throws Exception
    {
        Field field = ReflectionUtils
                .findFields(target.getClass(), f -> "steps".equals(f.getName()), HierarchyTraversalMode.TOP_DOWN)
                .get(0);
        ReflectionUtils.makeAccessible(field);
        return (List<Step>) ReflectionUtils.tryToReadFieldValue(field, target).get();
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
