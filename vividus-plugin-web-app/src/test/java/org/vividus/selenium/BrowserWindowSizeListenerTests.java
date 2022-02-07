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

package org.vividus.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Properties;

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Window;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.selenium.event.WebDriverCreateEvent;
import org.vividus.selenium.manager.IWebDriverManager;

@ExtendWith(MockitoExtension.class)
class BrowserWindowSizeListenerTests
{
    private static final String BROWSER_WINDOW_SIZE_META = "browserWindowSize";
    private static final String TARGET_SIZE_AS_STRING = "1024x768";
    private static final Dimension TARGET_SIZE = new Dimension(1024, 768);

    @Mock private RunContext runContext;
    @Mock private IWebDriverManager webDriverManager;
    @Mock private WebDriver webDriver;
    @InjectMocks private BrowserWindowSizeListener browserWindowSizeListener;

    @Test
    void shouldResizeWindowWithDesiredBrowserSizeFromStoryMeta()
    {
        when(webDriverManager.isElectronApp()).thenReturn(false);
        when(webDriverManager.isMobile()).thenReturn(false);
        var metaProperties = new Properties();
        metaProperties.put(BROWSER_WINDOW_SIZE_META, TARGET_SIZE_AS_STRING);
        var storyMeta = new Meta(metaProperties);
        var story = mock(Story.class);
        when(story.getMeta()).thenReturn(storyMeta);
        var runningStory = new RunningStory();
        runningStory.setStory(story);
        when(runContext.getRunningStory()).thenReturn(runningStory);
        Window window = mockWindow();
        when(webDriverManager.checkWindowFitsScreen(eq(TARGET_SIZE), argThat(consumer -> {
            consumer.accept(true, new Dimension(1200, 800));
            return true;
        }))).thenReturn(Optional.of(true));
        var event = new WebDriverCreateEvent(webDriver);
        browserWindowSizeListener.onWebDriverCreate(event);
        verify(window).setSize(TARGET_SIZE);
        verifyNoMoreInteractions(window);
    }

    @Test
    void shouldResizeWindowWithDesiredBrowserSizeFromScenarioMeta()
    {
        when(webDriverManager.isElectronApp()).thenReturn(false);
        when(webDriverManager.isMobile()).thenReturn(false);
        var metaProperties = new Properties();
        metaProperties.put(BROWSER_WINDOW_SIZE_META, TARGET_SIZE_AS_STRING);
        var scenarioMeta = new Meta(metaProperties);
        var scenario = mock(Scenario.class);
        when(scenario.getMeta()).thenReturn(scenarioMeta);
        var runningScenario = new RunningScenario();
        runningScenario.setScenario(scenario);
        var runningStory = new RunningStory();
        runningStory.setRunningScenario(runningScenario);
        when(runContext.getRunningStory()).thenReturn(runningStory);
        Window window = mockWindow();
        when(webDriverManager.checkWindowFitsScreen(eq(TARGET_SIZE), argThat(consumer -> {
            consumer.accept(true, new Dimension(1200, 800));
            return true;
        }))).thenReturn(Optional.of(true));
        var event = new WebDriverCreateEvent(webDriver);
        browserWindowSizeListener.onWebDriverCreate(event);
        verify(window).setSize(TARGET_SIZE);
        verifyNoMoreInteractions(window);
    }

    @Test
    void shouldFailToResizeWindowWhenItDoesNotFitToScreen()
    {
        when(webDriverManager.isElectronApp()).thenReturn(false);
        when(webDriverManager.isMobile()).thenReturn(false);
        var metaProperties = new Properties();
        metaProperties.put(BROWSER_WINDOW_SIZE_META, TARGET_SIZE_AS_STRING);
        var storyMeta = new Meta(metaProperties);
        var story = mock(Story.class);
        when(story.getMeta()).thenReturn(storyMeta);
        var runningStory = new RunningStory();
        runningStory.setStory(story);
        when(runContext.getRunningStory()).thenReturn(runningStory);
        var expected = new IllegalArgumentException();
        when(webDriverManager.checkWindowFitsScreen(eq(TARGET_SIZE), argThat(consumer -> {
            var exception = assertThrows(IllegalArgumentException.class,
                    () -> consumer.accept(false, new Dimension(800, 600)));
            assertEquals(
                    "Local or remote screen size \"800x600\" is less than desired browser window size \"1024x768\"",
                    exception.getMessage());
            return true;
        }))).thenThrow(expected);
        var actual = assertThrows(IllegalArgumentException.class,
                () -> browserWindowSizeListener.onWebDriverCreate(null));
        assertEquals(expected, actual);
    }

    @Test
    void shouldMaximizeWindowWhenNoStoryIsRunning()
    {
        when(webDriverManager.isElectronApp()).thenReturn(false);
        when(webDriverManager.isMobile()).thenReturn(false);
        when(runContext.getRunningStory()).thenReturn(null);
        Window window = mockWindow();
        var event = new WebDriverCreateEvent(webDriver);
        browserWindowSizeListener.onWebDriverCreate(event);
        verify(window).maximize();
    }

    @Test
    void shouldDoNothingForElectronApps()
    {
        when(webDriverManager.isElectronApp()).thenReturn(true);
        browserWindowSizeListener.onWebDriverCreate(null);
        verifyNoInteractions(runContext);
    }

    @Test
    void shouldDoNothingForMobile()
    {
        when(webDriverManager.isElectronApp()).thenReturn(false);
        when(webDriverManager.isMobile()).thenReturn(true);
        browserWindowSizeListener.onWebDriverCreate(null);
        verifyNoInteractions(runContext);
    }

    private Window mockWindow()
    {
        Options mockedOptions = mock(Options.class);
        when(webDriver.manage()).thenReturn(mockedOptions);
        Window mockedWindow = mock(Window.class);
        when(mockedOptions.window()).thenReturn(mockedWindow);
        return mockedWindow;
    }
}
