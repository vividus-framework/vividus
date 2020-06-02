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

package org.vividus.zephyr.model;

public class TestCase
{
    private String key;
    private TestCaseStatus status;

    public TestCase(String key, String status)
    {
        this.key = key;
        this.status = TestCaseStatus.valueOf(status.toUpperCase());
    }

    public TestCase(String key, TestCaseStatus status)
    {
        this.key = key;
        this.status = status;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public TestCaseStatus getStatus()
    {
        return status;
    }

    public void setStatus(TestCaseStatus status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return "{key=" + key + ", status=" + status + "}";
    }
}
