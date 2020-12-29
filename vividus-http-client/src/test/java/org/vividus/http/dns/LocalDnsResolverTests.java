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

package org.vividus.http.dns;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;

import org.apache.http.conn.DnsResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocalDnsResolverTests
{
    private static final String HOST = "host";

    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String IP_ADDRESS = "1.1.1.1";

    @Mock private InetAddress inetAddress;
    @Mock private DnsResolver fallbackDnsResolver;
    @InjectMocks private LocalDnsResolver localDnsResolver;

    @Test
    void testResolve() throws UnknownHostException
    {
        localDnsResolver.setDnsMappingStorage(Collections.singletonMap(HOST, IP_ADDRESS));
        try (MockedStatic<InetAddress> inetAddress = mockStatic(InetAddress.class))
        {
            inetAddress.when(() -> InetAddress.getByName(IP_ADDRESS)).thenReturn(this.inetAddress);
            assertArrayEquals(new InetAddress[] { this.inetAddress }, localDnsResolver.resolve(HOST));
            verifyNoInteractions(fallbackDnsResolver);
        }
    }

    @Test
    void testResolveHostsAreEmpty() throws UnknownHostException
    {
        localDnsResolver.setDnsMappingStorage(Collections.emptyMap());
        InetAddress[] inetAddresses = { inetAddress };
        when(fallbackDnsResolver.resolve(HOST)).thenReturn(inetAddresses);
        try (MockedStatic<InetAddress> inetAddress = mockStatic(InetAddress.class))
        {
            assertArrayEquals(inetAddresses, localDnsResolver.resolve(HOST));
            inetAddress.verifyNoInteractions();
            InetAddress.getByName(any());
        }
    }
}
