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

package org.vividus.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
class AbstractDesiredCapabilitiesConfigurerTests
{
    private static final String OUTER_KEY = "outer-key";
    private static final String INNER_KEY = "inner-key";
    private static final String VALUE = "value";

    @Mock private IBddRunContext bddRunContext;
    @InjectMocks private TestAbstractDesiredCapabilitiesConfigurer configurer;

    @Test
    void shouldPutNestedCapability()
    {
        DesiredCapabilities capabilities = mock(DesiredCapabilities.class);
        Map<String, Object> nestedMap = new HashMap<>();

        when(capabilities.getCapability(OUTER_KEY)).thenReturn(nestedMap);

        configurer.putNestedCapability(capabilities, OUTER_KEY, INNER_KEY, VALUE);

        assertEquals(Map.of(INNER_KEY, VALUE), nestedMap);
    }

    @Test
    void shouldCreateNestedMapAndPutNestedCapability()
    {
        DesiredCapabilities capabilities = mock(DesiredCapabilities.class);

        when(capabilities.getCapability(OUTER_KEY)).thenReturn(null);

        configurer.putNestedCapability(capabilities, OUTER_KEY, INNER_KEY, VALUE);

        verify(capabilities).setCapability(OUTER_KEY, Map.of(INNER_KEY, VALUE));
    }

    @Test
    void shouldConfigureTestName()
    {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        RunningStory runningStory = mockRunningStory();

        configurer.configureTestName(desiredCapabilities, OUTER_KEY, INNER_KEY);

        assertEquals(Map.of(OUTER_KEY, Map.of(INNER_KEY, VALUE)), desiredCapabilities.asMap());
        verifyNoMoreInteractions(bddRunContext, runningStory);
    }

    @Test
    void shouldConfigureTestNameByKey()
    {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        RunningStory runningStory = mockRunningStory();

        configurer.configureTestName(desiredCapabilities, INNER_KEY);

        assertEquals(Map.of(INNER_KEY, VALUE), desiredCapabilities.asMap());
        verifyNoMoreInteractions(bddRunContext, runningStory);
    }

    private RunningStory mockRunningStory()
    {
        RunningStory runningStory = mock(RunningStory.class);

        when(bddRunContext.getRootRunningStory()).thenReturn(runningStory);
        when(runningStory.getName()).thenReturn(VALUE);

        return runningStory;
    }

    @Test
    void shouldNotConfigureTestNameIfStoryIsNull()
    {
        RunningStory runningStory = mock(RunningStory.class);
        DesiredCapabilities desiredCapabilities = mock(DesiredCapabilities.class);

        when(bddRunContext.getRootRunningStory()).thenReturn(runningStory);
        when(runningStory.getName()).thenReturn(null);

        configurer.configureTestName(desiredCapabilities, OUTER_KEY, INNER_KEY);

        verifyNoMoreInteractions(bddRunContext, runningStory);
        verifyNoInteractions(desiredCapabilities);
    }

    private static final class TestAbstractDesiredCapabilitiesConfigurer extends AbstractDesiredCapabilitiesConfigurer
    {
        TestAbstractDesiredCapabilitiesConfigurer(IBddRunContext bddRunContext)
        {
            super(bddRunContext);
        }

        @Override
        public void configure(DesiredCapabilities desiredCapabilities)
        {
            throw new UnsupportedOperationException();
        }
    }
}
