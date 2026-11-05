/*
 * Copyright 2019-2026 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.WebDriverException;
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

        PushesFiles pushingFilesDriver = mock();

        when(webDriverProvider.getUnwrapped(PushesFiles.class)).thenReturn(pushingFilesDriver);

        deviceActions.pushFile(DEVICE_FILE_PATH, content);

        verify(pushingFilesDriver).pushFile(DEVICE_FILE_PATH, base64content);
        verifyNoMoreInteractions(webDriverProvider);
    }

    @Test
    void shouldDeleteFile()
    {
        JavascriptExecutor javascriptExecutor = mock();
        when(webDriverProvider.getUnwrapped(JavascriptExecutor.class)).thenReturn(javascriptExecutor);

        deviceActions.deleteFile(DEVICE_FILE_PATH);
        verify(javascriptExecutor).executeScript("mobile:deleteFile", Map.of("remotePath", DEVICE_FILE_PATH));
        verifyNoMoreInteractions(webDriverProvider);
    }

    @Test
    void shouldRotate()
    {
        SupportsRotation supportsRotation = mock();
        when(webDriverProvider.getUnwrapped(SupportsRotation.class)).thenReturn(supportsRotation);

        deviceActions.rotate(ScreenOrientation.LANDSCAPE);

        verify(supportsRotation).rotate(ScreenOrientation.LANDSCAPE);
        verify(webDriverSessionInfo).reset(WebDriverSessionAttributes.SCREEN_SIZE);
    }

    @Test
    void shouldResetScreenSizeWhenRotationFails()
    {
        SupportsRotation supportsRotation = mock();
        when(webDriverProvider.getUnwrapped(SupportsRotation.class)).thenReturn(supportsRotation);
        var exception = new WebDriverException("Rotation failed");
        doThrow(exception).when(supportsRotation).rotate(ScreenOrientation.LANDSCAPE);

        assertSame(exception, assertThrows(WebDriverException.class,
                () -> deviceActions.rotate(ScreenOrientation.LANDSCAPE)));

        verify(webDriverSessionInfo).reset(WebDriverSessionAttributes.SCREEN_SIZE);
    }

    @Test
    void shouldPullFile()
    {
        PullsFiles pullFileDriver = mock();
        when(webDriverProvider.getUnwrapped(PullsFiles.class)).thenReturn(pullFileDriver);
        deviceActions.pullFile(DEVICE_FILE_PATH);
        verify(pullFileDriver).pullFile(DEVICE_FILE_PATH);
        verifyNoMoreInteractions(webDriverProvider);
    }

    @Test
    void shouldReturnTrueWhenFileExists()
    {
        PullsFiles pullFileDriver = mock();
        when(webDriverProvider.getUnwrapped(PullsFiles.class)).thenReturn(pullFileDriver);
        when(pullFileDriver.pullFile(DEVICE_FILE_PATH)).thenReturn(new byte[0]);

        assertTrue(deviceActions.fileExists(DEVICE_FILE_PATH));

        verify(pullFileDriver).pullFile(DEVICE_FILE_PATH);
        verifyNoMoreInteractions(webDriverProvider);
    }

    @ParameterizedTest
    @ValueSource(strings = { "No such file or directory", "File does not exist" })
    void shouldReturnFalseWhenFileDoesNotExist(String errorMessage)
    {
        PullsFiles pullFileDriver = mock();
        when(webDriverProvider.getUnwrapped(PullsFiles.class)).thenReturn(pullFileDriver);
        when(pullFileDriver.pullFile(DEVICE_FILE_PATH)).thenThrow(new WebDriverException(errorMessage));

        assertFalse(deviceActions.fileExists(DEVICE_FILE_PATH));
        verify(pullFileDriver).pullFile(DEVICE_FILE_PATH);
        verifyNoMoreInteractions(webDriverProvider);
    }

    @Test
    void shouldRethrowWhenErrorSaysFileIsNotAccessible()
    {
        PullsFiles pullFileDriver = mock();
        when(webDriverProvider.getUnwrapped(PullsFiles.class)).thenReturn(pullFileDriver);
        var exception = new WebDriverException("File does not exist or is not accessible");
        when(pullFileDriver.pullFile(DEVICE_FILE_PATH)).thenThrow(exception);

        assertSame(exception, assertThrows(WebDriverException.class,
                () -> deviceActions.fileExists(DEVICE_FILE_PATH)));
    }

    @Test
    void shouldRethrowUnexpectedPullFileErrorWhenCheckingFileExistence()
    {
        PullsFiles pullFileDriver = mock();
        when(webDriverProvider.getUnwrapped(PullsFiles.class)).thenReturn(pullFileDriver);
        var exception = new WebDriverException("Connection timed out");
        when(pullFileDriver.pullFile(DEVICE_FILE_PATH)).thenThrow(exception);

        assertSame(exception, assertThrows(WebDriverException.class,
                () -> deviceActions.fileExists(DEVICE_FILE_PATH)));
    }

    @Test
    void shouldRethrowPullFileErrorWithoutMessageWhenCheckingFileExistence()
    {
        PullsFiles pullFileDriver = mock();
        when(webDriverProvider.getUnwrapped(PullsFiles.class)).thenReturn(pullFileDriver);
        WebDriverException exception = mock();
        when(exception.getMessage()).thenReturn(null);
        when(pullFileDriver.pullFile(DEVICE_FILE_PATH)).thenThrow(exception);

        assertSame(exception, assertThrows(WebDriverException.class,
                () -> deviceActions.fileExists(DEVICE_FILE_PATH)));
    }
}
