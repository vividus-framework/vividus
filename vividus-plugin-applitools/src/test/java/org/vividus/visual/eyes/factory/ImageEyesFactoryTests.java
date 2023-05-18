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

package org.vividus.visual.eyes.factory;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Set;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.EyesRunner;
import com.applitools.eyes.LogHandler;
import com.applitools.eyes.Logger;
import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.config.Configuration;
import com.applitools.eyes.images.Eyes;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.vividus.ui.ViewportSizeProvider;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.model.VisualActionType;

@ExtendWith(MockitoExtension.class)
class ImageEyesFactoryTests
{
    private static final String APP = "DOKA 2";

    private static final String BATCH_NAME = "batch_name";

    private static final String BASELINE = "baseline";

    private static final URI SERVER_URI = URI.create("https://eyesapi.applitools.com");

    private static final MatchLevel MATCH_LEVEL = MatchLevel.EXACT;

    private static final String HOST_OS = "hostOs";

    private static final String HOST_APP = "hostApp";

    private static final String EXECUTE_API_KEY = "executeApiKey";

    private static final String BASELINE_ENV_NAME = "baselineEnvName";

    private static final RectangleSize VIEWPORT = new RectangleSize(800, 600);

    @Mock private LogHandler logHandler;
    @Mock private ViewportSizeProvider viewportSizeProvider;
    @InjectMocks private ImageEyesFactory imageEyesFactory;

    @Test
    void shouldCreateEyesWithMandatoryParametersOnly() throws IllegalAccessException
    {
        ApplitoolsVisualCheck visualCheck = createVisualCheck(VisualActionType.ESTABLISH);
        visualCheck.getConfiguration().setSaveFailedTests(true)
                                      .setSaveNewTests(true)
                                      .setBatch(new BatchInfo(BATCH_NAME));
        Eyes eyes = imageEyesFactory.createEyes(visualCheck);
        Configuration configuration = eyes.getConfiguration();
        assertAll(
            () -> assertNull(configuration.getHostApp()),
            () -> assertNull(configuration.getHostOS()),
            () -> assertEquals(MatchLevel.STRICT, configuration.getMatchLevel()),
            () -> assertNull(configuration.getBaselineEnvName()),
            () -> assertTrue(configuration.getSaveFailedTests()),
            () -> assertTrue(configuration.getSaveNewTests()),
            () -> assertEquals(BATCH_NAME, configuration.getBatch().getName()),
            () -> assertEquals(VIEWPORT, configuration.getViewportSize()),
            () -> assertEquals(SERVER_URI, configuration.getServerUrl()),
            () -> assertEquals(EXECUTE_API_KEY, configuration.getApiKey()),
            () -> assertEquals(Set.of(logHandler), readLogHandler(eyes))
        );
        verifyNoInteractions(viewportSizeProvider);
    }

    private ApplitoolsVisualCheck createVisualCheck(VisualActionType action)
    {
        ApplitoolsVisualCheck visualCheck = new ApplitoolsVisualCheck(BATCH_NAME, BASELINE, action);
        Configuration config = new Configuration().setServerUrl(SERVER_URI.toString())
                                                  .setViewportSize(VIEWPORT)
                                                  .setApiKey(EXECUTE_API_KEY);
        visualCheck.setConfiguration(config);
        return visualCheck;
    }

    @Test
    void shouldCreateEyesWithAllParameters() throws IllegalAccessException
    {
        ApplitoolsVisualCheck visualCheck = createVisualCheck(VisualActionType.COMPARE_AGAINST);
        visualCheck.getConfiguration().setBaselineEnvName(BASELINE_ENV_NAME)
                                      .setAppName(APP)
                                      .setHostApp(HOST_APP)
                                      .setHostOS(HOST_OS)
                                      .setMatchLevel(MATCH_LEVEL)
                                      .setViewportSize(new RectangleSize(7680, 4320))
                                      .setSaveFailedTests(false)
                                      .setSaveNewTests(false)
                                      .setBatch(new BatchInfo(BATCH_NAME));
        Eyes eyes = imageEyesFactory.createEyes(visualCheck);
        Configuration configuration = eyes.getConfiguration();
        assertAll(
            () -> assertEquals(HOST_APP, configuration.getHostApp()),
            () -> assertEquals(HOST_OS, configuration.getHostOS()),
            () -> assertEquals(MATCH_LEVEL, configuration.getMatchLevel()),
            () -> assertEquals(BASELINE_ENV_NAME, configuration.getBaselineEnvName()),
            () -> assertFalse(configuration.getSaveFailedTests()),
            () -> assertFalse(configuration.getSaveNewTests()),
            () -> assertEquals(BATCH_NAME, configuration.getBatch().getName()),
            () -> assertEquals(new RectangleSize(7680, 4320), configuration.getViewportSize()),
            () -> assertEquals(SERVER_URI, configuration.getServerUrl()),
            () -> assertEquals(EXECUTE_API_KEY, configuration.getApiKey()),
            () -> assertEquals(Set.of(logHandler), readLogHandler(eyes))
        );
        verifyNoInteractions(viewportSizeProvider);
    }

    @Test
    void shouldCreateEyesWithEmptyViewportSize()
    {
        when(viewportSizeProvider.getViewportSize()).thenReturn(new Dimension(VIEWPORT.getWidth(),
                VIEWPORT.getHeight()));
        ApplitoolsVisualCheck visualCheck = createVisualCheck(VisualActionType.COMPARE_AGAINST);
        visualCheck.getConfiguration().setViewportSize(null);
        Eyes eyes = imageEyesFactory.createEyes(visualCheck);
        assertEquals(VIEWPORT, eyes.getConfiguration().getViewportSize());
    }

    @SuppressWarnings("unchecked")
    private Set<LogHandler> readLogHandler(Eyes eyes) throws IllegalAccessException
    {
        EyesRunner runner = (EyesRunner) FieldUtils.readField(eyes, "runner", true);
        Logger logger = (Logger) FieldUtils.readField(runner, "logger", true);
        return (Set<LogHandler>) FieldUtils.readField(logger.getLogHandler(), "logHandlers", true);
    }

    @Test
    void shouldReuseBatchWithTheSameName()
    {
        ApplitoolsVisualCheck visualCheck = createVisualCheck(VisualActionType.ESTABLISH);
        Eyes eyes1 = imageEyesFactory.createEyes(visualCheck);
        Eyes eyes2 = imageEyesFactory.createEyes(visualCheck);
        assertNotSame(eyes1, eyes2);
        assertSame(eyes1.getConfiguration().getBatch(), eyes2.getConfiguration().getBatch());
    }
}
