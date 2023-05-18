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

import java.util.List;
import java.util.Set;

import com.applitools.eyes.EyesRunner;
import com.applitools.eyes.LogHandler;
import com.applitools.eyes.Logger;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.config.Configuration;
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
import org.vividus.ui.ViewportSizeProvider;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.model.VisualActionType;

@ExtendWith(MockitoExtension.class)
class UfgEyesFactoryTests
{
    private static final RectangleSize RECT_SIZE = new RectangleSize(1, 1);

    @Mock private LogHandler logHandler;
    @Mock private ViewportSizeProvider viewportSizeProvider;
    @InjectMocks private UfgEyesFactory factory;

    @SuppressWarnings("unchecked")
    @Test
    void shouldCreateEyes() throws IllegalAccessException
    {
        DesktopBrowserInfo info = new DesktopBrowserInfo(RECT_SIZE.getWidth(), RECT_SIZE.getHeight(),
                BrowserType.CHROME);

        ApplitoolsVisualCheck check = new ApplitoolsVisualCheck("batch-name", "baseline-name",
                VisualActionType.ESTABLISH);
        Configuration configuration = new Configuration()
                .setViewportSize(RECT_SIZE)
                .setServerUrl("https://eyesapi.applitools.com")
                .setApiKey("execute-api-key")
                .addBrowser(info);
        check.setConfiguration(configuration);

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
        assertEquals(RECT_SIZE, renderInfo.getDeviceSize());
    }
}
