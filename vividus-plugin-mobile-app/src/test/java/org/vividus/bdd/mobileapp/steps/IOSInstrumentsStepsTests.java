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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IGenericWebDriverManager;
import org.vividus.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
public class IOSInstrumentsStepsTests
{
    private static final String PROFILE_NAME = "profileName";
    private static final String PROFILE = "profile";

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private IGenericWebDriverManager webDriverManager;

    @Mock
    private RemoteWebDriver webDriver;

    @Captor
    private ArgumentCaptor<Map<String, Object>> captor;

    @InjectMocks
    private IOSInstrumentsSteps instrumentsSteps;

    @Test
    void shouldStartRecordingOfInstrument()
    {
        when(webDriverManager.isIOS()).thenReturn(true);
        when(webDriverProvider.getUnwrapped(RemoteWebDriver.class)).thenReturn(webDriver);

        instrumentsSteps.startRecordingOfInstrument(PROFILE);

        verify(webDriver).executeScript(eq("mobile: startPerfRecord"), captor.capture());
        Map<String, Object> scriptParams = captor.getValue();
        assertEquals("current", scriptParams.get("pid"));
        assertEquals(PROFILE, scriptParams.get(PROFILE_NAME));
    }

    @Test
    void shouldStopRecordingInstrumentData() throws IOException
    {
        when(webDriverManager.isIOS()).thenReturn(true);
        when(webDriverProvider.getUnwrapped(RemoteWebDriver.class)).thenReturn(webDriver);
        String text = "text";
        Path tempFilepath = ResourceUtils.createTempFile("test", ".trace", null);
        when(webDriver.executeScript(eq("mobile: stopPerfRecord"), captor.capture())).thenReturn(text);

        instrumentsSteps.stopRecordingInstrumentData(PROFILE, tempFilepath.toString());

        Map<String, Object> scriptParams = captor.getValue();
        assertEquals(PROFILE, scriptParams.get(PROFILE_NAME));
        byte[] data = Files.readAllBytes(tempFilepath);
        assertEquals(text, new String(Base64.getMimeEncoder().encode(data), StandardCharsets.UTF_8));
    }

    @Test
    void shouldFailInNonIOSRun() throws IOException
    {
        when(webDriverManager.isIOS()).thenReturn(false);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> instrumentsSteps.startRecordingOfInstrument(PROFILE));
        assertEquals("Step is only supported on IOS devices", exception.getMessage());
    }
}
