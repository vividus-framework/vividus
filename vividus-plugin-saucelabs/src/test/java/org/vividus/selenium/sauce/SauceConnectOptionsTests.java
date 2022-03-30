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

package org.vividus.selenium.sauce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.vividus.util.ResourceUtils;

class SauceConnectOptionsTests
{
    private static final String SPACE = " ";
    private static final String PAC_FILE = "--pac file://";
    private static final String DOT_JS = ".js";
    private static final String PAC_TEST_TUNNEL = "pac-saucelabs-test-tunnel";
    private static final String PROXY = "test";
    private static final String TUNNEL_IDENTIFIER = "test-tunnel";
    private static final String PID_FILE_NAME = "sc_client-" + TUNNEL_IDENTIFIER + "-";
    private static final String TUNNEL_IDENTIFIER_OPTION = "--tunnel-identifier" + SPACE + TUNNEL_IDENTIFIER;
    private static final String PID_EXTENSION = ".pid";
    private static final String NO_REMOVE_COLLIDING_TUNNELS = "--no-remove-colliding-tunnels";
    private static final String NO_PROXY_CACHING = "--no-proxy-caching";
    private static final String SAUCE_LABS_REST_URL = "https://saucelabs.com/rest/v1/";
    private static final String PAC_DATA =
              "function FindProxyForURL(url, host) {%n"
            + "    if (shExpMatch(host, \"*.miso.saucelabs.com\") ||%n"
            + "        shExpMatch(host, \"*.api.testobject.com\") ||%n"
            + "        shExpMatch(host, \"*.saucelabs.com\") ||%n"
            + "        shExpMatch(host, \"saucelabs.com\") ||%n"
            + "        shExpMatch(host, \"%s\")) {%n"
            + "        // KGP and REST connections. Another proxy can also be specified%n"
            + "        return \"DIRECT\";%n"
            + "    }%n"
            + "    // Test HTTP traffic, route it through the custom proxy%n"
            + "    return \"PROXY test\";%n"
            + "}%n";

    private static final String PID_FILE = "--pidfile";

    private final SauceConnectOptions sauceConnectOptions = new SauceConnectOptions();

    @Test
    void testBuildWithProxy() throws IOException
    {
        sauceConnectOptions.setProxy(PROXY);
        try (MockedStatic<ResourceUtils> resources = mockStatic(ResourceUtils.class))
        {
            Path pacPath = mockPac(resources, null);
            Path pidPath = mockPid(resources);

            assertEquals(
                    TUNNEL_IDENTIFIER_OPTION + SPACE + PID_FILE + SPACE + pidPath + SPACE + PAC_FILE + pacPath + SPACE
                            + NO_REMOVE_COLLIDING_TUNNELS + SPACE + NO_PROXY_CACHING,
                    sauceConnectOptions.build(TUNNEL_IDENTIFIER));
        }
    }

    @Test
    void testBuildWithProxyWindowsPathDelimiters() throws IOException
    {
        sauceConnectOptions.setProxy(PROXY);
        try (MockedStatic<ResourceUtils> resources = mockStatic(ResourceUtils.class))
        {
            Path pacPath = mockPac(resources, null);
            when(pacPath.toString()).thenReturn("c:\\user\\temp.js");
            Path pidPath = mockPid(resources);

            assertEquals(
                    TUNNEL_IDENTIFIER_OPTION + SPACE + PID_FILE + SPACE + pidPath + SPACE + PAC_FILE + "c:/user/temp.js"
                            + SPACE + NO_REMOVE_COLLIDING_TUNNELS + SPACE + NO_PROXY_CACHING,
                    sauceConnectOptions.build(TUNNEL_IDENTIFIER));
        }
    }

    @Test
    void testBuildWithProxyWithAuth() throws IOException
    {
        sauceConnectOptions.setProxy(PROXY);
        String customFlags = "--auth host:9999:user:pass";
        sauceConnectOptions.setCustomArguments(customFlags);
        try (MockedStatic<ResourceUtils> resources = mockStatic(ResourceUtils.class))
        {
            Path pacPath = mockPac(resources, null);
            Path pidPath = mockPid(resources);

            assertEquals(customFlags + SPACE + TUNNEL_IDENTIFIER_OPTION + SPACE + PID_FILE + SPACE + pidPath + SPACE
                            + PAC_FILE + pacPath + SPACE + NO_REMOVE_COLLIDING_TUNNELS + SPACE + NO_PROXY_CACHING,
                    sauceConnectOptions.build(TUNNEL_IDENTIFIER));
        }
    }

    @Test
    void testBuildWithProxyWithSkipHostsPattern() throws IOException
    {
        String hosts = "example.com";
        sauceConnectOptions.setProxy(PROXY);
        sauceConnectOptions.setSkipProxyHostsPattern(hosts);
        try (MockedStatic<ResourceUtils> resources = mockStatic(ResourceUtils.class))
        {
            Path pacPath = mockPac(resources, hosts);
            Path pidPath = mockPid(resources);

            assertEquals(
                    TUNNEL_IDENTIFIER_OPTION + SPACE + PID_FILE + SPACE + pidPath + SPACE + PAC_FILE + pacPath + SPACE
                            + NO_REMOVE_COLLIDING_TUNNELS + SPACE + NO_PROXY_CACHING,
                    sauceConnectOptions.build(TUNNEL_IDENTIFIER));
        }
    }

    @Test
    void testBuildWOProxy() throws IOException
    {
        try (MockedStatic<ResourceUtils> resources = mockStatic(ResourceUtils.class))
        {
            Path pidPath = mockPid(resources);

            assertEquals(
                    TUNNEL_IDENTIFIER_OPTION + SPACE + PID_FILE + SPACE + pidPath + SPACE + NO_REMOVE_COLLIDING_TUNNELS
                            + SPACE + NO_PROXY_CACHING,
                    sauceConnectOptions.build(TUNNEL_IDENTIFIER));
        }
    }

    @Test
    void testBuildWOProxyNullOption() throws IOException
    {
        assertEquals(NO_REMOVE_COLLIDING_TUNNELS + SPACE + NO_PROXY_CACHING, sauceConnectOptions.build(null));
    }

    @Test
    void testBuildWithRestUrl() throws IOException
    {
        sauceConnectOptions.setRestUrl(SAUCE_LABS_REST_URL);
        assertEquals("--rest-url" + SPACE + SAUCE_LABS_REST_URL + SPACE + NO_REMOVE_COLLIDING_TUNNELS + SPACE
                + NO_PROXY_CACHING, sauceConnectOptions.build(null));
    }

    @Test
    void testHashCode()
    {
        assertEquals(createDefaultOptions().hashCode(), createDefaultOptions().hashCode());
    }

    @Test
    void testEqualsDifferentObjects()
    {
        assertEquals(createDefaultOptions(), createDefaultOptions());
    }

    @Test
    void testEqualsSameObjects()
    {
        SauceConnectOptions options = createDefaultOptions();
        assertEquals(options, options);
    }

    @Test
    void testNotEqualsToNull()
    {
        assertFalse(createDefaultOptions().equals(null));
    }

    @Test
    void testNotEqualsProxy()
    {
        SauceConnectOptions options = createDefaultOptions();
        options.setProxy(null);
        assertNotEquals(options, createDefaultOptions());
    }

    @Test
    void testNotEqualsSkipProxyHostsPattern()
    {
        SauceConnectOptions options = createDefaultOptions();
        options.setSkipProxyHostsPattern(null);
        assertNotEquals(options, createDefaultOptions());
    }

    @Test
    void testNotEqualsRestUrl()
    {
        SauceConnectOptions options = createDefaultOptions();
        options.setRestUrl(null);
        assertNotEquals(options, createDefaultOptions());
    }

    private SauceConnectOptions createDefaultOptions()
    {
        SauceConnectOptions options = new SauceConnectOptions();
        options.setProxy(PROXY);
        options.setSkipProxyHostsPattern("vividus\\.dev");
        options.setRestUrl(SAUCE_LABS_REST_URL);
        options.setCustomArguments("--verbose");
        return options;
    }

    private Path mockPid(MockedStatic<ResourceUtils> mock)
    {
        Path pidPath = mock(Path.class);
        mock.when(() -> ResourceUtils.createTempFile(PID_FILE_NAME, PID_EXTENSION, null)).thenReturn(pidPath);
        return pidPath;
    }

    private Path mockPac(MockedStatic<ResourceUtils> mock, String hosts)
    {
        Path pacPath = mock(Path.class);
        mock.when(() -> ResourceUtils.createTempFile(PAC_TEST_TUNNEL, DOT_JS, String.format(PAC_DATA, hosts)))
                .thenReturn(pacPath);
        return pacPath;
    }
}
