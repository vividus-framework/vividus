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

package org.vividus.runner;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jbehave.core.configuration.Keywords.GIVEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Scope;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.model.Composite;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.RegexCompositeParser;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.ConditionalStepCandidate;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.StepType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.IPathFinder;
import org.vividus.PathFinder;
import org.vividus.annotation.Replacement;
import org.vividus.configuration.BeanFactory;
import org.vividus.resource.StoryLoader;
import org.vividus.spring.ExtendedConfiguration;
import org.vividus.util.UriUtils;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class DeprecatedStepsReplacerTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(DeprecatedStepsReplacer.class);

    private static final String UNRESOLVED_DEPRECATION_LOG = "The step \"{}\" from \"{} - {}\" "
            + "is deprecated but cannot be replaced automatically, please replace it manually.";
    private static final String NEW_LINE = "\n";
    private static final String RESOURCE_LOCATION_ARG = "--resourceLocation";
    private static final String RESOURCE_LOCATION = "root/vividus/vividus-tests/src/main/resources";

    private static final String SCENARIO_HEADER = "Scenario: scenario name\n";
    private static final String STORY_LOCATION = "/story/TheHero.story";
    private static final String STORY_FULL_PATH_URI = Path.of(RESOURCE_LOCATION + STORY_LOCATION).toUri().toString();
    private static final String CODE_STEP_PATTERN = "deprecated step with parameters $p1-`$p2` and SubSteps:$p3";
    private static final String CODE_STEP_ALIAS_PATTERN = "deprecated step with parameters $p1-'$p2' and SubSteps:$p3";

    private static final String CODE_STEP_PATTERN_UNPRIORITIZED = "deprecated step with parameter $p1";
    private static final String CODE_STEP_UNPRIORITIZED = "Given deprecated step with parameter p1";
    private static final String CODE_STEP_UNPRIORITIZED_ACTUAL = "Given actual step with parameter p1";
    private static final String CODE_STEP_UNRESOLVED_PATTERN = "deprecated step without automatic replacement";
    private static final String CODE_STEP_UNRESOLVED = "Given deprecated step without automatic replacement";
    private static final String CODE_STEP_UNRESOLVED_ALIAS_PATTERN =
            "deprecated step without automatic replacement (alias)";
    private static final String CODE_STEP_UNRESOLVED_ALIAS =
            "Given deprecated step without automatic replacement (alias)";
    private static final String CODE_STEP_CONDITIONAL_PATTERN = "step for native app";
    private static final String CODE_STEP_CONDITIONAL = "Given step for native app";

    private static final Configuration CONFIGURATION = new ExtendedConfiguration();

    @BeforeEach
    void beforeEach()
    {
        CONFIGURATION.useStoryParser(new RegexStoryParser());
    }

    @Test
    @SuppressWarnings({ "PMD.MultipleStringLiteralsExtended", "PMD.MultipleStringLiterals" })
    void shouldReplaceDeprecatedJavaSteps() throws IOException, ParseException, NoSuchMethodException
    {
        String lifecycleBlock = "Lifecycle:\n"
                + "Before:\nScope: STORY\nGiven deprecated step without automatic replacement\n"
                + "After:\nScope: SCENARIO\nGiven deprecated step without automatic replacement (alias)\n\n";
        Lifecycle lifecycle = new Lifecycle(List.of(new Lifecycle.Steps(Scope.STORY, List.of(CODE_STEP_UNRESOLVED))),
                List.of(new Lifecycle.Steps(Scope.SCENARIO, List.of(CODE_STEP_UNRESOLVED_ALIAS))));
        String codeStepPrioritized = "codeStepPrioritized";
        String codeStep = "Given deprecated step with parameters ${p1}-`<p2>` and SubSteps:\n"
                        + "{headerSeparator=!, valueSeparator=!}\n"
                        + "!step!\n"
                        + "!Given deprecated step with parameters ${p1}-'<p2>' and SubSteps:!\n"
                        + "!|step|!";
        String codeStepNested =
                            "Given deprecated step with parameters ${p1}-'<p2>' and SubSteps:" + System.lineSeparator()
                          + "|step|";
        String codeStepActual = "Given actual step with parameters ${p1}-`<p2>` and SubSteps:\n"
                              + "{headerSeparator=!, valueSeparator=!}\n"
                              + "!step!\n"
                              + "!Given actual step with parameters ${p1}-`<p2>` and SubSteps:!\n"
                              + "!|step|!";
        String storyContentDeprecated = lifecycleBlock + SCENARIO_HEADER + codeStep + NEW_LINE + CODE_STEP_UNRESOLVED
                + NEW_LINE + CODE_STEP_CONDITIONAL;
        String storyContentActual = lifecycleBlock + SCENARIO_HEADER + codeStepActual + NEW_LINE + CODE_STEP_UNRESOLVED
                + NEW_LINE + CODE_STEP_CONDITIONAL;
        var stepCandidate = mockCodeStepCandidate(CODE_STEP_PATTERN, codeStep, codeStepPrioritized);
        var stepAliasCandidate = mockCodeStepCandidate(CODE_STEP_ALIAS_PATTERN, codeStepNested, codeStepPrioritized);
        var ignoredCandidate = mock(StepCandidate.class);
        when(ignoredCandidate.getPatternAsString()).thenReturn(CODE_STEP_PATTERN_UNPRIORITIZED);
        var deprecatedStepCandidate = mockCodeStepCandidate(CODE_STEP_UNRESOLVED_PATTERN,
                CODE_STEP_UNRESOLVED, "codeStepWithoutReplacement");
        var deprecatedStepCandidateAlias = mockCodeStepCandidate(CODE_STEP_UNRESOLVED_ALIAS_PATTERN,
                CODE_STEP_UNRESOLVED_ALIAS, "codeStepWithoutReplacement");
        var conditionalStepCandidate = mock(ConditionalStepCandidate.class);
        lenient().when(conditionalStepCandidate.matches(CODE_STEP_CONDITIONAL)).thenReturn(true);
        when(conditionalStepCandidate.getPatternAsString()).thenReturn(CODE_STEP_CONDITIONAL_PATTERN);

        try (var ignored = mockConstruction(RegexStoryParser.class, (mock, context) ->
        {
            var scenario = new Scenario("scenario name",
                    List.of(codeStep, CODE_STEP_UNRESOLVED, CODE_STEP_CONDITIONAL));
            var story = new Story(STORY_FULL_PATH_URI, null, null, null, null, lifecycle, List.of(scenario));
            assertRegexStoryParserConstruction(context);
            when(mock.parseStory(any(String.class))).thenReturn(story);
        }))
        {
            mockAndTestStepsReplacer(STORY_FULL_PATH_URI, storyContentDeprecated, storyContentActual,
                    List.of(deprecatedStepCandidate, deprecatedStepCandidateAlias, stepCandidate, stepAliasCandidate,
                            ignoredCandidate, conditionalStepCandidate));
        }
        verify(ignoredCandidate, never()).matches(codeStep);
        verify(conditionalStepCandidate).getMethod();
        assertThat(LOGGER.getLoggingEvents(), is(List.of(
                warn(UNRESOLVED_DEPRECATION_LOG, CODE_STEP_UNRESOLVED, "TheHero.story", "lifecycle (before steps)"),
                warn(UNRESOLVED_DEPRECATION_LOG, CODE_STEP_UNRESOLVED_ALIAS, "TheHero.story",
                        "lifecycle (after steps)"),
                warn(UNRESOLVED_DEPRECATION_LOG, CODE_STEP_UNRESOLVED, "TheHero.story", "Scenario: scenario name"))));
    }

    @Test
    void shouldNotReplaceNotImplementedSteps() throws IOException, ParseException
    {
        String storyNoForReplace = SCENARIO_HEADER + CODE_STEP_UNPRIORITIZED;
        try (var ignored = mockConstruction(RegexStoryParser.class, (mock, context) ->
        {
            var scenario = new Scenario(List.of(CODE_STEP_UNPRIORITIZED));
            var story = new Story(STORY_FULL_PATH_URI, List.of(scenario));
            when(mock.parseStory(any(String.class))).thenReturn(story);
        }))
        {
            mockAndTestStepsReplacer(STORY_FULL_PATH_URI, storyNoForReplace, storyNoForReplace, List.of());
        }
    }

    @Test
    void shouldReplaceDeprecatedCompositeSteps() throws IOException, ParseException
    {
        String notDeprecatedStep = "Given some actual step";
        String compositeStepHeader = "Composite: step wording\n";
        String stepsContentDeprecated = compositeStepHeader + CODE_STEP_UNPRIORITIZED + NEW_LINE + notDeprecatedStep
                + NEW_LINE + CODE_STEP_UNRESOLVED;
        String stepsContentActual = compositeStepHeader + CODE_STEP_UNPRIORITIZED_ACTUAL + NEW_LINE + notDeprecatedStep
                + NEW_LINE + CODE_STEP_UNRESOLVED;

        var compositeCandidate = mockCompositeStepCandidate(CODE_STEP_PATTERN_UNPRIORITIZED,
                CODE_STEP_UNPRIORITIZED, "!-- DEPRECATED: 0.10.0, Given actual step with parameter %1$s");
        var compositeCandidateWithoutReplace = mockCompositeStepCandidate(CODE_STEP_UNRESOLVED_PATTERN,
                CODE_STEP_UNRESOLVED,
                "!-- DEPRECATED: The step \"Given deprecated step without automatic replacement\" is "
                        + "deprecated and will be removed in VIVIDUS 3.5.1");
        var notDeprecatedCandidate = mockCompositeStepCandidate("some actual step", notDeprecatedStep);

        String stepsFileName = "The Hero's.steps";
        try (var ignored = mockConstruction(RegexCompositeParser.class, (mock, context) ->
        {
            var composed = mock(Composite.class);
            when(mock.parseComposites(any(String.class))).thenReturn(List.of(composed));
            when(composed.getSteps())
                    .thenReturn(List.of(CODE_STEP_UNPRIORITIZED, CODE_STEP_UNRESOLVED, notDeprecatedStep));
            when(composed.getStepType()).thenReturn(StepType.GIVEN);
            when(composed.getStepWithoutStartingWord()).thenReturn(CODE_STEP_UNRESOLVED_PATTERN);
        }))
        {
            mockAndTestStepsReplacer("file:/" + RESOURCE_LOCATION + "/folder with spaces/" + stepsFileName,
                    stepsContentDeprecated, stepsContentActual,
                    List.of(compositeCandidate, compositeCandidateWithoutReplace, notDeprecatedCandidate));
        }
        assertThat(LOGGER.getLoggingEvents(), is(List.of(
                warn(UNRESOLVED_DEPRECATION_LOG, CODE_STEP_UNRESOLVED, stepsFileName,
                        "Composite: Given deprecated step without automatic replacement"))));
    }

    private void mockAndTestStepsReplacer(String path, String resourceContentDeprecated, String resourceContentActual,
            List<StepCandidate> stepCandidates) throws IOException, ParseException
    {
        try (var beanFactory = mockStatic(BeanFactory.class);
                var fileUtils = mockStatic(FileUtils.class))
        {
            String resourceLocationAsUri = Path.of(RESOURCE_LOCATION).toUri().toString();
            var pathFinder = mock(PathFinder.class);
            when(pathFinder.findPaths(argThat(arg -> resourceLocationAsUri.equals(arg.getResourceLocation()))))
                    .thenReturn(List.of(path));
            beanFactory.when(() -> BeanFactory.getBean(IPathFinder.class)).thenReturn(pathFinder);

            var stepFactory = mock(InjectableStepsFactory.class);
            beanFactory.when(() -> BeanFactory.getBean(InjectableStepsFactory.class)).thenReturn(stepFactory);

            var storyLoader = mock(StoryLoader.class);
            when(storyLoader.loadResourceAsText(path)).thenReturn(resourceContentDeprecated);
            beanFactory.when(() -> BeanFactory.getBean(StoryLoader.class)).thenReturn(storyLoader);

            beanFactory.when(() -> BeanFactory.getBean(Configuration.class)).thenReturn(CONFIGURATION);

            var candidateSteps = mock(CandidateSteps.class);
            when(candidateSteps.listCandidates()).thenReturn(stepCandidates);
            when(stepFactory.createCandidateSteps()).thenReturn(List.of(candidateSteps));

            DeprecatedStepsReplacer.main(new String[] { RESOURCE_LOCATION_ARG, RESOURCE_LOCATION });

            beanFactory.verify(BeanFactory::open);

            Path pathToFile = Paths.get(UriUtils.createUri(FilenameUtils.getFullPath(path)));
            Path fullPath = Paths.get(pathToFile.toString(), FilenameUtils.getName(path));
            fileUtils.verify(() -> FileUtils.writeStringToFile(fullPath.toFile(), resourceContentActual,
                    StandardCharsets.UTF_8));
        }
    }

    private void assertRegexStoryParserConstruction(MockedConstruction.Context context)
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

    private StepCandidate mockCodeStepCandidate(String pattern, String matchedStep, String method)
            throws NoSuchMethodException
    {
        var candidate = mock(StepCandidate.class);
        when(candidate.getPatternAsString()).thenReturn(pattern);
        lenient().when(candidate.matches(matchedStep)).thenReturn(true);
        when(candidate.getStartingWord()).thenReturn(GIVEN);
        when(candidate.composedSteps()).thenReturn(new String[0]);
        when(candidate.getMethod())
                .thenReturn(DeprecatedStepsReplacerTests.class.getDeclaredMethod(method));
        return candidate;
    }

    private StepCandidate mockCompositeStepCandidate(String pattern, String matchedStep, String... compositesSteps)
    {
        var candidate = mock(StepCandidate.class);
        when(candidate.getPatternAsString()).thenReturn(pattern);
        lenient().when(candidate.matches(matchedStep)).thenReturn(true);
        when(candidate.getStartingWord()).thenReturn(GIVEN);
        when(candidate.composedSteps()).thenReturn(compositesSteps);
        return candidate;
    }

    @Replacement(versionToRemoveStep = "3.3.5",
            replacementFormatPattern = "Given actual step with parameters %1$s-`%2$s` and SubSteps:%3$s")
    @Given(priority = 1, value = CODE_STEP_PATTERN)
    @Alias(CODE_STEP_ALIAS_PATTERN)
    void codeStepPrioritized()
    {
        // nothing to do
    }

    @Given(CODE_STEP_PATTERN_UNPRIORITIZED)
    void codeStep()
    {
        // nothing to do
    }

    @Deprecated(since = "2.4.3", forRemoval = true)
    @Given(CODE_STEP_UNRESOLVED_PATTERN)
    @Alias(CODE_STEP_UNRESOLVED_ALIAS_PATTERN)
    void codeStepWithoutReplacement()
    {
        // nothing to do
    }
}
