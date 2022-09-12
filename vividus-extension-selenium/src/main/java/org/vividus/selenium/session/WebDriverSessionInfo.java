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

package org.vividus.selenium.session;

import com.google.common.eventbus.Subscribe;

import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.selenium.manager.AbstractWebDriverManagerContext;
import org.vividus.testcontext.TestContext;

public class WebDriverSessionInfo extends AbstractWebDriverManagerContext<WebDriverSessionAttribute>
{
    public WebDriverSessionInfo(TestContext testContext)
    {
        super(testContext, WebDriverSessionInfo.class);
    }

    @SuppressWarnings("PMD.UselessOverridingMethod")
    @Override
    public void reset(WebDriverSessionAttribute key)
    {
        super.reset(key);
    }

    @Subscribe
    public void onWebDriverQuit(AfterWebDriverQuitEvent event)
    {
        reset();
    }
}
