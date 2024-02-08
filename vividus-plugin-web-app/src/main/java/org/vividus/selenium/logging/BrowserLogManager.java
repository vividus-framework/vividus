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
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.Browser;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.WebDriverManager;

public class BrowserLogManager
{
    private final IWebDriverProvider webDriverProvider;

    public BrowserLogManager(IWebDriverProvider webDriverProvider)
    {
        this.webDriverProvider = webDriverProvider;
    }

    public Set<LogEntry> getFilteredLog(Set<BrowserLogLevel> logLevelsToInclude)
    {
        LogEntries log = getLog(false).get();
        return logLevelsToInclude.stream()
                .map(BrowserLogLevel::getLevel)
                .flatMap(level -> filter(log, level))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void resetBuffer(boolean ignoreUnsupportedDrivers)
    {
        getLog(ignoreUnsupportedDrivers);
    }

    private Optional<LogEntries> getLog(boolean ignoreUnsupportedDrivers)
    {
        WebDriver driver = webDriverProvider.get();
        // The Selenium log API isn't supported: https://github.com/w3c/webdriver/issues/406
        // Safari: https://developer.apple.com/documentation/webkit/macos_webdriver_commands_for_safari_11_1_and_earlier
        if (WebDriverManager.isBrowserAnyOf(driver, Browser.FIREFOX, Browser.IE, Browser.SAFARI))
        {
            if (ignoreUnsupportedDrivers)
            {
                return Optional.empty();
            }
            throw new IllegalStateException("Browser does not support retrieval of browser logs");
        }
        return Optional.of(driver.manage().logs().get(LogType.BROWSER));
    }

    private static Stream<LogEntry> filter(LogEntries log, Level level)
    {
        int levelValue = level.intValue();
        return log.getAll().stream().filter(logEntry -> levelValue == logEntry.getLevel().intValue());
    }
}
