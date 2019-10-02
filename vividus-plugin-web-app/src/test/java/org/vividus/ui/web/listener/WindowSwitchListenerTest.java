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

package org.vividus.ui.web.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.web.context.IWebUiContext;

@ExtendWith(MockitoExtension.class)
class WindowSwitchListenerTest
{
    private static final String WINDOW_NAME1 = "windowName1";
    private static final String WINDOW_NAME2 = "windowName2";

    @Mock
    private IWebDriverManager webDriverManager;

    @Mock
    private IWebUiContext webUiContext;

    @Mock
    private WebDriver webDriver;

    @InjectMocks
    private WindowSwitchListener windowSwitchListener;

    @Test
    void testBeforeWindow()
    {
        windowSwitchListener.beforeSwitchToWindow(WINDOW_NAME1, webDriver);

        verifyNoInteractions(webDriverManager, webUiContext);
    }

    @Test
    void testBeforeWindowWindowExists()
    {
        windowSwitchListener.afterSwitchToWindow(WINDOW_NAME1, webDriver);
        mockWindowHandles(WINDOW_NAME1, WINDOW_NAME2);
        windowSwitchListener.beforeSwitchToWindow(WINDOW_NAME2, webDriver);
        verifyNoInteractions(webUiContext, webDriver);
    }

    @Test
    void testBeforeWindowWindowNotExists()
    {
        windowSwitchListener.afterSwitchToWindow(WINDOW_NAME1, webDriver);
        TargetLocator targetLocator = mock(TargetLocator.class);
        when(webDriver.switchTo()).thenReturn(targetLocator);
        mockWindowHandles(WINDOW_NAME2);

        windowSwitchListener.beforeSwitchToWindow(WINDOW_NAME2, webDriver);

        verify(webUiContext).reset();
        verify(targetLocator).window(WINDOW_NAME2);
    }

    private void mockWindowHandles(String... names)
    {
        when(webDriverManager.getWindowHandles()).thenReturn(new HashSet<>(Arrays.asList(names)));
    }
}
