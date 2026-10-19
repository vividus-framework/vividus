/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.newrelic.model;

import java.util.Map;

public final class VividusStoryEvent extends NewRelicEvent
{
    private static final String EVENT_TYPE = "VividusStory";

    private final String storyName;
    private final int batchIndex;
    private final Status status;

    public VividusStoryEvent(String runId, String storyName, int batchIndex, Status status,
            Map<String, String> attributes)
    {
        super(EVENT_TYPE, runId, attributes);
        this.storyName = storyName;
        this.batchIndex = batchIndex;
        this.status = status;
    }

    public String getStoryName()
    {
        return storyName;
    }

    public int getBatchIndex()
    {
        return batchIndex;
    }

    public Status getStatus()
    {
        return status;
    }
}
