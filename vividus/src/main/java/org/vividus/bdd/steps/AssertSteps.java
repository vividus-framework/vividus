/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.steps;

import java.util.Objects;

import javax.inject.Inject;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.ScenarioType;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningStory;
import org.vividus.softassert.ISoftAssert;

public class AssertSteps
{
    @Inject private ISoftAssert softAssert;
    @Inject private IBddRunContext bddRunContext;

    @AfterScenario(uponType = ScenarioType.ANY)
    public void verifyIfAssertionsPassed()
    {
        boolean scenarioLevelGivenStoriesExistInChain = bddRunContext.getStoriesChain().stream()
                .skip(1)
                .map(RunningStory::getRunningScenario)
                .anyMatch(Objects::nonNull);
        if (bddRunContext.getRunningStory() == bddRunContext.getRootRunningStory()
                || !scenarioLevelGivenStoriesExistInChain)
        {
            softAssert.verify();
        }
    }
}
