/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.steps.integration;

import java.util.Map;

import org.apache.http.client.CookieStore;
import org.jbehave.core.annotations.When;
import org.vividus.http.HttpTestContext;
import org.vividus.reporter.event.AttachmentPublisher;
import org.vividus.ui.web.action.ICookieManager;

public class HttpRequestSteps
{
    private final ICookieManager cookieManager;
    private final HttpTestContext httpTestContext;
    private final AttachmentPublisher attachmentPublisher;

    public HttpRequestSteps(ICookieManager cookieManager, HttpTestContext httpTestContext,
            AttachmentPublisher attachmentPublisher)
    {
        this.cookieManager = cookieManager;
        this.httpTestContext = httpTestContext;
        this.attachmentPublisher = attachmentPublisher;
    }

    /**
     * Step implemented to reuse browser cookies for HTTP client executed requests.
     * Steps performed:
     *   - Set's browser cookies into API context
     */
    @When("I set browser cookies to the API context")
    public void executeRequestUsingBrowserCookies()
    {
        CookieStore cookieStore = cookieManager.getCookiesAsHttpCookieStore();
        attachmentPublisher.publishAttachment("org/vividus/steps/integration/browser-cookies.ftl",
                Map.of("cookies", cookieStore.getCookies()), "Browser cookies");
        httpTestContext.putCookieStore(cookieStore);
    }
}
