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
import com.applitools.eyes.visualgrid.services.VisualGridRunner;

import org.vividus.ui.ViewportSizeProvider;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.eyes.ufg.UfgEyes;

public class UfgEyesFactory extends AbstractEyesFactory
{
    protected UfgEyesFactory(LogHandler logHandler, ViewportSizeProvider viewportSizeProvider)
    {
        super(logHandler, viewportSizeProvider);
    }

    public UfgEyes createEyes(ApplitoolsVisualCheck check)
    {
        VisualGridRunner gridRunner = new VisualGridRunner();
        gridRunner.setLogHandler(getLogHandler());
        gridRunner.setDontCloseBatches(true);

        UfgEyes eyes = new UfgEyes(gridRunner);
        setViewportSize(check);
        eyes.setConfiguration(check.getConfiguration());
        return eyes;
    }
}
