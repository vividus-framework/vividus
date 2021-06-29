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

package org.vividus.crossbrowsertesting;

import com.crossbrowsertesting.AutomatedTest;
import com.crossbrowsertesting.Builders;
import com.google.common.eventbus.EventBus;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.cloud.AbstractCloudTestLinkPublisher;
import org.vividus.testcontext.TestContext;

public class CbtTestLinkPublisher extends AbstractCloudTestLinkPublisher
{
    protected CbtTestLinkPublisher(String username, String password, IWebDriverProvider webDriverProvider,
            EventBus eventBus, TestContext testContext)
    {
        super("CrossBrowserTesting", webDriverProvider, eventBus, testContext);
        Builders.login(username, password);
    }

    @Override
    protected String getCloudTestUrl(String sessionId) throws GetCloudTestUrlException
    {
        try
        {
            return new AutomatedTest(sessionId).getWebUrl();
        }
        catch (UnirestException exception)
        {
            throw new GetCloudTestUrlException(exception);
        }
    }
}
