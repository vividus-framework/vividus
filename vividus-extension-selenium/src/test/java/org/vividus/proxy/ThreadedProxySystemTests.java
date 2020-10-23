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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.browserup.bup.BrowserUpProxy;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.vividus.model.IntegerRange;
import org.vividus.util.Sleeper;

class ThreadedProxySystemTests
{
    @Test
    void testAllocatePorts() throws InterruptedException, ExecutionException, UnknownHostException
    {
        String host = "localhost";
        InetAddress address = InetAddress.getByName(host);
        IntegerRange range = new IntegerRange(Set.of(55_023, 53_450, 55_300));
        IProxyFactory proxyFactory = mock(IProxyFactory.class);
        ThreadedProxy threadedProxy = new ThreadedProxy(host, range, proxyFactory);

        ExecutorService executor = Executors.newFixedThreadPool(3);
        Map<String, Proxy> localProxies = mockLocalProxy(3);
        Supplier<Proxy> proxyGetter = () -> localProxies.get(Thread.currentThread().getName());
        when(proxyFactory.createProxy()).thenAnswer(inv -> proxyGetter.get());

        int tasksCount = 50;
        List<Callable<Optional<Throwable>>> tasks = new ArrayList<>(tasksCount);
        IntStream.range(0, tasksCount).forEach(i -> tasks.add(() ->
        {
            try
            {
                Proxy localProxy = proxyGetter.get();
                BrowserUpProxy mobProxy = mock(BrowserUpProxy.class);
                ArgumentCaptor<Integer> portCapturer = ArgumentCaptor.forClass(Integer.class);

                threadedProxy.start();

                verify(localProxy, atLeastOnce()).start(portCapturer.capture(), eq(address));
                when(localProxy.getProxyServer()).thenReturn(mobProxy);
                when(mobProxy.getPort()).thenReturn(portCapturer.getValue());
                int timeout = RandomUtils.nextInt(0, 100);
                Sleeper.sleep(Duration.ofMillis(timeout));

                threadedProxy.stop();
            }
            catch (Exception | AssertionError e)
            {
                return Optional.of(e);
            }
            return Optional.empty();
        }));
        List<Future<Optional<Throwable>>> futures = executor.invokeAll(tasks);
        executor.awaitTermination(5, TimeUnit.SECONDS);
        for (Future<Optional<Throwable>> future : futures)
        {
            assertTrue(future.get().isEmpty());
        }
    }

    private Map<String, Proxy> mockLocalProxy(int numberOfThreads)
    {
        String defaultThreadPrefix = "pool-1-thread-";
        return IntStream.range(0, numberOfThreads)
            .map(i -> i + 1)
            .boxed()
            .map(String::valueOf)
            .map(defaultThreadPrefix::concat)
            .collect(Collectors.toMap(Function.identity(), tn -> mock(Proxy.class)));
    }
}
