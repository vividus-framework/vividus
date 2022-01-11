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

package org.vividus.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.StepCandidate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction.Context;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.IPathFinder;
import org.vividus.PathFinder;
import org.vividus.SystemStreamTests;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;
import org.vividus.resource.StoryLoader;
import org.vividus.spring.ExtendedConfiguration;

@ExtendWith(MockitoExtension.class)
class BddStepsCounterTests extends SystemStreamTests
{
    private static final String STEP_PATTERN = "%s I do something with '%s'";
    private static final String AND = "And";
    private static final String WHEN = "When";
    private static final String THEN = "Then";
    private static final String VALUE = "amazing/value";
    private static final String VARIABLE = "$variable";
    private static final String WHEN_STEP = String.format(STEP_PATTERN, WHEN, VALUE);
    private static final String THEN_STEP = String.format(STEP_PATTERN, THEN, VALUE);
    private static final String AND_STEP = String.format(STEP_PATTERN, AND, VALUE);
    private static final String CANDIDATE_STRING = String.format(STEP_PATTERN, "", VARIABLE).trim();
    private static final String TOP_STEPS = "Top of the most used steps:";
    private static final String OCCURRENCES = "occurrence(s)";
    private static final String NO_MATCHED_STEPS = "Matched steps haven't been found";
    private static final String NO_STEP_CANDIDATES = "\nUnable to find StepCandidate(s) for following step(s):";
    private static final String COMMENT = "!--";
    private static final String DIR_OPT_NAME = "--dir";
    private static final String DIR_VALUE = "/story/regression";
    private static final String DEFAULT_STORY_LOCATION = "story";
    private static final String TWO_OCCURRENCES = "2";
    private static final String ONE_OCCURRENCE = "1";

    @Mock
    private StepCandidate stepCandidate;

    @Test
    void testNoMatchedSteps() throws IOException, ParseException
    {
        testCounter(new String[0], DEFAULT_STORY_LOCATION, List.of(WHEN_STEP), List.of(stepCandidate));
        String output = getOutStreamContent();
        assertTrue(output.contains(NO_MATCHED_STEPS));
        assertTrue(output.contains(NO_STEP_CANDIDATES));
        assertTrue(output.contains(WHEN_STEP));
        assertFalse(output.contains(TOP_STEPS));
        assertFalse(output.contains(TOP_STEPS));
    }

    @Test
    void testCommentedSteps() throws IOException, ParseException
    {
        testCounter(new String[0], DEFAULT_STORY_LOCATION, List.of(COMMENT + WHEN_STEP), List.of(stepCandidate));
        String output = getOutStreamContent();
        assertTrue(output.contains(NO_MATCHED_STEPS));
        assertFalse(output.contains(NO_STEP_CANDIDATES));
    }

    @Test
    void testDirectoryOptionIsPresent() throws IOException, ParseException
    {
        testCounter(new String[] { DIR_OPT_NAME, DIR_VALUE }, DIR_VALUE, List.of(), List.of());
    }

    @Test
    void testUnknownOptionIsPresent()
    {
        try (MockedStatic<Vividus> vividus = mockStatic(Vividus.class))
        {
            assertThrows(UnrecognizedOptionException.class,
                    () -> BddStepsCounter.main(new String[] { "--any", DIR_VALUE }));
            vividus.verify(Vividus::init);
        }
    }

    @Test
    void testLimitOptionIsPresent() throws IOException, ParseException
    {
        when(stepCandidate.matches(THEN_STEP, null)).thenReturn(true);
        when(stepCandidate.matches(WHEN_STEP, null)).thenReturn(true);
        when(stepCandidate.getStartingWord()).thenReturn(THEN).thenReturn(THEN).thenReturn(WHEN);
        when(stepCandidate.getPatternAsString()).thenReturn(CANDIDATE_STRING);
        testCounter(new String[] { "--top", ONE_OCCURRENCE }, DEFAULT_STORY_LOCATION,
                List.of(THEN_STEP, THEN_STEP, WHEN_STEP), List.of(stepCandidate));
        String output = getOutStreamContent();
        assertTrue(output.contains(String.format(STEP_PATTERN, THEN, VARIABLE)));
        assertFalse(output.contains(String.format(STEP_PATTERN, WHEN, VARIABLE)));
        assertTrue(output.contains(TWO_OCCURRENCES));
        assertFalse(output.contains(ONE_OCCURRENCE));
    }

    @Test
    void testAndStep() throws IOException, ParseException
    {
        when(stepCandidate.matches(WHEN_STEP, null)).thenReturn(true);
        when(stepCandidate.matches(AND_STEP, WHEN_STEP)).thenReturn(true);
        when(stepCandidate.getStartingWord()).thenReturn(WHEN);
        when(stepCandidate.getPatternAsString()).thenReturn(CANDIDATE_STRING);
        testCounter(new String[0], DEFAULT_STORY_LOCATION, List.of(WHEN_STEP, AND_STEP), List.of(stepCandidate));
        String output = getOutStreamContent();
        assertTrue(output.contains(TOP_STEPS));
        assertTrue(output.contains(OCCURRENCES));
        assertTrue(output.contains(String.format(STEP_PATTERN, WHEN, VARIABLE)));
        assertFalse(output.contains(NO_STEP_CANDIDATES));
        assertTrue(output.contains(TWO_OCCURRENCES));
    }

    @SuppressWarnings("try")
    private void testCounter(String[] args, String resourceLocation, List<String> steps,
            List<StepCandidate> stepCandidates) throws IOException, ParseException
    {
        String path = "";
        Scenario scenario = new Scenario(steps);
        Story story = new Story(path, List.of(scenario));

        try (MockedStatic<BeanFactory> beanFactory = mockStatic(BeanFactory.class);
                var regexStoryParser = mockConstruction(RegexStoryParser.class, (mock, context) -> {
                    assertRegexStoryParserConstruction(context);
                    when(mock.parseStory(any(String.class))).thenReturn(story);
                }))
        {
            StoryLoader storyLoader = mock(StoryLoader.class);
            when(storyLoader.loadResourceAsText(path)).thenReturn("");
            beanFactory.when(() -> BeanFactory.getBean(StoryLoader.class)).thenReturn(storyLoader);

            PathFinder pathFinder = mock(PathFinder.class);
            when(pathFinder.findPaths(argThat(arg -> resourceLocation.equals(arg.getResourceLocation())))).thenReturn(
                    List.of(path));
            beanFactory.when(() -> BeanFactory.getBean(IPathFinder.class)).thenReturn(pathFinder);

            ExtendedConfiguration configuration = new ExtendedConfiguration();
            configuration.useKeywords(new Keywords());
            beanFactory.when(() -> BeanFactory.getBean(Configuration.class)).thenReturn(configuration);

            InjectableStepsFactory stepFactory = mock(InjectableStepsFactory.class);
            beanFactory.when(() -> BeanFactory.getBean(InjectableStepsFactory.class)).thenReturn(stepFactory);

            CandidateSteps candidateSteps = mock(CandidateSteps.class);
            when(candidateSteps.listCandidates()).thenReturn(stepCandidates);
            when(stepFactory.createCandidateSteps()).thenReturn(List.of(candidateSteps));

            BddStepsCounter.main(args);

            beanFactory.verify(BeanFactory::open);
        }
    }

    private void assertRegexStoryParserConstruction(Context context)
    {
        assertEquals(1, context.getCount());
        var constructorArguments = context.arguments();
        assertEquals(1, constructorArguments.size());
        var constructorArgument = constructorArguments.get(0);
        assertInstanceOf(ExamplesTableFactory.class, constructorArgument);
        var examplesTableFactory = (ExamplesTableFactory) constructorArgument;
        assertSame(ExamplesTable.EMPTY, examplesTableFactory.createExamplesTable("t1.table"));
        assertSame(ExamplesTable.EMPTY, examplesTableFactory.createExamplesTable("t2.table"));
    }
}
