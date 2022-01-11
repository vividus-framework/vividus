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

package org.vividus.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.util.Properties;

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;

@ExtendWith(MockitoExtension.class)
class BrowserWindowSizeProviderTests
{
    private static final String REMOTE_SCREEN_RESOLUTION = "1280x1024";
    private static final String SUCCESSFUL_DESIRED_RESOLUTION = "1024x768";

    @Mock
    private RunContext runContext;

    @InjectMocks
    private BrowserWindowSizeProvider browserWindowSizeProvider;

    @BeforeEach
    void beforeEach()
    {
        browserWindowSizeProvider.setRemoteScreenResolution(REMOTE_SCREEN_RESOLUTION);
    }

    @Test
    void testGetBrowserWindowSizeWhenNoMetaSet()
    {
        RunningStory runningStory = mockRunningStory(null, null);
        when(runContext.getRunningStory()).thenReturn(runningStory);
        BrowserWindowSize browserWindowSize = browserWindowSizeProvider.getBrowserWindowSize(true);
        assertNull(browserWindowSize);
    }

    @Test
    void testGetBrowserWindowSizeWhenNoRunningStory()
    {
        when(runContext.getRunningStory()).thenReturn(null);
        BrowserWindowSize browserWindowSize = browserWindowSizeProvider.getBrowserWindowSize(true);
        assertNull(browserWindowSize);
    }

    @Test
    void testGetBrowserWindowSizeWhenNoRunningScenario()
    {
        RunningStory runningStory = new RunningStory();
        Story story = mock(Story.class);
        runningStory.setStory(story);
        when(story.getMeta()).thenReturn(new Meta(new Properties()));
        when(runContext.getRunningStory()).thenReturn(runningStory);
        BrowserWindowSize browserWindowSize = browserWindowSizeProvider.getBrowserWindowSize(true);
        assertNull(browserWindowSize);
    }

    @Test
    void testGetBrowserWindowSizeWhenStoryMetaSet()
    {
        String storyBrowserWindowSize = SUCCESSFUL_DESIRED_RESOLUTION;
        RunningStory runningStory = mockRunningStory(storyBrowserWindowSize, null);
        when(runContext.getRunningStory()).thenReturn(runningStory);
        BrowserWindowSize browserWindowSize = browserWindowSizeProvider.getBrowserWindowSize(true);
        assertEquals(new BrowserWindowSize(storyBrowserWindowSize).toDimension(), browserWindowSize.toDimension());
    }

    @Test
    void testGetBrowserWindowSizeWhenScenarioMetaSet()
    {
        String scenarioBrowserWindowSize = SUCCESSFUL_DESIRED_RESOLUTION;
        RunningStory runningStory = mock(RunningStory.class);
        RunningScenario runningScenario = mock(RunningScenario.class);
        when(runningStory.getRunningScenario()).thenReturn(runningScenario);
        Scenario scenario = mock(Scenario.class);
        when(runningScenario.getScenario()).thenReturn(scenario);
        when(scenario.getMeta()).thenReturn(newMeta(scenarioBrowserWindowSize));
        when(runContext.getRunningStory()).thenReturn(runningStory);
        BrowserWindowSize browserWindowSize = browserWindowSizeProvider.getBrowserWindowSize(true);
        assertEquals(new BrowserWindowSize(scenarioBrowserWindowSize).toDimension(), browserWindowSize.toDimension());
    }

    @Test
    void testGetBrowserWindowSizeWhenStoryBrowserWidthTooLarge()
    {
        String storyBrowserWindowSize = "1920x1080";
        RunningStory runningStory = mockRunningStory(storyBrowserWindowSize, null);
        when(runContext.getRunningStory()).thenReturn(runningStory);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> browserWindowSizeProvider.getBrowserWindowSize(true));
        assertEquals(getExceptionMessage(storyBrowserWindowSize), exception.getMessage());
    }

    @Test
    void testGetBrowserWindowSizeWhenStoryBrowserHeightTooLarge()
    {
        String storyBrowserWindowSize = "1280x1025";
        RunningStory runningStory = mockRunningStory(storyBrowserWindowSize, null);
        when(runContext.getRunningStory()).thenReturn(runningStory);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> browserWindowSizeProvider.getBrowserWindowSize(true));
        assertEquals(getExceptionMessage(storyBrowserWindowSize), exception.getMessage());
    }

    @Test
    void testGetBrowserWindowSizeWhenStoryMetaSetAtLocalEnvironment()
    {
        assumeFalse(GraphicsEnvironment.isHeadless());
        String storyBrowserWindowSize = "320x240";
        RunningStory runningStory = mockRunningStory(storyBrowserWindowSize, null);
        when(runContext.getRunningStory()).thenReturn(runningStory);
        BrowserWindowSize browserWindowSize = browserWindowSizeProvider.getBrowserWindowSize(false);
        assertEquals(new BrowserWindowSize(storyBrowserWindowSize).toDimension(), browserWindowSize.toDimension());
    }

    private static RunningStory mockRunningStory(String storyBrowserWindowSize, String scenarioBrowserWindowSize)
    {
        RunningStory runningStory = mock(RunningStory.class);
        RunningScenario runningScenario = mock(RunningScenario.class);
        when(runningStory.getRunningScenario()).thenReturn(runningScenario);
        Story story = mock(Story.class);
        when(runningStory.getStory()).thenReturn(story);
        when(story.getMeta()).thenReturn(newMeta(storyBrowserWindowSize));
        Scenario scenario = mock(Scenario.class);
        when(runningScenario.getScenario()).thenReturn(scenario);
        when(scenario.getMeta()).thenReturn(newMeta(scenarioBrowserWindowSize));
        return runningStory;
    }

    private static Meta newMeta(String browserWindowSize)
    {
        Properties metaProperties = new Properties();
        if (browserWindowSize != null)
        {
            metaProperties.setProperty("browserWindowSize", browserWindowSize);
        }
        return new Meta(metaProperties);
    }

    private static String getExceptionMessage(String browserWindowSize)
    {
        return "Local or remote screen size \"" + REMOTE_SCREEN_RESOLUTION
                + "\" is less than desired browser window size \"" + browserWindowSize + "\"";
    }
}
