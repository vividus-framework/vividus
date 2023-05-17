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

import com.applitools.eyes.LogHandler;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.config.Configuration;

import org.openqa.selenium.Dimension;
import org.vividus.ui.ViewportSizeProvider;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;

public abstract class AbstractEyesFactory
{
    private final LogHandler logHandler;
    private final ViewportSizeProvider viewportSizeProvider;

    protected AbstractEyesFactory(LogHandler logHandler, ViewportSizeProvider viewportSizeProvider)
    {
        this.logHandler = logHandler;
        this.viewportSizeProvider = viewportSizeProvider;
    }

    protected void setViewportSize(ApplitoolsVisualCheck applitoolsVisualCheck)
    {
        Configuration configuration = applitoolsVisualCheck.getConfiguration();
        if (configuration.getViewportSize() == null)
        {
            Dimension currentViewportSize = viewportSizeProvider.getViewportSize();
            RectangleSize viewportSize = new RectangleSize(currentViewportSize.getWidth(),
                    currentViewportSize.getHeight());
            configuration.setViewportSize(viewportSize);
        }
    }

    protected LogHandler getLogHandler()
    {
        return logHandler;
    }
}
