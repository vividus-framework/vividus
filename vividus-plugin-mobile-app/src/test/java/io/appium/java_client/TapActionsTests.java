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

package io.appium.java_client;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.RemoteWebElement;
import org.vividus.mobileapp.action.TapActions;
import org.vividus.selenium.IWebDriverProvider;

@ExtendWith(MockitoExtension.class)
class TapActionsTests
{
    private static final String ACTIONS_OPEN = "{actions=[";
    private static final String ACTIONS_CLOSE = "]}";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private RemoteWebElement element;
    @Mock private PerformsTouchActions touchActions;
    @InjectMocks private TapActions tapActions;

    @BeforeEach
    void init()
    {
        when(webDriverProvider.getUnwrapped(PerformsTouchActions.class)).thenReturn(touchActions);
        when(element.getId()).thenReturn("elementId");
    }

    @Test
    void shouldTapOnElement()
    {
        tapActions.tap(element, Duration.ofSeconds(1));

        verify(touchActions).performTouchAction(argThat(arg ->
        {
            String parameters = arg.getParameters().toString();
            String actions = ACTIONS_OPEN + "{action=press, options={element=elementId}}, "
                                          + "{action=wait, options={ms=1000}}, "
                                          + "{action=release, options={}}"
                                          + ACTIONS_CLOSE;
            return actions.equals(parameters);
        }));
    }

    @Test
    void shouldTapOnElementWithoutWaitIfDurationIsZero()
    {
        tapActions.tap(element);

        verify(touchActions).performTouchAction(argThat(arg ->
        {
            String parameters = arg.getParameters().toString();
            String actions = ACTIONS_OPEN + "{action=tap, options={element=elementId}}" + ACTIONS_CLOSE;
            return actions.equals(parameters);
        }));
    }
}
