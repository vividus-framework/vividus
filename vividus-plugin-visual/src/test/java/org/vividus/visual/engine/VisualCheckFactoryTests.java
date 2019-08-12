/*
 * Copyright 2019 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.vividus.selenium.screenshot.ScreenshotConfiguration;
import org.vividus.visual.engine.VisualCheckFactory.VisualCheck;
import org.vividus.visual.model.VisualActionType;

class VisualCheckFactoryTests
{
    private static final String NAME = "name";
    private final VisualCheckFactory visualCheckFactory = new VisualCheckFactory();

    @Test
    void shouldCreateVisualCheckWithoutIndexedBaseline()
    {
        visualCheckFactory.setScreenshotIndexer(Optional.empty());
        VisualCheck check = visualCheckFactory.create(NAME, VisualActionType.COMPARE_AGAINST);
        assertAll(
            () -> assertEquals(NAME, check.getBaselineName()),
            () -> assertEquals(VisualActionType.COMPARE_AGAINST, check.getAction()),
            () -> assertEquals(Map.of(), check.getElementsToIgnore()),
            () -> assertEquals(Optional.empty(), check.getScreenshotConfiguration()));
    }

    @Test
    void shouldCreateVisualCheckWithIndexedBaseline()
    {
        visualCheckFactory.setScreenshotIndexer(Optional.of(NAME));
        IScreenshotIndexer indexer = mock(IScreenshotIndexer.class);
        visualCheckFactory.setIndexers(Map.of(NAME, indexer));
        String indexedName = "name [0]";
        when(indexer.index(NAME)).thenReturn(indexedName);
        VisualCheck check = visualCheckFactory.create(NAME, VisualActionType.COMPARE_AGAINST);
        assertAll(
            () -> assertEquals(indexedName, check.getBaselineName()),
            () -> assertEquals(VisualActionType.COMPARE_AGAINST, check.getAction()),
            () -> assertEquals(Map.of(), check.getElementsToIgnore()),
            () -> assertEquals(Optional.empty(), check.getScreenshotConfiguration()));
    }

    @Test
    void shouldCreateVisualCheckWithScreenshotConfiguration()
    {
        visualCheckFactory.setScreenshotIndexer(Optional.empty());
        ScreenshotConfiguration screenshotConfiguration = mock(ScreenshotConfiguration.class);
        VisualCheck check = visualCheckFactory.create(NAME, VisualActionType.COMPARE_AGAINST,
                screenshotConfiguration);
        assertAll(
            () -> assertEquals(NAME, check.getBaselineName()),
            () -> assertEquals(VisualActionType.COMPARE_AGAINST, check.getAction()),
            () -> assertEquals(Map.of(), check.getElementsToIgnore()),
            () -> assertEquals(Optional.of(screenshotConfiguration), check.getScreenshotConfiguration()));
    }
}
