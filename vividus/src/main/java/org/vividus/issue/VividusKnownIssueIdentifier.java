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

import java.util.Map;

import org.vividus.softassert.issue.KnownIssueIdentifier;

/**
 * Vividus known issue identifier adapter
 */
public class VividusKnownIssueIdentifier extends KnownIssueIdentifier
{
    public void setStoryPattern(String pattern)
    {
        setTestSuitePattern(pattern);
    }

    public void setScenarioPattern(String pattern)
    {
        setTestCasePattern(pattern);
    }

    public void setStepPattern(String pattern)
    {
        setTestStepPattern(pattern);
    }

    public void setVariablePatterns(Map<String, String> patterns)
    {
        setRuntimeDataPatterns(patterns);
    }

    public void setFailStoryFast(boolean failStoryFast)
    {
        setFailTestSuiteFast(failStoryFast);
    }

    public void setFailScenarioFast(boolean failScenarioFast)
    {
        setFailTestCaseFast(failScenarioFast);
    }
}
