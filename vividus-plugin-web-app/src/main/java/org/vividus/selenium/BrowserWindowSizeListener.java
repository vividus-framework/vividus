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

import java.util.Optional;

import com.google.common.eventbus.Subscribe;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver.Window;
import org.vividus.context.RunContext;
import org.vividus.converter.ui.web.StringToDimensionParameterConverter;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.selenium.event.WebDriverCreateEvent;
import org.vividus.selenium.manager.IWebDriverManager;

public class BrowserWindowSizeListener
{
    private RunContext runContext;
    private IWebDriverManager webDriverManager;

    @Subscribe
    public void onWebDriverCreate(WebDriverCreateEvent event)
    {
        if (!webDriverManager.isElectronApp() && !webDriverManager.isMobile())
        {
            getBrowserWindowSizeFromMeta().ifPresentOrElse(
                    targetSize -> getWindow(event).setSize(targetSize),
                    () -> getWindow(event).maximize());
        }
    }

    private Window getWindow(WebDriverCreateEvent event)
    {
        return event.getWebDriver().manage().window();
    }

    private Optional<Dimension> getBrowserWindowSizeFromMeta()
    {
        RunningStory runningStory = runContext.getRunningStory();
        if (runningStory != null)
        {
            return Optional.ofNullable(runningStory.getRunningScenario())
                    .map(RunningScenario::getScenario)
                    .map(Scenario::getMeta)
                    .flatMap(this::findBrowserWindowSize)
                    .or(() -> findBrowserWindowSize(runningStory.getStory().getMeta()));
        }
        return Optional.empty();
    }

    private Optional<Dimension> findBrowserWindowSize(Meta meta)
    {
        return meta.getOptionalProperty("browserWindowSize")
                .map(StringToDimensionParameterConverter::convert)
                .map(browserWindowSize ->
                {
                    webDriverManager.checkWindowFitsScreen(browserWindowSize, (fitsScreen, screenResolution) -> {
                        Validate.isTrue(fitsScreen,
                                "Local or remote screen size \"%dx%d\" is less than desired browser window size "
                                        + "\"%dx%d\"",
                                screenResolution.getWidth(), screenResolution.getHeight(), browserWindowSize.getWidth(),
                                browserWindowSize.getHeight());
                    });
                    return browserWindowSize;
                });
    }

    public void setRunContext(RunContext runContext)
    {
        this.runContext = runContext;
    }

    public void setWebDriverManager(IWebDriverManager webDriverManager)
    {
        this.webDriverManager = webDriverManager;
    }
}
