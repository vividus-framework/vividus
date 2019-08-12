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

import static com.github.valfirst.slf4jtest.LoggingEvent.debug;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.api.IApiTestContext;
import org.vividus.ui.web.action.ICookieManager;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class})
class HttpRequestStepsTests
{
    @Mock
    private IApiTestContext apiTesingContext;

    @Mock
    private ICookieManager cookieManager;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(HttpRequestSteps.class);

    @InjectMocks
    private HttpRequestSteps httpRequestSteps;

    @Test
    void shouldSetCookiesFromBrowserAndDelegateRequestToApiSteps()
    {
        CookieStore cookieStore = mock(CookieStore.class);
        when(cookieManager.getCookiesAsHttpCookieStore()).thenReturn(cookieStore);
        BasicClientCookie cookie1 = new BasicClientCookie("key", "vale");
        BasicClientCookie cookie2 = new BasicClientCookie("key1", "vale1");
        when(cookieStore.getCookies()).thenReturn(List.of(cookie1, cookie2));
        httpRequestSteps.executeRequestUsingBrowserCookies();
        verify(apiTesingContext).putCookieStore(cookieStore);
        assertThat(logger.getLoggingEvents(),
                is(List.of(debug("Setting cookies into API context: {}",
                        "[version: 0][name: key][value: vale][domain: null][path: null][expiry: null];\n"
                      + "[version: 0][name: key1][value: vale1][domain: null][path: null][expiry: null]"))));
    }
}
