/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.applitools.executioncloud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.net.URI;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.context.RunContext;
import org.vividus.model.RunningStory;
import org.vividus.selenium.event.WebDriverCreateEvent;

@ExtendWith(MockitoExtension.class)
class ExecutionCloudCapabilitiesConfigurerTests
{
    private static final String SERVER_URI = "https://eyesapi.applitools.com";
    private static final String API_KEY = "api-key";
    private static final String APP_NAME = "app-name";
    private static final String RUN_NAME = "run-name";

    @Mock private RunContext runContext;

    @Test
    void shouldConfigureCapabilitooes()
    {
        ExecutionCloudCapabilitiesConfigurer configurer = new ExecutionCloudCapabilitiesConfigurer(API_KEY,
                URI.create(SERVER_URI), APP_NAME, RUN_NAME, runContext);
        DesiredCapabilities capabilities = new DesiredCapabilities();

        configurer.configure(capabilities);

        @SuppressWarnings("unchecked")
        Map<String, String> options = (Map<String, String>) capabilities.getCapability("applitools:options");
        assertEquals(SERVER_URI, options.get("eyesServerUrl"));
        assertEquals(API_KEY, options.get("apiKey"));
    }

    @Test
    void shouldStartTest()
    {
        String storyName = "story-name";
        RunningStory runningStory = mock();
        when(runContext.getRootRunningStory()).thenReturn(runningStory);
        when(runningStory.getName()).thenReturn(storyName);
        ExecutionCloudCapabilitiesConfigurer configurer = new ExecutionCloudCapabilitiesConfigurer(API_KEY,
                URI.create(SERVER_URI), APP_NAME, RUN_NAME, runContext);
        WebDriver webDriver = mock(withSettings().extraInterfaces(JavascriptExecutor.class));
        WebDriverCreateEvent event = new WebDriverCreateEvent(webDriver);

        configurer.onSessionStart(event);

        JavascriptExecutor executor = (JavascriptExecutor) webDriver;
        verify(executor).executeScript("applitools:startTest", Map.of(
            "testName", storyName,
            "appName", APP_NAME,
            "batch", Map.of("name", RUN_NAME)
        ));
    }
}
