/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.ui.web.playwright;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Function;
import java.util.stream.Stream;

import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Playwright;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BrowserTypeTests
{
    static Stream<Arguments> browsers()
    {
        return Stream.of(
                arguments(BrowserType.CHROMIUM, (Function<Playwright, com.microsoft.playwright.BrowserType>)
                        Playwright::chromium),
                arguments(BrowserType.FIREFOX, (Function<Playwright, com.microsoft.playwright.BrowserType>)
                        Playwright::firefox),
                arguments(BrowserType.WEB_KIT, (Function<Playwright, com.microsoft.playwright.BrowserType>)
                        Playwright::webkit)
        );
    }

    @SuppressWarnings("PMD.CloseResource")
    @ParameterizedTest
    @MethodSource("browsers")
    void shouldLaunchBrowser(BrowserType type, Function<Playwright, com.microsoft.playwright.BrowserType> factory)
    {
        Playwright playwright = mock();
        com.microsoft.playwright.BrowserType browserType = mock();
        when(factory.apply(playwright)).thenReturn(browserType);
        LaunchOptions launchOptions = mock();
        type.launchBrowser(playwright, launchOptions);
        verify(browserType).launch(launchOptions);
    }
}
