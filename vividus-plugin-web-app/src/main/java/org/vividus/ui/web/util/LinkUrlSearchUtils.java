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

package org.vividus.ui.web.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.vividus.util.UriUtils;

public final class LinkUrlSearchUtils
{
    private static final char SHARP_SYMBOL = '#';

    private LinkUrlSearchUtils()
    {
        //nothing to-do
    }

    public static String getCurrentHref(String href, String url, WebDriver webDriver)
    {
        if (href == null || url == null)
        {
            return null;
        }
        boolean startsWithSlash = url.charAt(0) == '/';
        String currentUrl = webDriver.getCurrentUrl();
        Pattern pattern = Pattern.compile(buildCurrentDomainPattern(startsWithSlash, currentUrl));
        if (startsWithSlash)
        {
            Matcher m = pattern.matcher(href);
            if (m.find())
            {
                return href.substring(m.group(1).length());
            }
        }
        else if (!pattern.matcher(url).find() && areSchemeAndAuthorityEqual(currentUrl, href))
        {
            if (url.charAt(0) == SHARP_SYMBOL)
            {
                return href.substring(href.indexOf(SHARP_SYMBOL));
            }
            Matcher m = pattern.matcher(currentUrl);
            if (m.find())
            {
                return href.substring(m.group(0).length());
            }
        }
        return href;
    }

    private static String buildCurrentDomainPattern(boolean absoluteUrl, String url)
    {
        StringBuilder currentDomainPattern = new StringBuilder("((%1$s://(.*:.*@)?%2$s%3$s){1}");
        try
        {
            URL currentUrl = new URL(url);
            int port = currentUrl.getPort();
            String portAsString = port != -1 ? ":" + port : "";
            currentDomainPattern.append(absoluteUrl ? ")(.[^/]*)" : ".*)(\\w*/)");
            return String.format(currentDomainPattern.toString(), currentUrl.getProtocol(), currentUrl.getHost(),
                    portAsString);
        }
        catch (MalformedURLException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static boolean areSchemeAndAuthorityEqual(String href, String url)
    {
        URI hrefUri = UriUtils.createUri(href);
        URI uri = UriUtils.createUri(url);
        return hrefUri.getScheme().equals(uri.getScheme()) && hrefUri.getAuthority().equals(uri.getAuthority());
    }
}
