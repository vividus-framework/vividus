/*
 * Copyright 2019-2021 the original author or authors.
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.bdd.mobileapp.model.NamedEntry;
import org.vividus.mobileapp.action.ApplicationActions;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManagerContext;
import org.vividus.selenium.manager.WebDriverManagerParameter;

import io.appium.java_client.ExecutesMethod;
import io.appium.java_client.HasSessionDetails;
import io.appium.java_client.InteractsWithApps;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class ApplicationStepsTests
{
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String CAPABILITY_NAME = "capabilityName";
    private static final String CAPABILITY_VALUE = "capabilityValue";
    private static final String APP = "app";
    private static final String APP_NAME = "vividus-mobile.app";
    private static final String SET_SETTINGS = "setSettings";
    private static final String SETTINGS = "settings";
    private static final String BUNDLE_ID = "bundleId";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(ApplicationSteps.class);

    @Mock private HasCapabilities hasCapabilities;
    @Mock private InteractsWithApps interacts;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IWebDriverManagerContext webDriverManagerContext;
    @Mock private ApplicationActions applicationActions;
    @InjectMocks private ApplicationSteps applicationSteps;

    private void mockCommons()
    {
        when(hasCapabilities.getCapabilities()).thenReturn(new MutableCapabilities(Map.of(APP, APP_NAME)));
        when(webDriverProvider.getUnwrapped(HasCapabilities.class)).thenReturn(hasCapabilities);
    }

    private void verifyLogs()
    {
        assertThat(logger.getLoggingEvents(), is(List.of(info("Started application located at {}", APP_NAME))));
    }

    @Test
    void shouldStartMobileApplicationWithCapabilities()
    {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities(Map.of(KEY, VALUE));

        mockCommons();
        when(webDriverManagerContext.getParameter(WebDriverManagerParameter.DESIRED_CAPABILITIES))
                .thenReturn(desiredCapabilities);

        NamedEntry capability = new NamedEntry();
        capability.setName(CAPABILITY_NAME);
        capability.setValue(CAPABILITY_VALUE);

        applicationSteps.startMobileApplicationWithCapabilities(List.of(capability));

        verify(webDriverManagerContext).putParameter(WebDriverManagerParameter.DESIRED_CAPABILITIES,
                new DesiredCapabilities(Map.of(KEY, VALUE, CAPABILITY_NAME, CAPABILITY_VALUE)));
        verifyNoMoreInteractions(webDriverProvider, hasCapabilities, webDriverManagerContext);
        verifyLogs();
    }

    @Test
    void shouldStartMobileApplication()
    {
        mockCommons();

        applicationSteps.startMobileApplication();

        verifyNoMoreInteractions(webDriverProvider, hasCapabilities);
        verifyLogs();
    }

    @Test
    void shouldCloseMobileApplication()
    {
        applicationSteps.closeMobileApplication();

        verify(webDriverProvider).end();
        verifyNoMoreInteractions(webDriverProvider);
    }

    @Test
    void shouldRestartMobileApplication()
    {
        when(webDriverProvider.getUnwrapped(InteractsWithApps.class)).thenReturn(interacts);

        applicationSteps.restartMobileApplication();

        verify(interacts).closeApp();
        verify(interacts).launchApp();
        verifyNoMoreInteractions(webDriverProvider);
    }

    @Test
    void shouldActivateApp()
    {
        applicationSteps.activateApp(BUNDLE_ID);
        verify(applicationActions).activateApp(BUNDLE_ID);
    }

    @Test
    void shouldTerminateApp()
    {
        applicationSteps.terminateApp(BUNDLE_ID);
        verify(applicationActions).terminateApp(BUNDLE_ID);
    }

    @Test
    void shouldChangeAppiumSettings()
    {
        ExecutesMethod executesMethod = mock(ExecutesMethod.class);
        when(webDriverProvider.getUnwrapped(ExecutesMethod.class)).thenReturn(executesMethod);

        applicationSteps.changeAppiumSettings(List.of(
                createSetting(KEY + 1, "50"),
                createSetting(KEY + 2, VALUE)));

        verify(executesMethod).execute(SET_SETTINGS, Map.of(SETTINGS, Map.of(KEY + 1, 50L, KEY + 2, VALUE)));
    }

    private static NamedEntry createSetting(String settingName, String settingValue)
    {
        NamedEntry setting = new NamedEntry();
        setting.setName(settingName);
        setting.setValue(settingValue);
        return setting;
    }
}
