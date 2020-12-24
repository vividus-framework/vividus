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

package org.vividus.ui.web.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.vividus.ui.context.IUiContext;

@ExtendWith(MockitoExtension.class)
class WebUiContextListenerTests
{
    private static final String WINDOW_NAME1 = "windowName1";
    private static final String WINDOW_NAME2 = "windowName2";

    @Mock
    private IUiContext uiContext;

    @InjectMocks
    private WebUiContextListener webUiContextListener;

    @Test
    void testAfterNavigateBack()
    {
        webUiContextListener.afterNavigateBack(mock(WebDriver.class));
        verify(uiContext).reset();
    }

    @Test
    void testAfterNavigateForward()
    {
        webUiContextListener.afterNavigateForward(mock(WebDriver.class));
        verify(uiContext).reset();
    }

    @Test
    void testAfterNavigateTo()
    {
        webUiContextListener.afterNavigateTo("url", mock(WebDriver.class));
        verify(uiContext).reset();
    }

    @Test
    void testBeforeWindow()
    {
        WebDriver webDriver = mock(WebDriver.class);
        webUiContextListener.beforeSwitchToWindow(WINDOW_NAME1, webDriver);
        verifyNoInteractions(uiContext);
    }

    @Test
    void testBeforeWindowWindowExists()
    {
        WebDriver webDriver = mock(WebDriver.class);
        webUiContextListener.afterSwitchToWindow(WINDOW_NAME1, webDriver);
        when(webDriver.getWindowHandles()).thenReturn(new LinkedHashSet<>(List.of(WINDOW_NAME1, WINDOW_NAME2)));
        webUiContextListener.beforeSwitchToWindow(WINDOW_NAME2, webDriver);
        verifyNoInteractions(uiContext);
    }

    @Test
    void testBeforeWindowWindowNotExists()
    {
        WebDriver webDriver = mock(WebDriver.class);
        webUiContextListener.afterSwitchToWindow(WINDOW_NAME1, webDriver);
        TargetLocator targetLocator = mock(TargetLocator.class);
        when(webDriver.switchTo()).thenReturn(targetLocator);
        when(webDriver.getWindowHandles()).thenReturn(Set.of(WINDOW_NAME2));

        webUiContextListener.beforeSwitchToWindow(WINDOW_NAME2, webDriver);

        verify(uiContext).reset();
        verify(targetLocator).window(WINDOW_NAME2);
    }
}
