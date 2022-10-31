/*
 * Copyright 2019-2022 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

class DesiredCapabilitiesAdjusterTests
{
    private static final String K = "k";
    private static final String V = "v";
    private static final String K_1 = "k1";
    private static final String V_1 = "v1";

    @Test
    void shouldAdjustDesiredCapabilities()
    {
        var adjuster = new TestAdjuster();
        var capabilities = new DesiredCapabilities(Map.of(K_1, V_1));
        adjuster.adjust(capabilities);
        assertEquals(Map.of(K, V, K_1, V_1), capabilities.asMap());
    }

    @Test
    void shouldFailInCaseOfConflictingDesiredCapabilities()
    {
        var adjuster = new TestAdjuster();
        var desiredCapabilities = new DesiredCapabilities(Map.of(K, V));
        var iae = assertThrows(IllegalArgumentException.class,
                () -> adjuster.adjust(desiredCapabilities));
        assertEquals("Capabilities adjuster tried to add conflicting capabilities: [k=v]", iae.getMessage());
    }

    private static final class TestAdjuster extends DesiredCapabilitiesAdjuster
    {
        @Override
        protected Map<String, Object> getExtraCapabilities(DesiredCapabilities desiredCapabilities)
        {
            return Map.of(K, V);
        }
    }
}
