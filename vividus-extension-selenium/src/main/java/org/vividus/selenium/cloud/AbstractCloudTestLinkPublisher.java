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

import java.util.Optional;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.ScenarioType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.vividus.reporter.event.LinkPublishEvent;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.testcontext.TestContext;

public abstract class AbstractCloudTestLinkPublisher
{
    private static final Object KEY = CloudTestLinkPublishState.class;

    private final String linkName;

    private final IWebDriverProvider webDriverProvider;
    private final EventBus eventBus;
    private final TestContext testContext;

    protected AbstractCloudTestLinkPublisher(String testCloudName, IWebDriverProvider webDriverProvider,
            EventBus eventBus, TestContext testContext)
    {
        this.linkName = format("%s Test URL", testCloudName);
        this.webDriverProvider = webDriverProvider;
        this.eventBus = eventBus;
        this.testContext = testContext;
    }

    protected abstract Optional<String> getCloudTestUrl(String sessionId);

    @BeforeScenario(uponType = ScenarioType.ANY)
    public void resetState()
    {
        testContext.put(KEY, new CloudTestLinkPublishState());
    }

    @AfterScenario(uponType = ScenarioType.ANY)
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

    private void publishCloudTestLink(String sessionId)
    {
        getCloudTestUrl(sessionId).ifPresent(link -> eventBus.post(new LinkPublishEvent(linkName, link)));
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
}
