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

package org.vividus.selenium.browserstack;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.browserstack.local.Local;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedConstruction;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.tunnel.TunnelException;
import org.vividus.selenium.tunnel.TunnelOptions;
import org.vividus.testcontext.ThreadedTestContext;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class BrowserStackLocalManagerTests
{
    private static final String PROXY = "localhost:52377";
    private static final String ACCESS_KEY = "access-key";

    @Captor private ArgumentCaptor<Map<String, String>> optionsCaptor;
    @Spy private ThreadedTestContext testContext;
    private BrowserStackLocalManager manager;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(BrowserStackLocalManager.class);

    @BeforeEach
    void init()
    {
        manager = new BrowserStackLocalManager(ACCESS_KEY, testContext);
    }

    @Test
    void shouldManageTunnelWithProxy() throws Exception
    {
        try (MockedConstruction<Local> localMocks = mockConstruction(Local.class))
        {
            String mainIdentifier = manager.start(createOptions(PROXY));
            String sameThreadIdentifier = manager.start(createOptions(PROXY));
            assertEquals(mainIdentifier, sameThreadIdentifier);

            Local local = localMocks.constructed().get(0);
            verify(local).start(optionsCaptor.capture());
            Map<String, String> parameters = optionsCaptor.getValue();
            assertThat(parameters, aMapWithSize(4));
            verifyParameters(parameters, mainIdentifier);
            String pacPath = parameters.get("-pac-file");
            String pac = FileUtils.fileRead(pacPath, StandardCharsets.UTF_8.toString());
            assertEquals("function FindProxyForURL(url, host) "
                    + "{ if (shExpMatch(host, \"*.browserstack.com\")) { return \"DIRECT\"; }"
                    + "return \"PROXY localhost:52377\"; }", pac);
            assertTrue(manager.isStarted());

            String reusedIdentifier = CompletableFuture.supplyAsync(() ->
            {
                try
                {
                    String id = manager.start(createOptions(PROXY));
                    manager.stop();
                    return id;
                }
                catch (TunnelException e)
                {
                    throw new IllegalStateException(e);
                }
            }).get();
            assertEquals(mainIdentifier, reusedIdentifier);
            assertTrue(manager.isStarted());

            manager.stop();
            assertFalse(manager.isStarted());

            verifyLogs(mainIdentifier);
            verify(local).stop(parameters);
        }
    }

    @Test
    void shouldManageTunnelWithoutProxy() throws Exception
    {
        try (MockedConstruction<Local> localMocks = mockConstruction(Local.class))
        {
            String mainIdentifier = manager.start(createOptions(null));
            Local local = localMocks.constructed().get(0);
            verify(local).start(optionsCaptor.capture());
            Map<String, String> parameters = optionsCaptor.getValue();
            assertThat(parameters, aMapWithSize(3));
            verifyParameters(parameters, mainIdentifier);
            assertTrue(manager.isStarted());

            manager.stop();
            assertFalse(manager.isStarted());

            verifyLogs(mainIdentifier);
            verify(local).stop(parameters);
        }
    }

    @Test
    void shouldWrapExceptionIntoTunnelExceptionOnStart()
    {
        Exception thrown = mock(Exception.class);
        try (MockedConstruction<Local> localMocks = mockConstruction(Local.class,
                (mock, context) -> doThrow(thrown).when(mock).start(any())))
        {
            TunnelException tunnelException = assertThrows(TunnelException.class,
                () -> manager.start(createOptions(null)));
            assertEquals(thrown, tunnelException.getCause());
        }
    }

    @Test
    void shouldWrapExceptionIntoTunnelExceptionOnStop() throws Exception
    {
        Exception thrown = mock(Exception.class);
        try (MockedConstruction<Local> localMocks = mockConstruction(Local.class,
                (mock, context) -> doThrow(thrown).when(mock).stop(any())))
        {
            manager.start(createOptions(null));
            TunnelException tunnelException = assertThrows(TunnelException.class, manager::stop);
            assertEquals(thrown, tunnelException.getCause());
        }
    }

    @Test
    void shouldNotStopNonStartedConnection() throws TunnelException
    {
        manager.stop();

        verify(testContext).get(argThat(
            key -> "class org.vividus.selenium.browserstack.BrowserStackLocalManager$BrowserStackLocalConnection"
                    .equals(key.toString())));
        verifyNoMoreInteractions(testContext);
    }

    private void verifyLogs(String id)
    {
        assertThat(logger.getLoggingEvents(),
                is(List.of(info("Starting BrowserStack Local connection with {} identifier", id),
                           info("Stopping BrowserStack Local connection with {} identifier", id))));
    }

    private static void verifyParameters(Map<String, String> parameters, String id)
    {
        assertEquals(ACCESS_KEY, parameters.get("key"));
        assertEquals(Boolean.TRUE.toString(), parameters.get("forcelocal"));
        assertEquals(id, parameters.get("localIdentifier"));
    }

    private static TunnelOptions createOptions(String proxy)
    {
        TunnelOptions options = new TunnelOptions();
        options.setProxy(proxy);
        return options;
    }
}
