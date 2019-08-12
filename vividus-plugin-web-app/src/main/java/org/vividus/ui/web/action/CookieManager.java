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

import java.net.URI;
import java.net.URL;
import java.util.Set;

import javax.inject.Inject;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.SetCookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.http.client.ClientBuilderUtils;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.web.util.InternetUtils;

public class CookieManager implements ICookieManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CookieManager.class);
    private static final String JS_COOKIE_FORMAT = "document.cookie='%s=%s; path=%s; domain=%s'";
    private static final String JS_DELETE_COOKIE_FORMAT = "document.cookie='%s=; expires=-1'";

    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IJavascriptActions javasciptActions;
    @Inject private IWebDriverManager webDriverManager;

    @Override
    public void addCookie(String cookieName, String cookieValue, String path, String urlAsString)
    {
        addCookie(cookieName, cookieValue, path, URI.create(urlAsString));
    }

    @Override
    public void addCookie(String cookieName, String cookieValue, String path, URL url)
    {
        executeAddCookieScript(cookieName, cookieValue, path, InternetUtils.getTopDomain(url));
    }

    @Override
    public void addCookie(String cookieName, String cookieValue, String path, URI uri)
    {
        executeAddCookieScript(cookieName, cookieValue, path, InternetUtils.getTopDomain(uri));
    }

    private void executeAddCookieScript(String cookieName, String cookieValue, String path, String domain)
    {
        String cookie = String.format(JS_COOKIE_FORMAT, cookieName, cookieValue, path, domain);
        LOGGER.debug("Adding cookie: {}", cookie);
        javasciptActions.executeScript(cookie);
    }

    @Override
    public void deleteAllCookies()
    {
        if (webDriverManager.isTypeAnyOf(WebDriverType.SAFARI))
        {
            getCookies().forEach(cookie -> deleteCookieByScript(cookie.getName()));
        }
        else
        {
            getOptions().deleteAllCookies();
        }
    }

    @Override
    public void deleteCookie(String cookieName)
    {
        if (webDriverManager.isTypeAnyOf(WebDriverType.SAFARI))
        {
            deleteCookieByScript(cookieName);
        }
        else
        {
            getOptions().deleteCookieNamed(cookieName);
        }
    }

    private void deleteCookieByScript(String cookieName)
    {
        // Workaround for safari driver bug:
        // https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/5212
        String deleteScript = String.format(JS_DELETE_COOKIE_FORMAT, cookieName);
        javasciptActions.executeScript(deleteScript);
    }

    @Override
    public Cookie getCookie(String cookieName)
    {
        return getOptions().getCookieNamed(cookieName);
    }

    @Override
    public Set<Cookie> getCookies()
    {
        return getOptions().getCookies();
    }

    @Override
    public CookieStore getCookiesAsHttpCookieStore()
    {
        return getCookies().stream().map(CookieManager::createHttpClientCookie).collect(
                ClientBuilderUtils.toCookieStore());
    }

    private static org.apache.http.cookie.Cookie createHttpClientCookie(Cookie seleniumCookie)
    {
        SetCookie httpClientCookie = new BasicClientCookie(seleniumCookie.getName(), seleniumCookie.getValue());
        httpClientCookie.setDomain(seleniumCookie.getDomain());
        httpClientCookie.setPath(seleniumCookie.getPath());
        httpClientCookie.setExpiryDate(seleniumCookie.getExpiry());
        httpClientCookie.setSecure(seleniumCookie.isSecure());
        return httpClientCookie;
    }

    private Options getOptions()
    {
        return webDriverProvider.get().manage();
    }
}
