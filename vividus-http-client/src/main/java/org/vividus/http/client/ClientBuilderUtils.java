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

import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;

import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.vividus.util.UriUtils;
import org.vividus.util.UriUtils.UserInfo;

public final class ClientBuilderUtils
{
    public static final AuthScope DEFAULT_AUTH_SCOPE = AuthScope.ANY;

    private ClientBuilderUtils()
    {
    }

    public static CredentialsProvider createCredentialsProvider(String usernamePassword)
    {
        return createCredentialsProvider(DEFAULT_AUTH_SCOPE, usernamePassword);
    }

    public static CredentialsProvider createCredentialsProvider(AuthScope authScope, String usernamePassword)
    {
        UserInfo userInfo = UriUtils.parseUserInfo(usernamePassword);
        return createCredentialsProvider(authScope, userInfo.getUser(), userInfo.getPassword());
    }

    public static CredentialsProvider createCredentialsProvider(String username, String password)
    {
        return createCredentialsProvider(DEFAULT_AUTH_SCOPE, username, password);
    }

    public static CredentialsProvider createCredentialsProvider(AuthScope authScope, String username, String password)
    {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(authScope, new UsernamePasswordCredentials(username, password));
        return credentialsProvider;
    }

    public static UrlEncodedFormEntity createUrlEncodedFormEntity(List<NameValuePair> params)
    {
        return new UrlEncodedFormEntity(params, (Charset) null);
    }

    public static CookieStore createCookieStore(Set<Cookie> cookies)
    {
        return cookies.stream().collect(toCookieStore());
    }

    public static Collector<Cookie, CookieStore, CookieStore> toCookieStore()
    {
        return new CookieStoreCollector();
    }
}
