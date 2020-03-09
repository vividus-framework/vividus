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

package org.vividus.visual.eyes.model;

import org.vividus.visual.model.VisualCheckResult;

public class ApplitoolsVisualCheckResult extends VisualCheckResult
{
    private String stepUrl;
    private String batchUrl;

    public ApplitoolsVisualCheckResult(ApplitoolsVisualCheck visualCheck)
    {
        super(visualCheck);
    }

    public String getStepUrl()
    {
        return stepUrl;
    }

    public void setStepUrl(String stepUrl)
    {
        this.stepUrl = stepUrl;
    }

    public String getBatchUrl()
    {
        return batchUrl;
    }

    public void setBatchUrl(String batchUrl)
    {
        this.batchUrl = batchUrl;
    }
}
