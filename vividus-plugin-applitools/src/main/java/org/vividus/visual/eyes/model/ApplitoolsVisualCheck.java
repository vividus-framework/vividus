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

package org.vividus.visual.eyes.model;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import com.applitools.eyes.MatchLevel;

import org.openqa.selenium.By;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheck;
import org.vividus.visual.screenshot.IgnoreStrategy;

public class ApplitoolsVisualCheck extends VisualCheck
{
    private String executeApiKey;
    private String readApiKey;
    private String hostApp;
    private String hostOS;
    private String viewportSize;
    private MatchLevel matchLevel;
    private URI serverUri;
    private String appName;
    private String batchName;
    private String baselineEnvName;
    private Set<By> elementsToIgnore = Set.of();
    private Set<By> areasToIgnore = Set.of();

    public ApplitoolsVisualCheck()
    {
        // Necessary for JBehave object instantiation;
    }

    public ApplitoolsVisualCheck(String batchName, String baselineName, VisualActionType action)
    {
        super(baselineName, action);
        this.batchName = batchName;
    }

    public void buildIgnores()
    {
        setElementsToIgnore(Map.of(IgnoreStrategy.AREA, areasToIgnore, IgnoreStrategy.ELEMENT, elementsToIgnore));
    }

    public String getExecuteApiKey()
    {
        return executeApiKey;
    }

    public void setExecuteApiKey(String executeApiKey)
    {
        this.executeApiKey = executeApiKey;
    }

    public String getReadApiKey()
    {
        return readApiKey;
    }

    public void setReadApiKey(String readApiKey)
    {
        this.readApiKey = readApiKey;
    }

    public String getHostApp()
    {
        return hostApp;
    }

    public void setHostApp(String hostApp)
    {
        this.hostApp = hostApp;
    }

    public String getHostOS()
    {
        return hostOS;
    }

    public void setHostOS(String hostOS)
    {
        this.hostOS = hostOS;
    }

    public MatchLevel getMatchLevel()
    {
        return matchLevel;
    }

    public void setMatchLevel(MatchLevel matchLevel)
    {
        this.matchLevel = matchLevel;
    }

    public URI getServerUri()
    {
        return serverUri;
    }

    public void setServerUri(URI serverUri)
    {
        this.serverUri = serverUri;
    }

    public String getAppName()
    {
        return appName;
    }

    public void setAppName(String appName)
    {
        this.appName = appName;
    }

    public String getBatchName()
    {
        return batchName;
    }

    public String getBaselineEnvName()
    {
        return baselineEnvName;
    }

    public void setBaselineEnvName(String baselineEnvName)
    {
        this.baselineEnvName = baselineEnvName;
    }

    public String getViewportSize()
    {
        return viewportSize;
    }

    public void setViewportSize(String viewportSize)
    {
        this.viewportSize = viewportSize;
    }

    public void setElementsToIgnore(Set<By> elementsToIgnore)
    {
        this.elementsToIgnore = elementsToIgnore;
    }

    public void setAreasToIgnore(Set<By> areasToIgnore)
    {
        this.areasToIgnore = areasToIgnore;
    }
}
