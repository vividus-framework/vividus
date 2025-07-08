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

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.http.client.CookieStoreCollector;
import org.vividus.selenium.IWebDriverProvider;

public class WebDriverCookieManager implements CookieManager<Cookie>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverCookieManager.class);

    private final IWebDriverProvider webDriverProvider;

    public WebDriverCookieManager(IWebDriverProvider webDriverProvider)
    {
        this.webDriverProvider = webDriverProvider;
    }

    @Override
    public void addCookie(String cookieName, String cookieValue, String path, String urlAsString)
    {
        String domain = InetAddressUtils.getDomain(urlAsString);
        addCookieToBrowserContext(cookieName, cookieValue, path, domain);
    }

    @Override
    public void addHttpClientCookies(List<org.apache.hc.client5.http.cookie.Cookie> httpCookies)
    {
        String currentUrl = webDriverProvider.get().getCurrentUrl();
        httpCookies.stream()
                .filter(httpCookie -> {
                    boolean subDomain = InetAddressUtils.isSubDomain(
                            StringUtils.removeStart(httpCookie.getDomain(), '.'), currentUrl);
                    if (!subDomain)
                    {
                        LOGGER.atInfo()
                                .addArgument(httpCookie::getDomain)
                                .addArgument(currentUrl)
                                .addArgument(httpCookie)
                                .log("The cookie is not added to the browser as its domain '{}' is neither subdomain"
                                     + " nor equal to the current page domain '{}'. Full cookie: {}");
                    }
                    return subDomain;
                })
                .forEach(
                        httpCookie -> addCookieToBrowserContext(httpCookie.getName(), httpCookie.getValue(),
                                httpCookie.getPath(), httpCookie.getDomain()));
    }

    private void addCookieToBrowserContext(String cookieName, String cookieValue, String path, String domain)
    {
        Cookie cookie = new Cookie.Builder(cookieName, cookieValue)
                .domain(domain)
                .path(path)
                .build();
        LOGGER.info("Adding cookie: {}", cookie);
        getOptions().addCookie(cookie);
    }

    @Override
    public void deleteAllCookies()
    {
        getOptions().deleteAllCookies();
    }

    @Override
    public void deleteCookie(String cookieName)
    {
        getOptions().deleteCookieNamed(cookieName);
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
        return getCookies().stream().map(WebDriverCookieManager::createHttpClientCookie).collect(
                new CookieStoreCollector());
    }

    private static org.apache.hc.client5.http.cookie.Cookie createHttpClientCookie(Cookie seleniumCookie)
    {
        BasicClientCookie httpClientCookie = new BasicClientCookie(seleniumCookie.getName(), seleniumCookie.getValue());
        httpClientCookie.setDomain(seleniumCookie.getDomain());
        httpClientCookie.setPath(seleniumCookie.getPath());
        Optional.ofNullable(seleniumCookie.getExpiry()).map(Date::toInstant).ifPresent(httpClientCookie::setExpiryDate);
        httpClientCookie.setSecure(seleniumCookie.isSecure());
        httpClientCookie.setAttribute(org.apache.hc.client5.http.cookie.Cookie.DOMAIN_ATTR, seleniumCookie.getDomain());
        httpClientCookie.setAttribute(org.apache.hc.client5.http.cookie.Cookie.PATH_ATTR, seleniumCookie.getPath());
        return httpClientCookie;
    }

    private Options getOptions()
    {
        return webDriverProvider.get().manage();
    }
}
