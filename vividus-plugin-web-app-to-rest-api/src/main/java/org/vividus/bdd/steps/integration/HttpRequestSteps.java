/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.steps.integration;

import java.util.stream.Collectors;
import javax.inject.Inject;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.http.HttpTestContext;
import org.vividus.ui.web.action.ICookieManager;

public class HttpRequestSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestSteps.class);
    @Inject private ICookieManager cookieManager;
    @Inject private HttpTestContext httpTestContext;

    /**
     * Step implemented to reuse browser cookies for HTTP client executed requests.
     * Steps performed:
     *   - Set's browser cookies into API context
     */
    @When("I set browser cookies to the API context")
    public void executeRequestUsingBrowserCookies()
    {
        CookieStore cookieStore = cookieManager.getCookiesAsHttpCookieStore();
        LOGGER.debug("Setting cookies into API context: {}",
                cookieStore.getCookies().stream()
                                        .map(Cookie::toString)
                                        .collect(Collectors.joining(";\n")));
        httpTestContext.putCookieStore(cookieStore);
    }
}
