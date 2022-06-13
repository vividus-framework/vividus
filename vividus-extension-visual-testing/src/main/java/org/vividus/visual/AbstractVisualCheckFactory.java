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

import javax.inject.Inject;

import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.ui.screenshot.ScreenshotParametersFactory;
import org.vividus.visual.model.AbstractVisualCheck;
import org.vividus.visual.screenshot.IScreenshotIndexer;

public abstract class AbstractVisualCheckFactory<T extends AbstractVisualCheck>
{
    private Map<String, IScreenshotIndexer> indexers;
    private Optional<String> screenshotIndexer;

    private final ScreenshotParametersFactory<ScreenshotConfiguration> screenshotParametersFactory;

    protected AbstractVisualCheckFactory(
        ScreenshotParametersFactory<ScreenshotConfiguration> screenshotParametersFactory)
    {
        this.screenshotParametersFactory = screenshotParametersFactory;
    }

    protected String createIndexedBaseline(String baselineName)
    {
        return screenshotIndexer.map(indexers::get)
                .map(indexer -> indexer.index(baselineName))
                .orElse(baselineName);
    }

    protected void withScreenshotConfiguration(T check, Optional<ScreenshotConfiguration> configuration)
    {
        check.setScreenshotParameters(screenshotParametersFactory.create(configuration));
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
