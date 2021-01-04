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

package org.vividus.lambdatest.report;

import static java.lang.String.format;

import java.util.Optional;

import com.google.common.eventbus.EventBus;

import org.vividus.selenium.IWebDriverProvider;
import org.vividus.testcontext.TestContext;
import org.vividus.ui.report.AbstractSessionLinkPublisher;

public class LambdaTestSessionLinkPublisher extends AbstractSessionLinkPublisher
{
    public LambdaTestSessionLinkPublisher(IWebDriverProvider webDriverProvider, EventBus eventBus,
            TestContext testContext)
    {
        super("LambdaTest", webDriverProvider, eventBus, testContext);
    }

    @Override
    protected Optional<String> getSessionUrl(String sessionId)
    {
        return Optional.of(format("https://automation.lambdatest.com/logs/?sessionID=%s", sessionId));
    }
}
