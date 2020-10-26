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

package org.vividus.selenium.browserstack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningStory;

@ExtendWith(MockitoExtension.class)
class BrowserStackCapabilitiesConfigurerTests
{
    @Mock private IBddRunContext bddRunContext;
    @Mock private DesiredCapabilities desiredCapabilities;
    @InjectMocks private BrowserStackCapabilitiesConfigurer configurer;

    @Test
    void shouldNotConfigureCapabilitiesIfBrowserStackInDisabled()
    {
        configurer.setBrowserStackEnabled(false);
        configurer.configure(desiredCapabilities);
        verifyNoInteractions(bddRunContext, desiredCapabilities);
    }

    @Test
    void shouldAddRunningStoryNameAsSessionName()
    {
        configurer.setBrowserStackEnabled(true);
        String name = "name";
        String bstackKey = "bstack:options";
        Map<String, Object> bstackMap = new HashMap<>();
        RunningStory runningStory = mock(RunningStory.class);

        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        when(runningStory.getName()).thenReturn(name);
        when(desiredCapabilities.getCapability(bstackKey)).thenReturn(bstackMap);

        configurer.configure(desiredCapabilities);

        assertEquals(Map.of("sessionName", name), bstackMap);
        verifyNoMoreInteractions(bddRunContext, desiredCapabilities, runningStory);
    }

    @Test
    void shouldNotAddRunningStoryNameItItsNull()
    {
        configurer.setBrowserStackEnabled(true);
        RunningStory runningStory = mock(RunningStory.class);

        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        when(runningStory.getName()).thenReturn(null);

        configurer.configure(desiredCapabilities);

        verifyNoMoreInteractions(bddRunContext, runningStory);
        verifyNoInteractions(desiredCapabilities);
    }
}
