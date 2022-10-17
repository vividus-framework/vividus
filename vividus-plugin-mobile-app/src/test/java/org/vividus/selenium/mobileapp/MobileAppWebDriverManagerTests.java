/*
 * Copyright 2019-2022 the original author or authors.
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

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
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.Response;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.session.WebDriverSessionAttributes;
import org.vividus.selenium.session.WebDriverSessionInfo;
import org.vividus.ui.action.JavascriptActions;

import io.appium.java_client.ExecutesMethod;
import io.appium.java_client.android.HasAndroidDeviceDetails;
import io.appium.java_client.remote.MobilePlatform;

@ExtendWith(MockitoExtension.class)
class MobileAppWebDriverManagerTests
{
    private static final String GET_SESSION_COMMAND = "getSession";
    private static final String STAT_BAR_HEIGHT = "statBarHeight";
    private static final String HEIGHT = "height";
    private static final String MOBILE_DEVICE_SCREEN_INFO_JS = "mobile:deviceScreenInfo";
    private static final Map<String, Object> STATUS_BAR_SIZE =
        Map.of("statusBarSize", Map.of("width", 375, HEIGHT, 44), "scale", 3);

    private static final byte[] IMAGE = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A,
            0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00,
            0x01, 0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89, 0x00, 0x00, 0x00, 0x0B, 0x49,
            0x44, 0x41, 0x54, 0x78, (byte) 0xDA, 0x63, 0x60, 0x00, 0x02, 0x00, 0x00, 0x05, 0x00, 0x01, (byte) 0xE9,
            (byte) 0xFA, (byte) 0xDC, (byte) 0xD8, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE,
            0x42, 0x60, (byte) 0x82 };

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private JavascriptActions javascriptActions;
    @Mock private WebDriverSessionInfo webDriverSessionInfo;
    @InjectMocks private MobileAppWebDriverManager driverManager;

    private int mockStatusBarHeightRetrieval()
    {
        long height = 101L;
        mockGetSession(Map.of(STAT_BAR_HEIGHT, height));
        return (int) height;
    }

    @Test
    void shouldProvideStatusBarHeightForAndroid()
    {
        mockCapabilities(MobilePlatform.ANDROID);
        var driverWithAndroidDeviceDetails = mock(HasAndroidDeviceDetails.class);
        when(webDriverProvider.getUnwrapped(HasAndroidDeviceDetails.class)).thenReturn(driverWithAndroidDeviceDetails);
        when(driverWithAndroidDeviceDetails.getSystemBars()).thenReturn(Map.of("statusBar", Map.of(HEIGHT, 100L)));
        assertEquals(100, driverManager.getStatusBarSize());
    }

    @Test
    void shouldTryToGetStatBarHeightInCaseOfWebDriverException()
    {
        mockCapabilities(MobilePlatform.ANDROID);
        var driverWithAndroidDeviceDetails = mock(HasAndroidDeviceDetails.class);
        when(webDriverProvider.getUnwrapped(HasAndroidDeviceDetails.class)).thenReturn(driverWithAndroidDeviceDetails);
        when(driverWithAndroidDeviceDetails.getSystemBars()).thenThrow(new WebDriverException());
        int statusBarHeight = mockStatusBarHeightRetrieval();
        assertEquals(statusBarHeight, driverManager.getStatusBarSize());
    }

    @Test
    void shouldProviderStatusBarHeightForIos()
    {
        mockCapabilities(MobilePlatform.IOS);
        int statusBarHeight = mockStatusBarHeightRetrieval();
        assertEquals(statusBarHeight, driverManager.getStatusBarSize());
    }

    @Test
    void shouldProviderStatusBarHeightForIosWhenSauceLabsThrowAnError()
    {
        mockCapabilities(MobilePlatform.IOS);
        ExecutesMethod executingMethodDriver = mock(ExecutesMethod.class);
        when(webDriverProvider.getUnwrapped(ExecutesMethod.class)).thenReturn(executingMethodDriver);
        when(executingMethodDriver.execute(GET_SESSION_COMMAND)).thenThrow(new WebDriverException(
                "failed serving request GET https://production-sushiboat.default/wd/hub/session/XXXX"));
        when(javascriptActions.executeScript(MOBILE_DEVICE_SCREEN_INFO_JS)).thenReturn(STATUS_BAR_SIZE);
        assertEquals(44, driverManager.getStatusBarSize());
    }

    @Test
    void shouldPerformJsRequestForStatBarHeightWhenSessionDetailsStatBarHeightIsNullForIos()
    {
        mockCapabilities(MobilePlatform.IOS);
        mockGetSession(Collections.EMPTY_MAP);
        when(javascriptActions.executeScript(MOBILE_DEVICE_SCREEN_INFO_JS)).thenReturn(STATUS_BAR_SIZE);
        assertEquals(44, driverManager.getStatusBarSize());
    }

    @Test
    void shouldThrowAnErrorWhenSessionDetailsStatBarHeightIsNullForAndroid()
    {
        mockCapabilities(MobilePlatform.ANDROID);
        when(webDriverProvider.getUnwrapped(HasAndroidDeviceDetails.class)).thenThrow(new WebDriverException("Ooops!"));
        ExecutesMethod executingMethodDriver = mock(ExecutesMethod.class);

        when(webDriverProvider.getUnwrapped(ExecutesMethod.class)).thenReturn(executingMethodDriver);
        Response response = new Response();
        response.setValue(Collections.EMPTY_MAP);

        when(executingMethodDriver.execute(GET_SESSION_COMMAND)).thenReturn(response);
        var exception = assertThrows(IllegalStateException.class, driverManager::getStatusBarSize);
        assertEquals("Unable to receive status bar height. Received value is null", exception.getMessage());
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
        Capabilities capabilities = new MutableCapabilities(Map.of(CapabilityType.PLATFORM_NAME, platform));
        when(((HasCapabilities) webDriver).getCapabilities()).thenReturn(capabilities);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldProvideDprForAndroidNativeApp()
    {
        var capabilities = new MutableCapabilities(Map.of(CapabilityType.PLATFORM_NAME, MobilePlatform.IOS));
        var webDriver = mock(WebDriver.class,
                withSettings().extraInterfaces(HasCapabilities.class, TakesScreenshot.class));
        when(((HasCapabilities) webDriver).getCapabilities()).thenReturn(capabilities);
        when(((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES)).thenReturn(IMAGE);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriverProvider.getUnwrapped(TakesScreenshot.class)).thenReturn((TakesScreenshot) webDriver);
        when(webDriverSessionInfo.get(eq(WebDriverSessionAttributes.DEVICE_PIXEL_RATIO),
                any(Supplier.class))).thenAnswer(invocation -> ((Supplier<?>) invocation.getArguments()[1]).get());
        var webDriverManager = new MobileAppWebDriverManager(webDriverProvider, webDriverSessionInfo, javascriptActions)
        {
            @Override
            public Dimension getSize()
            {
                return new Dimension(1, 1);
            }
        };
        assertEquals(1d, webDriverManager.getDpr());
        mockCapabilities(MobilePlatform.ANDROID);
        when(webDriverSessionInfo.get(eq(WebDriverSessionAttributes.DEVICE_PIXEL_RATIO),
                any(Supplier.class))).thenAnswer(invocation -> ((Supplier<?>) invocation.getArguments()[1]).get());

        assertEquals(1d, driverManager.getDpr());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldProvideDprForIOS()
    {
        mockCapabilities(MobilePlatform.IOS);
        when(webDriverSessionInfo.get(eq(WebDriverSessionAttributes.DEVICE_PIXEL_RATIO),
                any(Supplier.class))).thenAnswer(invocation -> ((Supplier<?>) invocation.getArguments()[1]).get());
        when(webDriverSessionInfo.get(eq(WebDriverSessionAttributes.SCREEN_SIZE), any(Supplier.class)))
                .thenReturn(new Dimension(1, 1));

        TakesScreenshot taker = mock(TakesScreenshot.class);
        when(webDriverProvider.getUnwrapped(TakesScreenshot.class)).thenReturn(taker);
        when(taker.getScreenshotAs(OutputType.BYTES)).thenReturn(IMAGE);
        assertEquals(1d, driverManager.getDpr());
        verify(webDriverProvider).getUnwrapped(TakesScreenshot.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldProvideDprWithoutCalculation()
    {
        double dpr = 3d;
        when(webDriverSessionInfo.get(eq(WebDriverSessionAttributes.DEVICE_PIXEL_RATIO),
                any(Supplier.class))).thenReturn(dpr);
        assertEquals(dpr, driverManager.getDpr());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldRethrowIOException()
    {
        try (MockedStatic<ImageIO> imageIo = Mockito.mockStatic(ImageIO.class))
        {
            mockCapabilities(MobilePlatform.IOS);
            imageIo.when(() -> ImageIO.read(any(InputStream.class))).thenThrow(new IOException("io is oi"));
            TakesScreenshot taker = mock(TakesScreenshot.class);
            when(webDriverProvider.getUnwrapped(TakesScreenshot.class)).thenReturn(taker);
            when(taker.getScreenshotAs(OutputType.BYTES)).thenReturn(IMAGE);
            when(webDriverSessionInfo.get(eq(WebDriverSessionAttributes.DEVICE_PIXEL_RATIO),
                    any(Supplier.class))).thenAnswer(invocation -> ((Supplier<?>) invocation.getArguments()[1]).get());
            assertThrows(UncheckedIOException.class, driverManager::getDpr);
        }
    }

    private void mockGetSession(Object value)
    {
        ExecutesMethod executingMethodDriver = mock(ExecutesMethod.class);
        when(webDriverProvider.getUnwrapped(ExecutesMethod.class)).thenReturn(executingMethodDriver);
        Response response = new Response();
        response.setValue(value);
        when(executingMethodDriver.execute(GET_SESSION_COMMAND)).thenReturn(response);
    }
}
