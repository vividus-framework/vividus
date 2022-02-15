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

package org.vividus.ui.web.action;

import java.util.Map;

import org.openqa.selenium.WebElement;

public class VideoPlayerActions
{
    private final WebJavascriptActions javascriptActions;

    public VideoPlayerActions(WebJavascriptActions javascriptActions)
    {
        this.javascriptActions = javascriptActions;
    }

    /**
     * Rewinds the video in the video player to specified time.
     * @param videoPlayer video player element
     * @param seconds time in seconds
     */
    public void rewind(WebElement videoPlayer, int seconds)
    {
        javascriptActions.executeScript("arguments[0].currentTime=arguments[1];", videoPlayer, seconds);
    }

    /**
     * Plays the video in the video player.
     * @param videoPlayer video player element
     */
    public void play(WebElement videoPlayer)
    {
        javascriptActions.executeScript("arguments[0].play();", videoPlayer);
    }

    /**
     * Stops the video in the video player.
     * @param videoPlayer video player element
     */
    public void pause(WebElement videoPlayer)
    {
        javascriptActions.executeScript("arguments[0].pause();", videoPlayer);
    }

    /**
     * @param videoPlayer
     * @return Map of video player properties: duration, src, currentTime, networkState
     */
    public Map<String, Object> getInfo(WebElement videoPlayer)
    {
        return javascriptActions.executeScript("return"
                                             + " { 'duration' : arguments[0].duration,"
                                             + "   'src' :  arguments[0].src,"
                                             + "   'currentTime' :  arguments[0].currentTime,"
                                             + "   'networkState' :  arguments[0].networkState,"
                                             + " }", videoPlayer);
    }
}
