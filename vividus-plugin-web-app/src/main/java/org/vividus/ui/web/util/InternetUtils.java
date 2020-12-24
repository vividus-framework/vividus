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

package org.vividus.ui.web.util;

import java.net.URI;

import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;

public final class InternetUtils
{
    private static final String DOMAIN_PARTS_SEPARATOR = ".";

    private InternetUtils()
    {
    }

    /**
     * Gets uri's top domain
     * @param uri Desired URI
     * @return Top domain, if there are several domains. Host, if there is only one domain
     */
    public static String getTopDomain(URI uri)
    {
        String host = uri.getHost();
        return host.contains(DOMAIN_PARTS_SEPARATOR) && !InetAddresses.isInetAddress(host)
                ? InternetDomainName.from(host).topPrivateDomain().toString()
                : host;
    }
}
