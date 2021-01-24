/*
 * Copyright 2019-2021 the original author or authors.
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

import org.jbehave.core.embedder.StoryControls;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.model.SoftAssertionError;

public class AssertionManager
{
    private final EventBus eventBus;
    private final ISoftAssert softAssert;
    private final StoryControls storyControls;

    private boolean failScenarioFast;

    public AssertionManager(EventBus eventBus, ISoftAssert softAssert, StoryControls storyControls)
    {
        this.eventBus = eventBus;
        this.softAssert = softAssert;
        this.storyControls = storyControls;
    }

    @Subscribe
    public void onAssertionFailure(AssertionFailedEvent event)
    {
        SoftAssertionError error = event.getSoftAssertionError();
        if (failScenarioFast && !error.isKnownIssue() || error.isFailTestCaseFast())
        {
            // Way to order EventBus listeners: all AssertionFailedEvent listeners and all events spawned by
            // these listeners must be processed, only after that verification may be triggered
            eventBus.post(new VerifyAssertionsEvent());
        }

        if (error.isFailTestSuiteFast())
        {
            storyControls.currentStoryControls().doResetStateBeforeScenario(false);
        }
    }

    @Subscribe
    public void onVerifyAssertions(@SuppressWarnings("unused") VerifyAssertionsEvent event)
    {
        softAssert.verify();
    }

    public void setFailScenarioFast(boolean failScenarioFast)
    {
        this.failScenarioFast = failScenarioFast;
    }

    private static class VerifyAssertionsEvent
    {
    }
}
