/*
 * Copyright 2019-2020 the original author or authors.
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

import javax.inject.Inject;

import org.openqa.selenium.WebElement;

public class VideoPlayerActions implements IVideoPlayerActions
{
    @Inject private WebJavascriptActions javascriptActions;

    @Override
    public void rewind(WebElement videoPlayer, int seconds)
    {
        javascriptActions.executeScript("arguments[0].currentTime=arguments[1];", videoPlayer, seconds);
    }

    @Override
    public void play(WebElement videoPlayer)
    {
        javascriptActions.executeScript("arguments[0].play();", videoPlayer);
    }

    @Override
    public void pause(WebElement videoPlayer)
    {
        javascriptActions.executeScript("arguments[0].pause();", videoPlayer);
    }
}
