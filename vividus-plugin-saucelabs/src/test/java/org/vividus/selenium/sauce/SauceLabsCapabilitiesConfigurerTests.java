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

package org.vividus.selenium.sauce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.model.Story;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningStory;
import org.vividus.selenium.tunnel.TunnelException;

@ExtendWith(MockitoExtension.class)
class SauceLabsCapabilitiesConfigurerTests
{
    private static final String SAUCE_OPTIONS = "sauce:options";

    private static final String NAME_CAPABILITY = "name";
    private static final String STORY_NAME = "my";

    private static final String TUNNEL_IDENTIFIER_CAPABILITY = "tunnelIdentifier";
    private static final String TUNNEL_ID = "tunnelId";

    private static final String STORY_PATH = STORY_NAME + ".story";

    @Mock private IBddRunContext bddRunContext;
    @Mock private SauceConnectManager sauceConnectManager;
    @InjectMocks private SauceLabsCapabilitiesConfigurer configurer;

    @Test
    void shouldStopSauceConnectOnWebDriverQuit() throws TunnelException
    {
        configurer.stopTunnel(null);
        verify(sauceConnectManager).stop();
    }

    @Test
    void shouldDoNothingWhenSauceConnectIsDisabledAndNoProxy()
    {
        mockRunningStory();
        configurer.setTunnellingEnabled(false);
        DesiredCapabilities desiredCapabilities = mockDesiredCapabilities(null, null);
        configurer.configure(desiredCapabilities);
        verify(desiredCapabilities).setCapability(SAUCE_OPTIONS, Map.of(NAME_CAPABILITY, STORY_NAME));
        verifyNoInteractions(sauceConnectManager);
    }

    @Test
    void shouldStartSauceConnectWhenSauceConnectIsEnabled()
    {
        mockRunningStory();
        configurer.setTunnellingEnabled(true);
        Map<String, Object> sauceOptions = new HashMap<>();
        DesiredCapabilities desiredCapabilities = mockDesiredCapabilities(null, sauceOptions);
        SauceConnectOptions sauceConnectOptions = new SauceConnectOptions();
        when(sauceConnectManager.start(sauceConnectOptions)).thenReturn(TUNNEL_ID);

        configurer.configure(desiredCapabilities);

        assertEquals(Map.of(NAME_CAPABILITY, STORY_NAME, TUNNEL_IDENTIFIER_CAPABILITY, TUNNEL_ID), sauceOptions);
        verifyNoMoreInteractions(sauceConnectManager);
    }

    @Test
    void shouldStartSauceConnectWhenSauceConnectIsDisabledButProxyIsStarted()
    {
        configurer.setTunnellingEnabled(false);
        String restUrl = "http://eu-central-1.saucelabs.com/rest/v1";
        configurer.setRestUrl(restUrl);
        String sauceConnectArguments = "--verbose";
        configurer.setSauceConnectArguments(sauceConnectArguments);
        Proxy proxy = mock(Proxy.class);
        String httpProxy = "http-proxy:8080";
        when(proxy.getHttpProxy()).thenReturn(httpProxy);
        Map<String, Object> sauceOptions = new HashMap<>();
        DesiredCapabilities desiredCapabilities = mockDesiredCapabilities(proxy, sauceOptions);
        SauceConnectOptions sauceConnectOptions = new SauceConnectOptions();
        sauceConnectOptions.setProxy(httpProxy);
        sauceConnectOptions.setRestUrl(restUrl);
        sauceConnectOptions.setCustomArguments(sauceConnectArguments);
        when(sauceConnectManager.start(sauceConnectOptions)).thenReturn(TUNNEL_ID);

        configurer.configure(desiredCapabilities);

        assertEquals(Map.of(TUNNEL_IDENTIFIER_CAPABILITY, TUNNEL_ID), sauceOptions);
        verifyNoMoreInteractions(sauceConnectManager);
    }

    private void mockRunningStory()
    {
        Story story = new Story(STORY_PATH, List.of());
        RunningStory runningStory = new RunningStory();
        runningStory.setStory(story);
        when(bddRunContext.getRootRunningStory()).thenReturn(runningStory);
    }

    private DesiredCapabilities mockDesiredCapabilities(Proxy proxy, Map<String, Object> sauceOptions)
    {
        DesiredCapabilities desiredCapabilities = mock(DesiredCapabilities.class);
        when(desiredCapabilities.getCapability(CapabilityType.PROXY)).thenReturn(proxy);
        when(desiredCapabilities.getCapability(SAUCE_OPTIONS)).thenReturn(sauceOptions);
        return desiredCapabilities;
    }
}
