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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.ui.screenshot.ScreenshotParameters;
import org.vividus.ui.screenshot.ScreenshotParametersFactory;
import org.vividus.visual.model.AbstractVisualCheck;
import org.vividus.visual.screenshot.IScreenshotIndexer;

@ExtendWith(MockitoExtension.class)
class VisualCheckFactoryTests
{
    private static final String BASELINE = "baseline";
    private static final String INDEXER = "indexer";

    private @Mock ScreenshotParametersFactory<ScreenshotConfiguration> screenshotParametersFactory;

    @InjectMocks
    private VisualCheckFactory visualCheckFactory;

    @Test
    void shouldReturnSameBaselineIfIndexerNameNotSet()
    {
        visualCheckFactory.setScreenshotIndexer(Optional.empty());
        visualCheckFactory.setIndexers(Map.of());
        assertEquals(BASELINE, visualCheckFactory.createIndexedBaseline(BASELINE));
    }

    @Test
    void shouldReturnSameBaselineIfIndexerNameSetButItDoesntExist()
    {
        visualCheckFactory.setScreenshotIndexer(Optional.of(INDEXER));
        visualCheckFactory.setIndexers(Map.of());
        assertEquals(BASELINE, visualCheckFactory.createIndexedBaseline(BASELINE));
    }

    @Test
    void shouldModifyBaselineName()
    {
        visualCheckFactory.setScreenshotIndexer(Optional.of(INDEXER));
        var indexer = mock(IScreenshotIndexer.class);
        visualCheckFactory.setIndexers(Map.of(INDEXER, indexer));
        var indexedBaseline = "baseline-1";
        when(indexer.index(BASELINE)).thenReturn(indexedBaseline);
        assertEquals(indexedBaseline, visualCheckFactory.createIndexedBaseline(BASELINE));
    }

    @Test
    void shouldSetScreenshotConfiguration()
    {
        var visualCheck = mock(AbstractVisualCheck.class);
        var parameters = Optional.of(mock(ScreenshotParameters.class));
        when(screenshotParametersFactory.create(Optional.empty())).thenReturn(parameters);
        visualCheckFactory.withScreenshotConfiguration(visualCheck, Optional.empty());
        verify(visualCheck).setScreenshotParameters(parameters);
    }

    private static final class VisualCheckFactory extends AbstractVisualCheckFactory<AbstractVisualCheck>
    {
        protected VisualCheckFactory(ScreenshotParametersFactory<ScreenshotConfiguration> screenshotParametersFactory)
        {
            super(screenshotParametersFactory);
        }
    }
}
