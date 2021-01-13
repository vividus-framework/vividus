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

package org.vividus.browserstack;

import com.browserstack.client.exception.BrowserStackException;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.cloud.AbstractCloudTestStatusManager;
import org.vividus.testcontext.TestContext;

public class BrowserStackTestStatusManager extends AbstractCloudTestStatusManager
{
    private final BrowserStackAutomateClient browserStackAutomateClient;

    public BrowserStackTestStatusManager(IWebDriverProvider webDriverProvider, TestContext testContext,
            BrowserStackAutomateClient browserStackAutomateClient)
    {
        super(webDriverProvider, testContext);
        this.browserStackAutomateClient = browserStackAutomateClient;
    }

    @Override
    protected void updateCloudTestStatus(String status) throws UpdateCloudTestStatusException
    {
        try
        {
            String sessionId = getWebDriverProvider().getUnwrapped(RemoteWebDriver.class).getSessionId().toString();
            browserStackAutomateClient.updateSessionStatus(sessionId, status);
        }
        catch (BrowserStackException e)
        {
            throw new UpdateCloudTestStatusException(e);
        }
    }
}
