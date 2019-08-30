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

package org.vividus.proxy;

import static org.mockito.Mockito.verify;

import com.browserup.bup.filters.RequestFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "org.vividus.proxy.*")
public class ThreadedProxyTests
{
    private final ThreadedProxy threadedProxy = new ThreadedProxy();

    @Mock
    private Proxy proxy;

    @Before
    public void before() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        PowerMockito.whenNew(Proxy.class).withNoArguments().thenReturn(proxy);
    }

    @Test
    public void testStart()
    {
        threadedProxy.start();
        verify(proxy).start();
    }

    @Test
    public void testSstopRecording()
    {
        threadedProxy.stopRecording();
        verify(proxy).stopRecording();
    }

    @Test
    public void teststartRecording()
    {
        threadedProxy.startRecording();
        verify(proxy).startRecording();
    }

    @Test
    public void teststop()
    {
        threadedProxy.stop();
        verify(proxy).stop();
    }

    @Test
    public void testisStarted()
    {
        threadedProxy.isStarted();
        verify(proxy).isStarted();
    }

    @Test
    public void testgetProxyServer()
    {
        threadedProxy.getProxyServer();
        verify(proxy).getProxyServer();
    }

    @Test
    public void testgetLog()
    {
        threadedProxy.getLog();
        verify(proxy).getLog();
    }

    @Test
    public void testclearRequestFilters()
    {
        threadedProxy.clearRequestFilters();
        verify(proxy).clearRequestFilters();
    }

    @Test
    public void testaddRequestFilter()
    {
        RequestFilter requestFilter = Mockito.mock(RequestFilter.class);
        threadedProxy.addRequestFilter(requestFilter);
        verify(proxy).addRequestFilter(requestFilter);
    }
}
