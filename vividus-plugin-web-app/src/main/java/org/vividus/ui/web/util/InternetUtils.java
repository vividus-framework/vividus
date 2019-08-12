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

import java.net.URI;
import java.net.URL;
import java.util.List;

import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;

public final class InternetUtils
{
    private static final String DOMAIN_PARTS_SEPARATOR = ".";

    private InternetUtils()
    {
    }

    /**
     * Gets the domain with desired levels.
     * <p>For example:</p>
     * <ul>
     * <li>for URI: "https://www.by.example.com" and levels = 2 method will return example.com</li>
     * <li>for URI: "https://www.by.example.com" and levels = 5 method will return www.by.example.com</li>
     * </ul>
     * @param uri  Uri to process
     * @param domainLevels desired domain levels
     * @return processed domain with desired levels
     */
    public static String getDomainName(URI uri, int domainLevels)
    {
        InternetDomainName domaneName = InternetDomainName.from(uri.getHost());
        List<String> domainParts = domaneName.parts();
        if (domainLevels < domainParts.size())
        {
            List<String> resultDomainParts = domainParts.subList(domainParts.size() - domainLevels, domainParts.size());
            return String.join(DOMAIN_PARTS_SEPARATOR, resultDomainParts);
        }
        return domaneName.toString();
    }

    /**
     * Gets url's top domain
     * @param url Desired URL
     * @return Top domain, if there are several domains. Host, if there is only one domain
     */
    public static String getTopDomain(URL url)
    {
        return getTopDomain(url.getHost());
    }

    /**
     * Gets uri's top domain
     * @param uri Desired URI
     * @return Top domain, if there are several domains. Host, if there is only one domain
     */
    public static String getTopDomain(URI uri)
    {
        return getTopDomain(uri.getHost());
    }

    private static String getTopDomain(String host)
    {
        return host.contains(DOMAIN_PARTS_SEPARATOR) && !InetAddresses.isInetAddress(host)
                ? InternetDomainName.from(host).topPrivateDomain().toString()
                : host;
    }
}
