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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.HttpTestContext;
import org.vividus.reporter.event.AttachmentPublisher;
import org.vividus.ui.web.action.CookieManager;

@ExtendWith(MockitoExtension.class)
class BrowserAndHttpContextIntegrationStepsTests
{
    @Mock private HttpTestContext httpTestContext;
    @Mock private CookieManager<?> cookieManager;
    @Mock private AttachmentPublisher attachmentPublisher;
    @InjectMocks private BrowserAndHttpContextIntegrationSteps steps;

    @Test
    void shouldSetCookiesFromBrowserAndDelegateRequestToApiSteps()
    {
        CookieStore cookieStore = mock();
        when(cookieManager.getCookiesAsHttpCookieStore()).thenReturn(cookieStore);
        var cookie1 = new BasicClientCookie("key", "value");
        var cookie2 = new BasicClientCookie("key1", "value1");
        when(cookieStore.getCookies()).thenReturn(List.of(cookie1, cookie2));
        steps.executeRequestUsingBrowserCookies();
        verify(httpTestContext).putCookieStore(cookieStore);
        var cookiesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(attachmentPublisher).publishAttachment(eq("org/vividus/integration/steps/cookies.ftl"),
                cookiesCaptor.capture(), eq("Browser cookies"));
        @SuppressWarnings("unchecked")
        Map<String, List<Cookie>> model = cookiesCaptor.getValue();

        List<Cookie> cookies = model.get("cookies");
        assertEquals(2, cookies.size());
        assertAll(
                () -> assertEquals("[name: key; value: value; domain: null; path: null; expiry: null]",
                        cookies.get(0).toString()),
                () -> assertEquals("[name: key1; value: value1; domain: null; path: null; expiry: null]",
                        cookies.get(1).toString())
        );
    }
}
