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

package org.vividus.mobileapp.steps;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.mobileapp.action.ApplicationActions;
import org.vividus.mobileapp.model.NamedEntry;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverStartContext;
import org.vividus.selenium.WebDriverStartParameters;

import io.appium.java_client.ExecutesMethod;
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

    @Mock(extraInterfaces = HasCapabilities.class)
    private WebDriver webDriverWithCapabilities;

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private WebDriverStartContext webDriverStartContext;
    @Mock private ApplicationActions applicationActions;
    @InjectMocks private ApplicationSteps applicationSteps;

    private void mockCommons(Map<String, String> capabilities)
    {
        when(((HasCapabilities) webDriverWithCapabilities).getCapabilities()).thenReturn(
                new MutableCapabilities(capabilities));
        when(webDriverProvider.getUnwrapped(HasCapabilities.class)).thenReturn(
                (HasCapabilities) webDriverWithCapabilities);
    }

    private void verifyLogs()
    {
        assertThat(logger.getLoggingEvents(), is(List.of(info("Started application located at {}", APP_NAME))));
    }

    @Test
    void shouldStartMobileApplicationWithCapabilities()
    {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities(Map.of(KEY, VALUE));

        mockCommons(Map.of(APP, APP_NAME));
        when(webDriverStartContext.get(WebDriverStartParameters.DESIRED_CAPABILITIES))
                .thenReturn(desiredCapabilities);

        NamedEntry capability = new NamedEntry();
        capability.setName(CAPABILITY_NAME);
        capability.setValue(CAPABILITY_VALUE);

        applicationSteps.startMobileApplicationWithCapabilities(List.of(capability));

        verify(webDriverStartContext).put(WebDriverStartParameters.DESIRED_CAPABILITIES,
                new DesiredCapabilities(Map.of(KEY, VALUE, CAPABILITY_NAME, CAPABILITY_VALUE)));
        verifyNoMoreInteractions(webDriverProvider, webDriverWithCapabilities, webDriverStartContext);
        verifyLogs();
    }

    @Test
    void shouldStartMobileApplication()
    {
        mockCommons(Map.of(APP, APP_NAME));

        applicationSteps.startMobileApplication();

        verifyNoMoreInteractions(webDriverProvider, webDriverWithCapabilities);
        verifyLogs();
    }

    @Test
    void shouldStartMobileApplicationWithoutAppCapabilityReturned()
    {
        mockCommons(Map.of());

        applicationSteps.startMobileApplication();

        verifyNoMoreInteractions(webDriverProvider, webDriverWithCapabilities);
        assertThat(logger.getLoggingEvents(), is(empty()));
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
        applicationSteps.reinstallMobileApplication(BUNDLE_ID);

        verify(applicationActions).reinstallApplication(BUNDLE_ID);
        verify(applicationActions).activateApp(BUNDLE_ID);
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

    @Test
    void shouldRunAppInBackground()
    {
        var appManager = mock(InteractsWithApps.class);
        when(webDriverProvider.getUnwrapped(InteractsWithApps.class)).thenReturn(appManager);
        var period = Duration.ofSeconds(1);
        applicationSteps.sendToBackgroundFor(period);
        verify(appManager).runAppInBackground(period);
    }

    private static NamedEntry createSetting(String settingName, String settingValue)
    {
        NamedEntry setting = new NamedEntry();
        setting.setName(settingName);
        setting.setValue(settingValue);
        return setting;
    }
}
