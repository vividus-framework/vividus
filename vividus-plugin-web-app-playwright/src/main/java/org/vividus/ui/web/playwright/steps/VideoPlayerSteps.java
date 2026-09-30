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

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.microsoft.playwright.Locator;

import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;
import org.vividus.variable.VariableScope;

public class VideoPlayerSteps
{
    private static final String VIDEO_PLAYER_EXPRESSION = "videoPlayer => videoPlayer";
    private static final String INFO_SCRIPT = """
            videoPlayer => ({
                'duration': videoPlayer.duration,
                'src': videoPlayer.src,
                'currentTime': videoPlayer.currentTime,
                'networkState': videoPlayer.networkState
            })""";

    private final UiContext uiContext;
    private final VariableContext variableContext;

    public VideoPlayerSteps(UiContext uiContext, VariableContext variableContext)
    {
        this.uiContext = uiContext;
        this.variableContext = variableContext;
    }

    /**
     * This step rewinds the video in the video player to specified time in seconds. The target element must be
     * &lt;video&gt; tag.
     *
     * @param seconds time in seconds
     * @param locator Locator to locate element
     */
    @When("I rewind time to `$number` seconds in video player located by `$locator`")
    public void rewindTimeInVideoPlayer(Integer seconds, PlaywrightLocator locator)
    {
        findVideoPlayerAndExecuteAction(locator,
                videoPlayer -> videoPlayer.evaluate("(videoPlayer, seconds) => videoPlayer.currentTime = seconds",
                        seconds));
    }

    /**
     * This step plays the video in the video player. The target element must be &lt;video&gt; tag.
     *
     * @param locator Locator to locate element
     */
    @When("I play video in video player located by `$locator`")
    public void playVideoInVideoPlayer(PlaywrightLocator locator)
    {
        findVideoPlayerAndExecuteAction(locator, videoPlayer -> videoPlayer.evaluate(VIDEO_PLAYER_EXPRESSION
                + ".play()"));
    }

    /**
     * This step pauses the video in the video player. The target element must be &lt;video&gt; tag.
     *
     * @param locator Locator to locate element
     */
    @When("I pause video in video player located by `$locator`")
    public void pauseVideoInVideoPlayer(PlaywrightLocator locator)
    {
        findVideoPlayerAndExecuteAction(locator, videoPlayer -> videoPlayer.evaluate(VIDEO_PLAYER_EXPRESSION
                + ".pause()"));
    }

    /**
     * Saves video player info: <b>duration</b>, <b>currentTime</b>, <b>src</b>, <b>networkState</b>.
     * For more information about the properties see
     * <a href="https://www.w3schools.com/tags/ref_av_dom.asp">HTML Audio/Video Properties</a>
     *
     * @param locator      Locator to locate element
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                       <li><b>STEP</b> - the variable will be available only within the step
     *                       <li><b>SCENARIO</b> - the variable will be available only within the scenario
     *                       <li><b>STORY</b> - the variable will be available within the whole story
     *                       <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The variable name to store results. If the variable name is <b>info</b>, the following
     *                     variables will be created:
     *                     <ul>
     *                       <li><b>${info.src}</b> - The the current source of the audio/video element </li>
     *                       <li><b>${info.duration}</b> - The length of the current audio/video (in seconds) </li>
     *                       <li><b>${info.currentTime}</b> - The current playback position in the audio/video
     *                         (in seconds)</li>
     *                       <li><b>${info.networkState}</b> - The the current network state of the audio/video.
     *                         For more information see:
     *                         <a href="https://www.w3schools.com/tags/av_prop_networkstate.asp">Network State</a></li>
     *                     </ul>
     */
    @When("I save info from video player located by `$locator` to $scopes variable `$variableName`")
    public void saveVideoInfo(PlaywrightLocator locator, Set<VariableScope> scopes, String variableName)
    {
        findVideoPlayerAndExecuteAction(locator,
                videoPlayer -> variableContext.putVariable(scopes, variableName, getVideoPlayerInfo(videoPlayer)));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getVideoPlayerInfo(Locator videoPlayer)
    {
        return (Map<String, Object>) videoPlayer.evaluate(INFO_SCRIPT);
    }

    private void findVideoPlayerAndExecuteAction(PlaywrightLocator locator, Consumer<Locator> actionConsumer)
    {
        actionConsumer.accept(uiContext.locateElement(locator));
    }
}
