/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.http.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.commons.lang3.function.FailableFunction;
import org.apache.hc.client5.http.DnsResolver;

public class LocalDnsResolver implements DnsResolver
{
    private Map<String, String> dnsMappingStorage;
    private DnsResolver fallbackDnsResolver;

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException
    {
        return resolve(host, inetAddress -> new InetAddress[] { inetAddress }, fallbackDnsResolver::resolve);
    }

    @Override
    public String resolveCanonicalHostname(String host) throws UnknownHostException
    {
        return resolve(host, inetAddress -> {
            String canonicalServer = inetAddress.getCanonicalHostName();
            return inetAddress.getHostAddress().contentEquals(canonicalServer) ? host : canonicalServer;
        }, fallbackDnsResolver::resolveCanonicalHostname);
    }

    private <T> T resolve(String host, FailableFunction<InetAddress, T, UnknownHostException> resolver,
            FailableFunction<String, T, UnknownHostException> fallback) throws UnknownHostException
    {
        String ipAddress = dnsMappingStorage.get(host);
        if (ipAddress != null)
        {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            return resolver.apply(inetAddress);
        }
        return fallback.apply(host);
    }

    public void setDnsMappingStorage(Map<String, String> dnsMappingStorage)
    {
        this.dnsMappingStorage = dnsMappingStorage;
    }

    public void setFallbackDnsResolver(DnsResolver fallbackDnsResolver)
    {
        this.fallbackDnsResolver = fallbackDnsResolver;
    }
}
