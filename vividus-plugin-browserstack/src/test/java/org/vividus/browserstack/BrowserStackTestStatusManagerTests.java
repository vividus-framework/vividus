/*
 * Copyright 2019-2023 the original author or authors.
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

import com.browserstack.client.exception.BrowserStackException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.cloud.AbstractCloudTestStatusManager.UpdateCloudTestStatusException;

@ExtendWith(MockitoExtension.class)
class BrowserStackTestStatusManagerTests
{
    private static final String SESSION_ID = "session-id";
    private static final String STATUS = "status";

    @Mock private BrowserStackAutomateClient appAutomateClient;
    @InjectMocks private BrowserStackTestStatusManager manager;

    @Test
    void shouldUpdateCloudTestStatus() throws BrowserStackException, UpdateCloudTestStatusException
    {
        manager.updateCloudTestStatus(SESSION_ID, STATUS);

        verify(appAutomateClient).updateSessionStatus(SESSION_ID, STATUS);
    }

    @Test
    void shouldWrapBrowserStackException() throws BrowserStackException
    {
        BrowserStackException browserStackException = mock();

        doThrow(browserStackException).when(appAutomateClient).updateSessionStatus(SESSION_ID, STATUS);

        var thrown = assertThrows(UpdateCloudTestStatusException.class,
            () -> manager.updateCloudTestStatus(SESSION_ID, STATUS));

        assertEquals(browserStackException, thrown.getCause());
    }
}
