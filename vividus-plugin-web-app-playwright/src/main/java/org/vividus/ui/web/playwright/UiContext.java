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

package org.vividus.ui.web.playwright;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Function;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import org.vividus.testcontext.TestContext;
import org.vividus.ui.ContextSourceCodeProvider;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;
import org.vividus.ui.web.playwright.locator.Visibility;

public class UiContext implements ContextSourceCodeProvider
{
    private static final Class<PlaywrightContext> PLAYWRIGHT_CONTEXT_KEY = PlaywrightContext.class;

    private final TestContext testContext;

    public UiContext(TestContext testContext)
    {
        this.testContext = testContext;
    }

    public void setCurrentPage(Page page)
    {
        getPlaywrightContext().page = page;
    }

    public Page getCurrentPage()
    {
        return getPlaywrightContext().page;
    }

    public void setCurrentFrame(FrameLocator frame)
    {
        getPlaywrightContext().frames.add(frame);
    }

    public FrameLocator getCurrentFrame()
    {
        return getPlaywrightContext().frames.isEmpty() ? null : getPlaywrightContext().frames.getLast();
    }

    public Locator getContext()
    {
        return getPlaywrightContext().context;
    }

    public void setContext(Locator context)
    {
        getPlaywrightContext().context = context;
    }

    public void reset()
    {
        getPlaywrightContext().frames.clear();
        resetContext();
    }

    public void resetToActiveFrame()
    {
        Deque<FrameLocator> frames = getPlaywrightContext().frames;
        while (!frames.isEmpty() && !frames.getLast().owner().isVisible())
        {
            frames.removeLast();
        }
    }

    public void resetContext()
    {
        getPlaywrightContext().context = null;
    }

    public Locator locateElement(PlaywrightLocator playwrightLocator)
    {
        String locator = playwrightLocator.getLocator();
        Locator locatorInContext = getInCurrentContext(
                context -> context.locator(locator),
                frame -> frame.locator(locator),
                page -> page.locator(locator)
        );
        return (playwrightLocator.getVisibility() == Visibility.VISIBLE)
                ? locatorInContext.locator("visible=true") : locatorInContext;
    }

    public Locator getCurrentContexOrPageRoot()
    {
        return getInCurrentContext(
                context -> context,
                FrameLocator::owner,
                page -> page.locator("//html/body")
        );
    }

    public void removePlaywrightContext()
    {
        testContext.remove(PLAYWRIGHT_CONTEXT_KEY);
    }

    @Override
    public Optional<String> getSourceCode()
    {
        return Optional.ofNullable(
                getInCurrentContext(
                        UiContext::getOuterHtml,
                        frame -> getOuterHtml(frame.locator("html")),
                        page -> Optional.ofNullable(page).map(Page::content).orElse(null)
                )
        );
    }

    private <R> R getInCurrentContext(Function<Locator, R> elementContextAction,
            Function<FrameLocator, R> frameContextAction, Function<Page, R> pageContextAction)
    {
        PlaywrightContext playwrightContext = getPlaywrightContext();
        Locator context = playwrightContext.context;
        if (context != null)
        {
            return elementContextAction.apply(context);
        }
        FrameLocator currentFrame = getCurrentFrame();
        if (currentFrame != null)
        {
            return frameContextAction.apply(currentFrame);
        }
        return pageContextAction.apply(playwrightContext.page);
    }

    private static String getOuterHtml(Locator context)
    {
        return (String) context.evaluate("el => el.outerHTML");
    }

    private PlaywrightContext getPlaywrightContext()
    {
        return testContext.get(PLAYWRIGHT_CONTEXT_KEY, PlaywrightContext::new);
    }

    private static final class PlaywrightContext
    {
        private Page page;
        private Locator context;
        private final LinkedList<FrameLocator> frames = new LinkedList<>();
    }
}
