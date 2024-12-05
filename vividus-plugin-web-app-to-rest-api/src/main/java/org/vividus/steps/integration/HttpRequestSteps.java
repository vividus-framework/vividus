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

package org.vividus.steps.integration;

import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.cookie.CookieStore;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.Cookie;
import org.vividus.http.CookieStoreProvider;
import org.vividus.reporter.event.AttachmentPublisher;
import org.vividus.ui.web.action.CookieManager;
import org.vividus.ui.web.action.NavigateActions;

public class HttpRequestSteps
{
    private static final String ATTACHMENT_TEMPLATE_PATH = "org/vividus/steps/integration/browser-cookies.ftl";
    private static final String TEMPLATE_COOKIES_KEY = "cookies";
    private final CookieManager<Cookie> cookieManager;
    private final AttachmentPublisher attachmentPublisher;
    private final CookieStoreProvider cookieStoreProvider;
    private final NavigateActions navigateActions;

    public HttpRequestSteps(CookieManager<Cookie> cookieManager, AttachmentPublisher attachmentPublisher,
                            CookieStoreProvider cookieStoreProvider, NavigateActions navigateActions)
    {
        this.cookieManager = cookieManager;
        this.attachmentPublisher = attachmentPublisher;
        this.cookieStoreProvider = cookieStoreProvider;
        this.navigateActions = navigateActions;
    }

    /**
     * Step implemented to reuse browser cookies for HTTP client executed requests.
     * Steps performed:
     *   - Set's browser cookies into HTTP context
     */
    @When("I set browser cookies to HTTP context")
    public void executeRequestUsingBrowserCookies()
    {
        CookieStore cookieStore = cookieManager.getCookiesAsHttpCookieStore();
        List<org.apache.hc.client5.http.cookie.Cookie> cookies = cookieStore.getCookies();
        attachmentPublisher.publishAttachment(ATTACHMENT_TEMPLATE_PATH,
                Map.of(TEMPLATE_COOKIES_KEY, cookies), "Browser cookies");
        CookieStore httpCookieStore = cookieStoreProvider.getCookieStore();
        cookies.forEach(httpCookieStore::addCookie);
    }

    /**
     * Step implemented to reuse HTTP client requests cookies in the browser.
     * <p>The actions performed by the step:</p>
     * <ul>
     * <li>sets HTTP context cookies into browser;</li>
     * <li>refresh the current page (this action is required to apply the changes in cookies).</li>
     * </ul>
     */
    @When("I set HTTP context cookies to browser")
    public void setHttpCookiesToBrowser()
    {
        setHttpCookiesToBrowserWithoutApply();
        navigateActions.refresh();
    }

    /**
     * Step implemented to reuse HTTP client requests cookies in the browser, but does not apply the changes in cookies.
     * The current page must be refreshed or the navigation must be performed to apply the cookie changes.
     */
    @When("I set HTTP context cookies to browser without applying changes")
    public void setHttpCookiesToBrowserWithoutApply()
    {
        List<org.apache.hc.client5.http.cookie.Cookie> cookies = cookieStoreProvider.getCookieStore().getCookies();
        attachmentPublisher.publishAttachment(ATTACHMENT_TEMPLATE_PATH,
                    Map.of(TEMPLATE_COOKIES_KEY, cookies), "HTTP cookies");
        cookies.forEach(cookieManager::addHttpClientCookie);
    }
}
