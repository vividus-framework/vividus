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

package org.vividus.selenium.sauce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import com.saucelabs.saucerest.DataCenter;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.vividus.util.ResourceUtils;

import nl.jqno.equalsverifier.EqualsVerifier;

class SauceConnectOptionsTests
{
    private static final String SPACE = " ";
    private static final String PAC_FILE = "--pac file://";
    private static final String DOT_JS = ".js";
    private static final String PAC_TEST_TUNNEL = "pac-saucelabs-test-tunnel";
    private static final String PROXY = "test";
    private static final String TUNNEL_NAME = "test-tunnel";
    private static final String TUNNEL_NAME_OPTION = "--tunnel-name" + SPACE + TUNNEL_NAME;
    private static final String TUNNEL_POOL = "--tunnel-pool";
    private static final DataCenter DATA_CENTER = DataCenter.US_WEST;
    private static final String REGION = "us-west";
    private static final String REGION_OPTION = "--region" + SPACE + REGION;
    private static final String DEFAULT_CUSTOM_ARGS = "--verbose";
    private static final Set<String> DEFAULT_SKIP_GLOB_HOST_PATTERNS = Set.of("vividus.dev");
    private static final String DEFAULT_MATCH_CHAIN = "shExpMatch(host, \"*.api.testobject.com\") || "
            + "shExpMatch(host, \"*.miso.saucelabs.com\") || shExpMatch(host, \"*.saucelabs.com\") || "
            + "shExpMatch(host, \"saucelabs.com\")";
    private static final String PAC_DATA =
              "function FindProxyForURL(url, host) {%n"
            + "    if (%s) {%n"
            + "        // KGP and REST connections. Another proxy can also be specified%n"
            + "        return \"DIRECT\";%n"
            + "    }%n"
            + "    // Test HTTP traffic, route it through the custom proxy%n"
            + "    return \"PROXY test\";%n"
            + "}%n";

    @Test
    void testBuildWithProxy() throws IOException
    {
        SauceConnectOptions sauceConnectOptions = createEmptyOptions();
        sauceConnectOptions.setProxy(PROXY);
        try (MockedStatic<ResourceUtils> resources = mockStatic(ResourceUtils.class))
        {
            Path pacPath = mockPac(resources, DEFAULT_MATCH_CHAIN);

            assertEquals(REGION_OPTION + SPACE + TUNNEL_NAME_OPTION + SPACE + PAC_FILE + pacPath + SPACE + TUNNEL_POOL,
                    sauceConnectOptions.build(TUNNEL_NAME));
        }
    }

    @Test
    void testBuildWithProxyForLatestSauceConnect() throws IOException
    {
        SauceConnectOptions sauceConnectOptions = new SauceConnectOptions(true, DATA_CENTER, null, Set.of());
        sauceConnectOptions.setProxy(PROXY);
        try (MockedStatic<ResourceUtils> resources = mockStatic(ResourceUtils.class))
        {
            Path pacPath = mockPac(resources, DEFAULT_MATCH_CHAIN);

            assertEquals(
                    REGION_OPTION + SPACE + TUNNEL_NAME_OPTION + SPACE + PAC_FILE + (SystemUtils.IS_OS_WINDOWS ? "/"
                            : "") + pacPath + SPACE + TUNNEL_POOL, sauceConnectOptions.build(TUNNEL_NAME));
        }
    }

    @Test
    void testBuildWithProxyWindowsPathDelimiters() throws IOException
    {
        SauceConnectOptions sauceConnectOptions = createEmptyOptions();
        sauceConnectOptions.setProxy(PROXY);
        try (MockedStatic<ResourceUtils> resources = mockStatic(ResourceUtils.class))
        {
            Path pacPath = mockPac(resources, DEFAULT_MATCH_CHAIN);
            when(pacPath.toString()).thenReturn("c:\\user\\temp.js");

            assertEquals(REGION_OPTION + SPACE + TUNNEL_NAME_OPTION + SPACE + PAC_FILE + "c:/user/temp.js" + SPACE
                         + TUNNEL_POOL, sauceConnectOptions.build(TUNNEL_NAME));
        }
    }

    @Test
    void testBuildWithProxyWithAuth() throws IOException
    {
        String customFlags = "--auth host:9999:user:pass";
        SauceConnectOptions sauceConnectOptions = createOptions(customFlags, Set.of(), PROXY);
        try (MockedStatic<ResourceUtils> resources = mockStatic(ResourceUtils.class))
        {
            Path pacPath = mockPac(resources, DEFAULT_MATCH_CHAIN);

            assertEquals(customFlags + SPACE + REGION_OPTION + SPACE + TUNNEL_NAME_OPTION + SPACE + PAC_FILE + pacPath
                         + SPACE + TUNNEL_POOL, sauceConnectOptions.build(TUNNEL_NAME));
        }
    }

    @Test
    void testBuildWithProxyWithSkipHostsPattern() throws IOException
    {
        Set<String> hosts = Set.of("example.com", "*.vividus.dev");
        SauceConnectOptions sauceConnectOptions = createOptions(null, hosts, PROXY);
        try (MockedStatic<ResourceUtils> resources = mockStatic(ResourceUtils.class))
        {
            String matchCondition = "shExpMatch(host, \"*.api.testobject.com\") || shExpMatch"
                    + "(host, \"*.miso.saucelabs.com\") || shExpMatch(host, \"*.saucelabs.com\") || shExpMatch(host, "
                    + "\"*.vividus.dev\") || shExpMatch(host, \"example.com\") || shExpMatch(host, \"saucelabs.com\")";
            Path pacPath = mockPac(resources, matchCondition);

            assertEquals(REGION_OPTION + SPACE + TUNNEL_NAME_OPTION + SPACE + PAC_FILE + pacPath + SPACE + TUNNEL_POOL,
                    sauceConnectOptions.build(TUNNEL_NAME));
        }
    }

    @Test
    void testBuildWOProxy() throws IOException
    {
        SauceConnectOptions sauceConnectOptions = createEmptyOptions();
        assertEquals(REGION_OPTION + SPACE + TUNNEL_NAME_OPTION + SPACE + TUNNEL_POOL,
                sauceConnectOptions.build(TUNNEL_NAME));
    }

    @Test
    void testBuildWOProxyNullOption() throws IOException
    {
        SauceConnectOptions sauceConnectOptions = createEmptyOptions();
        assertEquals(REGION_OPTION + SPACE + TUNNEL_POOL, sauceConnectOptions.build(null));
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
    void verifyHashCodeAndEquals()
    {
        EqualsVerifier.simple().forClass(SauceConnectOptions.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("customArguments")
                .verify();
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
        assertNotEquals(createOptions(DEFAULT_CUSTOM_ARGS, Set.of(), PROXY), createDefaultOptions());
    }

    @Test
    void testNotEqualsDataCenter()
    {
        assertNotEquals(
                createOptions(DataCenter.EU_CENTRAL, DEFAULT_CUSTOM_ARGS, DEFAULT_SKIP_GLOB_HOST_PATTERNS, PROXY),
                createDefaultOptions());
    }

    private SauceConnectOptions createDefaultOptions()
    {
        return createOptions(DEFAULT_CUSTOM_ARGS, DEFAULT_SKIP_GLOB_HOST_PATTERNS, PROXY);
    }

    private SauceConnectOptions createEmptyOptions()
    {
        return createOptions(null, Set.of(), null);
    }

    private SauceConnectOptions createOptions(String customArguments, Set<String> skipHostGlobPatterns, String proxy)
    {
        return createOptions(DATA_CENTER, customArguments, skipHostGlobPatterns, proxy);
    }

    private static SauceConnectOptions createOptions(DataCenter dataCenter, String customArguments,
            Set<String> skipHostGlobPatterns, String proxy)
    {
        SauceConnectOptions options = new SauceConnectOptions(false, dataCenter, customArguments, skipHostGlobPatterns);
        options.setProxy(proxy);
        return options;
    }

    private Path mockPac(MockedStatic<ResourceUtils> mock, String matchCondition)
    {
        Path pacPath = mock(Path.class);
        mock.when(() -> ResourceUtils.createTempFile(PAC_TEST_TUNNEL, DOT_JS, String.format(PAC_DATA, matchCondition)))
                .thenReturn(pacPath);
        return pacPath;
    }
}
