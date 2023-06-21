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

package org.vividus.selenium;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.context.RunContext;
import org.vividus.model.RunningStory;

public abstract class AbstractDesiredCapabilitiesConfigurer implements DesiredCapabilitiesConfigurer
{
    private final RunContext runContext;

    protected AbstractDesiredCapabilitiesConfigurer(RunContext runContext)
    {
        this.runContext = runContext;
    }

    @SuppressWarnings("unchecked")
    protected void putNestedCapability(DesiredCapabilities capabilities, String outerKey, String innerKey, Object value)
    {
        Map<String, Object> nestedOptions = (Map<String, Object>) capabilities.getCapability(outerKey);
        if (nestedOptions == null)
        {
            nestedOptions = new HashMap<>();
            capabilities.setCapability(outerKey, nestedOptions);
        }
        nestedOptions.put(innerKey, value);
    }

    protected void configureTestName(DesiredCapabilities desiredCapabilities, String parentKey, String testNameKey)
    {
        consumeTestName(name -> putNestedCapability(desiredCapabilities, parentKey, testNameKey, name));
    }

    protected void configureTestName(DesiredCapabilities desiredCapabilities, String testNameKey)
    {
        consumeTestName(name -> desiredCapabilities.setCapability(testNameKey, name));
    }

    protected void consumeTestName(Consumer<String> testNameConsumer)
    {
        Optional.ofNullable(runContext.getRootRunningStory())
                .map(RunningStory::getName)
                .ifPresent(testNameConsumer);
    }
}
