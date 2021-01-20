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

package org.vividus.bdd.browserstack.steps;

import static org.apache.commons.lang3.Validate.isTrue;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import com.browserstack.client.exception.BrowserStackException;
import com.google.common.eventbus.Subscribe;

import org.jbehave.core.annotations.When;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.browserstack.BrowserStackAutomateClient;
import org.vividus.json.JsonContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.testcontext.TestContext;

public class DebugSteps
{
    private static final String PREVIOUS_SESSION_ID = "previousSessionId";

    private final BrowserStackAutomateClient appAutomateClient;
    private final IBddVariableContext bddVariableContext;
    private final TestContext testContext;
    private final JsonContext jsonContext;
    private final IAttachmentPublisher attachmentPublisher;

    public DebugSteps(BrowserStackAutomateClient appAutomateClient, IBddVariableContext bddVariableContext,
            TestContext testContext, JsonContext jsonContext, IAttachmentPublisher attachmentPublisher)
    {
        this.appAutomateClient = appAutomateClient;
        this.bddVariableContext = bddVariableContext;
        this.testContext = testContext;
        this.jsonContext = jsonContext;
        this.attachmentPublisher = attachmentPublisher;
    }

    /**
     * Saves network logs from an application session. The application session must be closed before network logs
     * can be saved.
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A variable name
     * @throws BrowserStackException if error occurred while interacting with BrowserStack
     */
    @When("I save BrowserStack network logs to $scopes variable `$variableName`")
    public void saveNetworkLogs(Set<VariableScope> scopes, String variableName) throws BrowserStackException
    {
        bddVariableContext.putVariable(scopes, variableName, getNetworkLogs());
    }

    /**
     * Saves network traffic captured during application run into JSON context. The network traffic data is in HAR
     * format. The application session must be closed before network logs can be saved.
     * <br>
     * The network from the HAR can be accessed by using JSON steps that work with the JSON context
     * @throws BrowserStackException if error occurred while interacting with BrowserStack
     */
    @When("I save BrowserStack network logs to JSON context")
    public void saveNetworkLogsToJsonContext() throws BrowserStackException
    {
        String logs = getNetworkLogs();
        jsonContext.putJsonContext(logs);
        attachmentPublisher.publishAttachment(logs.getBytes(StandardCharsets.UTF_8), "BrowserStack network logs.har");
    }

    private String getNetworkLogs() throws BrowserStackException
    {
        String sessionId = testContext.get(PREVIOUS_SESSION_ID);
        isTrue(sessionId != null, "Unable to find a previous session");
        return appAutomateClient.getNetworkLogs(sessionId);
    }

    @Subscribe
    public void onWebDriverQuit(AfterWebDriverQuitEvent event)
    {
        testContext.put(PREVIOUS_SESSION_ID, event.getSessionId());
    }
}
