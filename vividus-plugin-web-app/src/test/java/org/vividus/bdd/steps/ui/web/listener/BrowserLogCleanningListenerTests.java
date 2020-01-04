/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.bdd.steps.ui.web.listener;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;

class BrowserLogCleanningListenerTests
{
    private final BrowserLogCleanningListener listener = new BrowserLogCleanningListener();

    @TestFactory
    Stream<DynamicTest> testCleanBeforeNavigate()
    {
        WebDriver driver = mock(WebDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        Consumer<Runnable> test = methodUnderTest ->
        {
            Options options = mock(Options.class);
            when(driver.manage()).thenReturn(options);
            Logs logs = mock(Logs.class);
            when(options.logs()).thenReturn(logs);
            when(logs.get(LogType.BROWSER)).thenReturn(mock(LogEntries.class));
            methodUnderTest.run();
        };
        return Stream.of(
                dynamicTest("beforeNavigateBack", () -> test.accept(() -> listener.beforeNavigateBack(driver))),
                dynamicTest("beforeNavigateForward", () -> test.accept(() -> listener.beforeNavigateForward(driver))),
                dynamicTest("beforeNavigateRefresh", () -> test.accept(() -> listener.beforeNavigateRefresh(driver))),
                dynamicTest("beforeNavigateTo", () -> test.accept(() -> listener.beforeNavigateTo("url", driver))));
    }
}
