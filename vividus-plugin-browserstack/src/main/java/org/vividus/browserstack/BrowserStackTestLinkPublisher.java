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

package org.vividus.browserstack;

import java.util.Optional;

import com.browserstack.automate.model.Session;
import com.google.common.eventbus.EventBus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.cloud.AbstractCloudTestLinkPublisher;
import org.vividus.testcontext.TestContext;

public class BrowserStackTestLinkPublisher extends AbstractCloudTestLinkPublisher
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserStackTestLinkPublisher.class);

    private final BrowserStackAutomateClient appAutomateClient;

    public BrowserStackTestLinkPublisher(IWebDriverProvider webDriverProvider, EventBus eventBus,
            TestContext testContext, BrowserStackAutomateClient appAutomateClient)
    {
        super("BrowserStack", webDriverProvider, eventBus, testContext);
        this.appAutomateClient = appAutomateClient;
    }

    @SuppressWarnings("IllegalCatchExtended")
    @Override
    public Optional<String> getCloudTestUrl(String sessionId)
    {
        try
        {
            Session session = appAutomateClient.getSession(sessionId);
            return Optional.of(session.getPublicUrl());
        }
        catch (Exception exception)
        {
            LOGGER.atError().addArgument(sessionId)
                            .setCause(exception)
                            .log("Unable to get an URL for BrowserStack session with the ID {}");
            return Optional.empty();
        }
    }
}
