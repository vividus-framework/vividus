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

package org.vividus.browserstack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.browserstack.client.exception.BrowserStackException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.cloud.AbstractCloudTestStatusManager.UpdateCloudTestStatusException;

@ExtendWith(MockitoExtension.class)
class BrowserStackTestStatusManagerTests
{
    private static final String SESSION_ID = "session-id";
    private static final String STATUS = "status";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private BrowserStackAutomateClient appAutomateClient;
    @InjectMocks private BrowserStackTestStatusManager manager;

    @Test
    void shouldUpdateCloudTestStatus() throws BrowserStackException, UpdateCloudTestStatusException
    {
        mockSessionId();

        manager.updateCloudTestStatus(STATUS);

        verify(appAutomateClient).updateSessionStatus(SESSION_ID, STATUS);
    }

    @Test
    void shouldWrapBrowserStackException() throws BrowserStackException
    {
        mockSessionId();
        BrowserStackException browserStackException = mock(BrowserStackException.class);

        doThrow(browserStackException).when(appAutomateClient).updateSessionStatus(SESSION_ID, STATUS);

        UpdateCloudTestStatusException thrown = assertThrows(UpdateCloudTestStatusException.class,
            () -> manager.updateCloudTestStatus(STATUS));

        assertEquals(browserStackException, thrown.getCause());
    }

    private void mockSessionId()
    {
        RemoteWebDriver remoteWebDriver = mock(RemoteWebDriver.class);
        SessionId sessionId = mock(SessionId.class);

        when(webDriverProvider.getUnwrapped(RemoteWebDriver.class)).thenReturn(remoteWebDriver);
        when(remoteWebDriver.getSessionId()).thenReturn(sessionId);
        when(sessionId.toString()).thenReturn(SESSION_ID);
    }
}
