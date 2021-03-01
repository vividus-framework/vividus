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

package org.vividus.bdd.steps;

import java.util.Objects;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.Then;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningStory;
import org.vividus.softassert.ISoftAssert;

public class AssertSteps
{
    @Inject private ISoftAssert softAssert;
    @Inject private IBddRunContext bddRunContext;

    @AfterScenario
    public void verifyIfAssertionsPassed()
    {
        boolean scenarioLevelGivenStoriesExistInChain = bddRunContext.getStoriesChain().stream()
                .skip(1)
                .map(RunningStory::getRunningScenario)
                .anyMatch(Objects::nonNull);
        if (bddRunContext.getRunningStory().equals(bddRunContext.getRootRunningStory())
                || !scenarioLevelGivenStoriesExistInChain)
        {
            softAssert.verify();
        }
    }

    /**
     * Steps walks the failed assertions and if any of assertion messages matches regex
     * all the failed assertions verified;
     * Step could be useful when you have to fail-fast scenario in case of some assertion;
     *
     * @param assertionPatter to match assertions
     */
    @Then("I verify assertions matching '$assertionPattern'")
    public void verifyIfAssertionMatcherPatter(Pattern assertionPatter)
    {
        softAssert.verify(assertionPatter);
    }
}
