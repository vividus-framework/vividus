/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.lighthouse.model;

import java.math.BigDecimal;

import org.jbehave.core.annotations.AsParameters;
import org.vividus.steps.ComparisonRule;

@AsParameters
public class MetricRule
{
    private String metric;
    private ComparisonRule rule;
    private BigDecimal threshold;

    public String getMetric()
    {
        return metric;
    }

    public void setMetric(String metric)
    {
        this.metric = metric;
    }

    public ComparisonRule getRule()
    {
        return rule;
    }

    public void setRule(ComparisonRule rule)
    {
        this.rule = rule;
    }

    public BigDecimal getThreshold()
    {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold)
    {
        this.threshold = threshold;
    }
}
