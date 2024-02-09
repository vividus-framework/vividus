/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.selenium.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.remote.Browser;

class BrowserLogManagerTests
{
    @Test
    void shouldReturnFilteredLogContainingEntries()
    {
        var webDriver = mockBrowserName(Browser.CHROME.browserName());
        var logEntries = mockLogRetrieval(webDriver);
        var filteredLog = BrowserLogManager.getFilteredLog(webDriver, Set.of(BrowserLogLevel.ERRORS));
        assertEquals(Set.of(logEntries.getAll().get(0)), filteredLog);
    }

    @Test
    void shouldReturnFilteredLogContainingNoEntries()
    {
        var webDriver = mockBrowserName(Browser.CHROME.browserName());
        mockLogRetrieval(webDriver);
        var filteredLog = BrowserLogManager.getFilteredLog(webDriver, Set.of(BrowserLogLevel.WARNINGS));
        assertEquals(Set.of(), filteredLog);
    }

    static Stream<Browser> notSupportedBrowsers()
    {
        return Stream.of(Browser.IE, Browser.SAFARI, Browser.FIREFOX);
    }

    @ParameterizedTest
    @MethodSource("notSupportedBrowsers")
    void shouldFailWhenBrowserDoesntSupportsLogs(Browser browser)
    {
        WebDriver webDriver = mockBrowserName(browser.browserName());
        var logLevelsToInclude = Set.of(BrowserLogLevel.ERRORS);
        var exception = assertThrows(IllegalStateException.class,
                () -> BrowserLogManager.getFilteredLog(webDriver, logLevelsToInclude));
        assertEquals("Browser does not support retrieval of browser logs", exception.getMessage());
    }

    @Test
    void shouldResetBuffer()
    {
        var webDriver = mockBrowserName(Browser.CHROME.browserName());
        Logs logs = mockLogsProvider(webDriver);
        BrowserLogManager.resetBuffer(webDriver);
        verify(logs).get(LogType.BROWSER);
    }

    @Test
    void shouldIgnoreUnsupportedFirefoxOnBufferReset()
    {
        var webDriver = mockBrowserName(Browser.FIREFOX.browserName());
        BrowserLogManager.resetBuffer(webDriver);
        verify(webDriver, never()).manage();
    }

    private WebDriver mockBrowserName(String browserName)
    {
        WebDriver webDriver = mock(withSettings().extraInterfaces(HasCapabilities.class));
        Capabilities capabilities = mock();
        when(((HasCapabilities) webDriver).getCapabilities()).thenReturn(capabilities);
        when(capabilities.getBrowserName()).thenReturn(browserName);
        return webDriver;
    }

    private static LogEntries mockLogRetrieval(WebDriver webDriver)
    {
        Logs logs = mockLogsProvider(webDriver);
        var severeEntry = new LogEntry(Level.SEVERE, 1L, "error message");
        var infoEntry = new LogEntry(Level.INFO, 1L, "info message");
        var logEntries = new LogEntries(Arrays.asList(severeEntry, infoEntry));
        when(logs.get(LogType.BROWSER)).thenReturn(logEntries);
        return logEntries;
    }

    private static Logs mockLogsProvider(WebDriver webDriver)
    {
        Options options = mock();
        when(webDriver.manage()).thenReturn(options);
        Logs logs = mock();
        when(options.logs()).thenReturn(logs);
        return logs;
    }
}
