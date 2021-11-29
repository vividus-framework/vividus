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

package org.vividus.selenium.lambdatest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.context.RunContext;
import org.vividus.model.RunningStory;

@ExtendWith(MockitoExtension.class)
class LambdaTestCapabilitiesConfigurerTests
{
    @Mock private RunningStory runningStory;
    @Mock private RunContext runContext;
    @InjectMocks private LambdaTestCapabilitiesConfigurer configurer;

    @Test
    void shouldConfigureCapabilities()
    {
        String storyName = "story-name";

        when(runContext.getRootRunningStory()).thenReturn(runningStory);
        when(runningStory.getName()).thenReturn(storyName);

        DesiredCapabilities capabilities = new DesiredCapabilities();
        configurer.configure(capabilities);

        assertEquals(Map.of("name", storyName), capabilities.asMap());
    }
}
