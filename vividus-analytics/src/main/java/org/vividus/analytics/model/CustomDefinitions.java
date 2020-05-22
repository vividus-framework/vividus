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

import java.util.Map;

public enum CustomDefinitions
{
    PROFILES("cd1"),
    JAVA("cd2"),
    VIVIDUS("cd3"),
    REMOTE("cd4"),
    PLUGIN_VERSION("cd5"),
    STORIES("cm1"),
    STEPS("cm2"),
    DURATION("cm3"),
    SCENARIOS("cm4");

    private final String analyticsKey;

    CustomDefinitions(String analyticsKey)
    {
        this.analyticsKey = analyticsKey;
    }

    public void add(Map<String, String> toModify, String value)
    {
        toModify.put(this.analyticsKey, value);
    }
}
