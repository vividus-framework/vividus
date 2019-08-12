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

package org.vividus.ui.web.action;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class VideoPlayerActionsTests
{
    @Mock
    private WebElement webElement;

    @Mock
    private IJavascriptActions javascriptActions;

    @InjectMocks
    private VideoPlayerActions videoPlayerActions;

    @Test
    void testRewind()
    {
        videoPlayerActions.rewind(webElement, 1);
        verify(javascriptActions).executeScript("arguments[0].currentTime=arguments[1];", webElement, 1);
    }

    @Test
    void testPlay()
    {
        videoPlayerActions.play(webElement);
        verify(javascriptActions).executeScript("arguments[0].play();", webElement);
    }

    @Test
    void testPause()
    {
        videoPlayerActions.pause(webElement);
        verify(javascriptActions).executeScript("arguments[0].pause();", webElement);
    }
}
