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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.model.IntegerRange;
import org.vividus.testcontext.SimpleTestContext;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
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

    @Mock private Proxy proxy;
    @Mock private IProxyFactory proxyFactory;
    private ThreadedProxy threadedProxy;

    @Test
    void testAllocatePort() throws UnknownHostException
    {
        int port = 55_389;
        when(proxyFactory.createProxy()).thenReturn(proxy);
        BrowserUpProxy mobProxy = mock(BrowserUpProxy.class);
        when(proxy.getProxyServer()).thenReturn(mobProxy);
        when(mobProxy.getPort()).thenReturn(port);
        threadedProxy = new ThreadedProxy(LOCALHOST, range(port), proxyFactory, new SimpleTestContext());
        InOrder order = inOrder(proxy);

        threadedProxy.start();
        threadedProxy.stop();

        order.verify(proxy).start(port, INET_ADDR);
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
        IntegerRange proxyPorts = range(0, 56_701);
        SimpleTestContext testContext = new SimpleTestContext();
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> new ThreadedProxy(LOCALHOST, proxyPorts, proxyFactory, testContext));
        assertEquals("Port 0 (ephemeral port selection) can not be used with custom ports", exception.getMessage());
        assertTrue(TEST_LOGGER.getLoggingEvents().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 65_536})
    void testAllocateIllegalPortsNumbers(int invalidPort)
    {
        IntegerRange proxyPorts = range(invalidPort);
        SimpleTestContext testContext = new SimpleTestContext();
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> new ThreadedProxy(LOCALHOST, proxyPorts, proxyFactory, testContext));
        assertEquals("Expected ports range is 1-65535 but got: " + invalidPort, exception.getMessage());
        assertTrue(TEST_LOGGER.getLoggingEvents().isEmpty());
    }

    @Test
    void testAllocateNoPortsAvailable() throws UnknownHostException
    {
        when(proxyFactory.createProxy()).thenReturn(proxy);
        threadedProxy = new ThreadedProxy(LOCALHOST, range(54_786), proxyFactory, new SimpleTestContext());
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
    void shouldCreateSeleniumProxy() throws UnknownHostException
    {
        defaultInit();
        threadedProxy.createSeleniumProxy();
        verify(proxy).createSeleniumProxy();
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
    void testClearRecordedData() throws UnknownHostException
    {
        defaultInit();
        threadedProxy.clearRecordedData();
        verify(proxy).clearRecordedData();
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
    void testGetHar() throws UnknownHostException
    {
        defaultInit();
        threadedProxy.getRecordedData();
        verify(proxy).getRecordedData();
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
        threadedProxy = new ThreadedProxy(LOCALHOST, range(0), proxyFactory, new SimpleTestContext());
        when(proxyFactory.createProxy()).thenReturn(proxy);
    }

    private IntegerRange range(Integer... numbers)
    {
        return new IntegerRange(Set.of(numbers));
    }
}
