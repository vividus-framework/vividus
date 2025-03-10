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

package org.vividus.ui.web.action;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.vividus.selenium.IWebDriverProvider;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class WebDriverCookieManagerTests
{
    private static final String PATH = "/";
    private static final String ZERO = "0";
    private static final String COOKIE_NAME = "name";
    private static final String COOKIE_VALUE = "cookieValue";
    private static final String DOMAIN = "https://www.domain.com";

    @Captor private ArgumentCaptor<Cookie> cookieCaptor;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private Options options;
    @InjectMocks private WebDriverCookieManager cookieManager;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(WebDriverCookieManager.class);

    @Test
    void shouldDeleteAllCookies()
    {
        configureMockedWebDriver();
        cookieManager.deleteAllCookies();
        verify(options).deleteAllCookies();
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
        configureMockedWebDriver();
        cookieManager.addCookie(COOKIE_NAME, ZERO, PATH, urlAsString);
        verify(options).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals(COOKIE_NAME, cookie.getName());
        assertEquals(ZERO, cookie.getValue());
        assertEquals(PATH, cookie.getPath());
        assertEquals(domain, cookie.getDomain());
        assertThat(logger.getLoggingEvents(), is(List.of(info("Adding cookie: {}", cookie))));
    }

    @Test
    void shouldAddHttpCookies()
    {
        var domain = ".onrender.com";
        var webDriver = configureMockedWebDriver();
        when(webDriver.getCurrentUrl()).thenReturn("https://vividus-test-site-a92k.onrender.com/stickyHeader.html");
        var httpCookie = new BasicClientCookie(COOKIE_NAME, ZERO);
        httpCookie.setPath(PATH);
        httpCookie.setDomain(domain);
        cookieManager.addHttpClientCookies(List.of(httpCookie));
        verify(options).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals(COOKIE_NAME, cookie.getName());
        assertEquals(ZERO, cookie.getValue());
        assertEquals(PATH, cookie.getPath());
        assertEquals(domain, cookie.getDomain());
    }

    @Test
    void shouldSkipAddingHttpCookieDueToDomainMismatch()
    {
        var domain = "google.com";
        var webDriver = mockWebDriver();
        var currentPageUrl = "https://vividus-test-site-a92k.onrender.com/";
        when(webDriver.getCurrentUrl()).thenReturn(currentPageUrl);
        var httpCookie = new BasicClientCookie(COOKIE_NAME, ZERO);
        httpCookie.setPath(PATH);
        httpCookie.setDomain(domain);
        cookieManager.addHttpClientCookies(List.of(httpCookie));
        verifyNoInteractions(options);
        assertThat(logger.getLoggingEvents(), is(List.of(
                info("The cookie is not added to the browser as its domain '{}' is neither subdomain nor equal to "
                     + "the current page domain '{}'. Full cookie: {}", domain, currentPageUrl, httpCookie)))
        );
    }

    @Test
    void shouldDeleteCookie()
    {
        configureMockedWebDriver();
        cookieManager.deleteCookie(COOKIE_NAME);
        verify(options).deleteCookieNamed(COOKIE_NAME);
    }

    @Test
    void shouldGetCookie()
    {
        configureMockedWebDriver();
        Cookie seleniumCookie = createSeleniumCookie(null);
        when(options.getCookieNamed(COOKIE_NAME)).thenReturn(seleniumCookie);
        assertEquals(seleniumCookie, cookieManager.getCookie(COOKIE_NAME));
    }

    @Test
    void shouldGetCookies()
    {
        configureMockedWebDriver();
        Set<Cookie> expectedCookies = mockGetCookies(createSeleniumCookie(null));
        assertEquals(expectedCookies, cookieManager.getCookies());
    }

    public static Stream<Date> expiryDates()
    {
        return Stream.of(
                Date.from(ZonedDateTime.now().plusDays(1).toInstant()),
                null
        );
    }

    @MethodSource("expiryDates")
    @ParameterizedTest
    void shouldGetCookiesAsHttpCookieStore(Date expiryDate)
    {
        configureMockedWebDriver();
        Cookie seleniumCookie = createSeleniumCookie(expiryDate);
        mockGetCookies(seleniumCookie);
        CookieStore cookieStore = cookieManager.getCookiesAsHttpCookieStore();
        var resultCookies = cookieStore.getCookies();
        assertEquals(1, resultCookies.size());
        var httpCookie = resultCookies.get(0);
        assertThat(httpCookie, instanceOf(BasicClientCookie.class));
        var clientCookie = (BasicClientCookie) httpCookie;
        assertAll(
            () -> assertEquals(seleniumCookie.getDomain(), clientCookie.getDomain()),
            () -> assertEquals(expiryDate != null ? expiryDate.toInstant().truncatedTo(ChronoUnit.SECONDS) : null,
                    clientCookie.getExpiryInstant()),
            () -> assertEquals(seleniumCookie.getName(), clientCookie.getName()),
            () -> assertEquals(seleniumCookie.getPath(), clientCookie.getPath()),
            () -> assertEquals(seleniumCookie.getValue(), clientCookie.getValue()),
            () -> assertEquals(seleniumCookie.isSecure(), clientCookie.isSecure()),
                () -> assertEquals(seleniumCookie.getDomain(),
                        clientCookie.getAttribute(org.apache.hc.client5.http.cookie.Cookie.DOMAIN_ATTR)),
                () -> assertEquals(seleniumCookie.getPath(),
                        clientCookie.getAttribute(org.apache.hc.client5.http.cookie.Cookie.PATH_ATTR))
        );
    }

    private WebDriver mockWebDriver()
    {
        WebDriver webDriver = mock();
        when(webDriverProvider.get()).thenReturn(webDriver);
        return webDriver;
    }

    private WebDriver configureMockedWebDriver()
    {
        WebDriver webDriver = mockWebDriver();
        when(webDriver.manage()).thenReturn(options);
        return webDriver;
    }

    private Cookie createSeleniumCookie(Date expiry)
    {
        return new Cookie(COOKIE_NAME, COOKIE_VALUE, DOMAIN, "cookiePath", expiry, true);
    }

    private Set<Cookie> mockGetCookies(Cookie cookie)
    {
        Set<Cookie> cookies = Set.of(cookie);
        when(options.getCookies()).thenReturn(cookies);
        return cookies;
    }
}
