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

public class Examples
{
    private List<String> steps;
    private Parameters parameters;
    private List<Example> examples;

    public List<String> getSteps()
    {
        return steps;
    }

    public void setSteps(List<String> steps)
    {
        this.steps = steps;
    }

    public Parameters getParameters()
    {
        return parameters;
    }

    public void setParameters(Parameters parameters)
    {
        this.parameters = parameters;
    }

    public List<Example> getExamples()
    {
        return examples;
    }

    public void setExamples(List<Example> examples)
    {
        this.examples = examples;
    }
}
