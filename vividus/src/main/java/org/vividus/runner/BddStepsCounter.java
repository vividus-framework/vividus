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

import static org.apache.commons.lang3.StringUtils.center;
import static org.apache.commons.lang3.StringUtils.rightPad;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.StepCandidate;
import org.vividus.bdd.IPathFinder;
import org.vividus.bdd.StoryLoader;
import org.vividus.bdd.batch.BatchResourceConfiguration;
import org.vividus.bdd.spring.Configuration;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;

public final class BddStepsCounter
{
    private static final String DEFAULT_STORY_LOCATION = "story";
    private static final String SPACE = " ";
    private static final String OCCURRENCES = "occurrence(s)";
    private final List<String> stepList = new ArrayList<>();
    private final Set<StepCandidate> stepCandidates = new HashSet<>();
    private final Map<String, Integer> stepsWithStats = new HashMap<>();
    private final List<String> missedSteps = new ArrayList<>();
    private int maxStepLength;

    private BddStepsCounter()
    {
    }

    public static void main(String[] args) throws IOException, ParseException
    {
        BddStepsCounter bddStepsCounter = new BddStepsCounter();
        bddStepsCounter.countSteps(args);
    }

    public void countSteps(String[] args) throws ParseException, IOException
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

        fillStepList(configuration, storyLoader, pathFinder, storyLocation);
        fillStepCandidates();
        fillStepsWithStats(configuration);
        printResults(commandLine, topOption, System.out);
    }

    private void fillStepList(Configuration configuration, StoryLoader storyLoader, IPathFinder pathFinder,
            String storyLocation) throws IOException
    {
        StoryParser storyParser = configuration.storyParser();
        Keywords keywords = configuration.keywords();
        BatchResourceConfiguration batchResourceConfiguration = createResourceBatch(storyLocation);
        for (String path : pathFinder.findPaths(batchResourceConfiguration))
        {
            String storyString = storyLoader.loadResourceAsText(path);
            Story story = storyParser.parseStory(storyString);
            for (Scenario scenario : story.getScenarios())
            {
                stepList.addAll(scenario.getSteps().stream().filter(step -> !step.startsWith(keywords.ignorable()))
                        .collect(Collectors.toList()));
            }
        }
    }

    private BatchResourceConfiguration createResourceBatch(String storyLocation)
    {
        BatchResourceConfiguration batchResourceConfiguration = new BatchResourceConfiguration();
        batchResourceConfiguration.setResourceLocation(storyLocation);
        batchResourceConfiguration.setResourceIncludePatterns("**/*.story");
        batchResourceConfiguration.setResourceExcludePatterns("");
        return batchResourceConfiguration;
    }

    private void fillStepCandidates()
    {
        InjectableStepsFactory stepFactory = BeanFactory.getBean(InjectableStepsFactory.class);
        for (CandidateSteps candidateSteps : stepFactory.createCandidateSteps())
        {
            stepCandidates.addAll(candidateSteps.listCandidates());
        }
    }

    private void fillStepsWithStats(Configuration configuration)
    {
        Keywords keywords = configuration.keywords();
        String previousNonAndStep = null;
        for (String stepValue : stepList)
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
            boolean isStepPresent = false;
            for (StepCandidate stepCandidate : stepCandidates)
            {
                if (stepCandidate.matches(stepValue, currentNonAndStep))
                {
                    isStepPresent = true;
                    String stepString = stepCandidate.getStartingWord() + SPACE + stepCandidate.getPatternAsString();
                    maxStepLength = Math.max(maxStepLength, stepString.length());
                    Integer count = stepsWithStats.get(stepString);
                    if (count != null)
                    {
                        stepsWithStats.put(stepString, ++count);
                    }
                    else
                    {
                        stepsWithStats.put(stepString, 1);
                    }
                    break;
                }
            }
            if (!isStepPresent)
            {
                missedSteps.add(stepValue);
            }
        }
    }

    private void printResults(CommandLine commandLine, Option topOption, PrintStream printStream)
    {
        if (!stepsWithStats.isEmpty())
        {
            printStream.println(center("Top of the most used bdd steps:", maxStepLength) + SPACE + OCCURRENCES);
            long limit = stepsWithStats.size();
            if (commandLine.hasOption(topOption.getOpt()))
            {
                limit = Long.parseLong(commandLine.getOptionValue(topOption.getOpt()));
            }
            final int finalMaxStepLength = maxStepLength;
            stepsWithStats
                    .entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .limit(limit)
                    .forEach(k -> printStream.println(rightPad(k.getKey(), finalMaxStepLength)
                                    + center(String.format(" %s", k.getValue()), OCCURRENCES.length())));
        }
        else
        {
            printStream.println("Matched steps haven't been found");
        }
        if (!missedSteps.isEmpty())
        {
            printStream.println("\nUnable to find StepCandidate(s) for following step(s):");
            missedSteps.forEach(printStream::println);
        }
    }
}
