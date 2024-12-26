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

import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.jbehave.core.annotations.When;
import org.vividus.http.HttpTestContext;
import org.vividus.http.client.CookieStoreProvider;
import org.vividus.reporter.event.AttachmentPublisher;
import org.vividus.ui.web.action.CookieManager;

public class BrowserAndHttpContextIntegrationSteps
{
    private final CookieManager<?> cookieManager;
    private final CookieStoreProvider cookieStoreProvider;
    private final HttpTestContext httpTestContext;
    private final Runnable refreshPageAction;
    private final AttachmentPublisher attachmentPublisher;

    public BrowserAndHttpContextIntegrationSteps(CookieManager<?> cookieManager,
            CookieStoreProvider cookieStoreProvider, HttpTestContext httpTestContext, Runnable refreshPageAction,
            AttachmentPublisher attachmentPublisher)
    {
        this.cookieManager = cookieManager;
        this.cookieStoreProvider = cookieStoreProvider;
        this.httpTestContext = httpTestContext;
        this.refreshPageAction = refreshPageAction;
        this.attachmentPublisher = attachmentPublisher;
    }

    /**
     * Sets the current browser cookies to the HTTP context to use them in further HTTP requests.
     */
    @When("I set browser cookies to HTTP context")
    public void executeRequestUsingBrowserCookies()
    {
        CookieStore cookieStore = cookieManager.getCookiesAsHttpCookieStore();
        publishAttachmentWithCookies(cookieStore.getCookies(), "Browser cookies");
        httpTestContext.putCookieStore(cookieStore);
    }

    /**
     * Sets the current cookies from the preceding HTTP calls to the current browser instance. After adding the cookies
     * the opened page is refreshed (this is required to apply the new cookies).
     * <br>
     * NOTE: If Selenium engine is used, ensure that the expected cookies from HTTP calls match the domain of the page
     * opened in the web browser.
     */
    @When("I set HTTP context cookies to browser")
    public void setHttpCookiesToBrowser()
    {
        setHttpCookiesToBrowserWithoutApply();
        refreshPageAction.run();
    }

    /**
     * Sets the current cookies from the preceding HTTP calls to the current browser instance, but does not apply the
     * changes in cookies instantly. The current page must be refreshed, or navigation must be performed to apply the
     * cookie changes.
     * <br>
     * NOTE: If Selenium engine is used, ensure that the expected cookies from HTTP calls match the domain of the
     * page opened in the web browser.
     */
    @When("I set HTTP context cookies to browser without applying changes")
    public void setHttpCookiesToBrowserWithoutApply()
    {
        List<org.apache.hc.client5.http.cookie.Cookie> cookies = cookieStoreProvider.getCookieStore().getCookies();
        publishAttachmentWithCookies(cookies, "All HTTP cookies");
        cookieManager.addHttpClientCookies(cookies);
    }

    private void publishAttachmentWithCookies(List<Cookie> cookies, String title)
    {
        attachmentPublisher.publishAttachment("org/vividus/integration/steps/cookies.ftl",
                Map.of("cookies", cookies), title);
    }
}
