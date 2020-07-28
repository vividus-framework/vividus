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

package org.vividus.bdd.mobileapp.steps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.Given;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.mobileapp.model.DesiredCapability;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManagerContext;
import org.vividus.selenium.manager.WebDriverManagerParameter;
import org.vividus.util.property.PropertyParser;

import io.appium.java_client.HasSessionDetails;

public class ApplicationSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationSteps.class);

    private final IWebDriverProvider webDriverProvider;
    private final IWebDriverManagerContext webDriverManagerContext;

    public ApplicationSteps(IWebDriverProvider webDriverProvider, IWebDriverManagerContext webDriverManagerContext)
    {
        this.webDriverProvider = webDriverProvider;
        this.webDriverManagerContext = webDriverManagerContext;
    }

    /**
     * Starts mobile application with the capabilities
     *
     * @param capabilities capabilities to start mobile application with
     */
    @Given("I start mobile application with capabilities:$capabilities")
    public void startMobileApplicationWithCapabilities(List<DesiredCapability> capabilities)
    {
        DesiredCapabilities desiredCapabilities = webDriverManagerContext
                .getParameter(WebDriverManagerParameter.DESIRED_CAPABILITIES);
        Map<String, Object> capabilitiesContainer = new HashMap<>(desiredCapabilities.asMap());
        capabilities.forEach(c -> PropertyParser.putByPath(capabilitiesContainer, c.getName(), c.getValue()));
        webDriverManagerContext.putParameter(WebDriverManagerParameter.DESIRED_CAPABILITIES,
                new DesiredCapabilities(capabilitiesContainer));
        startMobileApplication();
    }

    /**
     * Starts mobile application
     */
    @Given("I start mobile application")
    public void startMobileApplication()
    {
        HasSessionDetails details = webDriverProvider.getUnwrapped(HasSessionDetails.class);
        LOGGER.atInfo()
              .addArgument(() -> details.getSessionDetail("app"))
              .log("Started application located at {}");
    }
}
