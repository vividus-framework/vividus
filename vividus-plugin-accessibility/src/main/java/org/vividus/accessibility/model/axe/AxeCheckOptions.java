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

package org.vividus.accessibility.model.axe;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.vividus.accessibility.model.AbstractAccessibilityCheckOptions;

@JsonInclude(Include.NON_NULL)
public class AxeCheckOptions extends AbstractAccessibilityCheckOptions
{
    private AxeOptions runOnly;
    private Map<String, EnableableProperty> rules;

    public String getReporter()
    {
        return "v2";
    }

    public AxeOptions getRunOnly()
    {
        return runOnly;
    }

    public void setRunOnly(AxeOptions runOnly)
    {
        this.runOnly = runOnly;
    }

    public Map<String, EnableableProperty> getRules()
    {
        return rules;
    }

    public void setRules(Map<String, EnableableProperty> rules)
    {
        this.rules = rules;
    }
}
