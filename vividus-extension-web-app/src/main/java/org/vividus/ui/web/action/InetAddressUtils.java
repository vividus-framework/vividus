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

import java.net.URI;
import java.util.function.Function;

import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;

public final class InetAddressUtils
{
    private static final String DOMAIN_PARTS_SEPARATOR = ".";

    private InetAddressUtils()
    {
    }

    /**
     * Returns the domain of the URL.
     *
     * @param urlAsString String representation of the URL.
     * @return The domain of the URL.
     */
    public static String getDomain(String urlAsString)
    {
        return processDomain(urlAsString, DOMAIN_PARTS_SEPARATOR::concat, Function.identity());
    }

    /**
     * Checks if the specified domain is either a subdomain or equal to the given URL domain.
     *
     * @param domain      The domain to check.
     * @param urlAsString The URL as a string to check against.
     * @return true if the specified domain is either a subdomain or equal to the URL domain, false otherwise.
     */
    public static boolean isSubDomain(String domain, String urlAsString)
    {
        return processDomain(urlAsString, topDomain -> topDomain.endsWith(domain), host -> host.equals(domain));
    }

    private static <T> T processDomain(String urlAsString, Function<String, T> domainProcessor,
            Function<String, T> hostProcessor)
    {
        String host = URI.create(urlAsString).getHost();
        return host.contains(DOMAIN_PARTS_SEPARATOR) && !InetAddresses.isInetAddress(host)
                ? domainProcessor.apply(getTopDomain(host))
                : hostProcessor.apply(host);
    }

    private static String getTopDomain(String host)
    {
        return InternetDomainName.from(host).topPrivateDomain().toString();
    }
}
