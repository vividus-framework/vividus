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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.action.IJavascriptActions;

@ExtendWith(MockitoExtension.class)
class RuntimeEnvironmentTests
{
    private static final String USER_AGENT = "userAgent";

    @Mock
    private IJavascriptActions jsActions;

    @InjectMocks
    private RuntimeEnvironment runtimeConfiguration;

    @Test
    void testGetUserAgent()
    {
        Mockito.when(jsActions.getUserAgent()).thenReturn(USER_AGENT);
        assertEquals(USER_AGENT, runtimeConfiguration.getUserAgent());
    }

    @Test
    void testGetUserAgentMultipleCalls()
    {
        runtimeConfiguration.getUserAgent();
        runtimeConfiguration.getUserAgent();
        Mockito.verify(jsActions).getUserAgent();
    }
}
