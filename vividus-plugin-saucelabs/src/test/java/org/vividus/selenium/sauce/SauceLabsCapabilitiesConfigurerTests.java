/*
 * Copyright 2019-2023 the original author or authors.
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
import java.util.Set;
import java.util.stream.Stream;

import com.saucelabs.saucerest.DataCenter;

import org.jbehave.core.model.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.context.RunContext;
import org.vividus.model.RunningStory;
import org.vividus.selenium.tunnel.TunnelException;

@ExtendWith(MockitoExtension.class)
class SauceLabsCapabilitiesConfigurerTests
{
    private static final String SAUCE_OPTIONS = "sauce:options";

    private static final String NAME_CAPABILITY = "name";
    private static final String STORY_NAME = "my";

    private static final String TUNNEL_NAME_CAPABILITY = "tunnelName";
    private static final String TUNNEL_NAME = "my-tunnel-name";

    private static final String STORY_PATH = STORY_NAME + ".story";

    private static final String REST_URL = "https://api.eu-central-1.saucelabs.com/rest/v1";
    private static final String CUSTOM_ARGS = "--verbose";
    private static final Set<String> SKIP_HOST_GLOB_PATTERNS = Set.of("example.com");

    @Mock private RunContext runContext;
    @Mock private SauceConnectManager sauceConnectManager;
    private SauceLabsCapabilitiesConfigurer configurer;

    @BeforeEach
    void beforeEach()
    {
        configurer = new SauceLabsCapabilitiesConfigurer(true, runContext, sauceConnectManager, DataCenter.EU_CENTRAL);
    }

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
        var desiredCapabilities = mockDesiredCapabilities(null, null);
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
        var desiredCapabilities = mockDesiredCapabilities(null, sauceOptions);
        var sauceConnectOptions = new SauceConnectOptions(true, REST_URL, null, Set.of());
        when(sauceConnectManager.start(sauceConnectOptions)).thenReturn(TUNNEL_NAME);

        configurer.configure(desiredCapabilities);

        assertEquals(Map.of(NAME_CAPABILITY, STORY_NAME, TUNNEL_NAME_CAPABILITY, TUNNEL_NAME), sauceOptions);
        verifyNoMoreInteractions(sauceConnectManager);
    }

    @Test
    void shouldStartSauceConnectWhenSauceConnectIsDisabledButProxyIsStarted()
    {
        configurer.setTunnellingEnabled(false);
        configurer.setSauceConnectArguments(CUSTOM_ARGS);
        configurer.setSkipHostGlobPatterns(SKIP_HOST_GLOB_PATTERNS);
        Proxy proxy = mock();
        var httpProxy = "http-proxy:8080";
        when(proxy.getHttpProxy()).thenReturn(httpProxy);

        var sauceConnectOptions = new SauceConnectOptions(true, REST_URL, CUSTOM_ARGS, SKIP_HOST_GLOB_PATTERNS);
        sauceConnectOptions.setProxy(httpProxy);

        Map<String, Object> sauceOptions = new HashMap<>();
        var desiredCapabilities = mockDesiredCapabilities(proxy, sauceOptions);

        when(sauceConnectManager.start(sauceConnectOptions)).thenReturn(TUNNEL_NAME);

        configurer.configure(desiredCapabilities);

        assertEquals(Map.of(TUNNEL_NAME_CAPABILITY, TUNNEL_NAME), sauceOptions);
        verifyNoMoreInteractions(sauceConnectManager);
    }

    static Stream<Arguments> globPattens()
    {
        return Stream.of(
            Arguments.arguments(SKIP_HOST_GLOB_PATTERNS, SKIP_HOST_GLOB_PATTERNS),
            Arguments.arguments(null, Set.of())
        );
    }

    @ParameterizedTest
    @MethodSource("globPattens")
    void shouldCreateOptions(Set<String> setValue, Set<String> expectedValue)
    {
        configurer.setSauceConnectArguments(CUSTOM_ARGS);
        configurer.setSkipHostGlobPatterns(setValue);
        assertEquals(new SauceConnectOptions(true, REST_URL, CUSTOM_ARGS, expectedValue), configurer.createOptions());
    }

    private void mockRunningStory()
    {
        var story = new Story(STORY_PATH, List.of());
        var runningStory = new RunningStory();
        runningStory.setStory(story);
        when(runContext.getRootRunningStory()).thenReturn(runningStory);
    }

    private DesiredCapabilities mockDesiredCapabilities(Proxy proxy, Map<String, Object> sauceOptions)
    {
        DesiredCapabilities desiredCapabilities = mock();
        when(desiredCapabilities.getCapability(CapabilityType.PROXY)).thenReturn(proxy);
        when(desiredCapabilities.getCapability(SAUCE_OPTIONS)).thenReturn(sauceOptions);
        return desiredCapabilities;
    }
}
