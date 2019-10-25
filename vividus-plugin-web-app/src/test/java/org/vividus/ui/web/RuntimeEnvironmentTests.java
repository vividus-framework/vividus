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

package org.vividus.ui.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.action.IJavascriptActions;

@ExtendWith(MockitoExtension.class)
class RuntimeEnvironmentTests
{
    @Mock
    private IJavascriptActions javascriptActions;

    @InjectMocks
    private RuntimeEnvironment runtimeConfiguration;

    @Test
    void testGetUserAgent()
    {
        String userAgent = "userAgent";
        when(javascriptActions.getUserAgent()).thenReturn(userAgent);
        assertEquals(userAgent, runtimeConfiguration.getUserAgent());
    }

    @Test
    void testGetUserAgentMultipleTimes()
    {
        runtimeConfiguration.getUserAgent();
        runtimeConfiguration.getUserAgent();
        verify(javascriptActions, times(1)).getUserAgent();
    }

    @Test
    void testGetDevicePixelRatio()
    {
        double devicePixelRatio = 2d;
        when(javascriptActions.getDevicePixelRatio()).thenReturn(devicePixelRatio);
        assertEquals(devicePixelRatio, runtimeConfiguration.getDevicePixelRatio());
    }

    @Test
    void testGetDevicePixelRatioMultipleTimes()
    {
        runtimeConfiguration.getDevicePixelRatio();
        runtimeConfiguration.getDevicePixelRatio();
        verify(javascriptActions, times(1)).getDevicePixelRatio();
    }
}
