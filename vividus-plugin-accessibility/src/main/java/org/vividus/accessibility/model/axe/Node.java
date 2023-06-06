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

import java.util.List;

public class Node
{
    private String impact;
    private String html;
    private List<String> target;
    private List<CheckResult> any;
    private List<CheckResult> all;
    private List<CheckResult> none;

    public String getImpact()
    {
        return impact;
    }

    public void setImpact(String impact)
    {
        this.impact = impact;
    }

    public String getHtml()
    {
        return html;
    }

    public void setHtml(String html)
    {
        this.html = html;
    }

    public List<String> getTarget()
    {
        return target;
    }

    public void setTarget(List<String> target)
    {
        this.target = target;
    }

    public List<CheckResult> getAny()
    {
        return any;
    }

    public void setAny(List<CheckResult> any)
    {
        this.any = any;
    }

    public List<CheckResult> getAll()
    {
        return all;
    }

    public void setAll(List<CheckResult> all)
    {
        this.all = all;
    }

    public List<CheckResult> getNone()
    {
        return none;
    }

    public void setNone(List<CheckResult> none)
    {
        this.none = none;
    }
}
