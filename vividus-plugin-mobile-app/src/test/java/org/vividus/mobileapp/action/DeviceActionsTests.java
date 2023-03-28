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

package org.vividus.mobileapp.action;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.ScreenOrientation;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.session.WebDriverSessionAttributes;
import org.vividus.selenium.session.WebDriverSessionInfo;

import io.appium.java_client.PullsFiles;
import io.appium.java_client.PushesFiles;
import io.appium.java_client.remote.SupportsRotation;

@ExtendWith(MockitoExtension.class)
class DeviceActionsTests
{
    private static final String DEVICE_FILE_PATH = "device-file-path";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private WebDriverSessionInfo webDriverSessionInfo;
    @InjectMocks private DeviceActions deviceActions;

    @Test
    void shouldPushFile()
    {
        byte[] content = { 79, 105, 107, 75 };
        byte[] base64content = Base64.getEncoder().encode(content);

        var pushingFilesDriver = mock(PushesFiles.class);

        when(webDriverProvider.getUnwrapped(PushesFiles.class)).thenReturn(pushingFilesDriver);

        deviceActions.pushFile(DEVICE_FILE_PATH, content);

        verify(pushingFilesDriver).pushFile(DEVICE_FILE_PATH, base64content);
        verifyNoMoreInteractions(webDriverProvider);
    }

    @Test
    void shouldDeleteFile()
    {
        var javascriptExecutor = mock(JavascriptExecutor.class);
        when(webDriverProvider.getUnwrapped(JavascriptExecutor.class)).thenReturn(javascriptExecutor);

        deviceActions.deleteFile(DEVICE_FILE_PATH);
        verify(javascriptExecutor).executeScript("mobile:deleteFile", Map.of("remotePath", DEVICE_FILE_PATH));
        verifyNoMoreInteractions(webDriverProvider);
    }

    @Test
    void shouldRotate()
    {
        SupportsRotation supportsRotation = mock(SupportsRotation.class);
        when(webDriverProvider.getUnwrapped(SupportsRotation.class)).thenReturn(supportsRotation);

        deviceActions.rotate(ScreenOrientation.LANDSCAPE);

        verify(supportsRotation).rotate(ScreenOrientation.LANDSCAPE);
        verify(webDriverSessionInfo).reset(WebDriverSessionAttributes.SCREEN_SIZE);
    }

    @Test
    void shouldPullFile()
    {
        var pullFileDriver = mock(PullsFiles.class);
        when(webDriverProvider.getUnwrapped(PullsFiles.class)).thenReturn(pullFileDriver);
        deviceActions.pullFile(DEVICE_FILE_PATH);
        verify(pullFileDriver).pullFile(DEVICE_FILE_PATH);
        verifyNoMoreInteractions(webDriverProvider);
    }
}
