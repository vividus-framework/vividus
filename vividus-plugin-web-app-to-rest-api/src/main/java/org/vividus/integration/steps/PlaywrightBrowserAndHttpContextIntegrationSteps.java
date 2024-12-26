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

package org.vividus.integration.steps;

import com.microsoft.playwright.options.Cookie;

import org.vividus.http.HttpTestContext;
import org.vividus.http.client.CookieStoreProvider;
import org.vividus.reporter.event.AttachmentPublisher;
import org.vividus.ui.web.action.CookieManager;
import org.vividus.ui.web.playwright.UiContext;

public class PlaywrightBrowserAndHttpContextIntegrationSteps extends BrowserAndHttpContextIntegrationSteps
{
    public PlaywrightBrowserAndHttpContextIntegrationSteps(CookieManager<Cookie> cookieManager,
            HttpTestContext httpTestContext, UiContext uiContext, CookieStoreProvider cookieStoreProvider,
            AttachmentPublisher attachmentPublisher)
    {
        super(cookieManager, cookieStoreProvider, httpTestContext, () -> uiContext.getCurrentPage().reload(),
                attachmentPublisher);
    }
}
