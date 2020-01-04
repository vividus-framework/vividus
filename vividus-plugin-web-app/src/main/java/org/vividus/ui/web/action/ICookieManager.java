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

package org.vividus.ui.web.action;

import java.net.URI;
import java.net.URL;
import java.util.Set;

import org.apache.http.client.CookieStore;
import org.openqa.selenium.Cookie;

public interface ICookieManager
{
    void addCookie(String cookieName, String cookieValue, String path, String urlAsString);

    void addCookie(String cookieName, String cookieValue, String path, URL url);

    void addCookie(String cookieName, String cookieValue, String path, URI uri);

    void deleteAllCookies();

    void deleteCookie(String cookieName);

    Cookie getCookie(String cookieName);

    Set<Cookie> getCookies();

    CookieStore getCookiesAsHttpCookieStore();
}
