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

package org.vividus.visual.engine;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.vividus.selenium.screenshot.ScreenshotConfiguration;
import org.vividus.visual.model.VisualActionType;

public class VisualCheckFactory implements IVisualCheckFactory
{
    private Map<String, IScreenshotIndexer> indexers;

    private Optional<String> screenshotIndexer;

    @Override
    public VisualCheck create(String baselineName, VisualActionType actionType)
    {
        String indexedBaselineName = screenshotIndexer.map(in -> indexers.get(in))
                                                      .map(indexer -> indexer.index(baselineName))
                                                      .orElse(baselineName);
        return new VisualCheck(indexedBaselineName, actionType);
    }

    @Override
    public VisualCheck create(String baselineName, VisualActionType actionType,
            ScreenshotConfiguration configuration)
    {
        VisualCheck check = create(baselineName, actionType);
        check.setScreenshotConfiguration(Optional.of(configuration));
        return check;
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

    public static final class VisualCheck
    {
        private final String baselineName;
        private final VisualActionType action;
        private Map<IgnoreStrategy, Set<By>> elementsToIgnore = Map.of();
        private Optional<ScreenshotConfiguration> screenshotConfiguration = Optional.empty();
        private SearchContext searchContext;

        private VisualCheck(String baselineName, VisualActionType action)
        {
            this.baselineName = baselineName;
            this.action = action;
        }

        public String getBaselineName()
        {
            return baselineName;
        }

        public Map<IgnoreStrategy, Set<By>> getElementsToIgnore()
        {
            return elementsToIgnore;
        }

        public void setElementsToIgnore(Map<IgnoreStrategy, Set<By>> elementsToIgnore)
        {
            this.elementsToIgnore = elementsToIgnore;
        }

        public VisualActionType getAction()
        {
            return action;
        }

        public Optional<ScreenshotConfiguration> getScreenshotConfiguration()
        {
            return screenshotConfiguration;
        }

        public void setScreenshotConfiguration(Optional<ScreenshotConfiguration> screenshotConfiguration)
        {
            this.screenshotConfiguration = screenshotConfiguration;
        }

        public SearchContext getSearchContext()
        {
            return searchContext;
        }

        public void setSearchContext(SearchContext searchContext)
        {
            this.searchContext = searchContext;
        }
    }
}
