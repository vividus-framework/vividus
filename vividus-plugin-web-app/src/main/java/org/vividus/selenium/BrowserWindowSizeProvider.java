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

package org.vividus.selenium;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.inject.Inject;

import org.jbehave.core.model.Meta;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.MetaWrapper;
import org.vividus.bdd.model.RunningStory;

public class BrowserWindowSizeProvider implements IBrowserWindowSizeProvider
{
    @Inject private IBddRunContext bddRunContext;
    private String remoteScreenResolution;

    @Override
    public BrowserWindowSize getBrowserWindowSize(boolean remoteExecution)
    {
        RunningStory runningStory = bddRunContext.getRunningStory();
        if (runningStory != null)
        {
            Meta scenarioMeta = runningStory.getRunningScenario().getScenario().getMeta();
            BrowserWindowSize browserWindowSize = getBrowserSizeFromMeta(scenarioMeta, remoteExecution);
            return browserWindowSize == null
                    ? getBrowserSizeFromMeta(runningStory.getStory().getMeta(), remoteExecution)
                    : browserWindowSize;
        }
        return null;
    }

    private void checkDesiredBrowserWindowSize(BrowserWindowSize desiredBrowserWindowSize, boolean remoteExecution)
    {
        BrowserWindowSize screenSize = getScreenSize(remoteExecution);
        if (desiredBrowserWindowSize.getWidth() > screenSize.getWidth()
                || desiredBrowserWindowSize.getHeight() > screenSize.getHeight())
        {
            throw new IllegalArgumentException("Local or remote screen size \"" + screenSize
                    + "\" is less than desired browser window size \"" + desiredBrowserWindowSize + "\"");
        }
    }

    private BrowserWindowSize getScreenSize(boolean remoteExecution)
    {
        if (!remoteExecution)
        {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            return new BrowserWindowSize((int) screenSize.getWidth() + "x" + (int) screenSize.getHeight());
        }
        return new BrowserWindowSize(remoteScreenResolution);
    }

    private BrowserWindowSize getBrowserSizeFromMeta(Meta meta, boolean sauceLabsEnabled)
    {
        return new MetaWrapper(meta).getOptionalPropertyValue("browserWindowSize").map(windowSize ->
        {
            BrowserWindowSize browserWindowSize = new BrowserWindowSize(windowSize);
            checkDesiredBrowserWindowSize(browserWindowSize, sauceLabsEnabled);
            return browserWindowSize;
        }).orElse(null);
    }

    public void setRemoteScreenResolution(String remoteScreenResolution)
    {
        this.remoteScreenResolution = remoteScreenResolution;
    }
}
