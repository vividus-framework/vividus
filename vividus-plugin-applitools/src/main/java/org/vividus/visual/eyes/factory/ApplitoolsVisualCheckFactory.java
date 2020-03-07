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

import java.net.URI;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.applitools.eyes.MatchLevel;

import org.vividus.visual.IVisualCheckFactory;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.model.VisualActionType;

public class ApplitoolsVisualCheckFactory
{
    private final IVisualCheckFactory visualCheckFactory;

    private String executeApiKey;
    private String readApiKey;
    private String hostApp;
    private String hostOS;
    private String viewportSize;
    private MatchLevel matchLevel;
    private URI serverUri;
    private String appName = "Application";
    private String baselineEnvName;

    public ApplitoolsVisualCheckFactory(IVisualCheckFactory visualCheckFactory)
    {
        this.visualCheckFactory = visualCheckFactory;
    }

    public ApplitoolsVisualCheck create(String batchName, String baselineName, VisualActionType action)
    {
        ApplitoolsVisualCheck check = visualCheckFactory.create(baselineName, action,
            (bn, ac) -> new ApplitoolsVisualCheck(batchName, bn, ac));
        check.setExecuteApiKey(executeApiKey);
        check.setReadApiKey(readApiKey);
        check.setBaselineEnvName(baselineEnvName);
        check.setHostApp(hostApp);
        check.setHostOS(hostOS);
        check.setMatchLevel(matchLevel);
        check.setServerUri(serverUri);
        check.setViewportSize(viewportSize);
        check.setAppName(appName);
        return check;
    }

    public ApplitoolsVisualCheck unite(ApplitoolsVisualCheck visualCheck)
    {
        setIfEmpty(ApplitoolsVisualCheck::getExecuteApiKey, ApplitoolsVisualCheck::setExecuteApiKey,
                executeApiKey, visualCheck);
        setIfEmpty(ApplitoolsVisualCheck::getReadApiKey, ApplitoolsVisualCheck::setReadApiKey,
                readApiKey, visualCheck);
        setIfEmpty(ApplitoolsVisualCheck::getBaselineEnvName, ApplitoolsVisualCheck::setBaselineEnvName,
                baselineEnvName, visualCheck);
        setIfEmpty(ApplitoolsVisualCheck::getHostApp, ApplitoolsVisualCheck::setHostApp,
                hostApp, visualCheck);
        setIfEmpty(ApplitoolsVisualCheck::getHostOS, ApplitoolsVisualCheck::setHostOS,
                hostOS, visualCheck);
        setIfEmpty(ApplitoolsVisualCheck::getMatchLevel, ApplitoolsVisualCheck::setMatchLevel,
                matchLevel, visualCheck);
        setIfEmpty(ApplitoolsVisualCheck::getServerUri, ApplitoolsVisualCheck::setServerUri,
                serverUri, visualCheck);
        setIfEmpty(ApplitoolsVisualCheck::getViewportSize, ApplitoolsVisualCheck::setViewportSize,
                viewportSize, visualCheck);
        setIfEmpty(ApplitoolsVisualCheck::getAppName, ApplitoolsVisualCheck::setAppName,
                appName, visualCheck);
        return visualCheck;
    }

    private <T> void setIfEmpty(Function<ApplitoolsVisualCheck, T> getter,
            BiConsumer<ApplitoolsVisualCheck, T> setter, T toSet, ApplitoolsVisualCheck base)
    {
        if (null == getter.apply(base))
        {
            setter.accept(base, toSet);
        }
    }

    public void setExecuteApiKey(String executeApiKey)
    {
        this.executeApiKey = executeApiKey;
    }

    public void setReadApiKey(String readApiKey)
    {
        this.readApiKey = readApiKey;
    }

    public void setHostApp(String hostApp)
    {
        this.hostApp = hostApp;
    }

    public void setHostOS(String hostOS)
    {
        this.hostOS = hostOS;
    }

    public void setViewportSize(String viewportSize)
    {
        this.viewportSize = viewportSize;
    }

    public void setMatchLevel(MatchLevel matchLevel)
    {
        this.matchLevel = matchLevel;
    }

    public void setServerUri(URI serverUri)
    {
        this.serverUri = serverUri;
    }

    public void setAppName(String appName)
    {
        this.appName = appName;
    }

    public void setBaselineEnvName(String baselineEnvName)
    {
        this.baselineEnvName = baselineEnvName;
    }
}
