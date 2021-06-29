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

package org.vividus.selenium.cloud;

import static java.lang.String.format;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.BeforeScenario;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.reporter.event.LinkPublishEvent;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.testcontext.TestContext;

public abstract class AbstractCloudTestLinkPublisher
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCloudTestLinkPublisher.class);

    private static final Object KEY = CloudTestLinkPublishState.class;

    private final String linkName;
    private final String testCloudName;

    private final IWebDriverProvider webDriverProvider;
    private final EventBus eventBus;
    private final TestContext testContext;

    protected AbstractCloudTestLinkPublisher(String testCloudName, IWebDriverProvider webDriverProvider,
            EventBus eventBus, TestContext testContext)
    {
        this.linkName = format("%s Test URL", testCloudName);
        this.testCloudName = testCloudName;
        this.webDriverProvider = webDriverProvider;
        this.eventBus = eventBus;
        this.testContext = testContext;
    }

    protected abstract String getCloudTestUrl(String sessionId) throws GetCloudTestUrlException;

    @BeforeScenario
    public void resetState()
    {
        testContext.put(KEY, new CloudTestLinkPublishState());
    }

    @AfterScenario
    public void publishCloudTestLinkAfterScenario()
    {
        if (webDriverProvider.isWebDriverInitialized())
        {
            String sessionId = webDriverProvider.getUnwrapped(RemoteWebDriver.class).getSessionId().toString();
            publishCloudTestLink(sessionId);
            getPublishState().onPublishedAfterScenario();
        }
    }

    @Subscribe
    public void publishCloudTestLinkOnWebDriverQuit(AfterWebDriverQuitEvent event)
    {
        if (!getPublishState().isPublishedAfterScenario())
        {
            publishCloudTestLink(event.getSessionId());
        }
    }

    private CloudTestLinkPublishState getPublishState()
    {
        return testContext.get(KEY, CloudTestLinkPublishState.class);
    }

    @SuppressWarnings("IllegalCatchExtended")
    private void publishCloudTestLink(String sessionId)
    {
        try
        {
            eventBus.post(new LinkPublishEvent(linkName, getCloudTestUrl(sessionId)));
        }
        catch (Exception e)
        {
            LOGGER.atError()
                  .addArgument(testCloudName)
                  .addArgument(sessionId)
                  .setCause(e)
                  .log("Unable to get an URL for {} session with the ID {}");
        }
    }

    private final class CloudTestLinkPublishState
    {
        private boolean publishedAfterScenario;

        public boolean isPublishedAfterScenario()
        {
            return publishedAfterScenario;
        }

        public void onPublishedAfterScenario()
        {
            this.publishedAfterScenario = true;
        }
    }

    public static final class GetCloudTestUrlException extends Exception
    {
        private static final long serialVersionUID = -4333639036297482018L;

        public GetCloudTestUrlException(Throwable cause)
        {
            super(cause);
        }
    }
}
