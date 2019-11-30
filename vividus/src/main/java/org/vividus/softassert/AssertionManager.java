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

package org.vividus.softassert;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.vividus.softassert.event.AssertionFailedEvent;

public class AssertionManager
{
    private final EventBus eventBus;
    private final ISoftAssert softAssert;

    private boolean failFast;

    public AssertionManager(EventBus eventBus, ISoftAssert softAssert)
    {
        this.eventBus = eventBus;
        this.softAssert = softAssert;
    }

    @Subscribe
    public void onAssertionFailure(AssertionFailedEvent event)
    {
        if (failFast && !event.getSoftAssertionError().isKnownIssue())
        {
            // Way to order EventBus listeners: all AssertionFailedEvent listeners and all events spawned by
            // these listeners must be processed, only after that verification may be triggered
            eventBus.post(new VerifyAssertionsEvent());
        }
    }

    @Subscribe
    public void onVerifyAssertions(@SuppressWarnings("unused") VerifyAssertionsEvent event)
    {
        softAssert.verify();
    }

    public void setFailFast(boolean failFast)
    {
        this.failFast = failFast;
    }

    private static class VerifyAssertionsEvent
    {
    }
}
