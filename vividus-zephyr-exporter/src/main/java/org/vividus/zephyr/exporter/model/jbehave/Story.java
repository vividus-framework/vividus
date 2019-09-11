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

package org.vividus.zephyr.exporter.model.jbehave;

import java.util.List;

public class Story
{
    private List<Scenario> scenarios;

    public List<Scenario> getScenarios()
    {
        return scenarios;
    }

    public void setScenarios(List<Scenario> scenarios)
    {
        this.scenarios = scenarios;
    }
}
