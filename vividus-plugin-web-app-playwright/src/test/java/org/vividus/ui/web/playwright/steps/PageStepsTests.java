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

package org.vividus.ui.web.playwright.steps;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.web.action.NavigateActions;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.UiContext;

@SuppressWarnings("PMD.CloseResource")
@ExtendWith(MockitoExtension.class)
class PageStepsTests
{
    private static final String PAGE_URL = "https://docs.vividus.dev";
    private static final String TAB_CLOSED = "Current tab has been closed";

    @Mock private BrowserContextProvider browserContextProvider;
    @Mock private UiContext uiContext;
    @Mock private NavigateActions navigateActions;
    @Mock private WebApplicationConfiguration webApplicationConfiguration;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private PageSteps pageSteps;

    @Test
    void shouldOpenMainApplicationPage()
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(URI.create(PAGE_URL));
        Page page = mock();
        when(uiContext.getCurrentPage()).thenReturn(page);

        pageSteps.openMainApplicationPage();

        verify(page).navigate(PAGE_URL);
        verifyNoMoreInteractions(page);
    }

    @Test
    void shouldOpenPageInCurrentTab()
    {
        Page page = mock();
        when(uiContext.getCurrentPage()).thenReturn(page);

        pageSteps.openPage(PAGE_URL);

        verify(page).navigate(PAGE_URL);
        verifyNoMoreInteractions(page);
    }

    @Test
    void shouldOpenPageInNewTab()
    {
        when(uiContext.getCurrentPage()).thenReturn(null);
        BrowserContext browserContext = mock();
        when(browserContextProvider.get()).thenReturn(browserContext);
        Page page = mock();
        when(browserContext.newPage()).thenReturn(page);

        pageSteps.openPage(PAGE_URL);
        verifyInteractionsForNewPage(page, 1);
    }

    @Test
    void shouldOpenNewTab()
    {
        BrowserContext browserContext = mock();
        when(browserContextProvider.get()).thenReturn(browserContext);
        Page newTab = mock();
        when(browserContext.newPage()).thenReturn(newTab);

        pageSteps.openNewTab();
        verifyInteractionsForNewPage(newTab, 0);
    }

    @Test
    void shouldCloseCurrentTab()
    {
        Page currentTab = mock();
        when(uiContext.getCurrentPage()).thenReturn(currentTab);

        Page page1 = mock();
        Page page2 = mock();
        List<Page> allTabs = List.of(page1, page2, currentTab);

        BrowserContext browserContext = mock();
        when(browserContextProvider.get()).thenReturn(browserContext);
        when(browserContext.pages()).thenReturn(allTabs).thenReturn(allTabs.subList(0, allTabs.size() - 2));

        pageSteps.closeCurrentTab();
        verify(currentTab).close();
        verify(uiContext).reset();
        verify(uiContext).setCurrentPage(page2);
        verify(softAssert).recordAssertion(true, TAB_CLOSED);
    }

    @Test
    void shouldNotCloseCurrentTabIfOnlyOneTabPresent()
    {
        Page currentTab = mock();
        when(uiContext.getCurrentPage()).thenReturn(currentTab);

        BrowserContext browserContext = mock();
        when(browserContextProvider.get()).thenReturn(browserContext);
        when(browserContext.pages()).thenReturn(List.of(currentTab));

        pageSteps.closeCurrentTab();
        verifyNoInteractions(currentTab);
        verify(softAssert).recordAssertion(false, TAB_CLOSED);
    }

    @Test
    void shouldRefreshPage()
    {
        pageSteps.refreshPage();
        verify(navigateActions).refresh();
    }

    @Test
    void shouldNavigateBack()
    {
        Page page = mock();
        when(uiContext.getCurrentPage()).thenReturn(page);

        pageSteps.navigateBack();

        verify(page).goBack();
        verifyNoMoreInteractions(page);
    }

    @Test
    void shouldOpenRelativeUrl()
    {
        Page page = mock();
        when(uiContext.getCurrentPage()).thenReturn(page);
        when(page.url()).thenReturn("https://example.com/path/foo");

        pageSteps.openRelativeUrl("stats");

        verify(uiContext.getCurrentPage()).navigate("https://example.com/path/stats");
    }

    @Test
    void shouldAssertPageTitle()
    {
        Page mockPage = mock();
        when(uiContext.getCurrentPage()).thenReturn(mockPage);
        var pageTitle = "VIVIDUS";
        when(mockPage.title()).thenReturn(pageTitle);

        pageSteps.assertPageTitle(StringComparisonRule.IS_EQUAL_TO, pageTitle);

        verify(softAssert).assertThat(eq("Page title"), eq(pageTitle),
                argThat(argument -> argument.matches(pageTitle)));
    }

    @SuppressWarnings("unchecked")
    private void verifyInteractionsForNewPage(Page page, int navigateTimes)
    {
        ArgumentCaptor<Consumer<Frame>> frameConsumerCaptor = ArgumentCaptor.forClass(Consumer.class);
        var ordered = inOrder(uiContext, page);
        ordered.verify(uiContext).reset();
        ordered.verify(uiContext).setCurrentPage(page);
        ordered.verify(page).onFrameNavigated(frameConsumerCaptor.capture());
        ordered.verify(page, times(navigateTimes)).navigate(PAGE_URL);
        ordered.verifyNoMoreInteractions();
        Consumer<Frame> frameConsumer = frameConsumerCaptor.getValue();
        Frame frame = mock();
        when(frame.parentFrame()).thenReturn(null).thenReturn(mock(Frame.class));
        frameConsumer.accept(frame);
        ordered.verify(uiContext).reset();
        frameConsumer.accept(frame);
        ordered.verifyNoMoreInteractions();
    }
}
