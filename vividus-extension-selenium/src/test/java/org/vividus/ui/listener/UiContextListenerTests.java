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

package org.vividus.ui.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.selenium.event.WebDriverCreateEvent;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.context.SearchContextSetter;

@ExtendWith(MockitoExtension.class)
class UiContextListenerTests
{
    @Mock
    private IUiContext uiContext;

    @InjectMocks
    private UiContextListener uiContextListener;

    @Test
    void testOnWebDriverCreate()
    {
        WebDriver webDriver = mock(WebDriver.class);
        WebDriverCreateEvent event = new WebDriverCreateEvent(webDriver);
        uiContextListener.onWebDriverCreate(event);
        verify(uiContext).putSearchContext(eq(webDriver), any(SearchContextSetter.class));
    }

    @Test
    void testOnWebDriverQuit()
    {
        AfterWebDriverQuitEvent event = new AfterWebDriverQuitEvent(StringUtils.EMPTY);
        uiContextListener.onWebDriverQuit(event);
        verify(uiContext).clear();
    }
}
