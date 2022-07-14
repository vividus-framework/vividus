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

package org.vividus.visual.model;

import java.util.Optional;
import java.util.OptionalDouble;

import pazone.ashot.Screenshot;

public class VisualCheck extends AbstractVisualCheck
{
    private OptionalDouble acceptableDiffPercentage = OptionalDouble.empty();
    private OptionalDouble requiredDiffPercentage = OptionalDouble.empty();
    private Optional<String> baselineStorage = Optional.empty();
    private Optional<Screenshot> screenshot = Optional.empty();

    public VisualCheck(String baselineName, VisualActionType action)
    {
        super(baselineName, action);
    }

    public OptionalDouble getAcceptableDiffPercentage()
    {
        return acceptableDiffPercentage;
    }

    public void setAcceptableDiffPercentage(OptionalDouble acceptableDiffPercentage)
    {
        this.acceptableDiffPercentage = acceptableDiffPercentage;
    }

    public OptionalDouble getRequiredDiffPercentage()
    {
        return requiredDiffPercentage;
    }

    public void setRequiredDiffPercentage(OptionalDouble requiredDiffPercentage)
    {
        this.requiredDiffPercentage = requiredDiffPercentage;
    }

    public Optional<String> getBaselineStorage()
    {
        return baselineStorage;
    }

    public void setBaselineStorage(Optional<String> baselineStorage)
    {
        this.baselineStorage = baselineStorage;
    }

    public Optional<Screenshot> getScreenshot()
    {
        return screenshot;
    }

    public void setScreenshot(Optional<Screenshot> screenshot)
    {
        this.screenshot = screenshot;
    }
}
