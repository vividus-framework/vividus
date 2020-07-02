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

package org.vividus.visual.eyes.factory;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import com.applitools.connectivity.ServerConnector;
import com.applitools.eyes.LogHandler;
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
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.model.VisualActionType;

@ExtendWith(MockitoExtension.class)
class ImageEyesFactoryTests
{
    private static final String APP = "DOKA 2";

    private static final String BATCH_NAME = "batch_name";

    private static final String BASELINE = "baseline";

    private static final String VIEWPORT_SIZE = "7680x4320";

    private static final URI SERVER_URI = URI.create("https://eyesapi.applitools.com");

    private static final MatchLevel MATCH_LEVEL = MatchLevel.EXACT;

    private static final String HOST_OS = "hostOs";

    private static final String HOST_APP = "hostApp";

    private static final String EXECUTE_API_KEY = "executeApiKey";

    private static final String BASELINE_ENV_NAME = "baselineEnvName";

    @Mock private LogHandler logHandler;

    @InjectMocks private ImageEyesFactory imageEyesFactory;

    @Test
    void shouldCreateEyesWithMandatoryParametersOnly() throws IllegalAccessException
    {
        ApplitoolsVisualCheck visualCheck = createVisualCheck(VisualActionType.ESTABLISH);
        Eyes eyes = imageEyesFactory.createEyes(visualCheck);
        Configuration configuration = eyes.getConfiguration();
        ServerConnector readServerConnector = readServerConnector(eyes);
        assertAll(
            () -> assertNull(configuration.getHostApp()),
            () -> assertNull(configuration.getHostOS()),
            () -> assertNull(configuration.getMatchLevel()),
            () -> assertNull(configuration.getBaselineEnvName()),
            () -> assertTrue(configuration.getSaveFailedTests()),
            () -> assertTrue(configuration.getSaveNewTests()),
            () -> assertEquals(BATCH_NAME, configuration.getBatch().getName()),
            () -> assertNull(readViewportSize(eyes)),
            () -> assertEquals(SERVER_URI, readServerConnector.getServerUrl()),
            () -> assertEquals(EXECUTE_API_KEY, readServerConnector.getApiKey()),
            () -> assertSame(logHandler, eyes.getLogHandler())
        );
    }

    private RectangleSize readViewportSize(Eyes eyes) throws IllegalAccessException
    {
        return (RectangleSize) FieldUtils.readField(eyes, "viewportSize", true);
    }

    private ServerConnector readServerConnector(Eyes eyes) throws IllegalAccessException
    {
        return (ServerConnector) FieldUtils.readField(eyes, "serverConnector", true);
    }

    private ApplitoolsVisualCheck createVisualCheck(VisualActionType action)
    {
        ApplitoolsVisualCheck visualCheck = new ApplitoolsVisualCheck(BATCH_NAME, BASELINE, action);
        visualCheck.setServerUri(SERVER_URI);
        visualCheck.setExecuteApiKey(EXECUTE_API_KEY);
        return visualCheck;
    }

    @Test
    public void shouldCreateEyesWithAllParameters() throws IllegalAccessException
    {
        ApplitoolsVisualCheck visualCheck = createVisualCheck(VisualActionType.COMPARE_AGAINST);
        visualCheck.setBaselineEnvName(BASELINE_ENV_NAME);
        visualCheck.setAppName(APP);
        visualCheck.setHostApp(HOST_APP);
        visualCheck.setHostOS(HOST_OS);
        visualCheck.setMatchLevel(MATCH_LEVEL);
        visualCheck.setViewportSize(VIEWPORT_SIZE);
        Eyes actual = imageEyesFactory.createEyes(visualCheck);
        Configuration configuration = actual.getConfiguration();
        ServerConnector readServerConnector = readServerConnector(actual);
        assertAll(
            () -> assertEquals(HOST_APP, configuration.getHostApp()),
            () -> assertEquals(HOST_OS, configuration.getHostOS()),
            () -> assertEquals(MATCH_LEVEL, configuration.getMatchLevel()),
            () -> assertEquals(BASELINE_ENV_NAME, configuration.getBaselineEnvName()),
            () -> assertFalse(configuration.getSaveFailedTests()),
            () -> assertFalse(configuration.getSaveNewTests()),
            () -> assertEquals(BATCH_NAME, configuration.getBatch().getName()),
            () -> assertEquals(new RectangleSize(7680, 4320), readViewportSize(actual)),
            () -> assertEquals(SERVER_URI, readServerConnector.getServerUrl()),
            () -> assertEquals(EXECUTE_API_KEY, readServerConnector.getApiKey()),
            () -> assertSame(logHandler, actual.getLogHandler())
        );
    }

    @Test
    public void shouldReuseBatchWithTheSameName()
    {
        ApplitoolsVisualCheck visualCheck = createVisualCheck(VisualActionType.ESTABLISH);
        Eyes eyes1 = imageEyesFactory.createEyes(visualCheck);
        Eyes eyes2 = imageEyesFactory.createEyes(visualCheck);
        assertNotSame(eyes1, eyes2);
        assertSame(eyes1.getConfiguration().getBatch(), eyes2.getConfiguration().getBatch());
    }
}
