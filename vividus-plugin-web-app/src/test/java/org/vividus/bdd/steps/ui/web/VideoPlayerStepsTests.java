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

package org.vividus.bdd.steps.ui.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.ui.web.action.IVideoPlayerActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.util.LocatorUtil;

@ExtendWith(MockitoExtension.class)
class VideoPlayerStepsTests
{
    private static final String VIDEO_PLAYER = "Video player";
    private static final String VIDEO_PLAYER_NAME = "video_player_name";
    private static final SearchAttributes SEARCH_ATTRIBUTE = new SearchAttributes(ActionAttributeType.XPATH,
            LocatorUtil.getXPath("//video[@*='%1$s']", VIDEO_PLAYER_NAME));

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private IVideoPlayerActions videoPlayerActions;

    @InjectMocks
    private VideoPlayerSteps videoPlayerSteps;

    @Test
    void testRewindTimeInVideoPlayer()
    {
        WebElement videoPlayer = mockVideoPlayer();
        videoPlayerSteps.rewindTimeInVideoPlayer(1, VIDEO_PLAYER_NAME);
        verify(videoPlayerActions).rewind(videoPlayer, 1);
    }

    @Test
    void testRewindTimeInVideoPlayerNull()
    {
        when(baseValidations.assertIfElementExists(VIDEO_PLAYER, SEARCH_ATTRIBUTE)).thenReturn(null);
        videoPlayerSteps.rewindTimeInVideoPlayer(1, VIDEO_PLAYER_NAME);
        verifyNoInteractions(videoPlayerActions);
    }

    @Test
    void testPlayVideoInVideoPlayer()
    {
        WebElement videoPlayer = mockVideoPlayer();
        videoPlayerSteps.playVideoInVideoPlayer(VIDEO_PLAYER_NAME);
        verify(videoPlayerActions).play(videoPlayer);
    }

    @Test
    void testPauseVideoInVideoPlayer()
    {
        WebElement videoPlayer = mockVideoPlayer();
        videoPlayerSteps.pauseVideoInVideoPlayer(VIDEO_PLAYER_NAME);
        verify(videoPlayerActions).pause(videoPlayer);
    }

    private WebElement mockVideoPlayer()
    {
        WebElement videoPlayer = mock(WebElement.class);
        when(baseValidations.assertIfElementExists(VIDEO_PLAYER, SEARCH_ATTRIBUTE)).thenReturn(videoPlayer);
        return videoPlayer;
    }
}
