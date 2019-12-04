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

package org.vividus.ui.web.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.http.client.CookieStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.manager.IWebDriverManager;

@ExtendWith(MockitoExtension.class)
class CookieManagerTests
{
    private static final String PATH = "/";
    private static final String ZERO = "0";
    private static final String COOKIE_NAME = "name";
    private static final String COOKIE_VALUE = "cookieValue";
    private static final String DOMAIN = "https://www.domain.com";
    private static final String JS_DELETE_COOKIE_FORMAT = "document.cookie='name=; expires=-1'";
    private static final String JS_COOKIE_FORMAT = "document.cookie='name=0; path=/; domain=domain.com'";

    @Mock
    private IJavascriptActions javascriptActions;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private Options options;

    @Mock
    private IWebDriverManager webDriverManager;

    @InjectMocks
    private CookieManager cookieManager;

    @Test
    void testDeleteAllCookies()
    {
        configureMockedWebDriver();
        cookieManager.deleteAllCookies();
        verify(options).deleteAllCookies();
    }

    @Test
    void testDeleteAllCookiesSafari()
    {
        configureMockedWebDriver();
        mockIsSafari(true);
        mockGetCookies(new Cookie(COOKIE_NAME, COOKIE_VALUE));
        cookieManager.deleteAllCookies();
        verify(javascriptActions).executeScript(JS_DELETE_COOKIE_FORMAT);
    }

    @Test
    void testAddCookie()
    {
        cookieManager.addCookie(COOKIE_NAME, ZERO, PATH, DOMAIN);
        verify(javascriptActions).executeScript(JS_COOKIE_FORMAT);
    }

    @Test
    void testAddCookieUrl() throws MalformedURLException
    {
        cookieManager.addCookie(COOKIE_NAME, ZERO, PATH, new URL(DOMAIN));
        verify(javascriptActions).executeScript(JS_COOKIE_FORMAT);
    }

    @Test
    void testAddCookieUri()
    {
        cookieManager.addCookie(COOKIE_NAME, ZERO, PATH, URI.create(DOMAIN));
        verify(javascriptActions).executeScript(JS_COOKIE_FORMAT);
    }

    @Test
    void testDeleteCookieNotSafari()
    {
        configureMockedWebDriver();
        mockIsSafari(false);
        cookieManager.deleteCookie(COOKIE_NAME);
        verify(options).deleteCookieNamed(COOKIE_NAME);
    }

    @Test
    void testDeleteCookieSafari()
    {
        mockIsSafari(true);
        cookieManager.deleteCookie(COOKIE_NAME);
        verify(javascriptActions).executeScript(JS_DELETE_COOKIE_FORMAT);
    }

    @Test
    void testGetCookie()
    {
        configureMockedWebDriver();
        Cookie seleniumCookie = createSeleniumCookie();
        when(options.getCookieNamed(COOKIE_NAME)).thenReturn(seleniumCookie);
        assertEquals(seleniumCookie, cookieManager.getCookie(COOKIE_NAME));
    }

    @Test
    void testGetCookies()
    {
        configureMockedWebDriver();
        Set<Cookie> expectedCookies = mockGetCookies(createSeleniumCookie());
        assertEquals(expectedCookies, cookieManager.getCookies());
    }

    @Test
    void testGetCookiesAsHttpCookieStore()
    {
        configureMockedWebDriver();
        Cookie seleniumCookie = createSeleniumCookie();
        mockGetCookies(seleniumCookie);
        CookieStore cookieStore = cookieManager.getCookiesAsHttpCookieStore();
        List<org.apache.http.cookie.Cookie> resultCookies = cookieStore.getCookies();
        assertEquals(1, resultCookies.size());
        org.apache.http.cookie.Cookie httpCookie = resultCookies.get(0);
        assertEquals(seleniumCookie.getDomain(), httpCookie.getDomain());
        assertEquals(seleniumCookie.getExpiry(), httpCookie.getExpiryDate());
        assertEquals(seleniumCookie.getName(), httpCookie.getName());
        assertEquals(seleniumCookie.getPath(), httpCookie.getPath());
        assertEquals(seleniumCookie.getValue(), httpCookie.getValue());
        assertEquals(seleniumCookie.isSecure(), httpCookie.isSecure());
    }

    private void configureMockedWebDriver()
    {
        WebDriver webDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.manage()).thenReturn(options);
    }

    private Cookie createSeleniumCookie()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        return new Cookie(COOKIE_NAME, COOKIE_VALUE, DOMAIN, "cookiePath", calendar.getTime(), true);
    }

    private Set<Cookie> mockGetCookies(Cookie cookie)
    {
        Set<Cookie> cookies = Collections.singleton(cookie);
        when(options.getCookies()).thenReturn(cookies);
        return cookies;
    }

    private void mockIsSafari(boolean safari)
    {
        when(webDriverManager.isTypeAnyOf(WebDriverType.SAFARI)).thenReturn(safari);
    }
}
