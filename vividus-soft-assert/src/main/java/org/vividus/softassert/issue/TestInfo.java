/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.softassert.issue;

import java.util.Deque;

public class TestInfo
{
    private String testSuite;
    private String testCase;
    private Deque<String> testSteps;

    public String getTestSuite()
    {
        return testSuite;
    }

    public void setTestSuite(String testSuite)
    {
        this.testSuite = testSuite;
    }

    public String getTestCase()
    {
        return testCase;
    }

    public void setTestCase(String testCase)
    {
        this.testCase = testCase;
    }

    public Deque<String> getTestSteps()
    {
        return testSteps;
    }

    public void setTestSteps(Deque<String> testSteps)
    {
        this.testSteps = testSteps;
    }
}
