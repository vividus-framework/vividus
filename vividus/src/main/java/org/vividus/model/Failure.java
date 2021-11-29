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

package org.vividus.model;

import java.util.Optional;

public final class Failure
{
    private final String story;
    private final String scenario;
    private final String step;
    private final String message;

    private Failure(String story, String scenario, String step, String message)
    {
        this.story = story;
        this.scenario = scenario;
        this.step = step;
        this.message = message;
    }

    public static Failure from(RunningStory runningStory, String errorMessage)
    {
        String scenarioTitle = Optional.ofNullable(runningStory.getRunningScenario())
                                       .map(RunningScenario::getTitle)
                                       .orElse("");
        return new Failure(runningStory.getName(), scenarioTitle,
                runningStory.getRunningSteps().getFirst(), errorMessage);
    }

    public String getStory()
    {
        return story;
    }

    public String getScenario()
    {
        return scenario;
    }

    public String getStep()
    {
        return step;
    }

    public String getMessage()
    {
        return message;
    }
}
