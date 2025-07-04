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

import java.nio.file.Path;
import java.util.Optional;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.CDPSession;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.testcontext.TestContext;
import org.vividus.ui.web.playwright.network.NetworkContext;

public class BrowserContextProvider
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserContextProvider.class);
    private static final Class<BrowserContext> BROWSER_CONTEXT_KEY = BrowserContext.class;
    private static final Class<PlaywrightContext> PLAYWRIGHT_CONTEXT_KEY = PlaywrightContext.class;
    private static final Class<CDPSession> CDP_SESSION_KEY = CDPSession.class;

    private final BrowserType browserType;
    private final LaunchOptions launchOptions;
    private final TestContext testContext;
    private final BrowserContextConfiguration browserContextConfiguration;
    private final NetworkContext networkContext;

    public BrowserContextProvider(BrowserType browserType, LaunchOptions launchOptions, TestContext testContext,
            BrowserContextConfiguration browserContextConfiguration, NetworkContext networkContext)
    {
        this.browserType = browserType;
        this.launchOptions = launchOptions;
        this.testContext = testContext;
        this.browserContextConfiguration = browserContextConfiguration;
        this.networkContext = networkContext;
    }

    public BrowserContext get()
    {
        return testContext.get(BROWSER_CONTEXT_KEY, () -> {
            PlaywrightContext context = getPlaywrightContext();
            Browser browser = context.browser;
            if (!browser.isConnected())
            {
                browser = launchBrowserWithCurrentOptions(context.playwright);
                testContext.put(PLAYWRIGHT_CONTEXT_KEY, new PlaywrightContext(context.playwright, browser));
            }
            BrowserContext browserContext = browser.newContext();
            networkContext.listenNetwork(browserContext);
            if (browserContextConfiguration.isTracingEnabled())
            {
                browserContext.tracing().start(browserContextConfiguration.getTracingOptions());
            }
            long browserContextTimeout = browserContextConfiguration.getTimeout().toMillis();
            browserContext.setDefaultTimeout(browserContextTimeout);
            return browserContext;
        });
    }

    public CDPSession getCdpSession(Page page)
    {
        return testContext.get(CDP_SESSION_KEY, () -> {
            CDPSession cdpSession = page.context().newCDPSession(page);
            page.onClose(p -> cdpSession.detach());
            return cdpSession;
        });
    }

    private PlaywrightContext getPlaywrightContext()
    {
        return testContext.get(PLAYWRIGHT_CONTEXT_KEY, () -> {
            Playwright playwright = Playwright.create();
            Browser browser = launchBrowserWithCurrentOptions(playwright);
            return new PlaywrightContext(playwright, browser);
        });
    }

    public void closeBrowserContext()
    {
        Optional.ofNullable(testContext.get(BROWSER_CONTEXT_KEY, BrowserContext.class)).ifPresent(
                browserContext -> {
                    if (browserContextConfiguration.isTracingEnabled())
                    {
                        String tracesArchiveFileName = "traces-%s-%d.zip".formatted(Thread.currentThread().getName(),
                                System.currentTimeMillis());
                        Path tracesArchiveFilePath = browserContextConfiguration.getTracesOutputDirectory()
                                .resolve(tracesArchiveFileName);
                        browserContext.tracing().stop(new Tracing.StopOptions().setPath(tracesArchiveFilePath));
                        LOGGER.info("The recorded Playwright traces are saved at {}", tracesArchiveFilePath);
                    }
                    browserContext.pages().forEach(Page::close);
                    browserContext.close();
                    testContext.remove(BROWSER_CONTEXT_KEY);
                });
    }

    public void closePlaywrightContext()
    {
        Optional.ofNullable(testContext.get(PLAYWRIGHT_CONTEXT_KEY, PlaywrightContext.class))
                .ifPresent(playwrightContext -> {
                    playwrightContext.browser.close();
                    playwrightContext.playwright.close();
                    testContext.remove(PLAYWRIGHT_CONTEXT_KEY);
                });
    }

    public void closeBrowserInstance()
    {
        Optional.ofNullable(testContext.get(PLAYWRIGHT_CONTEXT_KEY, PlaywrightContext.class))
                .ifPresent(playwrightContext -> playwrightContext.browser.close());
    }

    public boolean isBrowserContextInitialized()
    {
        return null != testContext.get(BROWSER_CONTEXT_KEY);
    }

    private Browser launchBrowserWithCurrentOptions(Playwright playwright)
    {
        return browserType.launchBrowser(playwright, launchOptions);
    }

    private record PlaywrightContext(Playwright playwright, Browser browser)
    {
    }
}
