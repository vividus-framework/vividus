/*
 * Copyright 2019-2021 the original author or authors.
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.testcontext.TestContext;
import org.vividus.ui.web.event.PageLoadEndEvent;

@ExtendWith(MockitoExtension.class)
class FrameActionsTests
{
    private static final String VISIBLE_SCRIPT = "return document.body && document.body.clientHeight > 0"
            + " && document.body.clientWidth > 0;";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private TestContext testContext;
    @Mock private WebJavascriptActions javascriptActions;
    @InjectMocks private FrameActions frameActions;

    @Test
    void shouldSwitchToFrame()
    {
        WebElement frame = mock(WebElement.class);
        TargetLocator switcher = mockSwitcher();

        frameActions.switchToFrame(frame);

        verify(switcher).frame(frame);
        verify(testContext).put(argThat(keyMatcher()), eq(true));
    }

    @Test
    void shouldSwitchToRoot()
    {
        TargetLocator switcher = mockSwitcher();

        frameActions.switchToRoot();

        verify(switcher).defaultContent();
        verify(testContext).put(argThat(keyMatcher()), eq(false));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldHandlePageLoadEventWithoutNewPageLoadedWithinFrame()
    {
        when(testContext.get(argThat(keyMatcher()), any(Supplier.class))).thenReturn(true);
        when(javascriptActions.executeScript(VISIBLE_SCRIPT)).thenReturn(false).thenReturn(true);
        when(javascriptActions.executeScript("return window != window.parent;")).thenReturn(false);
        PageLoadEndEvent event = new PageLoadEndEvent(false, null);
        TargetLocator switcher = mockSwitcher();

        frameActions.handle(event);

        verify(switcher).parentFrame();
        verify(testContext).put(argThat(keyMatcher()), eq(false));
        verifyNoMoreInteractions(javascriptActions, switcher);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldNotHandlePageLoadEventIfContextIsNotInFrame()
    {
        when(testContext.get(argThat(keyMatcher()), any(Supplier.class))).thenReturn(false);

        frameActions.handle(null);

        verifyNoMoreInteractions(testContext);
        verifyNoInteractions(javascriptActions, webDriverProvider);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldCleanUpIfNewPageIsLoaded()
    {
        when(testContext.get(argThat(keyMatcher()), any(Supplier.class))).thenReturn(true);
        PageLoadEndEvent event = new PageLoadEndEvent(true, null);

        frameActions.handle(event);

        verify(testContext).remove(argThat(keyMatcher()));
        verifyNoMoreInteractions(testContext);
        verifyNoInteractions(javascriptActions, webDriverProvider);
    }

    private TargetLocator mockSwitcher()
    {
        WebDriver webDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        TargetLocator locator = mock(TargetLocator.class);
        when(webDriver.switchTo()).thenReturn(locator);
        return locator;
    }

    private static ArgumentMatcher<Class<?>> keyMatcher()
    {
        return arg -> StringUtils.endsWith(arg.toString(), "InFrame");
    }
}
