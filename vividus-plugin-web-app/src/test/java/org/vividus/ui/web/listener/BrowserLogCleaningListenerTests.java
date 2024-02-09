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

package org.vividus.ui.web.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.support.events.WebDriverListener;
import org.vividus.selenium.logging.BrowserLogManager;
import org.vividus.ui.web.listener.BrowserLogCleaningListener.Factory;

class BrowserLogCleaningListenerTests
{
    @Test
    void shouldResetBrowserLogBeforeAnyNavigation()
    {
        try (var browserLogManagerStaticMock = mockStatic(BrowserLogManager.class))
        {
            WebDriver webDriver = mock();
            WebDriverListener listener = new Factory().createListener(webDriver);
            listener.beforeAnyNavigationCall(mock(Navigation.class), null, null);
            browserLogManagerStaticMock.verify(() -> BrowserLogManager.resetBuffer(webDriver));
        }
    }
}
