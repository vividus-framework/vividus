/*
 * Copyright 2019 the original author or authors.
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

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
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

import org.junit.jupiter.api.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.remote.BrowserType;

class BrowserLogManagerTests
{
    private static final String ERROR_MESSAGE = "error message";

    @Test
    void shouldReturnLogs()
    {
        WebDriver webDriver = mockBrowserName(BrowserType.CHROME);
        LogEntries logEntries = mockLogRetrieval(webDriver);
        assertEquals(logEntries, BrowserLogManager.getLog(webDriver));
    }

    @Test
    void shouldReturnFilteredLogContainingEntries()
    {
        WebDriver webDriver = mockBrowserName(BrowserType.CHROME);
        LogEntries logEntries = mockLogRetrieval(webDriver);
        Set<LogEntry> filteredLog = BrowserLogManager.getFilteredLog(webDriver, singleton(BrowserLogLevel.ERRORS));
        assertEquals(singleton(logEntries.getAll().get(0)), filteredLog);
    }

    @Test
    void shouldReturnFilteredLogContainingNoEntries()
    {
        WebDriver webDriver = mockBrowserName(BrowserType.CHROME);
        mockLogRetrieval(webDriver);
        Set<LogEntry> filteredLog = BrowserLogManager.getFilteredLog(webDriver, singleton(BrowserLogLevel.WARNINGS));
        assertEquals(emptySet(), filteredLog);
    }

    @Test
    void shouldNotReturnLogsForFirefox()
    {
        WebDriver webDriver = mockBrowserName(BrowserType.FIREFOX);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> BrowserLogManager.getLog(webDriver));
        assertEquals("Firefox does not support retrieval of browser logs", exception.getMessage());
    }

    @Test
    void shouldIgnoreUnsupportedFirefoxIfFlagIsSet()
    {
        WebDriver webDriver = mockBrowserName(BrowserType.FIREFOX);
        BrowserLogManager.resetBuffer(webDriver, true);
        verify(webDriver, never()).manage();
    }

    @Test
    void shouldIgnoreUnsupportedIExplorerIfFlagIsSet()
    {
        WebDriver webDriver = mockBrowserName(BrowserType.IEXPLORE);
        BrowserLogManager.resetBuffer(webDriver, true);
        verify(webDriver, never()).manage();
    }

    private static WebDriver mockBrowserName(String browserName)
    {
        WebDriver webDriver = mock(WebDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        Capabilities capabilities = mock(Capabilities.class);
        when(((HasCapabilities) webDriver).getCapabilities()).thenReturn(capabilities);
        when(capabilities.getBrowserName()).thenReturn(browserName);
        return webDriver;
    }

    private static LogEntries mockLogRetrieval(WebDriver webDriver)
    {
        Options options = mock(Options.class);
        when(webDriver.manage()).thenReturn(options);
        Logs logs = mock(Logs.class);
        when(options.logs()).thenReturn(logs);
        when(options.logs()).thenReturn(logs);
        LogEntry severeEntry = new LogEntry(Level.SEVERE, 1L, ERROR_MESSAGE);
        LogEntry infoEntry = new LogEntry(Level.INFO, 1L, ERROR_MESSAGE);
        LogEntries logEntries = new LogEntries(Arrays.asList(severeEntry, infoEntry));
        when(logs.get(LogType.BROWSER)).thenReturn(logEntries);
        return logEntries;
    }
}
