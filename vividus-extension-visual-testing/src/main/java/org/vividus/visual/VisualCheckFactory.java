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

package org.vividus.visual;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.inject.Inject;

import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.ui.screenshot.ScreenshotParameters;
import org.vividus.ui.screenshot.ScreenshotParametersFactory;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheck;
import org.vividus.visual.screenshot.IScreenshotIndexer;

public class VisualCheckFactory implements IVisualCheckFactory
{
    private Map<String, IScreenshotIndexer> indexers;
    private Optional<String> screenshotIndexer;

    private final ScreenshotParametersFactory<ScreenshotConfiguration> screenshotParametersFactory;

    public VisualCheckFactory(ScreenshotParametersFactory<ScreenshotConfiguration> screenshotParametersFactory)
    {
        this.screenshotParametersFactory = screenshotParametersFactory;
    }

    @Override
    public VisualCheck create(String baselineName, VisualActionType actionType)
    {
        return create(baselineName, actionType, Optional.empty());
    }

    private String createIndexedBaseline(String baselineName)
    {
        return screenshotIndexer.map(indexers::get)
                                .map(indexer -> indexer.index(baselineName))
                                .orElse(baselineName);
    }

    @Override
    public <T extends VisualCheck> T create(String baselineName, VisualActionType actionType,
            BiFunction<String, VisualActionType, T> checkFactory)
    {
        String indexedBaselineName = createIndexedBaseline(baselineName);
        T check = checkFactory.apply(indexedBaselineName, actionType);
        withScreenshotConfiguration(check, Optional.empty());
        return check;
    }

    @Override
    public VisualCheck create(String baselineName, VisualActionType actionType,
            Optional<ScreenshotConfiguration> parameters)
    {
        String indexedBaselineName = createIndexedBaseline(baselineName);
        VisualCheck check = new VisualCheck(indexedBaselineName, actionType);
        withScreenshotConfiguration(check, parameters);
        return check;
    }

    private void withScreenshotConfiguration(VisualCheck check, Optional<ScreenshotConfiguration> configuration)
    {
        Optional<ScreenshotParameters> screenshotParameters = screenshotParametersFactory.create(configuration);
        check.setScreenshotParameters(screenshotParameters);
    }

    public void setScreenshotIndexer(Optional<String> screenshotIndexer)
    {
        this.screenshotIndexer = screenshotIndexer;
    }

    @Inject
    public void setIndexers(Map<String, IScreenshotIndexer> indexers)
    {
        this.indexers = indexers;
    }
}
