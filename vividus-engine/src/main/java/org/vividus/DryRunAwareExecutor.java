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

package org.vividus;

import java.util.function.Supplier;

import org.jbehave.core.embedder.StoryControls;

public interface DryRunAwareExecutor
{
    StoryControls getStoryControls();

    /**
     * Calculates the value if the run is not dry run, in case of dry run returns default value
     * @param <T> The type of value to return
     * @param valueProvider The value to calculate if the execution is not dry run
     * @param defaultValue The shortcut value if the execution is dry run
     * @return calculated value
     */
    default <T> T execute(Supplier<T> valueProvider, T defaultValue)
    {
        if (getStoryControls().dryRun())
        {
            return defaultValue;
        }
        return valueProvider.get();
    }
}
