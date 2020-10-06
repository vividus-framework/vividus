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

package org.vividus.bdd.model.jbehave;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Scenario
{
    private String title;
    private List<Meta> meta;
    private List<Step> steps;
    private Examples examples;

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public List<Meta> getMeta()
    {
        return meta;
    }

    public void setMeta(List<Meta> meta)
    {
        this.meta = meta;
    }

    public void setSteps(List<Step> steps)
    {
        this.steps = steps;
    }

    public Examples getExamples()
    {
        return examples;
    }

    public void setExamples(Examples examples)
    {
        this.examples = examples;
    }

    public List<Step> collectSteps()
    {
        if (steps != null)
        {
            return steps;
        }
        return Optional.ofNullable(examples)
                       .map(Examples::getExamples)
                       .filter(Objects::nonNull)
                       .map(e -> e.get(0))
                       .map(Example::getSteps)
                       .filter(Objects::nonNull)
                       .orElse(List.of());
    }
}
