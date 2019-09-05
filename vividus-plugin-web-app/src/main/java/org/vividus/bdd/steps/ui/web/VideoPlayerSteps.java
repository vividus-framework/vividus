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

import java.util.function.Consumer;

import javax.inject.Inject;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.ui.web.action.IVideoPlayerActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.util.LocatorUtil;

@TakeScreenshotOnFailure
public class VideoPlayerSteps
{
    @Inject private IBaseValidations baseValidations;
    @Inject private IVideoPlayerActions videoPlayerActions;

    /**
     * This step rewinds the video in the video player to specified time in seconds. The target element must be
     * &lt;video&gt; tag.
     * @param seconds time in seconds
     * @param videoPlayerName any attribute value of the element
     */
    @When("I rewind time to '$number' seconds in the video player with the name '$videoPlayerName'")
    public void rewindTimeInVideoPlayer(Integer seconds, String videoPlayerName)
    {
        findVideoPlayerAndExecuteAction(videoPlayerName, e -> videoPlayerActions.rewind(e, seconds));
    }

    /**
     * This step plays the video in the video player. The target element must be &lt;video&gt; tag.
     * @param videoPlayerName any attribute value of the element
     */
    @When("I play video in the video player with the name '$videoPlayerName'")
    public void playVideoInVideoPlayer(String videoPlayerName)
    {
        findVideoPlayerAndExecuteAction(videoPlayerName, videoPlayerActions::play);
    }

    /**
     * This step pauses the video in the video player. The target element must be &lt;video&gt; tag.
     * @param videoPlayerName any attribute value of the element
     */
    @When("I pause video in the video player with the name '$videoPlayerName'")
    public void pauseVideoInVideoPlayer(String videoPlayerName)
    {
        findVideoPlayerAndExecuteAction(videoPlayerName, videoPlayerActions::pause);
    }

    private void findVideoPlayerAndExecuteAction(String videoPlayerName, Consumer<WebElement> actionConsumer)
    {
        WebElement videoPlayer = baseValidations.assertIfElementExists("Video player", new SearchAttributes(
                ActionAttributeType.XPATH, LocatorUtil.getXPath("//video[@*='%1$s']", videoPlayerName)));
        if (videoPlayer != null)
        {
            actionConsumer.accept(videoPlayer);
        }
    }
}
