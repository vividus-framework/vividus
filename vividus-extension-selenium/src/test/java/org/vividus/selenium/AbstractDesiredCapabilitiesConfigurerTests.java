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

package org.vividus.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

class AbstractDesiredCapabilitiesConfigurerTests
{
    private static final String OUTER_KEY = "outer-key";
    private static final String INNER_KEY = "inner-key";
    private static final String VALUE = "value";

    private TestAbstractDesiredCapabilitiesConfigurer configurer = new TestAbstractDesiredCapabilitiesConfigurer();

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

    private static final class TestAbstractDesiredCapabilitiesConfigurer extends AbstractDesiredCapabilitiesConfigurer
    {
        @Override
        public void configure(DesiredCapabilities desiredCapabilities)
        {
            throw new UnsupportedOperationException();
        }
    }
}
