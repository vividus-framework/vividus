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

package org.vividus.bdd.browserstack.steps;

import java.util.Optional;

import com.browserstack.automate.model.Session;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.ScenarioType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.browserstack.BrowserStackAutomateClient;
import org.vividus.reporter.event.LinkPublishEvent;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.event.WebDriverQuitEvent;
import org.vividus.testcontext.TestContext;

public class BrowserStackSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserStackSteps.class);

    private static final Object KEY = SessionLinkPublishState.class;

    private final IWebDriverProvider webDriverProvider;
    private final EventBus eventBus;
    private final BrowserStackAutomateClient appAutomateClient;
    private final TestContext testContext;

    public BrowserStackSteps(IWebDriverProvider webDriverProvider, EventBus eventBus,
            BrowserStackAutomateClient appAutomateClient, TestContext testContext)
    {
        this.webDriverProvider = webDriverProvider;
        this.eventBus = eventBus;
        this.appAutomateClient = appAutomateClient;
        this.testContext = testContext;
    }

    @BeforeScenario(uponType = ScenarioType.ANY)
    public void resetState()
    {
        testContext.put(KEY, new SessionLinkPublishState());
    }

    @AfterScenario(uponType = ScenarioType.ANY)
    public void publishSessionLinkAfterScenario()
    {
        if (webDriverProvider.isWebDriverInitialized())
        {
            String sessionId = webDriverProvider.getUnwrapped(RemoteWebDriver.class).getSessionId().toString();
            publishSessionLink(sessionId);
            getPublishState().onPublishedAfterScenario();
        }
    }

    @Subscribe
    public void publishSessionLinkOnWebDriverQuit(WebDriverQuitEvent event)
    {
        if (!getPublishState().isPublishedAfterScenario())
        {
            publishSessionLink(event.getSessionId());
        }
    }

    private SessionLinkPublishState getPublishState()
    {
        return testContext.get(KEY, SessionLinkPublishState.class);
    }

    private void publishSessionLink(String sessionId)
    {
        getSessionLink(sessionId)
                .ifPresent(link -> eventBus.post(new LinkPublishEvent("BrowserStack Session URL", link)));
    }

    @SuppressWarnings("IllegalCatchExtended")
    private Optional<String> getSessionLink(String sessionId)
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

    private final class SessionLinkPublishState
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
