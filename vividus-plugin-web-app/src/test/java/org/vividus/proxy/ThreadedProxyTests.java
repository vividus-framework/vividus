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

package org.vividus.proxy;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.filters.RequestFilter;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.vividus.model.IntegerRange;

@ExtendWith(TestLoggerFactoryExtension.class)
class ThreadedProxyTests
{
    private static final TestLogger TEST_LOGGER = TestLoggerFactory.getTestLogger(ThreadedProxy.class);

    private static final String LOCALHOST = "localhost";
    private static final InetAddress INET_ADDR;

    static
    {
        try
        {
            INET_ADDR = InetAddress.getByName(LOCALHOST);
        }
        catch (UnknownHostException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Mock
    private IProxy proxy;

    @Mock
    private IProxyFactory proxyFactory;

    private ThreadedProxy threadedProxy;

    @BeforeEach
    void before()
    {
        MockitoAnnotations.initMocks(this);
        when(proxyFactory.createProxy()).thenReturn(proxy);
    }

    @Test
    void testAllocatePort() throws UnknownHostException
    {
        int port = 55_389;
        BrowserUpProxy mobProxy = mock(BrowserUpProxy.class);
        when(proxy.getProxyServer()).thenReturn(mobProxy);
        when(mobProxy.getPort()).thenReturn(port);
        threadedProxy = new ThreadedProxy(LOCALHOST, range(port), proxyFactory);
        InOrder order = inOrder(proxy);

        threadedProxy.start();
        threadedProxy.stop();

        order.verify(proxy).start(port, INET_ADDR);
        order.verify(proxy).getProxyServer();
        order.verify(proxy).stop();
        order.verifyNoMoreInteractions();

        String portsMessage = "Available ports for proxies in the pool: {}";
        assertEquals(List.of(
                info("Allocate {} port from the proxy ports pool", port),
                info(portsMessage, List.of().toString()),
                info("Return {} port back to the proxy ports pool", port),
                info(portsMessage, List.of(port).toString())),
                TEST_LOGGER.getLoggingEvents());
    }

    @Test
    void testAllocateDefaultPort() throws UnknownHostException
    {
        defaultInit();
        InOrder order = inOrder(proxy);

        threadedProxy.start();
        threadedProxy.stop();

        order.verify(proxy).start();
        order.verify(proxy).stop();
        order.verifyNoMoreInteractions();

        assertTrue(TEST_LOGGER.getLoggingEvents().isEmpty());
    }

    @Test
    void testAllocateIllegalPortsSequence()
    {
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> new ThreadedProxy(LOCALHOST, range(0, 56_701), proxyFactory));
        assertEquals("Port 0 (ephemeral port selection) can not be used with custom ports", exception.getMessage());
        assertTrue(TEST_LOGGER.getLoggingEvents().isEmpty());
    }

    @Test
    void testAllocateIllegalPortsNumbers()
    {
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> new ThreadedProxy(LOCALHOST, range(-1), proxyFactory));
        assertEquals("Expected ports range is 1-65535 but got: -1", exception.getMessage());
        assertTrue(TEST_LOGGER.getLoggingEvents().isEmpty());
    }

    @Test
    void testAllocateNoPortsAvailable() throws UnknownHostException
    {
        threadedProxy = new ThreadedProxy(LOCALHOST, range(54_786), proxyFactory);
        threadedProxy.start();
        Exception exception = assertThrows(IllegalArgumentException.class, threadedProxy::start);
        assertEquals("There are no available ports in the ports pool", exception.getMessage());
    }

    @Test
    void testStart() throws UnknownHostException
    {
        defaultInit();
        threadedProxy.start();
        verify(proxy).start();
    }

    @Test
    void testStartOnPort() throws UnknownHostException
    {
        threadedProxy = new ThreadedProxy(LOCALHOST, range(1), proxyFactory);
        threadedProxy.start(1, INET_ADDR);
        verify(proxy).start(1, INET_ADDR);
    }

    @Test
    void testStopRecording() throws UnknownHostException
    {
        defaultInit();
        threadedProxy.stopRecording();
        verify(proxy).stopRecording();
    }

    @Test
    void testStartRecording() throws UnknownHostException
    {
        defaultInit();
        threadedProxy.startRecording();
        verify(proxy).startRecording();
    }

    @Test
    void testStop() throws UnknownHostException
    {
        defaultInit();
        threadedProxy.stop();
        verify(proxy).stop();
    }

    @Test
    void testIsStarted() throws UnknownHostException
    {
        defaultInit();
        threadedProxy.isStarted();
        verify(proxy).isStarted();
    }

    @Test
    void testGetProxyServer() throws UnknownHostException
    {
        defaultInit();
        threadedProxy.getProxyServer();
        verify(proxy).getProxyServer();
    }

    @Test
    void testGetLog() throws UnknownHostException
    {
        defaultInit();
        threadedProxy.getLog();
        verify(proxy).getLog();
    }

    @Test
    void testClearRequestFilters() throws UnknownHostException
    {
        defaultInit();
        threadedProxy.clearRequestFilters();
        verify(proxy).clearRequestFilters();
    }

    @Test
    void testAddRequestFilter() throws UnknownHostException
    {
        defaultInit();
        RequestFilter requestFilter = mock(RequestFilter.class);
        threadedProxy.addRequestFilter(requestFilter);
        verify(proxy).addRequestFilter(requestFilter);
    }

    private void defaultInit() throws UnknownHostException
    {
        threadedProxy = new ThreadedProxy(LOCALHOST, range(0), proxyFactory);
    }

    private IntegerRange range(Integer... numbers)
    {
        return new IntegerRange(Set.of(numbers));
    }
}
