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

package org.vividus.runner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.cli.UnrecognizedOptionException;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.StepCandidate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vividus.SystemOutTests;
import org.vividus.bdd.IPathFinder;
import org.vividus.bdd.PathFinder;
import org.vividus.bdd.StoryLoader;
import org.vividus.bdd.spring.Configuration;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Vividus.class, BeanFactory.class })
public class BddStepsCounterTests extends SystemOutTests
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
    private static final String TOP_STEPS = "Top of the most used bdd steps:";
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
    private PathFinder pathFinder;

    @Mock
    private StoryLoader storyLoader;

    @Mock
    private StoryParser storyParser;

    @Mock
    private Configuration configuration;

    @Mock
    private Story story;

    @Mock
    private Scenario scenario;

    @Mock
    private CandidateSteps candidateSteps;

    @Mock
    private InjectableStepsFactory stepFactory;

    @Mock
    private StepCandidate stepCandidate;

    @Mock
    private Keywords keywords;

    @InjectMocks
    private BddStepsCounter bddStepsCounter;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Vividus.class);
        PowerMockito.mockStatic(BeanFactory.class);
    }

    @Test
    public void testNoMatchedSteps() throws Exception
    {
        mockStepsAndCandidates(DEFAULT_STORY_LOCATION, List.of(WHEN_STEP), List.of(stepCandidate));
        BddStepsCounter.main(new String[0]);
        String output = getOutput();
        assertTrue(output.contains(NO_MATCHED_STEPS));
        assertTrue(output.contains(NO_STEP_CANDIDATES));
        assertTrue(output.contains(WHEN_STEP));
        assertFalse(output.contains(TOP_STEPS));
        assertFalse(output.contains(TOP_STEPS));
    }

    @Test
    public void testCommentedSteps() throws Exception
    {
        mockStepsAndCandidates(DEFAULT_STORY_LOCATION, List.of(COMMENT + WHEN_STEP), List.of(stepCandidate));
        BddStepsCounter.main(new String[0]);
        String output = getOutput();
        assertTrue(output.contains(NO_MATCHED_STEPS));
        assertFalse(output.contains(NO_STEP_CANDIDATES));
    }

    @Test
    public void testDirectoryOptionIsPresent() throws Exception
    {
        mockStepsAndCandidates(DIR_VALUE, List.of(), List.of());
        BddStepsCounter.main(new String[] { DIR_OPT_NAME, DIR_VALUE });
    }

    @Test
    public void testUnknownOptionIsPresent()
    {
        assertThrows(UnrecognizedOptionException.class,
            () ->  BddStepsCounter.main(new String[] { "--any", DIR_VALUE }));
    }

    @Test
    public void testLimitOptionIsPresent() throws Exception
    {
        mockStepsAndCandidates(DEFAULT_STORY_LOCATION, List.of(THEN_STEP, THEN_STEP, WHEN_STEP),
                List.of(stepCandidate));
        when(stepCandidate.matches(THEN_STEP, null)).thenReturn(true);
        when(stepCandidate.matches(WHEN_STEP, null)).thenReturn(true);
        when(stepCandidate.getStartingWord()).thenReturn(THEN).thenReturn(THEN).thenReturn(WHEN);
        when(stepCandidate.getPatternAsString()).thenReturn(CANDIDATE_STRING);
        BddStepsCounter.main(new String[] { "--top", ONE_OCCURRENCE });
        String output = getOutput();
        assertTrue(output.contains(String.format(STEP_PATTERN, THEN, VARIABLE)));
        assertFalse(output.contains(String.format(STEP_PATTERN, WHEN, VARIABLE)));
        assertTrue(output.contains(TWO_OCCURRENCES));
        assertFalse(output.contains(ONE_OCCURRENCE));
    }

    @Test
    public void testAndStep() throws Exception
    {
        mockStepsAndCandidates(DEFAULT_STORY_LOCATION, List.of(WHEN_STEP, AND_STEP),
                List.of(stepCandidate));
        when(stepCandidate.matches(WHEN_STEP, null)).thenReturn(true);
        when(stepCandidate.matches(AND_STEP, WHEN_STEP)).thenReturn(true);
        when(stepCandidate.getStartingWord()).thenReturn(WHEN);
        when(stepCandidate.getPatternAsString()).thenReturn(CANDIDATE_STRING);
        BddStepsCounter.main(new String[0]);
        String output = getOutput();
        assertTrue(output.contains(TOP_STEPS));
        assertTrue(output.contains(OCCURRENCES));
        assertTrue(output.contains(String.format(STEP_PATTERN, WHEN, VARIABLE)));
        assertFalse(output.contains(NO_STEP_CANDIDATES));
        assertTrue(output.contains(TWO_OCCURRENCES));
    }

    private void mockStepsAndCandidates(String resourceLocation, List<String> steps, List<StepCandidate> stepCandidates)
            throws Exception
    {
        PowerMockito.whenNew(BddStepsCounter.class).withNoArguments().thenReturn(bddStepsCounter);
        PowerMockito.doNothing().when(Vividus.class, "init");
        when(BeanFactory.getBean(StoryLoader.class)).thenReturn(storyLoader);
        when(BeanFactory.getBean(IPathFinder.class)).thenReturn(pathFinder);
        when(BeanFactory.getBean(Configuration.class)).thenReturn(configuration);
        when(configuration.keywords()).thenReturn(keywords);
        when(keywords.and()).thenReturn(AND);
        when(keywords.ignorable()).thenReturn(COMMENT);
        when(configuration.storyParser()).thenReturn(storyParser);
        String path = "";
        when(pathFinder.findPaths(argThat(arg -> resourceLocation.equals(arg.getResourceLocation())))).thenReturn(
                List.of(path));
        String storyText = "";
        when(storyLoader.loadResourceAsText(path)).thenReturn(storyText);
        when(storyParser.parseStory(any(String.class))).thenReturn(story);
        when(story.getScenarios()).thenReturn(List.of(scenario));
        when(scenario.getSteps()).thenReturn(steps);
        when(BeanFactory.getBean(InjectableStepsFactory.class)).thenReturn(stepFactory);
        when(stepFactory.createCandidateSteps()).thenReturn(List.of(candidateSteps));
        when(candidateSteps.listCandidates()).thenReturn(stepCandidates);
    }
}
