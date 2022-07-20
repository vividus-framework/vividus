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

package org.vividus.azure.devops.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestPoint
{
    private int id;
    private ShallowReference testCase;
    private ShallowReference testPlan;
    private ShallowReference suite;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public ShallowReference getTestCase()
    {
        return testCase;
    }

    public void setTestCase(ShallowReference testCase)
    {
        this.testCase = testCase;
    }

    public ShallowReference getTestPlan()
    {
        return testPlan;
    }

    public void setTestPlan(ShallowReference testPlan)
    {
        this.testPlan = testPlan;
    }

    public ShallowReference getSuite()
    {
        return suite;
    }

    public void setSuite(ShallowReference suite)
    {
        this.suite = suite;
    }
}
