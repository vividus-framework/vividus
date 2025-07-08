/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.report.allure.model;

import java.util.List;
import java.util.stream.Stream;

import io.qameta.allure.entity.Status;

public class AllureCategory
{
    private String name;
    private List<Status> matchedStatuses;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Status> getMatchedStatuses()
    {
        return matchedStatuses;
    }

    public void setMatchedStatuses(List<Status> matchedStatuses)
    {
        this.matchedStatuses = matchedStatuses;
    }

    public void setStatuses(String statuses)
    {
        this.matchedStatuses = Stream.of(statuses.split(",")).map(s -> Status.valueOf(s.strip().toUpperCase()))
                .toList();
    }
}
