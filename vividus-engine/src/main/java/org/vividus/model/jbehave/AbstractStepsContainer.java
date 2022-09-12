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

package org.vividus.model.jbehave;

import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractStepsContainer
{
    private List<Step> beforeSystemScenarioSteps;
    private List<Step> beforeUserScenarioSteps;
    private List<Step> steps;
    private List<Step> afterUserScenarioSteps;
    private List<Step> afterSystemScenarioSteps;

    public List<Step> getBeforeSystemScenarioSteps()
    {
        return beforeSystemScenarioSteps;
    }

    public void setBeforeSystemScenarioSteps(List<Step> beforeSystemScenarioSteps)
    {
        this.beforeSystemScenarioSteps = beforeSystemScenarioSteps;
    }

    public List<Step> getBeforeUserScenarioSteps()
    {
        return beforeUserScenarioSteps;
    }

    public void setBeforeUserScenarioSteps(List<Step> beforeScenarioSteps)
    {
        this.beforeUserScenarioSteps = beforeScenarioSteps;
    }

    public List<Step> getSteps()
    {
        return steps;
    }

    public void setSteps(List<Step> steps)
    {
        this.steps = steps;
    }

    public List<Step> getAfterUserScenarioSteps()
    {
        return afterUserScenarioSteps;
    }

    public void setAfterUserScenarioSteps(List<Step> afterScenarioSteps)
    {
        this.afterUserScenarioSteps = afterScenarioSteps;
    }

    public List<Step> getAfterSystemScenarioSteps()
    {
        return afterSystemScenarioSteps;
    }

    public void setAfterSystemScenarioSteps(List<Step> afterSystemScenarioSteps)
    {
        this.afterSystemScenarioSteps = afterSystemScenarioSteps;
    }

    public Stream<Step> createStreamOfAllSteps()
    {
        return Stream.of(getBeforeSystemScenarioSteps(), getBeforeUserScenarioSteps(), getSteps(),
                getAfterUserScenarioSteps(), getAfterSystemScenarioSteps()).flatMap(List::stream);
    }
}
