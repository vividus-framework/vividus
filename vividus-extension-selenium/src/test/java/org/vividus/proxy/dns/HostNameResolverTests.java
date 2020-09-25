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

package org.vividus.proxy.dns;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

@PrepareForTest(HostNameResolver.class)
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
public class HostNameResolverTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(HostNameResolver.class);

    private static final String HOST = "host";
    private static final String IP_ADDRESS = "1.1.1.1";

    @Mock
    private InetAddress inetAddress;

    @InjectMocks
    private HostNameResolver hostNameResolver;

    @After
    public void clearLoggers()
    {
        TestLoggerFactory.clear();
    }

    @Test
    public void testResolve() throws Exception
    {
        hostNameResolver.setDnsMappingStorage(Collections.singletonMap(HOST, IP_ADDRESS));
        PowerMockito.mockStatic(InetAddress.class);
        when(InetAddress.getByName(IP_ADDRESS)).thenReturn(inetAddress);
        assertEquals(singletonList(inetAddress), hostNameResolver.resolve(HOST));
        assertTrue(LOGGER.getLoggingEvents().isEmpty());
        PowerMockito.verifyStatic(InetAddress.class, never());
        InetAddress.getAllByName(any());
    }

    @Test
    public void testResolveHostsAreEmpty() throws Exception
    {
        hostNameResolver.setDnsMappingStorage(Collections.emptyMap());
        HostNameResolver spyHostNameResolver = spy(hostNameResolver);
        doReturn(HOST).when(spyHostNameResolver).applyRemapping(HOST);
        doReturn(singletonList(inetAddress)).when(spyHostNameResolver).resolveRemapped(HOST);
        PowerMockito.mockStatic(InetAddress.class);
        assertEquals(singletonList(inetAddress), spyHostNameResolver.resolve(HOST));
        assertTrue(LOGGER.getLoggingEvents().isEmpty());
        PowerMockito.verifyStatic(InetAddress.class, never());
        InetAddress.getByName(any());
    }

    @Test
    public void testResolveException() throws Exception
    {
        UnknownHostException exception = mock(UnknownHostException.class);
        hostNameResolver.setDnsMappingStorage(Collections.singletonMap(HOST, IP_ADDRESS));
        HostNameResolver spyHostNameResolver = spy(hostNameResolver);
        PowerMockito.mockStatic(InetAddress.class);
        when(InetAddress.getByName(IP_ADDRESS)).thenThrow(exception);
        doReturn(HOST).when(spyHostNameResolver).applyRemapping(HOST);
        doReturn(singletonList(inetAddress)).when(spyHostNameResolver).resolveRemapped(HOST);
        assertEquals(singletonList(inetAddress), spyHostNameResolver.resolve(HOST));
        assertThat(LOGGER.getLoggingEvents(),
                is(singletonList(warn(exception, "Unable to determine hostname by ip address"))));
    }
}
