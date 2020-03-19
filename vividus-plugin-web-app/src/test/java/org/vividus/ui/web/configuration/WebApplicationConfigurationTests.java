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

package org.vividus.ui.web.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Dimension;
import org.vividus.ui.web.action.IJavascriptActions;

class WebApplicationConfigurationTests
{
    private static final String MAIN_APP_URL = "http://user:pass@test.vividus.org/";
    private static final int WIDTH_THRESHOLD = 100;
    private static final int VIEWPORT_HEIGHT = 600;

    static Stream<Arguments> resolutionDataProvider()
    {
        // @formatter:off
        return Stream.of(
                Arguments.of(99,  true),
                Arguments.of(100, true),
                Arguments.of(101, false)
        );
        // @formatter:on
    }

    @Test
    void testGetMainApplicationPageUrlWithUrlAuthentication()
    {
        WebApplicationConfiguration webApplicationConfiguration = new WebApplicationConfiguration(MAIN_APP_URL,
                AuthenticationMode.URL);
        URI actualUrl = webApplicationConfiguration.getMainApplicationPageUrl();
        assertEquals(URI.create(MAIN_APP_URL), actualUrl);
    }

    @Test
    void testGetMainApplicationPageUrlWithNullUrlAndNullAuthentication()
    {
        WebApplicationConfiguration webApplicationConfiguration = new WebApplicationConfiguration(null, null);
        URI actualUrl = webApplicationConfiguration.getMainApplicationPageUrl();
        assertNull(actualUrl);
    }

    @Test
    void testGetMainApplicationPageUrlWithNonNullUrlAndNullAuthentication()
    {
        WebApplicationConfiguration webApplicationConfiguration = new WebApplicationConfiguration(MAIN_APP_URL, null);
        URI actualUrl = webApplicationConfiguration.getMainApplicationPageUrl();
        assertEquals(URI.create(MAIN_APP_URL), actualUrl);
    }

    @Test
    void testTsTestEnvironmentTrue()
    {
        WebApplicationConfiguration webApplicationConfiguration = new WebApplicationConfiguration(MAIN_APP_URL,
                AuthenticationMode.URL);
        webApplicationConfiguration.setApplicationEnvironmentType("test");
        assertTrue(webApplicationConfiguration.isTestEnvironment());
    }

    @Test
    void testTsTestGetHost()
    {
        WebApplicationConfiguration webApplicationConfiguration = new WebApplicationConfiguration(MAIN_APP_URL,
                AuthenticationMode.URL);
        assertEquals("test.vividus.org", webApplicationConfiguration.getHost());
    }

    @Test
    void testTsTestGetHostNullUrlAndAuthenticationMode()
    {
        WebApplicationConfiguration webApplicationConfiguration = new WebApplicationConfiguration(null, null);
        assertNull(webApplicationConfiguration.getHost());
    }

    @Test
    void testTsTestGetBasicAuthNullUrlAndAuthenticationMode()
    {
        WebApplicationConfiguration webApplicationConfiguration = new WebApplicationConfiguration(null, null);
        assertNull(webApplicationConfiguration.getBasicAuthUser());
    }

    @Test
    void testTsTestGetAuthModeNullUrlAndAuthenticationMode()
    {
        WebApplicationConfiguration webApplicationConfiguration = new WebApplicationConfiguration(null, null);
        assertNull(webApplicationConfiguration.getAuthenticationMode());
    }

    @Test
    void testTsTestEnvironmentFalse()
    {
        WebApplicationConfiguration webApplicationConfiguration = new WebApplicationConfiguration(MAIN_APP_URL,
                AuthenticationMode.URL);
        webApplicationConfiguration.setApplicationEnvironmentType("prod");
        assertFalse(webApplicationConfiguration.isTestEnvironment());
    }

    @ParameterizedTest
    @MethodSource("resolutionDataProvider")
    void testIsMobileWindowResolution(int actualWidth, boolean mobileViewport)
    {
        WebApplicationConfiguration webApplicationConfiguration = prepareWebApplicationConfiguration(actualWidth);
        webApplicationConfiguration.setMobileScreenResolutionWidthThreshold(WIDTH_THRESHOLD);
        IJavascriptActions javascriptActions = mockJavascriptActions(actualWidth);
        assertEquals(mobileViewport, webApplicationConfiguration.isMobileViewport(javascriptActions));
    }

    @ParameterizedTest
    @MethodSource("resolutionDataProvider")
    void testIsTabletWindowResolution(int actualWidth, boolean tabletViewport)
    {
        WebApplicationConfiguration webApplicationConfiguration = prepareWebApplicationConfiguration(actualWidth);
        webApplicationConfiguration.setTabletScreenResolutionWidthThreshold(WIDTH_THRESHOLD);
        IJavascriptActions javascriptActions = mockJavascriptActions(actualWidth);
        assertEquals(tabletViewport, webApplicationConfiguration.isTabletViewport(javascriptActions));
    }

    private static WebApplicationConfiguration prepareWebApplicationConfiguration(int actualWidth)
    {
        WebApplicationConfiguration webApplicationConfiguration = new WebApplicationConfiguration(MAIN_APP_URL,
                AuthenticationMode.URL);
        return webApplicationConfiguration;
    }

    private static IJavascriptActions mockJavascriptActions(int actualWidth)
    {
        IJavascriptActions javascriptActions = mock(IJavascriptActions.class);
        when(javascriptActions.getViewportSize()).thenReturn(new Dimension(actualWidth, VIEWPORT_HEIGHT));
        return javascriptActions;
    }
}
