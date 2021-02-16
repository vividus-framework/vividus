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

package org.vividus.xray.model;

import java.util.List;

public class TestExecutionItem
{
    private String testKey;
    private TestExecutionItemStatus status;
    private List<TestExecutionItemStatus> examples;
    private String start;
    private String finish;

    public String getTestKey()
    {
        return testKey;
    }

    public void setTestKey(String testKey)
    {
        this.testKey = testKey;
    }

    public TestExecutionItemStatus getStatus()
    {
        return status;
    }

    public void setStatus(TestExecutionItemStatus status)
    {
        this.status = status;
    }

    public List<TestExecutionItemStatus> getExamples()
    {
        return examples;
    }

    public void setExamples(List<TestExecutionItemStatus> examples)
    {
        this.examples = examples;
    }

    public String getStart()
    {
        return start;
    }

    public void setStart(String start)
    {
        this.start = start;
    }

    public String getFinish()
    {
        return finish;
    }

    public void setFinish(String finish)
    {
        this.finish = finish;
    }
}
