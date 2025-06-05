/*
 * Copyright 2019-2025 the original author or authors.
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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.IPropertyParser;

@ExtendWith(MockitoExtension.class)
class GenericWebDriverFactoryTests
{
    private static final String CAPS = String.format("{%n  \"key1\" : \"arg\",%n  \"key2\" "
            + ": \"arg\",%n  \"key3\" : \"arg\",%n  \"key4\" : \"configurer\"%n}");
    private static final String SELENIUM_CAPABILITIES = "selenium.capabilities.";
    private static final String SELENIUM_GRID_CAPABILITIES = "selenium.grid.capabilities.";
    private static final String FALSE = "false";
    private static final String ARG = "arg";
    private static final String KEY4 = "key4";
    private static final String KEY3 = "key3";
    private static final String TRUE = "true";
    private static final String KEY2 = "key2";
    private static final String KEY1 = "key1";
    private static final String MAP = "map";
    private static final String CONFIGURER = "configurer";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(GenericWebDriverFactory.class);

    @Mock private IRemoteWebDriverFactory remoteWebDriverFactory;
    @Mock private IPropertyParser propertyParser;
    @Spy private final JsonUtils jsonUtils = new JsonUtils();
    @InjectMocks private GenericWebDriverFactory webDriverFactory;

    @Test
    void shouldReturnConvertedRemoteDriverCapability()
    {
        when(propertyParser.getPropertyValuesTreeByPrefix(SELENIUM_GRID_CAPABILITIES))
            .thenReturn(Map.of(KEY1, TRUE, KEY3, TRUE.toUpperCase(), KEY4, ARG));
        when(propertyParser.getPropertyValuesTreeByPrefix(SELENIUM_CAPABILITIES))
            .thenReturn(Map.of(KEY1, FALSE, KEY2, FALSE));
        var remoteCapabilities = webDriverFactory.getWebDriverCapabilities(false, new DesiredCapabilities());
        var localCapabilities = webDriverFactory.getWebDriverCapabilities(true, new DesiredCapabilities());
        assertAll(
            () -> assertTrue((boolean) remoteCapabilities.getCapability(KEY1)),
            () -> assertFalse((boolean) remoteCapabilities.getCapability(KEY2)),
            () -> assertTrue((boolean) remoteCapabilities.getCapability(KEY3)),
            () -> assertEquals(ARG, remoteCapabilities.getCapability(KEY4)),
            () -> assertFalse((boolean) localCapabilities.getCapability(KEY1)),
            () -> assertFalse((boolean) localCapabilities.getCapability(KEY2)),
            () -> assertNull(localCapabilities.getCapability(KEY3)),
            () -> assertNull(localCapabilities.getCapability(KEY4))
        );
    }

    @Test
    void shouldMergeWebDriverCapabilitiesAndRemoteWebDriverCapabilitiesForRemoteDriver()
    {
        lenient().when(propertyParser.getPropertyValuesTreeByPrefix(SELENIUM_CAPABILITIES))
            .thenReturn(Map.of(
                KEY1, FALSE,
                KEY2, FALSE,
                KEY3, ARG,
                MAP, Map.of(KEY1, TRUE)));
        lenient().when(propertyParser.getPropertyValuesTreeByPrefix(SELENIUM_GRID_CAPABILITIES))
            .thenReturn(Map.of(
                KEY1, TRUE,
                KEY2, TRUE,
                MAP, Map.of(KEY2, FALSE)));
        var notExistingCapability = "some-name";
        var remoteCapabilities = webDriverFactory.getWebDriverCapabilities(false, new DesiredCapabilities());
        var localCapabilities = webDriverFactory.getWebDriverCapabilities(true, new DesiredCapabilities());
        assertAll(
            () -> assertTrue((boolean) remoteCapabilities.getCapability(KEY1)),
            () -> assertTrue((boolean) remoteCapabilities.getCapability(KEY2)),
            () -> assertEquals(ARG, remoteCapabilities.getCapability(KEY3)),
            () -> assertEquals(Map.of(KEY1, TRUE, KEY2, FALSE), remoteCapabilities.getCapability(MAP)),
            () -> assertNull(remoteCapabilities.getCapability(notExistingCapability)),
            () -> assertNull(localCapabilities.getCapability(notExistingCapability))
        );
    }

    @Test
    void shouldCacheCapabilities()
    {
        when(propertyParser.getPropertyValuesTreeByPrefix(SELENIUM_CAPABILITIES))
            .thenReturn(Map.of(KEY1, FALSE, KEY2, FALSE, KEY3, ARG));
        when(propertyParser.getPropertyValuesTreeByPrefix(SELENIUM_GRID_CAPABILITIES))
            .thenReturn(Map.of(KEY1, TRUE, KEY2, TRUE));
        var remoteCapabilities = webDriverFactory.getWebDriverCapabilities(false, new DesiredCapabilities());
        assertAll(
            () -> assertTrue((boolean) remoteCapabilities.getCapability(KEY1)),
            () -> assertTrue((boolean) remoteCapabilities.getCapability(KEY2)),
            () -> assertEquals(ARG, remoteCapabilities.getCapability(KEY3))
        );
        verify(propertyParser).getPropertyValuesTreeByPrefix(SELENIUM_CAPABILITIES);
        verify(propertyParser).getPropertyValuesTreeByPrefix(SELENIUM_GRID_CAPABILITIES);
    }

    @Test
    void testGetRemoteWebDriver() throws MalformedURLException
    {
        var adjuster = new DesiredCapabilitiesAdjuster()
        {
            @Override
            protected Map<String, Object> getExtraCapabilities(DesiredCapabilities desiredCapabilities)
            {
                return Map.of(KEY4, CONFIGURER);
            }
        };
        var testWebDriverFactory = new GenericWebDriverFactory(remoteWebDriverFactory, propertyParser, jsonUtils,
                Optional.of(Set.of(adjuster)), Optional.empty());
        var remoteWebDriver = mock(RemoteWebDriver.class,
                withSettings().extraInterfaces(HasCapabilities.class));
        Capabilities capabilities = new DesiredCapabilities(Map.of(KEY1, ARG, KEY2, ARG, KEY3, ARG, KEY4, CONFIGURER));

        when(propertyParser.getPropertyValuesTreeByPrefix(SELENIUM_CAPABILITIES)).thenReturn(Map.of(KEY1, ARG));
        when(propertyParser.getPropertyValuesTreeByPrefix(SELENIUM_GRID_CAPABILITIES)).thenReturn(Map.of(KEY2, ARG));
        when(remoteWebDriverFactory.getRemoteWebDriver(capabilities)).thenReturn(remoteWebDriver);
        when(remoteWebDriver.getCapabilities()).thenReturn(capabilities);

        testWebDriverFactory.createWebDriver(new DesiredCapabilities(Map.of(KEY3, ARG)));

        assertThat(logger.getLoggingEvents(),
                is(List.of(info("Requested capabilities:\n{}", CAPS), info("Session capabilities:\n{}", CAPS))));
    }

    @Test
    void shouldUpdateCapabilitiesUsingConfigurer()
    {
        var desiredCapabilitiesAdjuster = mock(DesiredCapabilitiesAdjuster.class);
        var capabilities = mock(DesiredCapabilities.class);
        var testWebDriverFactory = new GenericWebDriverFactory(remoteWebDriverFactory, propertyParser, jsonUtils,
                Optional.of(Set.of(desiredCapabilitiesAdjuster)), Optional.empty());
        testWebDriverFactory.updateDesiredCapabilities(capabilities);
        verify(desiredCapabilitiesAdjuster).adjust(capabilities);
    }

    @Test
    void shouldValidateProperties()
    {
        SeleniumConfigurationValidator propertyValidator = mock();
        var testWebDriverFactory = new GenericWebDriverFactory(remoteWebDriverFactory, propertyParser, jsonUtils,
                Optional.empty(), Optional.of(propertyValidator));
        testWebDriverFactory.validateConfigurationProperties();
        verify(propertyValidator).validate();
    }
}
