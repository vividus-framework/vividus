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

import com.applitools.eyes.EyesRunner;
import com.applitools.eyes.LogHandler;
import com.applitools.eyes.images.Eyes;
import com.applitools.eyes.images.ImageRunner;

import org.vividus.ui.ViewportSizeProvider;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;

public class ImageEyesFactory extends AbstractEyesFactory
{
    public ImageEyesFactory(LogHandler logHandler, ViewportSizeProvider viewportSizeProvider)
    {
        super(logHandler, viewportSizeProvider);
    }

    public Eyes createEyes(ApplitoolsVisualCheck applitoolsVisualCheck)
    {
        EyesRunner runner = new ImageRunner();
        runner.setLogHandler(getLogHandler());

        Eyes eyes = new Eyes(runner);
        setViewportSize(applitoolsVisualCheck);
        eyes.setConfiguration(applitoolsVisualCheck.getConfiguration());
        return eyes;
    }
}
