/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.analytics;

import java.util.concurrent.CompletableFuture;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import org.vividus.analytics.model.AnalyticsEventBatch;

public class AnalyticsService
{
    private boolean enabled;
    private final GoogleAnalyticsFacade analytics;

    public AnalyticsService(GoogleAnalyticsFacade analytics)
    {
        this.analytics = analytics;
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onAnalyticEvent(AnalyticsEventBatch event)
    {
        if (enabled)
        {
            CompletableFuture.runAsync(() -> analytics.postEvent(event));
        }
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
}
