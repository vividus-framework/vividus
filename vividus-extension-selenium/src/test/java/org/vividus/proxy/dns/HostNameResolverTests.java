/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.proxy.dns;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class HostNameResolverTests
{
    private static final String HOST = "host";
    private static final String IP_ADDRESS = "1.1.1.1";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(HostNameResolver.class);

    @Mock private InetAddress inetAddress;
    @InjectMocks private HostNameResolver hostNameResolver;

    @Test
    void testResolve()
    {
        hostNameResolver.setDnsMappingStorage(Map.of(HOST, IP_ADDRESS));
        try (MockedStatic<InetAddress> inetAddressMock = mockStatic(InetAddress.class))
        {
            inetAddressMock.when(() -> InetAddress.getByName(IP_ADDRESS)).thenReturn(inetAddress);
            assertEquals(List.of(inetAddress), hostNameResolver.resolve(HOST));
            assertTrue(logger.getLoggingEvents().isEmpty());
            inetAddressMock.verify(() -> InetAddress.getAllByName(any()), never());
        }
    }

    @Test
    void testResolveHostsAreEmpty()
    {
        hostNameResolver.setDnsMappingStorage(Map.of());
        HostNameResolver spyHostNameResolver = spy(hostNameResolver);
        doReturn(HOST).when(spyHostNameResolver).applyRemapping(HOST);
        doReturn(List.of(inetAddress)).when(spyHostNameResolver).resolveRemapped(HOST);
        try (MockedStatic<InetAddress> inetAddressMock = mockStatic(InetAddress.class))
        {
            assertEquals(List.of(inetAddress), spyHostNameResolver.resolve(HOST));
            assertTrue(logger.getLoggingEvents().isEmpty());
            inetAddressMock.verify(() -> InetAddress.getByName(any()), never());
        }
    }

    @Test
    void testResolveException()
    {
        UnknownHostException exception = mock(UnknownHostException.class);
        hostNameResolver.setDnsMappingStorage(Map.of(HOST, IP_ADDRESS));
        HostNameResolver spyHostNameResolver = spy(hostNameResolver);
        try (MockedStatic<InetAddress> inetAddressMock = mockStatic(InetAddress.class))
        {
            inetAddressMock.when(() -> InetAddress.getByName(IP_ADDRESS)).thenThrow(exception);
            doReturn(HOST).when(spyHostNameResolver).applyRemapping(HOST);
            doReturn(List.of(inetAddress)).when(spyHostNameResolver).resolveRemapped(HOST);
            assertEquals(List.of(inetAddress), spyHostNameResolver.resolve(HOST));
            assertThat(logger.getLoggingEvents(),
                    is(List.of(warn(exception, "Unable to determine hostname by ip address"))));
        }
    }
}
