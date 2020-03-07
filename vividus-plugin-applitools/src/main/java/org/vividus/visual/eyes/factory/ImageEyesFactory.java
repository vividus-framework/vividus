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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Named;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.LogHandler;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.images.Eyes;

import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.model.VisualActionType;

@Named
public class ImageEyesFactory
{
    private final LogHandler logHandler;
    private final Map<String, BatchInfo> batchStorage = new ConcurrentHashMap<>();

    public ImageEyesFactory(LogHandler logHandler)
    {
        this.logHandler = logHandler;
    }

    public Eyes createEyes(ApplitoolsVisualCheck applitoolsVisualCheck)
    {
        Eyes eyes = new Eyes();
        eyes.setApiKey(applitoolsVisualCheck.getExecuteApiKey());
        String viewporSize = applitoolsVisualCheck.getViewportSize();
        //Environment
        eyes.setExplicitViewportSize(viewporSize == null ? null : RectangleSize.parse(viewporSize));
        eyes.setHostApp(applitoolsVisualCheck.getHostApp());
        eyes.setHostOS(applitoolsVisualCheck.getHostOS());
        eyes.setBaselineEnvName(applitoolsVisualCheck.getBaselineEnvName());

        eyes.setMatchLevel(applitoolsVisualCheck.getMatchLevel());
        eyes.setServerUrl(applitoolsVisualCheck.getServerUri());
        eyes.setLogHandler(logHandler);
        boolean saveTests = applitoolsVisualCheck.getAction() == VisualActionType.ESTABLISH;
        eyes.setSaveFailedTests(saveTests);
        eyes.setSaveNewTests(saveTests);
        String batchName = applitoolsVisualCheck.getBatchName();
        eyes.setBatch(batchStorage.computeIfAbsent(batchName, BatchInfo::new));
        return eyes;
    }
}
