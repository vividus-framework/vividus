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

package org.vividus.http.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.BasicCookieStore;
import org.junit.jupiter.api.Test;

@SuppressWarnings("checkstyle:MethodCount")
class HttpClientConfigTests
{
    private static final String BASE_URL = "http://somewh.ere/";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "pass";
    private static final AuthScope AUTH_SCOPE = new AuthScope("host", 1, "realm");
    private final HttpClientConfig config = new HttpClientConfig();

    @Test
    void testHasBaseUrlExists()
    {
        config.setBaseUrl(BASE_URL);
        assertTrue(config.hasBaseUrl());
    }

    @Test
    void testGetAndSetBaseUrl()
    {
        config.setBaseUrl(BASE_URL);
        assertEquals(BASE_URL, config.getBaseUrl());
    }

    @Test
    void testDefaultBaseUrl()
    {
        assertNull(config.getBaseUrl());
    }

    @Test
    void testHasBaseUrlEmpty()
    {
        config.setBaseUrl("");
        assertFalse(config.hasBaseUrl());
    }

    @Test
    void testHasBaseUrlNotExists()
    {
        assertFalse(config.hasBaseUrl());
    }

    @Test
    void testGetAndSetAuthConfig()
    {
        AuthConfig authConfig = new AuthConfig();
        authConfig.setPassword(PASSWORD);
        authConfig.setUsername(USERNAME);
        config.setAuthConfig(authConfig);
        assertEquals(authConfig, config.getAuthConfig());
        assertEquals(PASSWORD, config.getAuthConfig().getPassword());
        assertEquals(USERNAME, config.getAuthConfig().getUsername());
    }

    @Test
    void testHasAuthScopeExists()
    {
        config.setAuthScope(AUTH_SCOPE);
        assertTrue(config.hasAuthScope());
        assertEquals(AUTH_SCOPE, config.getAuthScope());
    }

    @Test
    void testGetAndSetAuthScope()
    {
        config.setAuthScope(AUTH_SCOPE);
        assertEquals(AUTH_SCOPE, config.getAuthScope());
    }

    @Test
    void testHasAuthScopeNotExists()
    {
        assertFalse(config.hasAuthScope());
    }

    @Test
    void testDefaultAuthScope()
    {
        assertNull(config.getAuthScope());
    }

    @Test
    void testCreateHeadersExists()
    {
        String header1 = "header1";
        String header2 = "header2";
        String value1 = "value1";
        String value2 = "value2";
        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put(header1, value1);
        customHeaders.put(header2, value2);
        config.setHeaders(customHeaders);

        List<Header> headers = config.createHeaders();
        assertEquals(customHeaders.size(), headers.size());
        assertThat(headers.get(0).getName(), anyOf(equalTo(header1), equalTo(header2)));
        assertThat(headers.get(0).getValue(), anyOf(equalTo(value1), equalTo(value2)));
        assertThat(headers.get(1).getName(), anyOf(equalTo(header1), equalTo(header2)));
        assertThat(headers.get(1).getValue(), anyOf(equalTo(value1), equalTo(value2)));
    }

    @Test
    void testCreateHeadersNotExists()
    {
        List<Header> headers = config.createHeaders();
        assertEquals(0, headers.size());
    }

    @Test
    void testCreateHeadersEmptyMap()
    {
        config.setHeaders(new HashMap<>());
        List<Header> headers = config.createHeaders();
        assertEquals(0, headers.size());
    }

    @Test
    void testDefaultConnectionManager()
    {
        assertNull(config.getConnectionManager());
    }

    @Test
    void testGetAndSetConnectionManager()
    {
        HttpClientConnectionManager connectionManager = mock(HttpClientConnectionManager.class);
        config.setConnectionManager(connectionManager);
        assertEquals(connectionManager, config.getConnectionManager());
    }

    @Test
    void testDefaultLastRequestInterceptor()
    {
        assertNull(config.getLastRequestInterceptor());
    }

    @Test
    void testGetAndSetLastRequestInterceptor()
    {
        HttpRequestInterceptor interceptor = mock(HttpRequestInterceptor.class);
        config.setLastRequestInterceptor(interceptor);
        assertEquals(interceptor, config.getLastRequestInterceptor());
    }

    @Test
    void testDefaultLastResponseInterceptor()
    {
        assertNull(config.getLastResponseInterceptor());
    }

    @Test
    void testGetAndSetLastResponseInterceptor()
    {
        HttpResponseInterceptor interceptor = mock(HttpResponseInterceptor.class);
        config.setLastResponseInterceptor(interceptor);
        assertEquals(interceptor, config.getLastResponseInterceptor());
    }

    @Test
    void testDefaultRedirectStrategy()
    {
        assertNull(config.getRedirectStrategy());
    }

    @Test
    void testGetAndSetRedirectStrategy()
    {
        RedirectStrategy redirectStrategy = mock(RedirectStrategy.class);
        config.setRedirectStrategy(redirectStrategy);
        assertEquals(redirectStrategy, config.getRedirectStrategy());
    }

    @Test
    void testDefaultConnectionRequestTimeout()
    {
        config.setConnectionRequestTimeout(-1);
        assertEquals(-1, config.getConnectionRequestTimeout());
    }

    @Test
    void testGetAndSetConnectionRequestTimeout()
    {
        int connectionRequestTimeout = 1000;
        config.setConnectionRequestTimeout(connectionRequestTimeout);
        assertEquals(connectionRequestTimeout, config.getConnectionRequestTimeout());
    }

    @Test
    void testDefaultConnectTimeout()
    {
        config.setConnectTimeout(-1);
        assertEquals(-1, config.getConnectTimeout());
    }

    @Test
    void testGetAndSetConnectTimeout()
    {
        int connectionTimeout = 1000;
        config.setConnectTimeout(connectionTimeout);
        assertEquals(connectionTimeout, config.getConnectTimeout());
    }

    @Test
    void testDefaultSocketTimeout()
    {
        config.setSocketTimeout(0);
        assertEquals(0, config.getSocketTimeout());
    }

    @Test
    void testGetAndSetSocketTimeout()
    {
        int socketTimeout = 1000;
        config.setSocketTimeout(socketTimeout);
        assertEquals(socketTimeout, config.getSocketTimeout());
    }

    @Test
    void testHasCookieStore()
    {
        config.setCookieStore(new BasicCookieStore());
        assertTrue(config.hasCookieStore());
    }

    @Test
    void testDoesNotHaveCookieStore()
    {
        assertFalse(config.hasCookieStore());
    }

    @Test
    void testCookieStore()
    {
        CookieStore cookieStore = new BasicCookieStore();
        config.setCookieStore(cookieStore);
        assertEquals(cookieStore, config.getCookieStore());
    }

    @Test
    void testSkipResponseEntity()
    {
        config.setSkipResponseEntity(false);
        assertFalse(config.isSkipResponseEntity());
    }

    @Test
    void testGetAndSetDnsResolver()
    {
        DnsResolver resolver = mock(DnsResolver.class);
        config.setDnsResolver(resolver);
        assertEquals(resolver, config.getDnsResolver());
    }

    @Test
    void testGetAndSetCookieSpec()
    {
        String cookieSpec = "cookieSpec";
        config.setCookieSpec(cookieSpec);
        assertEquals(cookieSpec, config.getCookieSpec());
    }

    @Test
    void testDefaultCookieSpec()
    {
        assertNull(config.getCookieSpec());
    }

    @Test
    void testGetAndSetHttpRequestRetryHandler()
    {
        HttpRequestRetryHandler handler = mock(HttpRequestRetryHandler.class);
        config.setHttpRequestRetryHandler(handler);
        assertEquals(handler, config.getHttpRequestRetryHandler());
    }
}
