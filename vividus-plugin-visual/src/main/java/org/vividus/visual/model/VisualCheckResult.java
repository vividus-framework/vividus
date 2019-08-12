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

package org.vividus.visual.model;

import org.vividus.visual.engine.VisualCheckFactory.VisualCheck;

public class VisualCheckResult
{
    private final String baselineName;
    private final VisualActionType actionType;
    private String baseline;
    private String checkpoint;
    private String diff;
    private boolean passed;

    public VisualCheckResult(VisualCheck visualCheck)
    {
        this.baselineName = visualCheck.getBaselineName();
        this.actionType = visualCheck.getAction();
    }

    public String getBaseline()
    {
        return baseline;
    }

    public void setBaseline(String baseline)
    {
        this.baseline = baseline;
    }

    public String getCheckpoint()
    {
        return checkpoint;
    }

    public void setCheckpoint(String checkpoint)
    {
        this.checkpoint = checkpoint;
    }

    public String getDiff()
    {
        return diff;
    }

    public void setDiff(String diff)
    {
        this.diff = diff;
    }

    public String getBaselineName()
    {
        return baselineName;
    }

    public boolean isPassed()
    {
        return passed;
    }

    public void setPassed(boolean passed)
    {
        this.passed = passed;
    }

    public VisualActionType getActionType()
    {
        return actionType;
    }
}
