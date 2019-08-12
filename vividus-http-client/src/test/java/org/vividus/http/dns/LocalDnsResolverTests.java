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

package org.vividus.http.dns;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.Collections;

import org.apache.http.conn.DnsResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

@PrepareForTest(LocalDnsResolver.class)
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
public class LocalDnsResolverTests
{
    private static final String HOST = "host";

    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String IP_ADDRESS = "1.1.1.1";

    @Mock
    private InetAddress inetAddress;

    @Mock
    private DnsResolver fallbackDnsResolver;

    @InjectMocks
    private LocalDnsResolver localDnsResolver;

    @Test
    public void testResolve() throws Exception
    {
        localDnsResolver.setDnsMappingStorage(Collections.singletonMap(HOST, IP_ADDRESS));
        PowerMockito.mockStatic(InetAddress.class);
        when(InetAddress.getByName(IP_ADDRESS)).thenReturn(inetAddress);
        assertArrayEquals(new InetAddress[] { inetAddress }, localDnsResolver.resolve(HOST));
        verifyZeroInteractions(fallbackDnsResolver);
    }

    @Test
    public void testResolveHostsAreEmpty() throws Exception
    {
        localDnsResolver.setDnsMappingStorage(Collections.emptyMap());
        InetAddress[] inetAddresses = { inetAddress };
        when(fallbackDnsResolver.resolve(HOST)).thenReturn(inetAddresses);
        PowerMockito.mockStatic(InetAddress.class);
        assertArrayEquals(inetAddresses, localDnsResolver.resolve(HOST));
        PowerMockito.verifyStatic(InetAddress.class, never());
        InetAddress.getByName(any());
    }
}
