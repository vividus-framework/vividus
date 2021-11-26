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

package org.vividus.issue;

import java.util.Optional;

import org.jbehave.core.model.Scenario;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.softassert.issue.ITestInfoProvider;
import org.vividus.softassert.issue.TestInfo;

public class TestInfoProvider implements ITestInfoProvider
{
    private final RunContext runContext;

    public TestInfoProvider(RunContext runContext)
    {
        this.runContext = runContext;
    }

    @Override
    public TestInfo getTestInfo()
    {
        RunningStory runningStory = runContext.getRunningStory();
        TestInfo testInfo = new TestInfo();
        if (runningStory != null)
        {
            testInfo.setTestSuite(runningStory.getName());
            Optional.ofNullable(runningStory.getRunningScenario())
                .map(RunningScenario::getScenario)
                .map(Scenario::getTitle)
                .ifPresent(testInfo::setTestCase);
            testInfo.setTestSteps(runningStory.getRunningSteps());
        }
        return testInfo;
    }
}
