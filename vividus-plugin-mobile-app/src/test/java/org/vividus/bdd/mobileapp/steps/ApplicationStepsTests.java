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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.bdd.mobileapp.model.DesiredCapability;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManagerContext;
import org.vividus.selenium.manager.WebDriverManagerParameter;

import io.appium.java_client.HasSessionDetails;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class ApplicationStepsTests
{
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String CAPABILITY_NAME = "capabilityName";
    private static final String CAPABILITY_VALUE = "capabilityValue";
    private static final String APP = "app";
    private static final String APP_NAME = "vividus-mobile.app";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(ApplicationSteps.class);

    @Mock private HasSessionDetails details;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IWebDriverManagerContext webDriverManagerContext;
    @InjectMocks private ApplicationSteps applicationSteps;

    @BeforeEach
    void init()
    {
        when(details.getSessionDetail(APP)).thenReturn(APP_NAME);
        when(webDriverProvider.getUnwrapped(HasSessionDetails.class)).thenReturn(details);
    }

    @AfterEach
    void afterEach()
    {
        assertThat(logger.getLoggingEvents(), is(List.of(info("Started application located at {}", APP_NAME))));
    }

    @Test
    void testStartMobileApplicationWithCapabilities()
    {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities(Map.of(KEY, VALUE));

        when(webDriverManagerContext.getParameter(WebDriverManagerParameter.DESIRED_CAPABILITIES))
                .thenReturn(desiredCapabilities);

        DesiredCapability capability = new DesiredCapability();
        capability.setName(CAPABILITY_NAME);
        capability.setValue(CAPABILITY_VALUE);

        applicationSteps.startMobileApplicationWithCapabilities(List.of(capability));

        verify(webDriverManagerContext).putParameter(WebDriverManagerParameter.DESIRED_CAPABILITIES,
                new DesiredCapabilities(Map.of(KEY, VALUE, CAPABILITY_NAME, CAPABILITY_VALUE)));
        verifyNoMoreInteractions(webDriverProvider, details, webDriverManagerContext);
    }

    @Test
    void testStartMobileApplication()
    {
        applicationSteps.startMobileApplication();

        verifyNoMoreInteractions(webDriverProvider, details);
    }
}
