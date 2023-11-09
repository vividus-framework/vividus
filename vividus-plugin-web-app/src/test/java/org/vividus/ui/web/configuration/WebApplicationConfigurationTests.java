/*
 * Copyright 2019-2023 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.junit.jupiter.api.Test;

class WebApplicationConfigurationTests
{
    private static final String MAIN_APP_URL = "http://user:pass@test.vividus.org/";

    @Test
    void testGetMainApplicationPageUrlWithUrlAuthentication()
    {
        var webApplicationConfiguration = new WebApplicationConfiguration(MAIN_APP_URL, AuthenticationMode.URL);
        var mainAppUri = URI.create(MAIN_APP_URL);
        assertEquals(mainAppUri, webApplicationConfiguration.getMainApplicationPageUrl());
        assertEquals(mainAppUri, webApplicationConfiguration.getMainApplicationPageUrlUnsafely());
    }

    @Test
    void testGetMainApplicationPageUrlWithNullUrlAndNullAuthentication()
    {
        var webApplicationConfiguration = new WebApplicationConfiguration(null, null);
        var exception = assertThrows(IllegalArgumentException.class,
                webApplicationConfiguration::getMainApplicationPageUrl);
        assertEquals("URL of the main application page should be non-blank", exception.getMessage());
    }

    @Test
    void shouldCheckIfTheMainApplicationUrlIsNotSet()
    {
        var webApplicationConfiguration = new WebApplicationConfiguration(null, null);
        assertNull(webApplicationConfiguration.getMainApplicationPageUrlUnsafely());
    }

    @Test
    void testGetMainApplicationPageUrlWithNonNullUrlAndNullAuthentication()
    {
        var webApplicationConfiguration = new WebApplicationConfiguration(MAIN_APP_URL, null);
        var actualUrl = webApplicationConfiguration.getMainApplicationPageUrl();
        assertEquals(URI.create(MAIN_APP_URL), actualUrl);
    }

    @Test
    void testTsTestGetBasicAuthNullUrlAndAuthenticationMode()
    {
        var webApplicationConfiguration = new WebApplicationConfiguration(null, null);
        assertNull(webApplicationConfiguration.getBasicAuthUser());
    }

    @Test
    void testTsTestGetAuthModeNullUrlAndAuthenticationMode()
    {
        var webApplicationConfiguration = new WebApplicationConfiguration(null, null);
        assertNull(webApplicationConfiguration.getAuthenticationMode());
    }
}
