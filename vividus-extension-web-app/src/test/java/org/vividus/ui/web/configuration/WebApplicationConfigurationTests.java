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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.junit.jupiter.api.Test;

class WebApplicationConfigurationTests
{
    private static final String MAIN_APP_URL = "http://user:pass@test.vividus.org/";
    private static final String USER_INFO = "user:pass";

    @Test
    void testConfigurationWithUrlAndAuthentication()
    {
        var webApplicationConfiguration = new WebApplicationConfiguration(MAIN_APP_URL, AuthenticationMode.URL);
        var mainAppUri = URI.create(MAIN_APP_URL);
        assertAll(
                () -> assertEquals(mainAppUri, webApplicationConfiguration.getMainApplicationPageUrl()),
                () -> assertEquals(mainAppUri, webApplicationConfiguration.getMainApplicationPageUrlUnsafely()),
                () -> assertEquals(USER_INFO, webApplicationConfiguration.getBasicAuthUser()),
                () -> assertEquals(AuthenticationMode.URL, webApplicationConfiguration.getAuthenticationMode())
        );
    }

    @Test
    void testConfigurationWithNullUrlAndNullAuthentication()
    {
        var webApplicationConfiguration = new WebApplicationConfiguration(null, null);

        assertAll(
                () -> {
                    var exception = assertThrows(IllegalArgumentException.class,
                            webApplicationConfiguration::getMainApplicationPageUrl);
                    assertEquals("URL of the main application page should be non-blank", exception.getMessage());
                },
                () -> assertNull(webApplicationConfiguration.getMainApplicationPageUrlUnsafely()),
                () -> assertNull(webApplicationConfiguration.getBasicAuthUser()),
                () -> assertNull(webApplicationConfiguration.getAuthenticationMode())
        );
    }

    @Test
    void testConfigurationWithUrlAndNullAuthentication()
    {
        var webApplicationConfiguration = new WebApplicationConfiguration(MAIN_APP_URL, null);
        var mainAppUri = URI.create(MAIN_APP_URL);
        assertAll(
                () -> assertEquals(mainAppUri, webApplicationConfiguration.getMainApplicationPageUrl()),
                () -> assertEquals(mainAppUri, webApplicationConfiguration.getMainApplicationPageUrlUnsafely()),
                () -> assertEquals(USER_INFO, webApplicationConfiguration.getBasicAuthUser()),
                () -> assertNull(webApplicationConfiguration.getAuthenticationMode())
        );
    }
}
