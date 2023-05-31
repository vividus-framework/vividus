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

package org.vividus.replacement;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.jbehave.core.model.StepPattern;
import org.jbehave.core.steps.NullStepMonitor;

public class CollectingStepPatternsMonitor extends NullStepMonitor implements StepPatternsRegistry
{
    private final Map<String, Pattern> stepPatternsCache = new ConcurrentHashMap<>();

    @Override
    public void stepMatchesPattern(String step, boolean matches, StepPattern pattern, Method method,
            Object stepsInstance)
    {
        if (matches)
        {
            String key = pattern.type() + " " + pattern.annotated();
            stepPatternsCache.putIfAbsent(key, Pattern.compile(pattern.resolved(), Pattern.DOTALL));
        }
    }

    @Override
    public Map<String, Pattern> getRegisteredStepPatterns()
    {
        return stepPatternsCache;
    }
}
