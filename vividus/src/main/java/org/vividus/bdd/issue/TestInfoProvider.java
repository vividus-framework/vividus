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

package org.vividus.bdd.issue;

import java.util.Optional;

import org.jbehave.core.model.Scenario;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.softassert.issue.ITestInfoProvider;
import org.vividus.softassert.issue.TestInfo;

public class TestInfoProvider implements ITestInfoProvider
{
    private IBddRunContext bddRunContext;

    @Override
    public TestInfo getTestInfo()
    {
        RunningStory runningStory = bddRunContext.getRunningStory();
        TestInfo testInfo = new TestInfo();
        if (runningStory != null)
        {
            testInfo.setTestSuite(runningStory.getName());
            Optional.ofNullable(runningStory.getRunningScenario())
                .map(RunningScenario::getScenario)
                .map(Scenario::getTitle)
                .ifPresent(testInfo::setTestCase);
            testInfo.setTestStep(runningStory.getRunningStep());
        }
        return testInfo;
    }

    public void setBddRunContext(IBddRunContext bddRunContext)
    {
        this.bddRunContext = bddRunContext;
    }
}
