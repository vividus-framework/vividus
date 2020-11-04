/*
 * Copyright 2019-2020 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockConstruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class ClientBuilderUtilsTests
{
    private static final AuthScope DEFAULT_AUTH_SCOPE = AuthScope.ANY;

    @Test
    void testCreateUrlEncodedFormEntity() throws IOException
    {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("param1", "value1"));
        params.add(new BasicNameValuePair("param2", "value2"));
        UrlEncodedFormEntity expectedEntity = new UrlEncodedFormEntity(params);
        UrlEncodedFormEntity actualEntity = ClientBuilderUtils.createUrlEncodedFormEntity(params);
        assertEquals(expectedEntity.getContentLength(), actualEntity.getContentLength());
    }

    @Test
    void testCreateUrlEncodedFormEntityThrowingException() throws IOException
    {
        UrlEncodedFormEntity expectedEntity = new UrlEncodedFormEntity(new ArrayList<>());
        UrlEncodedFormEntity actualEntity = ClientBuilderUtils.createUrlEncodedFormEntity(new ArrayList<>());
        assertEquals(expectedEntity.getContentLength(), actualEntity.getContentLength());
    }

    @Test
    void testCreateCredentialsProviderSplit()
    {
        CredentialsProvider actualProvider = ClientBuilderUtils.createCredentialsProvider("user:pass");
        verifyProvider(actualProvider, DEFAULT_AUTH_SCOPE, "user", "pass");
    }

    @Test
    void testCreateCredentialsProviderSplitWithScope()
    {
        final AuthScope authScope = new AuthScope("host1", 1);
        CredentialsProvider actualProvider = ClientBuilderUtils.createCredentialsProvider(authScope, "user13:pass13");
        verifyProvider(actualProvider, authScope, "user13", "pass13");
    }

    @Test
    void testCreateCredentialsProviderSimple()
    {
        String user = "user2";
        String pass = "pass123";
        CredentialsProvider actualProvider = ClientBuilderUtils.createCredentialsProvider(user, pass);
        verifyProvider(actualProvider, DEFAULT_AUTH_SCOPE, user, pass);
    }

    @Test
    void testCreateCredentialsProviderSimpleWithScope()
    {
        String user = "user23";
        String pass = "pass1234";
        AuthScope authScope = new AuthScope("host2", 1);
        CredentialsProvider actualProvider = ClientBuilderUtils.createCredentialsProvider(authScope, user, pass);
        verifyProvider(actualProvider, authScope, user, pass);
    }

    @Test
    void testCreateCookieStore()
    {
        Cookie cookie = new BasicClientCookie("name", "value");
        List<Cookie> result = ClientBuilderUtils.createCookieStore(Collections.singleton(cookie)).getCookies();
        Assertions.assertFalse(result.isEmpty());
        assertEquals(result.get(0), cookie);
    }

    @Test
    void testToCookieStore()
    {
        try (MockedConstruction<CookieStoreCollector> cookieStoreCollector = mockConstruction(
                CookieStoreCollector.class))
        {
            assertEquals(cookieStoreCollector.constructed(), List.of(ClientBuilderUtils.toCookieStore()));
        }
    }

    private static void verifyProvider(CredentialsProvider actualProvider, AuthScope expectedAuthScope,
            String expectedUser, String expectedPass)
    {
        String actualName = actualProvider.getCredentials(expectedAuthScope).getUserPrincipal().getName();
        String actualPass = actualProvider.getCredentials(expectedAuthScope).getPassword();
        assertEquals(expectedUser, actualName);
        assertEquals(expectedPass, actualPass);
    }
}
