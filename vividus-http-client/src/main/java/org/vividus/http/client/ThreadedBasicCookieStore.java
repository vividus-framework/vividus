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

package org.vividus.http.client;

import java.util.Date;
import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;

public class ThreadedBasicCookieStore implements CookieStore
{
    private final ThreadLocal<BasicCookieStore> cookieStoreThreadLocal = ThreadLocal.withInitial(BasicCookieStore::new);

    @Override
    public void addCookie(final Cookie cookie)
    {
        cookieStoreThreadLocal.get().addCookie(cookie);
    }

    @Override
    public List<Cookie> getCookies()
    {
        return cookieStoreThreadLocal.get().getCookies();
    }

    @Override
    public boolean clearExpired(final Date date)
    {
        return cookieStoreThreadLocal.get().clearExpired(date);
    }

    @Override
    public void clear()
    {
        cookieStoreThreadLocal.get().clear();
    }

    @Override
    public String toString()
    {
        return cookieStoreThreadLocal.get().toString();
    }
}
