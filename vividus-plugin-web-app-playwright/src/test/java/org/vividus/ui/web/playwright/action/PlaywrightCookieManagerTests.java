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

package org.vividus.ui.web.playwright.action;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.options.Cookie;

import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.playwright.BrowserContextProvider;

@ExtendWith(MockitoExtension.class)
class PlaywrightCookieManagerTests
{
    private static final String PATH = "/";
    private static final String ZERO = "0";
    private static final String COOKIE_NAME = "name";
    private static final String COOKIE_VALUE = "cookieValue";
    private static final String DOMAIN = "https://www.domain.com";

    @Mock private BrowserContextProvider browserContextProvider;
    @Mock private BrowserContext browserContext;
    @InjectMocks private PlaywrightCookieManager cookieManager;

    @Test
    void shouldDeleteAllCookies()
    {
        when(browserContextProvider.get()).thenReturn(browserContext);
        cookieManager.deleteAllCookies();
        verify(browserContext).clearCookies();
    }

    @Test
    void shouldDeleteCookie()
    {
        ArgumentCaptor<BrowserContext.ClearCookiesOptions> clearCookiesOptionsCaptor = ArgumentCaptor.captor();
        when(browserContextProvider.get()).thenReturn(browserContext);
        cookieManager.deleteCookie(COOKIE_NAME);
        verify(browserContext).clearCookies(clearCookiesOptionsCaptor.capture());
        BrowserContext.ClearCookiesOptions clearCookiesOptions = clearCookiesOptionsCaptor.getValue();
        assertEquals(COOKIE_NAME, clearCookiesOptions.name);
    }

    @ParameterizedTest
    @CsvSource({
            "https://www.domain.com, .domain.com",
            "http://localhost:8080,  localhost",
            "http://test.topdomain.com/test/test, .topdomain.com",
            "http://testproduct:80/test, testproduct",
            "http://127.0.0.1:8080, 127.0.0.1"
    })
    void shouldAddCookie(String urlAsString, String domain)
    {
        ArgumentCaptor<List<Cookie>> cookieCaptor = ArgumentCaptor.captor();
        when(browserContextProvider.get()).thenReturn(browserContext);
        cookieManager.addCookie(COOKIE_NAME, ZERO, PATH, urlAsString);
        verify(browserContext).addCookies(cookieCaptor.capture());

        Cookie cookie = cookieCaptor.getValue().get(0);
        assertEquals(COOKIE_NAME, cookie.name);
        assertEquals(ZERO, cookie.value);
        assertEquals(PATH, cookie.path);
        assertEquals(domain, cookie.domain);
    }

    @Test
    void shouldAddHttpCookie()
    {
        String domain = "api_domain";
        BasicClientCookie httpCookie = new BasicClientCookie(COOKIE_NAME, ZERO);
        httpCookie.setPath(PATH);
        httpCookie.setDomain(domain);

        ArgumentCaptor<List<Cookie>> cookieCaptor = ArgumentCaptor.captor();
        when(browserContextProvider.get()).thenReturn(browserContext);
        cookieManager.addHttpClientCookies(List.of(httpCookie));
        verify(browserContext).addCookies(cookieCaptor.capture());

        Cookie cookie = cookieCaptor.getValue().get(0);
        assertEquals(COOKIE_NAME, cookie.name);
        assertEquals(ZERO, cookie.value);
        assertEquals(PATH, cookie.path);
        assertEquals(domain, cookie.domain);
    }

    @Test
    void shouldGetCookie()
    {
        when(browserContextProvider.get()).thenReturn(browserContext);
        Cookie cookie = createCookie();
        when(browserContext.cookies()).thenReturn(List.of(cookie));
        assertEquals(cookie, cookieManager.getCookie(COOKIE_NAME));
    }

    @Test
    void shouldGetNullCookie()
    {
        when(browserContextProvider.get()).thenReturn(browserContext);
        Cookie cookie = createCookie();
        when(browserContext.cookies()).thenReturn(List.of(cookie));
        assertNull(cookieManager.getCookie("INCORRECT_NAME"));
    }

    @Test
    void shouldGetCookies()
    {
        when(browserContextProvider.get()).thenReturn(browserContext);
        List<Cookie> expectedCookies = List.of(createCookie());
        when(browserContext.cookies()).thenReturn(expectedCookies);
        assertEquals(expectedCookies, cookieManager.getCookies());
    }

    static Stream<Arguments> expiryDates()
    {
        Instant expiryInstant = ZonedDateTime.now().plusDays(1).toInstant();
        return Stream.of(
                arguments((double) expiryInstant.getEpochSecond(), expiryInstant.truncatedTo(ChronoUnit.SECONDS)),
                arguments(-1.0, null),
                arguments(null, null)
        );
    }

    @MethodSource("expiryDates")
    @ParameterizedTest
    void shouldGetCookiesAsHttpCookieStore(Double expiryDate, Instant expectedExpiryInstant)
    {
        when(browserContextProvider.get()).thenReturn(browserContext);
        Cookie cookie = createCookie();
        cookie.expires = expiryDate;
        when(browserContext.cookies()).thenReturn(List.of(cookie));

        CookieStore cookieStore = cookieManager.getCookiesAsHttpCookieStore();
        List<org.apache.hc.client5.http.cookie.Cookie> resultCookies = cookieStore.getCookies();
        assertEquals(1, resultCookies.size());
        org.apache.hc.client5.http.cookie.Cookie httpCookie = resultCookies.get(0);
        assertThat(httpCookie, instanceOf(BasicClientCookie.class));
        BasicClientCookie clientCookie = (BasicClientCookie) httpCookie;
        assertAll(() -> assertEquals(cookie.domain, clientCookie.getDomain()),
                () -> assertEquals(expectedExpiryInstant, clientCookie.getExpiryInstant()),
                () -> assertEquals(cookie.name, clientCookie.getName()),
                () -> assertEquals(cookie.path, clientCookie.getPath()),
                () -> assertEquals(cookie.value, clientCookie.getValue()),
                () -> assertEquals(cookie.secure, clientCookie.isSecure()), () -> assertEquals(cookie.domain,
                        clientCookie.getAttribute(org.apache.hc.client5.http.cookie.Cookie.DOMAIN_ATTR)),
                () -> assertEquals(cookie.path,
                        clientCookie.getAttribute(org.apache.hc.client5.http.cookie.Cookie.PATH_ATTR))
        );
    }

    private Cookie createCookie()
    {
        return new Cookie(COOKIE_NAME, COOKIE_VALUE)
                .setPath(PATH)
                .setDomain(DOMAIN)
                .setSecure(true);
    }
}
