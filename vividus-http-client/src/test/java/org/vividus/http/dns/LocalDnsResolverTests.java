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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.hc.client5.http.DnsResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocalDnsResolverTests
{
    private static final String HOST = "foo";

    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String IP_ADDRESS = "1.1.1.1";

    @Mock private InetAddress inetAddress;
    @Mock private DnsResolver fallbackDnsResolver;
    @InjectMocks private LocalDnsResolver localDnsResolver;

    @Test
    void shouldResolveFromCustomMapping() throws UnknownHostException
    {
        localDnsResolver.setDnsMappingStorage(Map.of(HOST, IP_ADDRESS));
        try (var inetAddressStaticMock = mockStatic(InetAddress.class))
        {
            inetAddressStaticMock.when(() -> InetAddress.getByName(IP_ADDRESS)).thenReturn(inetAddress);
            assertArrayEquals(new InetAddress[] { this.inetAddress }, localDnsResolver.resolve(HOST));
            verifyNoInteractions(fallbackDnsResolver);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { HOST, "foo.mycompany.com" })
    void shouldResolveCanonicalHostnameFromCustomMapping(String cname) throws UnknownHostException
    {
        localDnsResolver.setDnsMappingStorage(Map.of(HOST, IP_ADDRESS));
        try (var inetAddressStaticMock = mockStatic(InetAddress.class))
        {
            inetAddressStaticMock.when(() -> InetAddress.getByName(IP_ADDRESS)).thenReturn(inetAddress);
            when(inetAddress.getCanonicalHostName()).thenReturn(cname);
            when(inetAddress.getHostAddress()).thenReturn(HOST);
            assertEquals(cname, localDnsResolver.resolveCanonicalHostname(HOST));
            verifyNoInteractions(fallbackDnsResolver);
        }
    }

    @Test
    void shouldResolveWithFallback() throws UnknownHostException
    {
        localDnsResolver.setDnsMappingStorage(Map.of());
        var inetAddresses = new InetAddress[] { inetAddress };
        when(fallbackDnsResolver.resolve(HOST)).thenReturn(inetAddresses);
        try (var inetAddressStaticMock = mockStatic(InetAddress.class))
        {
            assertArrayEquals(inetAddresses, localDnsResolver.resolve(HOST));
            inetAddressStaticMock.verifyNoInteractions();
        }
    }

    @Test
    void shouldResolveCanonicalHostnameWithFallback() throws UnknownHostException
    {
        localDnsResolver.setDnsMappingStorage(Map.of());
        var cname = "cname";
        when(fallbackDnsResolver.resolveCanonicalHostname(HOST)).thenReturn(cname);
        try (var inetAddressStaticMock = mockStatic(InetAddress.class))
        {
            assertEquals(cname, localDnsResolver.resolveCanonicalHostname(HOST));
            inetAddressStaticMock.verifyNoInteractions();
        }
    }
}
