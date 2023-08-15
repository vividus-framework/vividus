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

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jbehave.core.junit.JUnit4StoryRunner;
import org.junit.runner.Description;
import org.junit.runners.model.InitializationError;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;

// Fix once deprecated child class is removed
@SuppressWarnings({ "checkstyle:HideUtilityClassConstructor", "PMD.UseUtilityClass" })
public class ScenariosCounter
{
    private static final String DEFAULT_STORY_LOCATION = "story";
    private static final String FORMATTER = "%5d | %s";

    public static void main(String[] args) throws ParseException, InitializationError, ReflectiveOperationException
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
        configureStoryLocation(storyLocation);

        System.out.println("Story parsing may take up to 5 minutes. Please be patient.");
        JUnit4StoryRunner runner = new JUnit4StoryRunner(StoriesRunner.class);

        print(getNumberOfChildren(runner.getDescription(), Level.STORY.getLevel()), "Stories");
        print(getNumberOfChildren(runner.getDescription(), Level.SCENARIO.getLevel()), "Scenarios");
        print(getNumberOfChildren(runner.getDescription(), Level.EXAMPLE.getLevel()), "Scenarios with Examples");
    }

    private static int getNumberOfChildren(Description description, int level)
    {
        if (level < 1)
        {
            return 1;
        }
        int childrenNumber = 0;
        for (Description childDescription : description.getChildren())
        {
            if (childDescription.getMethodName() == null)
            {
                childrenNumber += getNumberOfChildren(childDescription, level - 1);
            }
        }
        return childrenNumber > 0 ? childrenNumber : 1;
    }

    private static void print(int number, String message)
    {
        System.out.printf(FORMATTER, number, message);
        System.out.println();
    }

    private static void configureStoryLocation(String storyLocation)
    {
        Properties properties = BeanFactory.getBean("properties", Properties.class);
        properties.put("batch-1.resource-location", storyLocation);
        properties.put("batch-1.resource-include-patterns", "**/*.story");
        properties.put("batch-1.resource-exclude-patterns", "");
    }

    private enum Level
    {
        STORY(1), SCENARIO(2), EXAMPLE(3);

        private final int level;

        Level(int level)
        {
            this.level = level;
        }

        private int getLevel()
        {
            return level;
        }
    }
}
