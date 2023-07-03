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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.AllStepCandidates;
import org.jbehave.core.model.Composite;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.RegexCompositeParser;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.parsers.StepMatcher;
import org.jbehave.core.steps.ConditionalStepCandidate;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.StepFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.IPathFinder;
import org.vividus.annotation.Replacement;
import org.vividus.batch.BatchConfiguration;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;
import org.vividus.replacement.DeprecatedCompositeStepsReporter;
import org.vividus.resource.StoryLoader;
import org.vividus.util.UriUtils;

@SuppressWarnings("PMD.ExcessiveImports")
public final class DeprecatedStepsReplacer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DeprecatedStepsReplacer.class);

    private static final String MANDATORY_STEP_COLUMN_NAME = "step";
    private static final String END_OF_STEP_LOOKAHEAD = "(?=\\h*(\\v|$))";
    private static final Pattern NESTED_STEPS_TABLE_PATTERN = Pattern.compile("(\\v|^).\\h*step\\h*.\\v");
    private static final Pattern COMPOSITE_DEPRECATION_NOTIFICATION
            = Pattern.compile("!--\\s+DEPRECATED: The step .* is deprecated and will be removed in .*");

    private final Configuration configuration;
    private final Keywords keywords;

    private AllStepCandidates stepCandidates;

    private DeprecatedStepsReplacer(Configuration configuration)
    {
        this.configuration = configuration;
        this.keywords = configuration.keywords();
    }

    public static void main(String[] args) throws IOException, ParseException
    {
        Vividus.init();
        Configuration configuration = BeanFactory.getBean(Configuration.class);

        Options options = new Options();
        Option directoryOption = new Option("rl", "resourceLocation", true, "Directory to find stories/steps.");
        options.addOption(directoryOption);
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(options, args);
        String testResourcesLocation = commandLine.getOptionValue(directoryOption.getOpt());

        DeprecatedStepsReplacer deprecatedStepsReplacer = new DeprecatedStepsReplacer(configuration);
        deprecatedStepsReplacer.replaceDeprecatedSteps(testResourcesLocation);
    }

    private void replaceDeprecatedSteps(String resourcesLocation) throws IOException
    {
        InjectableStepsFactory stepsFactory = BeanFactory.getBean(InjectableStepsFactory.class);
        BatchConfiguration batchConfiguration = new BatchConfiguration();
        batchConfiguration.setResourceLocation(Path.of(resourcesLocation).toUri().toString());
        batchConfiguration.setResourceIncludePatterns("**/*.story,**/*.steps");

        stepCandidates = new AllStepCandidates(configuration.stepConditionMatcher(),
                stepsFactory.createCandidateSteps());
        IPathFinder pathFinder = BeanFactory.getBean(IPathFinder.class);

        for (String path : pathFinder.findPaths(batchConfiguration))
        {
            replaceStepsInResource(path);
        }
    }

    @SuppressWarnings("PMD.NcssCount")
    private void replaceStepsInResource(String resourcePath) throws IOException
    {
        Path pathToFile = Paths.get(UriUtils.createUri(FilenameUtils.getFullPath(resourcePath)));
        String resourceName = FilenameUtils.getName(resourcePath);
        Path fullPath = Paths.get(pathToFile.toString(), resourceName);
        StoryLoader storyLoader = BeanFactory.getBean(StoryLoader.class);
        String resourceContent = storyLoader.loadResourceAsText(resourcePath);

        Map<String, List<String>> steps;
        if ("story".equals(FilenameUtils.getExtension(resourcePath)))
        {
            RegexStoryParser storyParser = new RegexStoryParser(new ExamplesTableFactory(keywords, null, null)
            {
                @Override
                public ExamplesTable createExamplesTable(String input)
                {
                    return ExamplesTable.EMPTY;
                }
            });
            Story story = storyParser.parseStory(resourceContent);
            Lifecycle lifecycle = story.getLifecycle();
            steps = story.getScenarios().stream().collect(Collectors.toMap(e -> "Scenario: " + e.getTitle(),
                    Scenario::getSteps,
                    (left, right) -> Stream.concat(left.stream(), right.stream()).collect(Collectors.toList())));
            steps.put("lifecycle (before steps)", lifecycle.getScopes().stream().map(lifecycle::getBeforeSteps)
                    .flatMap(List::stream).collect(Collectors.toList()));
            steps.put("lifecycle (after steps)", lifecycle.getScopes().stream().map(lifecycle::getAfterSteps)
                    .flatMap(List::stream).collect(Collectors.toList()));
        }
        else
        {
            RegexCompositeParser compositeParser = new RegexCompositeParser(keywords);
            steps = compositeParser.parseComposites(resourceContent).stream()
                    .collect(Collectors.toMap(e -> String.format("Composite: %s %s",
                            StringUtils.capitalize(e.getStepType().name().toLowerCase()),
                            e.getStepWithoutStartingWord()), Composite::getSteps));
        }

        String resourceWithReplacedDeprecatedSteps = resourceContent;
        for (Map.Entry<String, List<String>> resourceSection : steps.entrySet())
        {
            resourceWithReplacedDeprecatedSteps = replaceStepsInSection(resourceWithReplacedDeprecatedSteps,
                    resourceName, resourceSection.getKey(), resourceSection.getValue(), 0);
        }
        FileUtils.writeStringToFile(fullPath.toFile(), resourceWithReplacedDeprecatedSteps, StandardCharsets.UTF_8);
    }

    @SuppressWarnings({ "PMD.CognitiveComplexity", "PMD.NcssCount" })
    private String replaceStepsInSection(String content, String resourceName, String resourceSection,
            List<String> steps, int nestingLevel)
    {
        List<String> currentLevelNestedSteps = new ArrayList<>();
        String processedContent = content;
        int currentNestingLevel = nestingLevel;

        for (String step : steps)
        {
            StepFinder stepFinder = configuration.stepFinder();
            List<StepCandidate> stepCandidatesPrioritised = stepFinder.prioritise(step,
                    stepCandidates.getRegularSteps());

            for (StepCandidate stepCandidate : stepCandidatesPrioritised)
            {
                if (stepCandidate.matches(step))
                {
                    StepMatcher stepMatcher = configuration.stepPatternParser().parseStep(stepCandidate.getStepType(),
                            stepCandidate.getPatternAsString());
                    String[] stepParameters = getParametersFromStep(
                            stepMatcher.matcher(keywords.stepWithoutStartingWord(step)), currentLevelNestedSteps);

                    if (stepCandidate instanceof ConditionalStepCandidate)
                    {
                        break;
                    }
                    String deprecatedStepFormatPattern = getActualStepFormatPattern(stepCandidate);

                    if (!deprecatedStepFormatPattern.isEmpty())
                    {
                        try (Formatter formatter = new Formatter())
                        {
                            formatter.format(deprecatedStepFormatPattern, (Object[]) stepParameters);
                            String escapedDeprecatedStep = Pattern.quote(step);
                            String escapedActualStep = Matcher.quoteReplacement(formatter.toString());
                            processedContent = currentNestingLevel == 0
                                    ? processedContent.replaceFirst(escapedDeprecatedStep + END_OF_STEP_LOOKAHEAD,
                                            escapedActualStep)
                                    : replaceInTable(processedContent, step, escapedActualStep, currentNestingLevel);
                        }
                    }
                    else if (isDeprecatedStepWithoutReplacement(stepCandidate))
                    {
                        LOGGER.atWarn().addArgument(step).addArgument(resourceName).addArgument(resourceSection)
                                .log("The step \"{}\" from \"{} - {}\" "
                                   + "is deprecated but cannot be replaced automatically, please replace it manually.");
                    }
                    break;
                }
            }
        }
        return currentLevelNestedSteps.isEmpty()
                ? processedContent
                : replaceStepsInSection(processedContent, resourceName, resourceSection, currentLevelNestedSteps,
                        ++currentNestingLevel);
    }

    private String getActualStepFormatPattern(StepCandidate stepCandidate)
    {
        Optional<String> deprecationPatternInComposite = Stream.ofNullable(stepCandidate.composedSteps())
                .flatMap(Stream::of)
                .flatMap(c -> DeprecatedCompositeStepsReporter.DEPRECATED_COMPOSITE_STEP_COMMENT_PATTERN.matcher(c)
                        .results())
                .map(r -> r.group(2)).findFirst();
        return deprecationPatternInComposite.orElseGet(
                () -> Optional.ofNullable(stepCandidate.getMethod())
                        .map(m -> stepCandidate.getMethod().getAnnotation(Replacement.class))
                        .map(Replacement::replacementFormatPattern).orElse(StringUtils.EMPTY));
    }

    private boolean isDeprecatedStepWithoutReplacement(StepCandidate stepCandidate)
    {
        boolean deprecatedComposite = Stream.ofNullable(stepCandidate.composedSteps())
                .flatMap(Stream::of)
                .anyMatch(c -> COMPOSITE_DEPRECATION_NOTIFICATION.matcher(c).matches());
        boolean deprecatedCodeStep = Optional.ofNullable(stepCandidate.getMethod())
                .filter(m -> m.isAnnotationPresent(Deprecated.class)).isPresent();
        return deprecatedComposite || deprecatedCodeStep;
    }

    private String[] getParametersFromStep(Matcher matcher, List<String> nestedSteps)
    {
        matcher.matches();
        String[] parameters = new String[matcher.groupCount()];
        for (int i = 0; i < parameters.length; i++)
        {
            parameters[i] = matcher.group(i + 1);
            fetchNestedStepsFromParameter(nestedSteps, parameters[i]);
        }
        return parameters;
    }

    private void fetchNestedStepsFromParameter(List<String> nestedSteps, String parameter)
    {
        Matcher matcher = NESTED_STEPS_TABLE_PATTERN.matcher(parameter);
        if (matcher.find())
        {
            ExamplesTableFactory examplesTableFactory = configuration.examplesTableFactory();
            ExamplesTable examplesTable = examplesTableFactory.createExamplesTable(parameter);

            String tableContent = examplesTable.getRows().stream().map(p -> p.get(MANDATORY_STEP_COLUMN_NAME))
                    .collect(Collectors.joining(System.lineSeparator()));
            if (!tableContent.isEmpty())
            {
                List<String> tableNestedSteps = configuration.storyParser().parseStory(tableContent).getScenarios()
                        .get(0).getSteps();
                nestedSteps.addAll(tableNestedSteps);
            }
        }
    }

    private String replaceInTable(String content, String oldStepRaw, String escapedActualStep, int nestingLevel)
    {
        String editableContent = content;
        String lineSeparatorRegex = "\\v";
        String[] oldStepLines = getLinesWithoutEmptyElements(oldStepRaw.split(lineSeparatorRegex));
        String[] newStepLines = getLinesWithoutEmptyElements(escapedActualStep.split(lineSeparatorRegex));

        for (int i = 0; i < oldStepLines.length; i++)
        {
            String endOfLine = "(?=\\h*\\S{" + nestingLevel + "}(\\v|$))";
            editableContent = editableContent.replaceFirst(Pattern.quote(oldStepLines[i]) + endOfLine, newStepLines[i]);
        }
        return editableContent;
    }

    private String[] getLinesWithoutEmptyElements(String... lines)
    {
        return Stream.of(lines).filter(StringUtils::isNotBlank).toArray(String[]::new);
    }
}
