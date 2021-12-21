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

package org.vividus.mobileapp.action;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.IWebDriverProvider;

import io.appium.java_client.PushesFiles;

@ExtendWith(MockitoExtension.class)
class DeviceActionsTests
{
    private static final String RESOURCE_PATH = "data.txt";
    private static final String DEVICE_FILE_PATH = "device-file-path";
    private static final byte[] RESOURCE_BODY = { 79, 105, 107, 75 };

    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private DeviceActions deviceActions;

    @Test
    void shouldPushFile()
    {
        var pushingFilesDriver = mock(PushesFiles.class);

        when(webDriverProvider.getUnwrapped(PushesFiles.class)).thenReturn(pushingFilesDriver);

        deviceActions.pushFile(DEVICE_FILE_PATH, RESOURCE_PATH);

        verify(pushingFilesDriver).pushFile(DEVICE_FILE_PATH, RESOURCE_BODY);
        verifyNoMoreInteractions(webDriverProvider);
    }
}
