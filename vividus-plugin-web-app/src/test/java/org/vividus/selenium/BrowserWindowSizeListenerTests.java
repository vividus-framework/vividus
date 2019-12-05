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

package org.vividus.selenium;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.web.event.WebDriverCreateEvent;

@ExtendWith(MockitoExtension.class)
class BrowserWindowSizeListenerTests
{
    @Mock
    private IWebDriverManager webDriverManager;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private IBrowserWindowSizeProvider browserWindowSizeProvider;

    @InjectMocks
    private BrowserWindowSizeListener browserWindowSizeListener;

    @Test
    void testOnWebDriverCreate()
    {
        boolean remoteExecution = true;
        when(webDriverProvider.isRemoteExecution()).thenReturn(remoteExecution);
        BrowserWindowSize browserWindowSize = mock(BrowserWindowSize.class);
        when(browserWindowSizeProvider.getBrowserWindowSize(remoteExecution)).thenReturn(browserWindowSize);
        browserWindowSizeListener.onWebDriverCreate(new WebDriverCreateEvent(mock(WebDriver.class)));
        verify(webDriverManager).resize(browserWindowSize);
    }
}
