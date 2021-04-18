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

package org.vividus.selenium.driver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.TouchScreen;

@ExtendWith(MockitoExtension.class)
class DelegatingWebDriverTests
{
    private static final String ADVANCED_INTERACTION_NOT_SUPPORTED = "Underlying driver does not implement advanced"
            + " user interactions yet.";
    private static final String JAVASCRIPT_NOT_SUPPORTED = "Underlying driver instance does not support executing"
            + " javascript";
    private static final String SCRIPT = "testScript";

    @Mock
    private WebDriver wrappedDriver;

    @InjectMocks
    private DelegatingWebDriver delegatingWebDriver;

    @Test
    void testGet()
    {
        String url = "URL";
        delegatingWebDriver.get(url);
        verify(wrappedDriver).get(url);
    }

    @Test
    void testGetCurrentUrl()
    {
        String currentUrl = "CurrentUrl";
        when(wrappedDriver.getCurrentUrl()).thenReturn(currentUrl);
        assertEquals(currentUrl, delegatingWebDriver.getCurrentUrl());
    }

    @Test
    void testGetTitle()
    {
        String title = "Title";
        when(wrappedDriver.getTitle()).thenReturn(title);
        assertEquals(title, delegatingWebDriver.getTitle());
    }

    @Test
    void testFindElements()
    {
        By by = Mockito.mock(By.class);
        List<WebElement> list = new ArrayList<>();
        WebElement element = Mockito.mock(WebElement.class);
        list.add(element);
        when(wrappedDriver.findElements(by)).thenReturn(list);
        assertEquals(list, delegatingWebDriver.findElements(by));
    }

    @Test
    void testFindElement()
    {
        By by = Mockito.mock(By.class);
        WebElement element = Mockito.mock(WebElement.class);
        when(wrappedDriver.findElement(by)).thenReturn(element);
        assertEquals(element, delegatingWebDriver.findElement(by));
    }

    @Test
    void testGetPageSource()
    {
        String pageSource = "PageSource";
        when(wrappedDriver.getPageSource()).thenReturn(pageSource);
        assertEquals(pageSource, delegatingWebDriver.getPageSource());
    }

    @Test
    void testClose()
    {
        delegatingWebDriver.close();
        verify(wrappedDriver).close();
    }

    @Test
    void testQuit()
    {
        delegatingWebDriver.quit();
        verify(wrappedDriver).quit();
    }

    @Test
    void testGetWindowHandles()
    {
        String window = "windowHandle";
        Set<String> windows = new HashSet<>();
        windows.add(window);
        when(wrappedDriver.getWindowHandles()).thenReturn(windows);
        assertEquals(windows, delegatingWebDriver.getWindowHandles());
    }

    @Test
    void testGetWindowHandle()
    {
        String window = "Window";
        when(wrappedDriver.getWindowHandle()).thenReturn(window);
        assertEquals(window, delegatingWebDriver.getWindowHandle());
    }

    @Test
    void testSwitchTo()
    {
        TargetLocator targetLocator = Mockito.mock(TargetLocator.class);
        when(wrappedDriver.switchTo()).thenReturn(targetLocator);
        assertEquals(targetLocator, delegatingWebDriver.switchTo());
    }

    @Test
    void testNavigate()
    {
        Navigation navigation = Mockito.mock(Navigation.class);
        when(wrappedDriver.navigate()).thenReturn(navigation);
        assertEquals(navigation, delegatingWebDriver.navigate());
    }

    @Test
    void testManage()
    {
        Options options = Mockito.mock(Options.class);
        when(wrappedDriver.manage()).thenReturn(options);
        assertEquals(options, delegatingWebDriver.manage());
    }

    @Test
    void testGetWrappedDriver()
    {
        assertEquals(wrappedDriver, delegatingWebDriver.getWrappedDriver());
    }

    @Test
    void testGetTouch()
    {
        WebDriver driverWithTouchScreen = Mockito.mock(WebDriver.class,
                withSettings().extraInterfaces(HasTouchScreen.class));
        TouchScreen touchScreen = Mockito.mock(TouchScreen.class);
        when(((HasTouchScreen) driverWithTouchScreen).getTouch()).thenReturn(touchScreen);
        assertEquals(touchScreen, new DelegatingWebDriver(driverWithTouchScreen).getTouch());
    }

    @Test
    void testGetTouchUnsupportedOperationException()
    {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                delegatingWebDriver::getTouch);
        assertEquals(ADVANCED_INTERACTION_NOT_SUPPORTED, exception.getMessage());
    }

    @Test
    void testGetKeyboard()
    {
        WebDriver driverWithInputDevices = Mockito.mock(WebDriver.class,
                withSettings().extraInterfaces(HasInputDevices.class));
        Keyboard keyboard = Mockito.mock(Keyboard.class);
        when(((HasInputDevices) driverWithInputDevices).getKeyboard()).thenReturn(keyboard);
        assertEquals(keyboard, new DelegatingWebDriver(driverWithInputDevices).getKeyboard());
    }

    @Test
    void testGetKeyboardUnsupportedOperationException()
    {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                delegatingWebDriver::getKeyboard);
        assertEquals(ADVANCED_INTERACTION_NOT_SUPPORTED, exception.getMessage());
    }

    @Test
    void testGetMouse()
    {
        WebDriver driverWithInputDevices = Mockito.mock(WebDriver.class,
                withSettings().extraInterfaces(HasInputDevices.class));
        Mouse mouse = Mockito.mock(Mouse.class);
        when(((HasInputDevices) driverWithInputDevices).getMouse()).thenReturn(mouse);
        assertEquals(mouse, new DelegatingWebDriver(driverWithInputDevices).getMouse());
    }

    @Test
    void testGetMouseUnsupportedOperationException()
    {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                delegatingWebDriver::getMouse);
        assertEquals(ADVANCED_INTERACTION_NOT_SUPPORTED, exception.getMessage());
    }

    @Test
    void testGetScreenshotAs()
    {
        WebDriver takesScreenshotDriver = Mockito.mock(WebDriver.class,
                withSettings().extraInterfaces(TakesScreenshot.class));
        File file = Mockito.mock(File.class);
        when(((TakesScreenshot) takesScreenshotDriver).getScreenshotAs(OutputType.FILE)).thenReturn(file);
        assertEquals(file, new DelegatingWebDriver(takesScreenshotDriver).getScreenshotAs(OutputType.FILE));
    }

    @Test
    void testGetScreenshotAsUnsupportedOperationException()
    {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
            () -> delegatingWebDriver.getScreenshotAs(OutputType.FILE));
        assertEquals("Underlying driver instance does not support taking screenshots", exception.getMessage());
    }

    @Test
    void testExecuteScript()
    {
        WebDriver javascriptExecutorDriver = Mockito.mock(WebDriver.class,
                withSettings().extraInterfaces(JavascriptExecutor.class));
        DelegatingWebDriver javascriptExecutorDelegatingDriver = new DelegatingWebDriver(javascriptExecutorDriver);
        javascriptExecutorDelegatingDriver.executeScript(SCRIPT);
        verify((JavascriptExecutor) javascriptExecutorDriver).executeScript(SCRIPT);
    }

    @Test
    void testExecuteScriptUnsupportedOperationException()
    {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
            () -> delegatingWebDriver.executeScript(SCRIPT));
        assertEquals(JAVASCRIPT_NOT_SUPPORTED, exception.getMessage());
    }

    @Test
    void testExecuteAsyncScript()
    {
        WebDriver javascriptExecutorDriver = Mockito.mock(WebDriver.class,
                withSettings().extraInterfaces(JavascriptExecutor.class));
        DelegatingWebDriver javascriptExecutorDelegatingDriver = new DelegatingWebDriver(javascriptExecutorDriver);
        javascriptExecutorDelegatingDriver.executeAsyncScript(SCRIPT);
        verify((JavascriptExecutor) javascriptExecutorDriver).executeAsyncScript(SCRIPT);
    }

    @Test
    void testExecuteAsyncScriptUnsupportedOperationException()
    {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
            () -> delegatingWebDriver.executeAsyncScript(SCRIPT));
        assertEquals(JAVASCRIPT_NOT_SUPPORTED, exception.getMessage());
    }

    @Test
    void testGetCapabilities()
    {
        WebDriver driverWithCapabilities = Mockito.mock(WebDriver.class,
                withSettings().extraInterfaces(HasCapabilities.class));
        Capabilities capabilities = Mockito.mock(Capabilities.class);
        when(((HasCapabilities) driverWithCapabilities).getCapabilities()).thenReturn(capabilities);
        assertEquals(capabilities, new DelegatingWebDriver(driverWithCapabilities).getCapabilities());
    }

    @Test
    void testGetCapabilitiesIllegalStateException()
    {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                delegatingWebDriver::getCapabilities);
        assertEquals("Wrapped WebDriver doesn't implement HasCapabilities interface", exception.getMessage());
    }

    @Test
    void testPerform()
    {
        WebDriver interactiveDriver = Mockito.mock(WebDriver.class, withSettings().extraInterfaces(Interactive.class));
        DelegatingWebDriver interactiveDelegatingDriver = new DelegatingWebDriver(interactiveDriver);
        interactiveDelegatingDriver.perform(List.of());
        verify((Interactive) interactiveDriver).perform(List.of());
    }

    @Test
    void testPerformUnsupportedOperationException()
    {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
            () -> delegatingWebDriver.perform(List.of()));
        assertEquals(ADVANCED_INTERACTION_NOT_SUPPORTED, exception.getMessage());
    }

    @Test
    void testResetInputState()
    {
        WebDriver interactiveDriver = Mockito.mock(WebDriver.class, withSettings().extraInterfaces(Interactive.class));
        DelegatingWebDriver interactiveDelegatingDriver = new DelegatingWebDriver(interactiveDriver);
        interactiveDelegatingDriver.resetInputState();
        verify((Interactive) interactiveDriver).resetInputState();
    }

    @Test
    void testResetInputStateUnsupportedOperationException()
    {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                delegatingWebDriver::resetInputState);
        assertEquals(ADVANCED_INTERACTION_NOT_SUPPORTED, exception.getMessage());
    }
}
