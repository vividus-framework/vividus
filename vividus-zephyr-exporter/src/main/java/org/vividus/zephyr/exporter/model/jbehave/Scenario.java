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
import java.util.Optional;

public class Scenario
{
    private String title;
    private List<Meta> meta;
    private List<Step> steps;
    private Examples examples;

    public Optional<String> findTestCaseId()
    {
        return Optional.ofNullable(getMeta()).flatMap(metas -> metas.stream().filter(
            meta -> "testCaseId".equals(meta.getName())).findFirst().map(Meta::getValue));
    }

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

    public List<Step> getSteps()
    {
        return steps;
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
}
