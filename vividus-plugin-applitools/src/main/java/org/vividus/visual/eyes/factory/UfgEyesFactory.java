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
import com.applitools.eyes.config.Configuration;
import com.applitools.eyes.visualgrid.model.IRenderingBrowserInfo;
import com.applitools.eyes.visualgrid.services.VisualGridRunner;

import org.vividus.ui.ViewportSizeProvider;
import org.vividus.visual.eyes.model.UfgVisualCheck;
import org.vividus.visual.eyes.ufg.UfgEyes;

public class UfgEyesFactory extends AbstractEyesFactory
{
    protected UfgEyesFactory(LogHandler logHandler, ViewportSizeProvider viewportSizeProvider)
    {
        super(logHandler, viewportSizeProvider);
    }

    public UfgEyes createEyes(UfgVisualCheck ufgVisualCheck)
    {
        VisualGridRunner gridRunner = new VisualGridRunner();
        gridRunner.setLogHandler(getLogHandler());
        gridRunner.setDontCloseBatches(true);

        UfgEyes eyes = new UfgEyes(gridRunner);
        Configuration configuration = createConfiguration(ufgVisualCheck);
        IRenderingBrowserInfo[] browserInfos = ufgVisualCheck.getRenderInfos()
                .toArray(IRenderingBrowserInfo[]::new);
        configuration.addBrowsers(browserInfos);
        eyes.setConfiguration(configuration);
        return eyes;
    }
}
