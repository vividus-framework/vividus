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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
import java.net.URI;
import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.IPropertyParser;

@ExtendWith(MockitoExtension.class)
class AbstractWebDriverFactoryTests
{
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
    private static final URI URL = URI.create("https://example.com");

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AbstractWebDriverFactory.class);

    @Mock private IRemoteWebDriverFactory remoteWebDriverFactory;
    @Mock private IPropertyParser propertyParser;
    @Spy private final JsonUtils jsonUtils = new JsonUtils();
    @InjectMocks private TestWebDriverFactory webDriverFactory;

    @Test
    void shouldReturnConvertedRemoteDriverCapability()
    {
        lenient().when(propertyParser.getPropertyValuesTreeByPrefix(SELENIUM_GRID_CAPABILITIES))
            .thenReturn(Map.of(KEY1, TRUE, KEY3, TRUE.toUpperCase(), KEY4, ARG));
        lenient().when(propertyParser.getPropertyValuesTreeByPrefix(SELENIUM_CAPABILITIES))
            .thenReturn(Map.of(KEY1, FALSE, KEY2, FALSE));
        Assertions.assertAll(
            () -> assertTrue((boolean) webDriverFactory.getCapability(KEY1, false)),
            () -> assertFalse((boolean) webDriverFactory.getCapability(KEY2, false)),
            () -> assertTrue((boolean) webDriverFactory.getCapability(KEY3, false)),
            () -> assertEquals(ARG, webDriverFactory.getCapability(KEY4, false)),
            () -> assertFalse((boolean) webDriverFactory.getCapability(KEY1, true)),
            () -> assertFalse((boolean) webDriverFactory.getCapability(KEY2, true)),
            () -> assertNull(webDriverFactory.getCapability(KEY3, true)),
            () -> assertNull(webDriverFactory.getCapability(KEY4, true)));
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
        String notExistingCapability = "some-name";
        Assertions.assertAll(
            () -> assertTrue((boolean) webDriverFactory.getCapability(KEY1, false)),
            () -> assertTrue((boolean) webDriverFactory.getCapability(KEY2, false)),
            () -> assertEquals(ARG, webDriverFactory.getCapability(KEY3, false)),
            () -> assertEquals(Map.of(KEY1, TRUE, KEY2, FALSE), webDriverFactory.getCapability(MAP, false)),
            () -> assertNull(webDriverFactory.getCapability(notExistingCapability, false)),
            () -> assertNull(webDriverFactory.getCapability(notExistingCapability, true)));
    }

    @Test
    void shouldCacheCapabilities()
    {
        lenient().when(propertyParser.getPropertyValuesTreeByPrefix(SELENIUM_CAPABILITIES))
            .thenReturn(Map.of(KEY1, FALSE, KEY2, FALSE, KEY3, ARG));
        lenient().when(propertyParser.getPropertyValuesTreeByPrefix(SELENIUM_GRID_CAPABILITIES))
            .thenReturn(Map.of(KEY1, TRUE, KEY2, TRUE));
        Assertions.assertAll(
            () -> assertTrue((boolean) webDriverFactory.getCapability(KEY1, false)),
            () -> assertTrue((boolean) webDriverFactory.getCapability(KEY2, false)),
            () -> assertEquals(ARG, webDriverFactory.getCapability(KEY3, false)));
        verify(propertyParser).getPropertyValuesTreeByPrefix(SELENIUM_CAPABILITIES);
        verify(propertyParser).getPropertyValuesTreeByPrefix(SELENIUM_GRID_CAPABILITIES);
    }

    @Test
    void testGetRemoteWebDriver() throws MalformedURLException
    {
        RemoteWebDriver remoteWebDriver = mock(RemoteWebDriver.class,
                withSettings().extraInterfaces(HasCapabilities.class));
        Capabilities capabilities = new DesiredCapabilities(Map.of(KEY1, ARG, KEY2, ARG, KEY3, ARG));

        webDriverFactory.setRemoteDriverUrl(URL.toURL());
        when(propertyParser.getPropertyValuesTreeByPrefix(SELENIUM_CAPABILITIES)).thenReturn(Map.of(KEY1, ARG));
        when(propertyParser.getPropertyValuesTreeByPrefix(SELENIUM_GRID_CAPABILITIES)).thenReturn(Map.of(KEY2, ARG));
        when(remoteWebDriverFactory.getRemoteWebDriver(URL.toURL(), capabilities)).thenReturn(remoteWebDriver);
        when(((HasCapabilities) remoteWebDriver).getCapabilities()).thenReturn(capabilities);

        webDriverFactory.getRemoteWebDriver(new DesiredCapabilities(Map.of(KEY3, ARG)));

        assertThat(logger.getLoggingEvents(),
                is(List.of(info("Session capabilities:\n{}", String.format("{%n  \"key1\" : \"arg\",%n  \"key2\" "
                        + ": \"arg\",%n  \"key3\" : \"arg\"%n}")))));
    }

    private static final class TestWebDriverFactory extends AbstractWebDriverFactory
    {
        TestWebDriverFactory(IRemoteWebDriverFactory remoteWebDriverFactory, IPropertyParser propertyParser,
                JsonUtils jsonUtils)
        {
            super(remoteWebDriverFactory, propertyParser, jsonUtils);
        }

        @Override
        protected DesiredCapabilities updateDesiredCapabilities(DesiredCapabilities desiredCapabilities)
        {
            return desiredCapabilities;
        }

        @Override
        protected void configureWebDriver(WebDriver webDriver)
        {
            // empty
        }
    }
}
