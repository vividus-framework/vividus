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

import java.nio.file.Path;
import java.util.Optional;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.Tracing.StartOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.testcontext.TestContext;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class BrowserContextProvider
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserContextProvider.class);
    private static final Class<BrowserContext> BROWSER_CONTEXT_KEY = BrowserContext.class;

    private final BrowserType browserType;
    private final LaunchOptions launchOptions;
    private final Tracing.StartOptions tracingOptions;
    private final Path tracesOutputDirectory;
    private final TestContext testContext;

    private PlaywrightContext playwrightContext;

    public BrowserContextProvider(BrowserType browserType, LaunchOptions launchOptions, StartOptions tracingOptions,
            Path tracesOutputDirectory, TestContext testContext)
    {
        this.browserType = browserType;
        this.launchOptions = launchOptions;
        this.tracingOptions = tracingOptions;
        this.tracesOutputDirectory = tracesOutputDirectory;
        this.testContext = testContext;
    }

    public BrowserContext get()
    {
        return testContext.get(BROWSER_CONTEXT_KEY, () -> {
            BrowserContext browserContext = getPlaywrightContext().browser().newContext();
            if (isTracingEnabled())
            {
                browserContext.tracing().start(tracingOptions);
            }
            return browserContext;
        });
    }

    public void closeCurrentContext()
    {
        Optional.ofNullable(testContext.get(BROWSER_CONTEXT_KEY, BrowserContext.class)).ifPresent(
                browserContext -> {
                    if (isTracingEnabled())
                    {
                        String tracesArchiveFileName = "traces-%s-%d.zip".formatted(Thread.currentThread().getName(),
                                System.currentTimeMillis());
                        Path tracesArchiveFilePath = tracesOutputDirectory.resolve(tracesArchiveFileName);
                        browserContext.tracing().stop(new Tracing.StopOptions().setPath(tracesArchiveFilePath));
                        LOGGER.info("The recorded Playwright traces are saved at {}", tracesArchiveFilePath);
                    }
                    browserContext.pages().forEach(Page::close);
                    browserContext.close();
                    testContext.remove(BROWSER_CONTEXT_KEY);
                });
    }

    // Impossible because 'destroy' is invoked by Spring in thread-safe way
    @SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
    public void destroy()
    {
        Optional.ofNullable(playwrightContext).ifPresent(context -> {
            context.browser().close();
            context.playwright().close();
        });
    }

    // This implementation of DC Locking algorithm is correct,
    // check: https://shipilev.net/blog/2014/safe-public-construction/
    @SuppressFBWarnings({ "IS2_INCONSISTENT_SYNC", "DC_DOUBLECHECK" })
    // Playwright and Browser instances are closed in 'destroy' method
    @SuppressWarnings("PMD.CloseResource")
    private PlaywrightContext getPlaywrightContext()
    {
        PlaywrightContext pc = playwrightContext;
        if (pc == null)
        {
            synchronized (this)
            {
                pc = playwrightContext;
                if (pc == null)
                {
                    Playwright playwright = Playwright.create();
                    Browser browser = browserType.launchBrowser(playwright, launchOptions);
                    pc = new PlaywrightContext(playwright, browser);
                    playwrightContext = pc;
                }
            }
        }
        return pc;
    }

    private boolean isTracingEnabled()
    {
        return tracingOptions.screenshots || tracingOptions.snapshots;
    }

    private record PlaywrightContext(Playwright playwright, Browser browser)
    {
    }
}
