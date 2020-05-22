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

package org.vividus.analytics.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CustomFieldsTests
{
    private static final String REMOTE = "true";
    private static final String DURATION = "3";
    private static final String STORIES = "2";
    private static final String STEPS = "1";
    private static final String PROFILE = "web/desktop/chrome";
    private static final String JAVA = "13.0.1+9";
    private static final String PLUGIN = "0.2.0";
    private static final String VIVIDUS = "0.1.0";
    private static final String SCENARIOS = "5";

    @Test
    void shouldAddValueWithCorrectKey()
    {
        Map<String, String> payload = new HashMap<>();
        CustomDefinitions.VIVIDUS.add(payload, VIVIDUS);
        CustomDefinitions.PLUGIN_VERSION.add(payload, PLUGIN);
        CustomDefinitions.JAVA.add(payload, JAVA);
        CustomDefinitions.PROFILES.add(payload, PROFILE);
        CustomDefinitions.STEPS.add(payload, STEPS);
        CustomDefinitions.STORIES.add(payload, STORIES);
        CustomDefinitions.DURATION.add(payload, DURATION);
        CustomDefinitions.REMOTE.add(payload, REMOTE);
        CustomDefinitions.SCENARIOS.add(payload, SCENARIOS);
        Map<String, String> expected = Map.of("cd1", PROFILE,
                                              "cd2", JAVA,
                                              "cd3", VIVIDUS,
                                              "cd4", REMOTE,
                                              "cd5", PLUGIN,
                                              "cm1", STORIES,
                                              "cm2", STEPS,
                                              "cm3", DURATION,
                                              "cm4", SCENARIOS);
        assertEquals(expected, payload);
    }
}
