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

import java.util.List;

import com.applitools.eyes.visualgrid.model.IRenderingBrowserInfo;

import org.vividus.ui.screenshot.ScreenshotParametersFactory;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.eyes.model.UfgVisualCheck;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.screenshot.BaselineIndexer;

public class UfgApplitoolsVisualCheckFactory extends AbstractApplitoolsVisualCheckFactory
{
    @SuppressWarnings("rawtypes")
    public UfgApplitoolsVisualCheckFactory(
        ScreenshotParametersFactory screenshotParametersFactory, BaselineIndexer baselineIndexer)
    {
        super(screenshotParametersFactory, baselineIndexer);
    }

    public UfgVisualCheck create(String batchName, String baselineName, VisualActionType action,
            List<IRenderingBrowserInfo> infos)
    {
        UfgVisualCheck check = new UfgVisualCheck(batchName, indexBaseline(baselineName), action, infos);
        configure(check);
        return check;
    }

    public UfgVisualCheck create(ApplitoolsVisualCheck baseCheck, List<IRenderingBrowserInfo> infos)
    {
        UfgVisualCheck check = new UfgVisualCheck(baseCheck.getBatchName(),
                baseCheck.getBaselineName(), baseCheck.getAction(), infos);
        check.setExecuteApiKey(baseCheck.getExecuteApiKey());
        check.setReadApiKey(baseCheck.getReadApiKey());
        check.setHostApp(baseCheck.getHostApp());
        check.setHostOS(baseCheck.getHostOS());
        check.setViewportSize(baseCheck.getViewportSize());
        check.setMatchLevel(baseCheck.getMatchLevel());
        check.setServerUri(baseCheck.getServerUri());
        check.setAppName(baseCheck.getAppName());
        check.setBaselineEnvName(baseCheck.getBaselineEnvName());
        check.setElementsToIgnore(baseCheck.getElementsToIgnore());
        check.setAreasToIgnore(baseCheck.getAreasToIgnore());
        return check;
    }
}
