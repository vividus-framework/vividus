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

package org.vividus.mobileapp.action;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;

@ExtendWith(MockitoExtension.class)
class DeviceActionsTests
{
    private static final String RESOURCE_PATH = "data.txt";
    private static final String DEVICE_FILE_PATH = "device-file-path";
    private static final byte[] RESOURCE_BODY = { 79, 105, 107, 75 };

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private GenericWebDriverManager genericWebDriverManager;
    @InjectMocks private DeviceActions deviceActions;

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(webDriverProvider, genericWebDriverManager);
    }

    @Test
    void shouldPushFileOntoAndroidDevice()
    {
        io.appium.java_client.android.PushesFiles pushFiles = mock(io.appium.java_client.android.PushesFiles.class);

        when(genericWebDriverManager.isAndroidNativeApp()).thenReturn(true);
        when(webDriverProvider.getUnwrapped(io.appium.java_client.android.PushesFiles.class)).thenReturn(pushFiles);

        deviceActions.pushFile(DEVICE_FILE_PATH, RESOURCE_PATH);

        verify(pushFiles).pushFile(DEVICE_FILE_PATH, RESOURCE_BODY);
    }

    @Test
    void shouldPushFileOntoIOSDevice()
    {
        io.appium.java_client.ios.PushesFiles pushFiles = mock(io.appium.java_client.ios.PushesFiles.class);

        when(genericWebDriverManager.isAndroidNativeApp()).thenReturn(false);
        when(webDriverProvider.getUnwrapped(io.appium.java_client.ios.PushesFiles.class)).thenReturn(pushFiles);

        deviceActions.pushFile(DEVICE_FILE_PATH, RESOURCE_PATH);

        verify(pushFiles).pushFile(DEVICE_FILE_PATH, RESOURCE_BODY);
    }
}
