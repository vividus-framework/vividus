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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.Browser;
import org.vividus.selenium.manager.WebDriverManager;

public final class BrowserLogManager
{
    private BrowserLogManager()
    {
    }

    public static Set<LogEntry> getFilteredLog(WebDriver webDriver, Set<BrowserLogLevel> logLevelsToInclude)
    {
        if (doesWebDriverSupportLogApi(webDriver))
        {
            LogEntries log = getLog(webDriver);
            return logLevelsToInclude.stream()
                    .map(BrowserLogLevel::getLevel)
                    .map(Level::intValue)
                    .flatMap(level -> log.getAll().stream().filter(logEntry -> level == logEntry.getLevel().intValue()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        throw new IllegalStateException("Browser does not support retrieval of browser logs");
    }

    public static void resetBuffer(WebDriver webDriver)
    {
        if (doesWebDriverSupportLogApi(webDriver))
        {
            getLog(webDriver);
        }
    }

    private static LogEntries getLog(WebDriver webDriver)
    {
        return webDriver.manage().logs().get(LogType.BROWSER);
    }

    private static boolean doesWebDriverSupportLogApi(WebDriver webDriver)
    {
        // The Selenium log API isn't supported: https://github.com/w3c/webdriver/issues/406
        // Safari: https://developer.apple.com/documentation/webkit/macos_webdriver_commands_for_safari_11_1_and_earlier
        return !WebDriverManager.isBrowserAnyOf(webDriver, Browser.FIREFOX, Browser.IE, Browser.SAFARI);
    }
}
