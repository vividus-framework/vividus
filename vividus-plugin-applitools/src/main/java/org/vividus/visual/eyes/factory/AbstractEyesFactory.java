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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.LogHandler;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.config.Configuration;

import org.openqa.selenium.Dimension;
import org.vividus.ui.ViewportSizeProvider;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.model.VisualActionType;

public abstract class AbstractEyesFactory
{
    private final LogHandler logHandler;
    private final Map<String, BatchInfo> batchStorage = new ConcurrentHashMap<>();
    private final ViewportSizeProvider viewportSizeProvider;

    protected AbstractEyesFactory(LogHandler logHandler, ViewportSizeProvider viewportSizeProvider)
    {
        this.logHandler = logHandler;
        this.viewportSizeProvider = viewportSizeProvider;
    }

    protected Configuration createConfiguration(ApplitoolsVisualCheck applitoolsVisualCheck)
    {
        Configuration configuration = new Configuration();
        configuration.setApiKey(applitoolsVisualCheck.getExecuteApiKey());
        // Environment
        Dimension viewportSize = Optional.ofNullable(applitoolsVisualCheck.getViewportSize())
                .orElseGet(viewportSizeProvider::getViewportSize);
        configuration.setViewportSize(new RectangleSize(viewportSize.getWidth(), viewportSize.getHeight()));
        configuration.setHostApp(applitoolsVisualCheck.getHostApp());
        configuration.setHostOS(applitoolsVisualCheck.getHostOS());
        configuration.setBaselineEnvName(applitoolsVisualCheck.getBaselineEnvName());

        configuration.setMatchLevel(applitoolsVisualCheck.getMatchLevel());
        configuration.setServerUrl(applitoolsVisualCheck.getServerUri().toString());
        boolean saveTests = applitoolsVisualCheck.getAction() == VisualActionType.ESTABLISH;
        configuration.setSaveFailedTests(saveTests);
        configuration.setSaveNewTests(saveTests);
        String batchName = applitoolsVisualCheck.getBatchName();
        configuration.setBatch(batchStorage.computeIfAbsent(batchName, BatchInfo::new));
        return configuration;
    }

    protected LogHandler getLogHandler()
    {
        return logHandler;
    }
}
