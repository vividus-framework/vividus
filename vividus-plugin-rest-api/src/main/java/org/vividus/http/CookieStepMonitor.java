/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.http;

import java.lang.reflect.Method;

import org.jbehave.core.steps.NullStepMonitor;

public class CookieStepMonitor extends NullStepMonitor
{
    private final CookieStoreProvider cookieStoreProvider;

    public CookieStepMonitor(CookieStoreProvider cookieStoreProvider)
    {
        this.cookieStoreProvider = cookieStoreProvider;
    }

    @Override
    public void afterPerforming(String step, boolean dryRun, Method method)
    {
        if (!dryRun)
        {
            cookieStoreProvider.resetStepCookies();
        }
    }
}
