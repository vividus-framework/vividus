/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.visual.eyes.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Optional;
import java.util.function.BiFunction;

import com.applitools.eyes.MatchLevel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.ui.screenshot.ScreenshotParameters;
import org.vividus.ui.screenshot.ScreenshotParametersFactory;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.screenshot.BaselineIndexer;

@ExtendWith(MockitoExtension.class)
class ApplitoolsVisualCheckFactoryTests
{
    private static final String BATCH_NAME = "batch_name";

    private static final VisualActionType ACTION = VisualActionType.ESTABLISH;

    private static final String BASELINE = "baseline";

    private static final String VIEWPORT_SIZE = "7680x4320";

    private static final URI SERVER_URI = URI.create("https://eyesapi.applitools.com");

    private static final String READ_API_KEY = "readApiKey";

    private static final MatchLevel MATCH_LEVEL = MatchLevel.EXACT;

    private static final String HOST_OS = "hostOs";

    private static final String HOST_APP = "hostApp";

    private static final String EXECUTE_API_KEY = "executeApiKey";

    private static final String BASELINE_ENV_NAME = "baselineEnvName";

    @Mock private ScreenshotParametersFactory<ScreenshotConfiguration> screenshotParametersFactory;
    @Mock private BaselineIndexer baselineIndexer;
    @InjectMocks private ApplitoolsVisualCheckFactory factory;

    @BeforeEach
    void setUp()
    {
        factory.setBaselineEnvName(BASELINE_ENV_NAME);
        factory.setExecuteApiKey(EXECUTE_API_KEY);
        factory.setHostApp(HOST_APP);
        factory.setHostOS(HOST_OS);
        factory.setMatchLevel(MATCH_LEVEL);
        factory.setReadApiKey(READ_API_KEY);
        factory.setServerUri(SERVER_URI);
        factory.setViewportSize(VIEWPORT_SIZE);
    }

    @Test
    void shouldCreateApplitoolsVisualCheckAndSetDefaultProperties()
    {
        when(baselineIndexer.createIndexedBaseline(BASELINE)).thenReturn(BASELINE);
        var screenshotParameters = mock(ScreenshotParameters.class);
        when(screenshotParametersFactory.create(Optional.empty())).thenReturn(Optional.of(screenshotParameters));
        var applitoolsVisualCheck = factory.create(BATCH_NAME, BASELINE, ACTION);
        Assertions.assertAll(
            () -> assertEquals(BASELINE, applitoolsVisualCheck.getBaselineName()),
            () -> assertEquals(ACTION, applitoolsVisualCheck.getAction()),
            () -> assertEquals(BATCH_NAME, applitoolsVisualCheck.getBatchName()),
            () -> assertEquals(EXECUTE_API_KEY, applitoolsVisualCheck.getExecuteApiKey()),
            () -> assertEquals(HOST_APP, applitoolsVisualCheck.getHostApp()),
            () -> assertEquals(HOST_OS, applitoolsVisualCheck.getHostOS()),
            () -> assertEquals(MATCH_LEVEL, applitoolsVisualCheck.getMatchLevel()),
            () -> assertEquals(READ_API_KEY, applitoolsVisualCheck.getReadApiKey()),
            () -> assertEquals(SERVER_URI, applitoolsVisualCheck.getServerUri()),
            () -> assertEquals(VIEWPORT_SIZE, applitoolsVisualCheck.getViewportSize()),
            () -> assertEquals(BASELINE_ENV_NAME, applitoolsVisualCheck.getBaselineEnvName()),
            () -> assertEquals("Application", applitoolsVisualCheck.getAppName()),
            () -> assertEquals(screenshotParameters, applitoolsVisualCheck.getScreenshotParameters().get()));
    }

    @Test
    void shouldUniteExistingPropertiesAndDefaultOnes()
    {
        var applitoolsVisualCheck = new ApplitoolsVisualCheck(BATCH_NAME, BASELINE, ACTION);
        var matchLevel = MatchLevel.NONE;
        applitoolsVisualCheck.setMatchLevel(matchLevel);
        var appName = "Application Under Test";
        factory.setAppName(appName);
        var actual = factory.unite(applitoolsVisualCheck);
        assertSame(applitoolsVisualCheck, actual);
        Assertions.assertAll(
            () -> assertEquals(BASELINE, applitoolsVisualCheck.getBaselineName()),
            () -> assertEquals(ACTION, applitoolsVisualCheck.getAction()),
            () -> assertEquals(BATCH_NAME, applitoolsVisualCheck.getBatchName()),
            () -> assertEquals(EXECUTE_API_KEY, applitoolsVisualCheck.getExecuteApiKey()),
            () -> assertEquals(HOST_APP, applitoolsVisualCheck.getHostApp()),
            () -> assertEquals(HOST_OS, applitoolsVisualCheck.getHostOS()),
            () -> assertEquals(matchLevel, applitoolsVisualCheck.getMatchLevel()),
            () -> assertEquals(READ_API_KEY, applitoolsVisualCheck.getReadApiKey()),
            () -> assertEquals(SERVER_URI, applitoolsVisualCheck.getServerUri()),
            () -> assertEquals(VIEWPORT_SIZE, applitoolsVisualCheck.getViewportSize()),
            () -> assertEquals(BASELINE_ENV_NAME, applitoolsVisualCheck.getBaselineEnvName()),
            () -> assertEquals(appName, applitoolsVisualCheck.getAppName()));
    }

    private BiFunction<String, VisualActionType, ApplitoolsVisualCheck> factoryMatcher()
    {
        return argThat(f ->
        {
            ApplitoolsVisualCheck visualCheck = f.apply(BASELINE, ACTION);
            Assertions.assertAll(
                () -> assertEquals(BASELINE, visualCheck.getBaselineName()),
                () -> assertEquals(ACTION, visualCheck.getAction()),
                () -> assertEquals(BATCH_NAME, visualCheck.getBatchName()));
            return true;
        });
    }
}
