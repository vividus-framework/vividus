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

import java.util.List;

public class TestCase
{
    private List<String> keys;
    private TestCaseStatus status;

    public TestCase(List<String> keys, String status)
    {
        this.keys = keys;
        this.status = TestCaseStatus.valueOf(status.toUpperCase());
    }

    public TestCase(String key, TestCaseStatus status)
    {
        this.keys = List.of(key);
        this.status = status;
    }

    public List<String> getKeys()
    {
        return keys;
    }

    public String getKey()
    {
        return keys.get(0);
    }

    public TestCaseStatus getStatus()
    {
        return status;
    }

    @Override
    public String toString()
    {
        return "{keys=" + keys + ", status=" + status + "}";
    }
}
