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

package org.vividus.mobitru.mobileapp;

import com.google.common.eventbus.Subscribe;

import org.vividus.mobileapp.action.ApplicationActions;
import org.vividus.selenium.event.WebDriverCreateEvent;

public class MobitruApplicationActivator
{
    private final ApplicationActions applicationActions;
    private String bundleId;

    public MobitruApplicationActivator(ApplicationActions applicationActions)
    {
        this.applicationActions = applicationActions;
    }

    @Subscribe
    public void onSessionStart(WebDriverCreateEvent event)
    {
        applicationActions.activateApp(bundleId);
    }

    public void setBundleId(String bundleId)
    {
        this.bundleId = bundleId;
    }
}
