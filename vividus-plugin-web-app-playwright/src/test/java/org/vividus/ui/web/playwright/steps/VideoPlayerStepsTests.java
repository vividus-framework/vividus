/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.steps;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import com.microsoft.playwright.Locator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class VideoPlayerStepsTests
{
    private static final String VARIABLE_NAME = "videoInfo";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);

    @Mock private UiContext uiContext;
    @Mock private VariableContext variableContext;
    @Mock private PlaywrightLocator playwrightLocator;
    @Mock private Locator videoPlayer;
    @InjectMocks private VideoPlayerSteps steps;

    @Test
    void shouldRewindTimeInVideoPlayer()
    {
        when(uiContext.locateElement(playwrightLocator)).thenReturn(videoPlayer);
        steps.rewindTimeInVideoPlayer(42, playwrightLocator);
        verify(videoPlayer).evaluate("(videoPlayer, seconds) => videoPlayer.currentTime = seconds", 42);
    }

    @Test
    void shouldPlayVideoInVideoPlayer()
    {
        when(uiContext.locateElement(playwrightLocator)).thenReturn(videoPlayer);
        steps.playVideoInVideoPlayer(playwrightLocator);
        verify(videoPlayer).evaluate("videoPlayer => videoPlayer.play()");
    }

    @Test
    void shouldPauseVideoInVideoPlayer()
    {
        when(uiContext.locateElement(playwrightLocator)).thenReturn(videoPlayer);
        steps.pauseVideoInVideoPlayer(playwrightLocator);
        verify(videoPlayer).evaluate("videoPlayer => videoPlayer.pause()");
    }

    @Test
    void shouldSaveVideoInfo()
    {
        when(uiContext.locateElement(playwrightLocator)).thenReturn(videoPlayer);
        Map<String, Object> info = Map.of("duration", 10.0);
        when(videoPlayer.evaluate(eq("""
                videoPlayer => ({
                    'duration': videoPlayer.duration,
                    'src': videoPlayer.src,
                    'currentTime': videoPlayer.currentTime,
                    'networkState': videoPlayer.networkState
                })"""))).thenReturn(info);
        steps.saveVideoInfo(playwrightLocator, SCOPES, VARIABLE_NAME);
        verify(variableContext).putVariable(SCOPES, VARIABLE_NAME, info);
    }
}
