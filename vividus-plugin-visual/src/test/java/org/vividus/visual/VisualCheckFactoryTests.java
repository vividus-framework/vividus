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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.ui.screenshot.ScreenshotParameters;
import org.vividus.ui.screenshot.ScreenshotParametersFactory;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheck;
import org.vividus.visual.screenshot.IScreenshotIndexer;

@ExtendWith(MockitoExtension.class)
class VisualCheckFactoryTests
{
    private static final String NAME = "name";
    private static final String INDEXED_NAME = NAME + " [0]";

    @Mock private ScreenshotParametersFactory<ScreenshotConfiguration> screenshotParametersFactory;
    @InjectMocks private VisualCheckFactory visualCheckFactory;

    @Test
    void shouldCreateVisualCheckWithoutIndexedBaseline()
    {
        visualCheckFactory.setScreenshotIndexer(Optional.empty());
        visualCheckFactory.setIndexers(Map.of());
        VisualCheck check = visualCheckFactory.create(NAME, VisualActionType.COMPARE_AGAINST);
        assertAll(
            () -> assertEquals(NAME, check.getBaselineName()),
            () -> assertEquals(VisualActionType.COMPARE_AGAINST, check.getAction()),
            () -> assertEquals(OptionalDouble.empty(), check.getAcceptableDiffPercentage()),
            () -> assertEquals(Map.of(), check.getElementsToIgnore()),
            () -> assertEquals(Optional.empty(), check.getScreenshotParameters()));
    }

    @Test
    void shouldCreateVisualCheckFromInputFactoryWithIndexedBaseline()
    {
        mockIndexer();
        VisualCheck check = visualCheckFactory.create(NAME, VisualActionType.COMPARE_AGAINST, VisualCheck::new);
        assertAll(
            () -> assertEquals(INDEXED_NAME, check.getBaselineName()),
            () -> assertEquals(VisualActionType.COMPARE_AGAINST, check.getAction())
        );
    }

    @Test
    void shouldCreateVisualCheckWithIndexedBaseline()
    {
        mockIndexer();
        VisualCheck check = visualCheckFactory.create(NAME, VisualActionType.COMPARE_AGAINST);
        assertAll(
            () -> assertEquals(INDEXED_NAME, check.getBaselineName()),
            () -> assertEquals(VisualActionType.COMPARE_AGAINST, check.getAction()),
            () -> assertEquals(OptionalDouble.empty(), check.getAcceptableDiffPercentage()),
            () -> assertEquals(Map.of(), check.getElementsToIgnore()),
            () -> assertEquals(Optional.empty(), check.getScreenshotParameters()));
    }

    private void mockIndexer()
    {
        visualCheckFactory.setScreenshotIndexer(Optional.of(NAME));
        IScreenshotIndexer indexer = mock(IScreenshotIndexer.class);
        visualCheckFactory.setIndexers(Map.of(NAME, indexer));
        when(indexer.index(NAME)).thenReturn(INDEXED_NAME);
    }

    @Test
    void shouldCreateVisualCheckWithScreenshotConfiguration()
    {
        visualCheckFactory.setScreenshotIndexer(Optional.empty());
        visualCheckFactory.setIndexers(Map.of());
        ScreenshotConfiguration screenshotConfiguration = mock(ScreenshotConfiguration.class);
        ScreenshotParameters screenshotParameters = mock(ScreenshotParameters.class);
        when(screenshotParametersFactory.create(Optional.of(screenshotConfiguration)))
                .thenReturn(Optional.of(screenshotParameters));
        VisualCheck check = visualCheckFactory.create(NAME, VisualActionType.COMPARE_AGAINST,
                Optional.of(screenshotConfiguration));
        assertAll(
            () -> assertEquals(NAME, check.getBaselineName()),
            () -> assertEquals(VisualActionType.COMPARE_AGAINST, check.getAction()),
            () -> assertEquals(OptionalDouble.empty(), check.getAcceptableDiffPercentage()),
            () -> assertEquals(Map.of(), check.getElementsToIgnore()),
            () -> assertEquals(Optional.of(screenshotParameters), check.getScreenshotParameters()));
    }
}
