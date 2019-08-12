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

package org.vividus.bdd.model;

import java.util.Optional;
import java.util.UUID;

import org.jbehave.core.model.Scenario;

public class RunningScenario
{
    private final String uuid = UUID.randomUUID().toString();
    private Scenario scenario;
    private boolean failed;
    private String title;
    private int index = -1;

    public String getUuid()
    {
        return uuid;
    }

    public Scenario getScenario()
    {
        return scenario;
    }

    public void setScenario(Scenario scenario)
    {
        this.scenario = scenario;
    }

    public boolean isFailed()
    {
        return failed;
    }

    public void setFailed(boolean failed)
    {
        this.failed = failed;
    }

    public String getTitle()
    {
        return Optional.ofNullable(title).orElseGet(() -> scenario.getTitle());
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }
}
