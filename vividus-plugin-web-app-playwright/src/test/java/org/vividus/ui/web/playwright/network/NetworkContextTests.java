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

package org.vividus.ui.web.playwright.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.function.Consumer;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Request;

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.testcontext.TestContext;
import org.vividus.testcontext.ThreadedTestContext;

@ExtendWith(MockitoExtension.class)
class NetworkContextTests
{
    private final TestContext testContext = new ThreadedTestContext();

    @Mock private BrowserContext browserContext;
    @Mock private RunContext runContext;

    @ParameterizedTest
    @CsvSource({
            "true, otherMeta, 1",
            "false, proxy, 1",
            "true, proxy, 1",
            "false, otherMeta, 0"
    })
    void shouldRecordNetworkBeforeStory(boolean networkRecordingEnabledInProperties, String metaValue,
            int recordedRequests)
    {
        NetworkContext networkContext = new NetworkContext(testContext, runContext,
                networkRecordingEnabledInProperties);
        mockMetaForStoryAndScenario(metaValue, "otherMeta");

        networkContext.beforeStory();
        networkContext.listenNetwork(browserContext);

        ArgumentCaptor<Consumer<Request>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(browserContext).onRequest(captor.capture());
        Consumer<Request> consumer = captor.getValue();
        consumer.accept(mock(Request.class));
        assertEquals(recordedRequests, networkContext.getNetworkRecordings().size());
    }

    @ParameterizedTest
    @CsvSource({
            "false, proxy, otherMeta, 1",
            "false, otherMeta, proxy, 1",
            "false, proxy, proxy, 1",
            "false, otherMeta, otherMeta, 0",
            "true, proxy, otherMeta, 1",
            "true, otherMeta, proxy, 1",
            "true, proxy, proxy, 1",
            "true, otherMeta, otherMeta, 1"
    })
    void shouldRecordNetworkBeforeScenario(boolean networkRecordingEnabledInProperties,
            String storyMetaValue, String scenarioMetaValue, int recordedRequests)
    {
        NetworkContext networkContext = new NetworkContext(testContext, runContext,
                networkRecordingEnabledInProperties);
        mockMetaForStoryAndScenario(storyMetaValue, scenarioMetaValue);

        networkContext.beforeStory();
        networkContext.beforeScenario();
        networkContext.afterScenario();
        networkContext.beforeScenario();
        networkContext.listenNetwork(browserContext);

        ArgumentCaptor<Consumer<Request>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(browserContext).onRequest(captor.capture());
        Consumer<Request> consumer = captor.getValue();
        consumer.accept(mock(Request.class));
        assertEquals(recordedRequests, networkContext.getNetworkRecordings().size());
    }

    @Test
    void shouldDisableRecordingAfterStory()
    {
        NetworkContext networkContext = new NetworkContext(testContext, runContext,
                true);

        networkContext.beforeStory();
        networkContext.beforeScenario();
        networkContext.afterScenario();
        networkContext.afterStory();
        networkContext.beforeStory();
        networkContext.afterStory();
        networkContext.listenNetwork(browserContext);

        ArgumentCaptor<Consumer<Request>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(browserContext).onRequest(captor.capture());
        Consumer<Request> consumer = captor.getValue();
        consumer.accept(mock(Request.class));
        assertEquals(0, networkContext.getNetworkRecordings().size());
    }

    @Test
    void shouldDisableRecordingAfterScenario()
    {
        NetworkContext networkContext = new NetworkContext(testContext, runContext,
                true);

        networkContext.beforeStory();
        networkContext.beforeScenario();
        networkContext.afterScenario();
        networkContext.listenNetwork(browserContext);

        ArgumentCaptor<Consumer<Request>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(browserContext).onRequest(captor.capture());
        Consumer<Request> consumer = captor.getValue();
        consumer.accept(mock(Request.class));
        assertEquals(0, networkContext.getNetworkRecordings().size());
    }

    private void mockMetaForStoryAndScenario(String storyMetaValue, String scenarioMetaValue)
    {
        RunningStory runningStory = mock();
        RunningScenario runningScenario = mock();
        Story story = mock();
        Scenario scenario = mock();
        lenient().when(runContext.getRunningStory()).thenReturn(runningStory);
        lenient().when(runningStory.getStory()).thenReturn(story);
        lenient().when(runningStory.getRunningScenario()).thenReturn(runningScenario);
        lenient().when(runningScenario.getScenario()).thenReturn(scenario);
        lenient().when(story.getMeta()).thenReturn(new Meta(List.of(storyMetaValue)));
        lenient().when(scenario.getMeta()).thenReturn(new Meta(List.of(scenarioMetaValue)));
    }
}
