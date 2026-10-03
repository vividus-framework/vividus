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

package org.vividus.ui.web.playwright.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.playwright.UiContext;

@ExtendWith(MockitoExtension.class)
class PlaywrightNavigateActionsTests
{
    @Mock private UiContext uiContext;
    @InjectMocks private PlaywrightNavigateActions navigateActions;

    @Test
    void shouldReturnCurrentPageUrl()
    {
        Page page = mock();
        when(uiContext.getCurrentPage()).thenReturn(page);
        var url = "https://docs.vividus.dev/";
        when(page.url()).thenReturn(url);

        var actualUrl = navigateActions.getCurrentUrl();

        assertEquals(url, actualUrl);
    }

    @Test
    void shouldRefreshPage()
    {
        Page page = mock();
        when(uiContext.getCurrentPage()).thenReturn(page);

        navigateActions.refresh();

        verify(page).reload();
        verifyNoMoreInteractions(page);
    }
}
