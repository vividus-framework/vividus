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
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class ClientBuilderUtilsTest
{
    private static final AuthScope DEFAULT_AUTH_SCOPE = AuthScope.ANY;

    @Test
    public void testCreateUrlEncodedFormEntity() throws IOException
    {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("param1", "value1"));
        params.add(new BasicNameValuePair("param2", "value2"));
        UrlEncodedFormEntity expectedEntity = new UrlEncodedFormEntity(params);
        UrlEncodedFormEntity actualEntity = ClientBuilderUtils.createUrlEncodedFormEntity(params);
        Assertions.assertEquals(expectedEntity.getContentLength(), actualEntity.getContentLength());
    }

    @Test
    public void testCreateUrlEncodedFormEntityThrowingException() throws IOException
    {
        UrlEncodedFormEntity expectedEntity = new UrlEncodedFormEntity(new ArrayList<>());
        UrlEncodedFormEntity actualEntity = ClientBuilderUtils.createUrlEncodedFormEntity(new ArrayList<>());
        Assertions.assertEquals(expectedEntity.getContentLength(), actualEntity.getContentLength());
    }

    @Test
    public void testCreateCredentialsProviderSplit()
    {
        CredentialsProvider actualProvider = ClientBuilderUtils.createCredentialsProvider("user:pass");
        verifyProvider(actualProvider, DEFAULT_AUTH_SCOPE, "user", "pass");
    }

    @Test
    public void testCreateCredentialsProviderSplitWithScope()
    {
        final AuthScope authScope = new AuthScope("host1", 1);
        CredentialsProvider actualProvider = ClientBuilderUtils.createCredentialsProvider(authScope, "user13:pass13");
        verifyProvider(actualProvider, authScope, "user13", "pass13");
    }

    @Test
    public void testCreateCredentialsProviderSimple()
    {
        String user = "user2";
        String pass = "pass123";
        CredentialsProvider actualProvider = ClientBuilderUtils.createCredentialsProvider(user, pass);
        verifyProvider(actualProvider, DEFAULT_AUTH_SCOPE, user, pass);
    }

    @Test
    public void testCreateCredentialsProviderSimpleWithScope()
    {
        String user = "user23";
        String pass = "pass1234";
        AuthScope authScope = new AuthScope("host2", 1);
        CredentialsProvider actualProvider = ClientBuilderUtils.createCredentialsProvider(authScope, user, pass);
        verifyProvider(actualProvider, authScope, user, pass);
    }

    @Test
    public void testCreateCookieStore()
    {
        Cookie cookie = new BasicClientCookie("name", "value");
        List<Cookie> result = ClientBuilderUtils.createCookieStore(Collections.singleton(cookie)).getCookies();
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(result.get(0), cookie);
    }

    @Test
    @PrepareForTest({ CookieStoreCollector.class, ClientBuilderUtils.class })
    public void testToCookieStore() throws Exception
    {
        CookieStoreCollector cookieStoreCollector = new CookieStoreCollector();
        PowerMockito.whenNew(CookieStoreCollector.class).withNoArguments().thenReturn(cookieStoreCollector);
        Assertions.assertEquals(cookieStoreCollector, ClientBuilderUtils.toCookieStore());
    }

    private static void verifyProvider(CredentialsProvider actualProvider, AuthScope expectedAuthScope,
            String expectedUser, String expectedPass)
    {
        String actualName = actualProvider.getCredentials(expectedAuthScope).getUserPrincipal().getName();
        String actualPass = actualProvider.getCredentials(expectedAuthScope).getPassword();
        Assertions.assertEquals(expectedUser, actualName);
        Assertions.assertEquals(expectedPass, actualPass);
    }
}
