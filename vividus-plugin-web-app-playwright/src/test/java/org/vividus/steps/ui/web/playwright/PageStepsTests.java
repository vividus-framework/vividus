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

package org.vividus.steps.ui.web.playwright;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.UiContext;

@SuppressWarnings("PMD.CloseResource")
@ExtendWith(MockitoExtension.class)
class PageStepsTests
{
    private static final String PAGE_URL = "https://docs.vividus.dev";

    @Mock private BrowserContextProvider browserContextProvider;
    @Mock private UiContext uiContext;
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
    }

    @Test
    void shouldOpenPageInCurrentTab()
    {
        Page page = mock();
        when(uiContext.getCurrentPage()).thenReturn(page);

        pageSteps.openPage(PAGE_URL);

        verify(page).navigate(PAGE_URL);
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

        var ordered = inOrder(uiContext, page);
        ordered.verify(uiContext).setCurrentPage(page);
        ordered.verify(page).navigate(PAGE_URL);
    }

    @Test
    void shouldRefreshPage()
    {
        Page page = mock();
        when(uiContext.getCurrentPage()).thenReturn(page);

        pageSteps.refreshPage();

        verify(page).reload();
    }

    @Test
    void shouldNavigateBack()
    {
        Page page = mock();
        when(uiContext.getCurrentPage()).thenReturn(page);

        pageSteps.navigateBack();

        verify(page).goBack();
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
}
