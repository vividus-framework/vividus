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

import static org.apache.commons.lang3.StringUtils.center;
import static org.apache.commons.lang3.StringUtils.rightPad;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.StepCandidate;
import org.vividus.IPathFinder;
import org.vividus.batch.BatchConfiguration;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;
import org.vividus.resource.StoryLoader;

public final class StepsCounter
{
    private static final String DEFAULT_STORY_LOCATION = "story";
    private static final String SPACE = " ";
    private static final String OCCURRENCES = "occurrence(s)";

    private StepsCounter()
    {
    }

    public static void main(String[] args) throws IOException, ParseException
    {
        Vividus.init();
        CommandLine commandLine;
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        Option directoryOption = new Option("d", "dir", true, "directory to count scenarios in (e.g. story/release).");
        Option topOption = new Option("t", "top", true, "the number of most popular steps");
        options.addOption(directoryOption);
        options.addOption(topOption);
        commandLine = parser.parse(options, args);

        StoryLoader storyLoader = BeanFactory.getBean(StoryLoader.class);
        IPathFinder pathFinder = BeanFactory.getBean(IPathFinder.class);
        String storyLocation = commandLine.hasOption(directoryOption.getOpt())
                ? commandLine.getOptionValue(directoryOption.getOpt()) : DEFAULT_STORY_LOCATION;
        Configuration configuration = BeanFactory.getBean(Configuration.class);

        List<String> steps = collectSteps(configuration, storyLoader, pathFinder, storyLocation);
        Set<StepCandidate> stepCandidates = collectStepCandidates();
        StepsUsageDetails stepsUsageDetails = fillStepsWithStats(configuration, stepCandidates, steps);
        printResults(commandLine, topOption, System.out, stepsUsageDetails);
    }

    private static List<String> collectSteps(Configuration configuration, StoryLoader storyLoader,
            IPathFinder pathFinder, String storyLocation) throws IOException
    {
        Keywords keywords = configuration.keywords();
        RegexStoryParser storyParser = new RegexStoryParser(new ExamplesTableFactory(keywords, null, null)
        {
            @Override
            public ExamplesTable createExamplesTable(String input)
            {
                return ExamplesTable.EMPTY;
            }
        });
        BatchConfiguration batchConfiguration = new BatchConfiguration();
        batchConfiguration.setResourceLocation(storyLocation);
        return pathFinder.findPaths(batchConfiguration)
                .stream()
                .map(storyLoader::loadResourceAsText)
                .map(storyParser::parseStory)
                .flatMap(story -> story.getScenarios().stream())
                .flatMap(scenario -> scenario.getSteps().stream())
                .filter(step -> !step.startsWith(keywords.ignorable()))
                .toList();
    }

    private static Set<StepCandidate> collectStepCandidates()
    {
        InjectableStepsFactory stepFactory = BeanFactory.getBean(InjectableStepsFactory.class);
        return stepFactory.createCandidateSteps().stream()
                .map(CandidateSteps::listCandidates)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private static StepsUsageDetails fillStepsWithStats(Configuration configuration, Set<StepCandidate> stepCandidates,
            List<String> steps)
    {
        StepsUsageDetails stepsUsageDetails = new StepsUsageDetails();
        Keywords keywords = configuration.keywords();
        String previousNonAndStep = null;
        for (String stepValue : steps)
        {
            String currentNonAndStep = null;
            if (stepValue.startsWith(keywords.and()))
            {
                currentNonAndStep = previousNonAndStep;
            }
            else
            {
                previousNonAndStep = stepValue;
            }
            boolean stepPresent = false;
            for (StepCandidate stepCandidate : stepCandidates)
            {
                if (stepCandidate.matches(stepValue, currentNonAndStep))
                {
                    stepPresent = true;
                    String stepString = stepCandidate.getStartingWord() + SPACE + stepCandidate.getPatternAsString();
                    stepsUsageDetails.incrementStepUsage(stepString);
                    break;
                }
            }
            if (!stepPresent)
            {
                stepsUsageDetails.missedSteps.add(stepValue);
            }
        }
        return stepsUsageDetails;
    }

    private static void printResults(CommandLine commandLine, Option topOption, PrintStream printStream,
            StepsUsageDetails stepsUsageDetails)
    {
        if (!stepsUsageDetails.stepsWithStats.isEmpty())
        {
            printStream.println(
                    center("Top of the most used steps:", stepsUsageDetails.maxStepLength) + SPACE + OCCURRENCES);
            long limit = stepsUsageDetails.stepsWithStats.size();
            if (commandLine.hasOption(topOption.getOpt()))
            {
                limit = Long.parseLong(commandLine.getOptionValue(topOption.getOpt()));
            }
            stepsUsageDetails.stepsWithStats
                    .entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .limit(limit)
                    .forEach(k -> printStream.println(rightPad(k.getKey(), stepsUsageDetails.maxStepLength)
                                    + center(String.format(" %s", k.getValue()), OCCURRENCES.length())));
        }
        else
        {
            printStream.println("Matched steps haven't been found");
        }
        if (!stepsUsageDetails.missedSteps.isEmpty())
        {
            printStream.println("\nUnable to find StepCandidate(s) for following step(s):");
            stepsUsageDetails.missedSteps.forEach(printStream::println);
        }
    }

    private static final class StepsUsageDetails
    {
        private final Map<String, Integer> stepsWithStats = new HashMap<>();
        private final List<String> missedSteps = new ArrayList<>();
        private int maxStepLength;

        private void incrementStepUsage(String step)
        {
            stepsWithStats.compute(step, (k, count) -> count == null ? 1 : count + 1);
            maxStepLength = Math.max(maxStepLength, step.length());
        }
    }
}
