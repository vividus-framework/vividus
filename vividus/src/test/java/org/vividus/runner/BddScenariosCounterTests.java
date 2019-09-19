/*
 * Copyright 2019 the original author or authors.
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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.Properties;

import com.github.valfirst.jbehave.junit.monitoring.JUnitReportingRunner;

import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vividus.SystemOutTests;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Vividus.class, BeanFactory.class, BddScenariosCounter.class })
@PowerMockIgnore({ "javax.*", "com.sun.*", "jdk.*", "org.xml.*", "org.w3c.*" })
public class BddScenariosCounterTests extends SystemOutTests
{
    private static final String DEFAULT_STORY_LOCATION = "story";
    private static final String DIR_VALUE = "story/bvt";
    private static final String SEPARATOR = " | ";
    private static final String STORIES = "Stories";
    private static final String SCENARIOS = "Scenarios";
    private static final String SCENARIOS_WITH_EXAMPLES = "Scenarios with Examples";
    private static final String PROPERTIES = "properties";
    private static final String ROOT = "root description";
    private static final String STORY = "story description";
    private static final String SCENARIO = "scenario description";
    private static final String EXAMPLE = "example description";
    private static final String BEFORE = "before description";
    private static final String RESOURCE_LOCATION = "bdd.story-loader.batch1.resource-location";

    @Before
    public void before()
    {
        PowerMockito.mockStatic(Vividus.class);
    }

    @Test
    public void testCounterIgnoresDescriptionsWithMethodNames() throws Exception
    {
        Description root = Description.createSuiteDescription(ROOT);
        Description beforeStories = Description.createTestDescription(Object.class, BEFORE);
        Description story = Description.createSuiteDescription(STORY);
        Description beforeStory = Description.createTestDescription(Object.class, BEFORE);
        Description scenario = Description.createSuiteDescription(SCENARIO);

        root.addChild(beforeStories);
        root.addChild(story);
        story.addChild(beforeStory);
        story.addChild(scenario);

        Properties properties = mockPropertiesBeanInstantiation();
        mockJUnitReportingRunnerInstantiation(root);

        BddScenariosCounter.main(new String[0]);

        verify(properties).put(RESOURCE_LOCATION, DEFAULT_STORY_LOCATION);
        assertThat(getOutput(), containsString(1 + SEPARATOR + STORIES));
        assertThat(getOutput(), containsString(1 + SEPARATOR + SCENARIOS));
        assertThat(getOutput(), containsString(1 + SEPARATOR + SCENARIOS_WITH_EXAMPLES));
    }

    @Test
    public void testMultipleChildDescriptions() throws Exception
    {
        Description root = Description.createSuiteDescription(ROOT);
        Description story = Description.createSuiteDescription(STORY);
        Description scenario = Description.createSuiteDescription(SCENARIO);
        Description scenarioWithoutExamples = Description.createSuiteDescription(SCENARIO);
        Description example = Description.createSuiteDescription(EXAMPLE);

        root.addChild(story);
        story.addChild(scenario);
        story.addChild(scenarioWithoutExamples);
        scenario.addChild(example);
        scenario.addChild(example);

        mockPropertiesBeanInstantiation();
        mockJUnitReportingRunnerInstantiation(root);

        BddScenariosCounter.main(new String[0]);

        assertThat(getOutput(), containsString(1 + SEPARATOR + STORIES));
        assertThat(getOutput(), containsString(2 + SEPARATOR + SCENARIOS));
        assertThat(getOutput(), containsString(3 + SEPARATOR + SCENARIOS_WITH_EXAMPLES));
    }

    @Test
    public void testDirectoryOptionIsPresent() throws Exception
    {
        Properties properties = mockPropertiesBeanInstantiation();
        mockJUnitReportingRunnerInstantiation(Description.createSuiteDescription(ROOT));
        BddScenariosCounter.main(new String[] {"--dir", DIR_VALUE});
        verify(properties).put(RESOURCE_LOCATION, DIR_VALUE);
    }

    @Test
    public void testUnknownOptionIsPresent()
    {
        assertThrows(UnrecognizedOptionException.class, () ->
                BddScenariosCounter.main(new String[] { "--any", DIR_VALUE}));
    }

    @Test
    public void testHelpOptionIsPresent() throws Exception
    {
        BddScenariosCounter.main(new String[] {"--help"});
        assertThat(getOutput(), containsString("usage: BddScenariosCounter"));
    }

    private Properties mockPropertiesBeanInstantiation()
    {
        Properties properties = mock(Properties.class);
        PowerMockito.mockStatic(BeanFactory.class);
        when(BeanFactory.getBean(PROPERTIES, Properties.class)).thenReturn(properties);
        return properties;
    }

    private JUnitReportingRunner mockJUnitReportingRunnerInstantiation(Description root) throws Exception
    {
        JUnitReportingRunner runner = mock(JUnitReportingRunner.class);
        whenNew(JUnitReportingRunner.class).withArguments(StoriesRunner.class).thenReturn(runner);
        when(runner.getDescription()).thenReturn(root);
        return runner;
    }
}
