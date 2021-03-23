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

package org.vividus.bdd.mobileapp.steps;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.mobileapp.action.DeviceActions;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.ui.action.JavascriptActions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.android.nativekey.PressesKey;

@SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class DeviceStepsTests
{
    private static final String DEVICE_FOLDER = "/device/folder";

    @Mock private DeviceActions deviceActions;
    @Mock private GenericWebDriverManager genericWebDriverManager;
    @Mock private JavascriptActions javascriptActions;
    @Mock private IWebDriverProvider webDriverProvider;
    private DeviceSteps deviceSteps;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(DeviceSteps.class);

    @BeforeEach
    void init()
    {
        deviceSteps = new DeviceSteps(DEVICE_FOLDER, deviceActions, genericWebDriverManager, javascriptActions,
                webDriverProvider);
    }

    @Test
    void shoulUploadFileToDevice()
    {
        String fileName = "file.txt";
        String filePath = Paths.get("/local/fs/", fileName).toString();

        deviceSteps.uploadFileToDevice(filePath);

        verify(deviceActions).pushFile(Paths.get(DEVICE_FOLDER, fileName).toString(), filePath);
        assertThat(logger.getLoggingEvents(), is(List.of(
            info("Uploading file '{}' to a device at '{}' folder", filePath, DEVICE_FOLDER)
        )));
    }

    @Test
    void shouldPressIOSKey()
    {
        String key = "Home";

        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);

        deviceSteps.pressKey(key);

        verify(javascriptActions).executeScript("mobile: pressButton", Map.of("name", key));
        verifyNoInteractions(webDriverProvider);
    }

    @Test
    void shouldPressAndroidKey()
    {
        ArgumentCaptor<KeyEvent> keyCaptor = ArgumentCaptor.forClass(KeyEvent.class);
        PressesKey pressesKey = mock(PressesKey.class);
        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(false);
        when(webDriverProvider.getUnwrapped(PressesKey.class)).thenReturn(pressesKey);

        deviceSteps.pressKey(AndroidKey.SPACE.name());

        verify(pressesKey).pressKey(keyCaptor.capture());

        assertEquals(Map.of("keycode", 62), keyCaptor.getValue().build());
        verifyNoMoreInteractions(webDriverProvider, genericWebDriverManager);
    }

    @Test
    void shouldNotPressUnsupportedAndroidKey()
    {
        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(false);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deviceSteps.pressKey("unsupported key"));
        assertEquals("Unsupported Android key: unsupported key", exception.getMessage());
        verifyNoMoreInteractions(webDriverProvider, genericWebDriverManager);
    }
}
