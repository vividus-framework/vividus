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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import com.microsoft.playwright.Page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.testcontext.SimpleTestContext;

@ExtendWith(MockitoExtension.class)
class UiContextTests
{
    private final UiContext uiContext = new UiContext(new SimpleTestContext());

    @SuppressWarnings("PMD.CloseResource")
    @Test
    void shouldSetAndGetPage()
    {
        Page page = mock();

        uiContext.setCurrentPage(page);
        var currentPage = uiContext.getCurrentPage();

        assertSame(page, currentPage);
    }

    @Test
    void shouldReturnNullWhenPageNotSet()
    {
        var currentPage = uiContext.getCurrentPage();

        assertNull(currentPage);
    }
}
