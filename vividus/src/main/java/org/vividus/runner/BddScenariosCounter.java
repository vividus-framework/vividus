/*
 * Copyright 2019-2022 the original author or authors.
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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.RegexStoryParser;
import org.junit.runners.model.InitializationError;
import org.vividus.IPathFinder;
import org.vividus.batch.BatchResourceConfiguration;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;
import org.vividus.resource.StoryLoader;

public final class BddScenariosCounter
{
    private static final String DEFAULT_STORY_LOCATION = "story";
    private static final String FORMATTER = "%5d | %s";

    private BddScenariosCounter()
    {
    }

    public static void main(String[] args) throws ParseException, InitializationError,
            ReflectiveOperationException, IOException
    {
        BddScenariosCounter bddScenariosCounter = new BddScenariosCounter();
        bddScenariosCounter.countScenario(args);
    }

    public void countScenario(String[] args) throws ParseException, IOException
    {
        Vividus.init();
        CommandLineParser parser = new DefaultParser();
        Option helpOption = new Option("h", "help", false, "print this message.");
        Option directoryOption = new Option("d", "dir", true, "directory to count scenarios in (e.g. story/release).");
        Options options = new Options();
        options.addOption(helpOption);
        options.addOption(directoryOption);
        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.hasOption(helpOption.getOpt()))
        {
            new HelpFormatter().printHelp("ScenariosCounter", options);
            return;
        }
        String storyLocation = commandLine.hasOption(directoryOption.getOpt())
                ? commandLine.getOptionValue(directoryOption.getOpt()) : DEFAULT_STORY_LOCATION;
        StoryLoader storyLoader = BeanFactory.getBean(StoryLoader.class);
        IPathFinder pathFinder = BeanFactory.getBean(IPathFinder.class);
        Configuration configuration = BeanFactory.getBean(Configuration.class);
        List<Story> stories = countStories(configuration, storyLoader, pathFinder, storyLocation);
        int countScenarios = (int) stories.stream()
                .flatMap(story -> story.getScenarios().stream())
                .count();
        int countScenariosEx = countScenariosWithExamples(stories);
        print(stories.size(), "Stories");
        print(countScenarios, "Scenarios");
        print(countScenariosEx, "All executed scenarios (with Examples)");
    }

    private int countScenariosWithExamples(List<Story> stories)
    {
        AtomicInteger countAllScenariosWithExamples = new AtomicInteger();
        stories.forEach(story ->
        {
            int countLifecycleExamples = story.getLifecycle().getExamplesTable().getRowCount();
            countLifecycleExamples = countLifecycleExamples > 0 ? countLifecycleExamples : 1;
            AtomicInteger countScenariosWithExamples = new AtomicInteger();
            List<Scenario> scenarios = story.getScenarios().stream().collect(Collectors.toList());
            scenarios.forEach(scenario ->
            {
                if (!scenario.getExamplesTable().isEmpty())
                {
                    countScenariosWithExamples.addAndGet(scenario.getExamplesTable().getRowCount());
                }
                else
                {
                    countScenariosWithExamples.incrementAndGet();
                }
            });
            countAllScenariosWithExamples.addAndGet(countLifecycleExamples * countScenariosWithExamples.get());
        });
        return countAllScenariosWithExamples.get();
    }

    private List<Story> countStories(Configuration configuration, StoryLoader storyLoader, IPathFinder pathFinder,
                                     String storyLocation) throws IOException
    {
        Keywords keywords = configuration.keywords();
        RegexStoryParser storyParser = new RegexStoryParser(new ExamplesTableFactory(keywords, null, null)
        {
            @Override
            public ExamplesTable createExamplesTable(String input)
            {
                return new ExamplesTable(input);
            }
        });
        BatchResourceConfiguration batchResourceConfiguration = createResourceBatch(storyLocation);
        return pathFinder.findPaths(batchResourceConfiguration)
                .stream()
                .map(storyLoader::loadResourceAsText)
                .map(storyParser::parseStory)
                .collect(Collectors.toList());
    }

    private static void print(int number, String message)
    {
        System.out.printf(FORMATTER, number, message);
        System.out.println();
    }

    private BatchResourceConfiguration createResourceBatch(String storyLocation)
    {
        BatchResourceConfiguration batchResourceConfiguration = new BatchResourceConfiguration();
        batchResourceConfiguration.setResourceLocation(storyLocation);
        batchResourceConfiguration.setResourceIncludePatterns("**/*.story");
        batchResourceConfiguration.setResourceExcludePatterns(StringUtils.EMPTY);
        return batchResourceConfiguration;
    }
}
