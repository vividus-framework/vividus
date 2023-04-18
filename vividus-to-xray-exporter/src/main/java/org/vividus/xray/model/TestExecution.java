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

package org.vividus.xray.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class TestExecution
{
    private String testExecutionKey;
    private TestExecutionInfo info;
    private List<TestExecutionItem> tests;

    public String getTestExecutionKey()
    {
        return testExecutionKey;
    }

    public void setTestExecutionKey(String testExecutionKey)
    {
        this.testExecutionKey = testExecutionKey;
    }

    public TestExecutionInfo getInfo()
    {
        return info;
    }

    public void setInfo(TestExecutionInfo info)
    {
        this.info = info;
    }

    public List<TestExecutionItem> getTests()
    {
        return tests;
    }

    public void setTests(List<TestExecutionItem> tests)
    {
        this.tests = tests;
    }
}
