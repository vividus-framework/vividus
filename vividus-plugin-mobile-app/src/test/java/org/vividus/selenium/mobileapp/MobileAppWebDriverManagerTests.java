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

package org.vividus.selenium.mobileapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.Response;
import org.vividus.selenium.IWebDriverProvider;

import io.appium.java_client.ExecutesMethod;
import io.appium.java_client.HasSessionDetails;
import io.appium.java_client.android.HasAndroidDeviceDetails;
import io.appium.java_client.remote.MobilePlatform;

@ExtendWith(MockitoExtension.class)
class MobileAppWebDriverManagerTests
{
    private static final String GET_SYSTEM_BARS = "getSystemBars";

    private static final String STAT_BAR_HEIGHT = "statBarHeight";

    private static final byte[] IMAGE = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A,
            0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00,
            0x01, 0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89, 0x00, 0x00, 0x00, 0x0B, 0x49,
            0x44, 0x41, 0x54, 0x78, (byte) 0xDA, 0x63, 0x60, 0x00, 0x02, 0x00, 0x00, 0x05, 0x00, 0x01, (byte) 0xE9,
            (byte) 0xFA, (byte) 0xDC, (byte) 0xD8, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE,
            0x42, 0x60, (byte) 0x82 };

    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private MobileAppWebDriverManager driverManager;

    @Test
    void shouldProvideStatusBarHeightForAndorid()
    {
        mockCapabilities(MobilePlatform.ANDROID);
        HasAndroidDeviceDetails hasAndroidDeviceDetails = mock(HasAndroidDeviceDetails.class,
                withSettings().extraInterfaces(ExecutesMethod.class));
        when(webDriverProvider.getUnwrapped(HasAndroidDeviceDetails.class)).thenReturn(hasAndroidDeviceDetails);
        Response response = mock(Response.class);
        when(response.getValue()).thenReturn(Map.of("statusBar", Map.of("height", 100L)));
        when(hasAndroidDeviceDetails.execute(GET_SYSTEM_BARS, Map.of())).thenReturn(response);
        assertEquals(100, driverManager.getStatusBarSize());
    }

    @Test
    void shouldTryToGetStatBarHeightInCaseOfWebDriverException()
    {
        HasAndroidDeviceDetails hasAndroidDeviceDetails = mock(HasAndroidDeviceDetails.class,
                withSettings().extraInterfaces(ExecutesMethod.class));
        mockCapabilities(MobilePlatform.ANDROID);
        when(webDriverProvider.getUnwrapped(HasAndroidDeviceDetails.class)).thenReturn(hasAndroidDeviceDetails);
        when(hasAndroidDeviceDetails.execute(GET_SYSTEM_BARS, Map.of())).thenThrow(new WebDriverException());
        HasSessionDetails details = mock(HasSessionDetails.class);
        when(webDriverProvider.getUnwrapped(HasSessionDetails.class)).thenReturn(details);
        when(details.getSessionDetail(STAT_BAR_HEIGHT)).thenReturn(101L);
        assertEquals(101, driverManager.getStatusBarSize());
    }

    @Test
    void shouldProviderStatusBarHeightForIos()
    {
        mockCapabilities(MobilePlatform.IOS);
        HasSessionDetails details = mock(HasSessionDetails.class);
        when(webDriverProvider.getUnwrapped(HasSessionDetails.class)).thenReturn(details);
        when(details.getSessionDetail(STAT_BAR_HEIGHT)).thenReturn(102L);
        assertEquals(102, driverManager.getStatusBarSize());
    }

    @Test
    void shouldProviderStatusBarHeightForTvOS()
    {
        mockCapabilities(MobilePlatform.TVOS);
        assertEquals(0, driverManager.getStatusBarSize());
        verify(webDriverProvider).get();
        verifyNoMoreInteractions(webDriverProvider);
    }

    private void mockCapabilities(String platform)
    {
        WebDriver webDriver = mock(WebDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        when(webDriverProvider.get()).thenReturn(webDriver);
        Capabilities capabilitiesMock = mock(Capabilities.class);
        when(((HasCapabilities) webDriver).getCapabilities()).thenReturn(capabilitiesMock);
        driverManager.setMobileApp(true);
        when(capabilitiesMock.getCapability(CapabilityType.PLATFORM_NAME)).thenReturn(platform);
    }

    @Test
    void shouldProvideDpr()
    {
        MobileAppWebDriverManager spyingDriverManager = new MobileAppWebDriverManager(webDriverProvider, null) {
            @Override
            public Dimension getSize()
            {
                return new Dimension(1, 1);
            }
        };
        TakesScreenshot taker = mock(TakesScreenshot.class);
        when(webDriverProvider.getUnwrapped(TakesScreenshot.class)).thenReturn(taker);
        when(taker.getScreenshotAs(OutputType.BYTES)).thenReturn(IMAGE);
        assertEquals(1d, spyingDriverManager.getDpr());
        assertEquals(1d, spyingDriverManager.getDpr());
        verify(webDriverProvider).getUnwrapped(TakesScreenshot.class);
    }

    @Test
    void shouldRethrowIOException()
    {
        try (MockedStatic<ImageIO> imageIo = Mockito.mockStatic(ImageIO.class))
        {
            imageIo.when(() -> ImageIO.read(any(InputStream.class))).thenThrow(new IOException("io is oi"));
            TakesScreenshot taker = mock(TakesScreenshot.class);
            when(webDriverProvider.getUnwrapped(TakesScreenshot.class)).thenReturn(taker);
            when(taker.getScreenshotAs(OutputType.BYTES)).thenReturn(IMAGE);
            assertThrows(UncheckedIOException.class, driverManager::getDpr);
        }
    }
}
