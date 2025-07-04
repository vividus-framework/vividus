/*
 * Copyright 2019-2025 the original author or authors.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.CDPSession;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.Tracing.StartOptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.ui.web.playwright.network.NetworkContext;

@SuppressWarnings("PMD.CloseResource")
@ExtendWith(MockitoExtension.class)
class BrowserContextProviderTests
{
    private static final long TIMEOUT_MILLIS = 100;

    @Spy private final SimpleTestContext testContext = new SimpleTestContext();
    @Spy private final StartOptions tracingOptions = new StartOptions().setScreenshots(false).setSnapshots(false);
    @Mock private BrowserType browserType;
    @Mock private LaunchOptions launchOptions;
    @Mock private Path tracesOutputDirectory;
    @Mock private NetworkContext networkContext;
    private BrowserContextProvider browserContextProvider;

    @BeforeEach
    void setUp()
    {
        Duration duration = Duration.ofMillis(TIMEOUT_MILLIS);
        BrowserContextConfiguration browserContextConfiguration = new BrowserContextConfiguration(tracingOptions,
                duration, tracesOutputDirectory);
        browserContextProvider = new BrowserContextProvider(browserType, launchOptions, testContext,
                browserContextConfiguration, networkContext);
    }

    @Test
    void shouldCreateBrowserContextAtFirstRetrievalOnly()
    {
        try (var playwrightStaticMock = mockStatic(Playwright.class))
        {
            // First retrieval
            Playwright playwright = mock();
            playwrightStaticMock.when(Playwright::create).thenReturn(playwright);
            Browser browser = mock();
            when(browser.isConnected()).thenReturn(true);
            when(browserType.launchBrowser(playwright, launchOptions)).thenReturn(browser);
            BrowserContext browserContext = mock();
            when(browser.newContext()).thenReturn(browserContext);

            var actual = browserContextProvider.get();

            assertSame(browserContext, actual);
            verify(networkContext).listenNetwork(actual);
            verify(browserContext).setDefaultTimeout(TIMEOUT_MILLIS);
            verifyNoMoreInteractions(browserContext);
            playwrightStaticMock.reset();
            reset(playwright, browser);

            // Second retrieval
            var actual2 = browserContextProvider.get();

            assertSame(browserContext, actual2);
            verify(browserContext).setDefaultTimeout(TIMEOUT_MILLIS);
            verifyNoMoreInteractions(browserContext);
            verifyNoInteractions(playwright, browser);
            playwrightStaticMock.verifyNoInteractions();

            // Close context
            when(browserContext.pages()).thenReturn(List.of());
            browserContextProvider.closeBrowserContext();

            verify(browserContext).close();
            verifyNoMoreInteractions(browserContext);
            verifyNoInteractions(tracesOutputDirectory);
        }
    }

    @Test
    void shouldReCreateClosedBrowser()
    {
        Browser firstBrowserInstance = mock();
        Browser secondBrowserInstance = mock();
        Browser thirdBrowserInstance = mock();
        when(browserType.launchBrowser(any(), any())).thenReturn(firstBrowserInstance).thenReturn(secondBrowserInstance)
                .thenReturn(thirdBrowserInstance);
        when(firstBrowserInstance.newContext()).thenReturn(mock(BrowserContext.class));
        when(firstBrowserInstance.isConnected()).thenReturn(true).thenReturn(false);
        when(secondBrowserInstance.newContext()).thenReturn(mock(BrowserContext.class));
        when(secondBrowserInstance.isConnected()).thenReturn(false).thenReturn(false);
        when(thirdBrowserInstance.newContext()).thenReturn(mock(BrowserContext.class));

        browserContextProvider.get();
        browserContextProvider.closeBrowserContext();
        browserContextProvider.closeBrowserInstance();
        browserContextProvider.get();
        browserContextProvider.closeBrowserContext();
        browserContextProvider.closeBrowserInstance();
        browserContextProvider.get();

        verify(firstBrowserInstance).close();
        verify(secondBrowserInstance).close();
        verify(browserType, times(3)).launchBrowser(any(), any());
        verify(testContext, times(6)).put(any(), any());
    }

    static Stream<Arguments> tracingConfigurations()
    {
        return Stream.of(
                arguments(true, true),
                arguments(false, true),
                arguments(true, false)
        );
    }

    @ParameterizedTest
    @MethodSource("tracingConfigurations")
    void shouldCreateTracesArchiveWhenAnyTracingOptionIsEnabled(boolean tracingScreenshots, boolean tracingSnapshots)
    {
        tracingOptions.setScreenshots(tracingScreenshots).setSnapshots(tracingSnapshots);
        try (var playwrightStaticMock = mockStatic(Playwright.class))
        {
            // Retrieve context
            Playwright playwright = mock();
            playwrightStaticMock.when(Playwright::create).thenReturn(playwright);
            Browser browser = mock();
            when(browserType.launchBrowser(playwright, launchOptions)).thenReturn(browser);
            BrowserContext browserContext = mock();
            when(browser.newContext()).thenReturn(browserContext);
            Tracing tracing = mock();
            when(browserContext.tracing()).thenReturn(tracing);

            var actual = browserContextProvider.get();

            assertSame(browserContext, actual);
            verify(browserContext).setDefaultTimeout(TIMEOUT_MILLIS);
            verifyNoMoreInteractions(browserContext);
            verify(tracing).start(tracingOptions);
            playwrightStaticMock.reset();
            reset(playwright, browser);

            // Close context
            var fileNameArgumentCaptor = ArgumentCaptor.forClass(String.class);
            Path tracesArchivePath = mock();
            when(tracesOutputDirectory.resolve(fileNameArgumentCaptor.capture())).thenReturn(tracesArchivePath);

            Page page = mock();
            when(browserContext.pages()).thenReturn(List.of(page));
            browserContextProvider.closeBrowserContext();

            var ordered = inOrder(tracing, page, browserContext);
            var stopOptionsArgumentCaptor = ArgumentCaptor.forClass(Tracing.StopOptions.class);
            ordered.verify(tracing).stop(stopOptionsArgumentCaptor.capture());
            ordered.verify(page).close();
            ordered.verify(browserContext).close();

            verifyNoInteractions(browser, playwright);
            playwrightStaticMock.verifyNoInteractions();

            assertThat(fileNameArgumentCaptor.getValue(), matchesPattern("traces-.*-\\d{13}\\.zip"));
            assertEquals(tracesArchivePath, stopOptionsArgumentCaptor.getValue().path);

            // Open new context
            BrowserContext newBrowserContext = mock();
            when(browser.newContext()).thenReturn(newBrowserContext);
            Tracing newTracing = mock();
            when(newBrowserContext.tracing()).thenReturn(newTracing);

            var anotherActual = browserContextProvider.get();

            assertSame(newBrowserContext, anotherActual);
            assertNotSame(actual, anotherActual);
            verify(newBrowserContext).setDefaultTimeout(TIMEOUT_MILLIS);
            verifyNoMoreInteractions(newBrowserContext);
            verify(tracing).start(tracingOptions);
            verifyNoInteractions(playwright);
            playwrightStaticMock.verifyNoInteractions();
        }
    }

    @Test
    void shouldClosePlaywrightContext()
    {
        try (var playwrightStaticMock = mockStatic(Playwright.class))
        {
            Playwright playwright = mock();
            playwrightStaticMock.when(Playwright::create).thenReturn(playwright);
            Browser browser = mock();
            when(browserType.launchBrowser(playwright, launchOptions)).thenReturn(browser);
            BrowserContext browserContext = mock();
            when(browser.newContext()).thenReturn(browserContext);
            browserContextProvider.get();
            browserContextProvider.closePlaywrightContext();
            var ordered = inOrder(browser, playwright);
            ordered.verify(browser).close();
            ordered.verify(playwright).close();
        }
    }

    @ParameterizedTest
    @CsvSource({
            "BrowserContext, true",
            ",               false"
    })
    void shouldCheckBrowserContextInitStatus(String browserContext, boolean initStatus)
    {
        when(testContext.get(BrowserContext.class)).thenReturn(browserContext);
        assertEquals(initStatus, browserContextProvider.isBrowserContextInitialized());
    }

    @Test
    void shouldCreateCdpSession()
    {
        Page page = mock();
        BrowserContext context = mock();
        when(page.context()).thenReturn(context);
        browserContextProvider.getCdpSession(page);
        verify(context).newCDPSession(page);
        verify(page).onClose(any());
    }

    @Test
    void shouldReturnCdpSession()
    {
        Page page = mock();
        testContext.put(CDPSession.class, mock(CDPSession.class));
        browserContextProvider.getCdpSession(page);
        verifyNoInteractions(page);
    }
}
