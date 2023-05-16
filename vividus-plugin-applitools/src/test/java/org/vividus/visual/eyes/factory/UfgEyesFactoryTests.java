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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.List;
import java.util.Set;

import com.applitools.eyes.EyesRunner;
import com.applitools.eyes.LogHandler;
import com.applitools.eyes.Logger;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.selenium.BrowserType;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.visualgrid.model.DesktopBrowserInfo;
import com.applitools.eyes.visualgrid.model.RenderBrowserInfo;
import com.applitools.eyes.visualgrid.services.VisualGridRunner;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.vividus.ui.ViewportSizeProvider;
import org.vividus.visual.eyes.model.UfgVisualCheck;
import org.vividus.visual.model.VisualActionType;

@ExtendWith(MockitoExtension.class)
class UfgEyesFactoryTests
{
    private static final Dimension DIMENSION = new Dimension(1, 1);

    @Mock private LogHandler logHandler;
    @Mock private ViewportSizeProvider viewportSizeProvider;
    @InjectMocks private UfgEyesFactory factory;

    @SuppressWarnings("unchecked")
    @Test
    void shouldCreateEyes() throws IllegalAccessException
    {
        DesktopBrowserInfo info = new DesktopBrowserInfo(DIMENSION.getWidth(), DIMENSION.getHeight(),
                BrowserType.CHROME);

        UfgVisualCheck check = new UfgVisualCheck("batch-name", "baseline-name",
                VisualActionType.ESTABLISH, List.of(info));
        check.setViewportSize(DIMENSION);
        check.setServerUri(URI.create("https://eyesapi.applitools.com"));
        check.setExecuteApiKey("execute-api-key");

        Eyes eyes = factory.createEyes(check);

        EyesRunner runner = (EyesRunner) FieldUtils.readField(eyes, "runner", true);
        assertThat(runner, instanceOf(VisualGridRunner.class));
        Logger logger = (Logger) FieldUtils.readField(runner, "logger", true);
        Set<LogHandler> loggers = (Set<LogHandler>) FieldUtils.readField(logger.getLogHandler(), "logHandlers", true);
        assertThat(loggers, hasItem(logHandler));

        List<RenderBrowserInfo> renderInfos = eyes.getConfiguration().getBrowsersInfo();
        assertThat(renderInfos, hasSize(1));
        RenderBrowserInfo renderInfo = renderInfos.get(0);
        assertEquals(BrowserType.CHROME, renderInfo.getBrowserType());
        assertEquals(new RectangleSize(DIMENSION.getWidth(), DIMENSION.getHeight()), renderInfo.getDeviceSize());
    }
}
