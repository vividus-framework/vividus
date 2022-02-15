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

package org.vividus.steps.ui.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.context.VariableContext;
import org.vividus.steps.ui.validation.BaseValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.web.action.VideoPlayerActions;

@ExtendWith(MockitoExtension.class)
class VideoPlayerStepsTests
{
    private static final String VIDEO_PLAYER = "Video player";
    private static final Locator LOCATOR = mock(Locator.class);

    @Mock private BaseValidations baseValidations;

    @Mock private VideoPlayerActions videoPlayerActions;

    @Mock private VariableContext variableContext;

    @InjectMocks
    private VideoPlayerSteps videoPlayerSteps;

    @Test
    void testRewindTimeInVideoPlayer()
    {
        WebElement videoPlayer = mockVideoPlayer();
        videoPlayerSteps.rewindTimeInVideoPlayer(1, LOCATOR);
        verify(videoPlayerActions).rewind(videoPlayer, 1);
    }

    @Test
    void testRewindTimeInVideoPlayerNull()
    {
        when(baseValidations.assertElementExists(VIDEO_PLAYER, LOCATOR)).thenReturn(Optional.empty());
        videoPlayerSteps.rewindTimeInVideoPlayer(1, LOCATOR);
        verifyNoInteractions(videoPlayerActions);
    }

    @Test
    void testPlayVideoInVideoPlayer()
    {
        WebElement videoPlayer = mockVideoPlayer();
        videoPlayerSteps.playVideoInVideoPlayer(LOCATOR);
        verify(videoPlayerActions).play(videoPlayer);
    }

    @Test
    void testPauseVideoInVideoPlayer()
    {
        WebElement videoPlayer = mockVideoPlayer();
        videoPlayerSteps.pauseVideoInVideoPlayer(LOCATOR);
        verify(videoPlayerActions).pause(videoPlayer);
    }

    @Test
    void shouldProvideInfoAboutVideo()
    {
        WebElement videoPlayer = mockVideoPlayer();
        Map<String, Object> info = Map.of("duration", "101");
        when(videoPlayerActions.getInfo(videoPlayer)).thenReturn(info);
        videoPlayerSteps.saveVideoInfo(LOCATOR, Set.of(), VIDEO_PLAYER);
        verify(variableContext).putVariable(Set.of(), VIDEO_PLAYER, info);
    }

    private WebElement mockVideoPlayer()
    {
        WebElement videoPlayer = mock(WebElement.class);
        when(baseValidations.assertElementExists(VIDEO_PLAYER, LOCATOR)).thenReturn(Optional.of(videoPlayer));
        return videoPlayer;
    }
}
