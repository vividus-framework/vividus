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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.jbehave.core.junit.JUnit4StoryRunner;
import org.junit.jupiter.api.Test;
import org.junit.runner.Description;
import org.junit.runners.model.InitializationError;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;
import org.mockito.MockedStatic;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;

class ScenariosCounterTests
{
    private static final String DEFAULT_STORY_LOCATION = "story";
    private static final String DIR_VALUE = "story/bvt";
    private static final String STORIES = "Stories";
    private static final String SCENARIOS = "Scenarios";
    private static final String SCENARIOS_WITH_EXAMPLES = "Scenarios with Examples";
    private static final String PROPERTIES = "properties";
    private static final String ROOT = "root description";
    private static final String STORY = "story description";
    private static final String SCENARIO = "scenario description";
    private static final String EXAMPLE = "example description";
    private static final String BEFORE = "before description";
    private static final String RESOURCE_LOCATION = "batch-1.resource-location";
    private static final String PARSING_NOTIFICATION = "Story parsing may take up to 5 minutes. Please be patient.";
    private static final String OUTPUT_LINE_FORMAT = "    %d | %s";

    @Test
    @StdIo
    void testCounterIgnoresDescriptionsWithMethodNames(StdOut stdOut)
            throws ParseException, ReflectiveOperationException, InitializationError
    {
        var root = Description.createSuiteDescription(ROOT);
        var beforeStories = Description.createTestDescription(Object.class, BEFORE);
        var story = Description.createSuiteDescription(STORY);
        var beforeStory = Description.createTestDescription(Object.class, BEFORE);
        var scenario = Description.createSuiteDescription(SCENARIO);

        root.addChild(beforeStories);
        root.addChild(story);
        story.addChild(beforeStory);
        story.addChild(scenario);

        testCounter(new String[0], root, DEFAULT_STORY_LOCATION);

        assertThat(stdOut.capturedLines(), arrayContaining(
                PARSING_NOTIFICATION,
                String.format(OUTPUT_LINE_FORMAT, 1, STORIES),
                String.format(OUTPUT_LINE_FORMAT, 1, SCENARIOS),
                String.format(OUTPUT_LINE_FORMAT, 1, SCENARIOS_WITH_EXAMPLES)
        ));
    }

    @Test
    @StdIo
    void testMultipleChildDescriptions(StdOut stdOut)
            throws ParseException, ReflectiveOperationException, InitializationError
    {
        var root = Description.createSuiteDescription(ROOT);
        var story = Description.createSuiteDescription(STORY);
        var scenario = Description.createSuiteDescription(SCENARIO);
        var scenarioWithoutExamples = Description.createSuiteDescription(SCENARIO);
        var example = Description.createSuiteDescription(EXAMPLE);

        root.addChild(story);
        story.addChild(scenario);
        story.addChild(scenarioWithoutExamples);
        scenario.addChild(example);
        scenario.addChild(example);

        testCounter(new String[0], root, DEFAULT_STORY_LOCATION);

        assertThat(stdOut.capturedLines(), arrayContaining(
                PARSING_NOTIFICATION,
                String.format(OUTPUT_LINE_FORMAT, 1, STORIES),
                String.format(OUTPUT_LINE_FORMAT, 2, SCENARIOS),
                String.format(OUTPUT_LINE_FORMAT, 3, SCENARIOS_WITH_EXAMPLES)
        ));
    }

    @Test
    void testDirectoryOptionIsPresent() throws ParseException, ReflectiveOperationException, InitializationError
    {
        var root = Description.createSuiteDescription(ROOT);

        testCounter(new String[] { "--dir", DIR_VALUE }, root, DIR_VALUE);
    }

    @Test
    void testUnknownOptionIsPresent()
    {
        try (var vividus = mockStatic(Vividus.class))
        {
            assertThrows(UnrecognizedOptionException.class,
                    () -> ScenariosCounter.main(new String[] { "--any", DIR_VALUE }));
            vividus.verify(Vividus::init);
        }
    }

    @Test
    @StdIo
    void testHelpOptionIsPresent(StdOut stdOut) throws ParseException, ReflectiveOperationException, InitializationError
    {
        try (var vividus = mockStatic(Vividus.class))
        {
            ScenariosCounter.main(new String[] { "--help" });
            vividus.verify(Vividus::init);
            assertThat(stdOut.capturedLines(), arrayContaining(
                    "usage: ScenariosCounter",
                    " -d,--dir <arg>   directory to count scenarios in (e.g. story/release).",
                    " -h,--help        print this message."
            ));
        }
    }

    @SuppressWarnings("try")
    private void testCounter(String[] args, Description root, String dir)
            throws ParseException, InitializationError, ReflectiveOperationException
    {
        try (var beanFactory = mockStatic(BeanFactory.class);
             var ignored = mockConstruction(JUnit4StoryRunner.class,
                        (runner, context) -> {
                            assertEquals(1, context.getCount());
                            assertEquals(List.of(StoriesRunner.class), context.arguments());
                            when(runner.getDescription()).thenReturn(root);
                        }
            )
        )
        {
            var properties = mockPropertiesBeanInstantiation(beanFactory);

            ScenariosCounter.main(args);

            beanFactory.verify(BeanFactory::open);
            verify(properties).put(RESOURCE_LOCATION, dir);
        }
    }

    private Properties mockPropertiesBeanInstantiation(MockedStatic<BeanFactory> beanFactory)
    {
        var properties = mock(Properties.class);
        beanFactory.when(() -> BeanFactory.getBean(PROPERTIES, Properties.class)).thenReturn(properties);
        return properties;
    }
}
