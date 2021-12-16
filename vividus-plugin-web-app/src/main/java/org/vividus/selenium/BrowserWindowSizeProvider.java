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

package org.vividus.selenium;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;

import com.google.common.base.Suppliers;

import org.jbehave.core.model.Meta;
import org.vividus.context.RunContext;
import org.vividus.converter.ui.web.StringToBrowserWindowSizeParameterConverter;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;

public class BrowserWindowSizeProvider implements IBrowserWindowSizeProvider
{
    @Inject private RunContext runContext;
    private String remoteScreenResolution;
    private final Supplier<Optional<BrowserWindowSize>> remoteBrowserWindowSizeSupplier = Suppliers.memoize(() ->
            StringToBrowserWindowSizeParameterConverter.convert(remoteScreenResolution));

    @SuppressWarnings("removal")
    @Override
    public BrowserWindowSize getBrowserWindowSizeFromMeta(boolean remoteExecution)
    {
        RunningStory runningStory = runContext.getRunningStory();
        if (runningStory != null)
        {
            RunningScenario runningScenario = runningStory.getRunningScenario();
            BrowserWindowSize browserWindowSize = null;
            if (null != runningScenario)
            {
                Meta scenarioMeta = runningScenario.getScenario().getMeta();
                browserWindowSize = getBrowserSizeFromMeta(scenarioMeta, remoteExecution);
            }
            return browserWindowSize == null
                    ? getBrowserSizeFromMeta(runningStory.getStory().getMeta(), remoteExecution)
                    : browserWindowSize;
        }
        return null;
    }

    private void checkDesiredBrowserWindowSize(BrowserWindowSize desiredBrowserWindowSize, boolean remoteExecution)
    {
        BrowserWindowSize screenSize = getMaximumBrowserWindowSize(remoteExecution).get();
        if (desiredBrowserWindowSize.getWidth() > screenSize.getWidth()
                || desiredBrowserWindowSize.getHeight() > screenSize.getHeight())
        {
            throw new IllegalArgumentException("Local or remote screen size \"" + screenSize
                    + "\" is less than desired browser window size \"" + desiredBrowserWindowSize + "\"");
        }
    }

    @Override
    public Optional<BrowserWindowSize> getMaximumBrowserWindowSize(boolean remoteExecution)
    {
        if (!remoteExecution && !GraphicsEnvironment.isHeadless())
        {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            return Optional.of(new BrowserWindowSize((int) screenSize.getWidth(), (int) screenSize.getHeight()));
        }
        return remoteBrowserWindowSizeSupplier.get();
    }

    private BrowserWindowSize getBrowserSizeFromMeta(Meta meta, boolean remoteExecution)
    {
        return meta.getOptionalProperty("browserWindowSize")
                .map(StringToBrowserWindowSizeParameterConverter::convert)
                .map(Optional::get)
                .map(browserWindowSize ->
                {
                    checkDesiredBrowserWindowSize(browserWindowSize, remoteExecution);
                    return browserWindowSize;
                }).orElse(null);
    }

    public void setRemoteScreenResolution(String remoteScreenResolution)
    {
        this.remoteScreenResolution = remoteScreenResolution;
    }
}
